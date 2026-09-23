package com.submate.backend.billing;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional
public class BillingService {
    private final SubscriptionRepository subscriptions;
    private final PaymentRepository payments;
    private final RefundRepository refunds;
    private final PlanCatalog plans;
    private final Clock clock;

    public BillingService(SubscriptionRepository subscriptions, PaymentRepository payments,
                          RefundRepository refunds, PlanCatalog plans, Clock clock) {
        this.subscriptions = subscriptions; this.payments = payments;
        this.refunds = refunds; this.plans = plans; this.clock = clock;
    }
    LocalDate today() { return LocalDate.now(clock); }
    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
    private void owner(String expected, String actual) {
        if (!expected.equals(actual)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "내역을 찾을 수 없습니다.");
    }
    private Subscription lockedSubscription(Long id) {
        return subscriptions.lockById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "구독을 찾을 수 없습니다."));
    }
    private void expire(Subscription subscription) {
        if (!today().isBefore(subscription.endDate)) {
            subscription.status = Subscription.Status.EXPIRED;
            subscription.entitlementKey = null;
        }
    }

    public Payment checkout(String username, String planId, String requestKey, boolean success) {
        var previous = payments.findByUsernameAndRequestKey(username, requestKey);
        if (previous.isPresent()) {
            var p = previous.get();
            if (!p.planId.equals(planId) || (p.status == Payment.Status.FAILED) == success)
                throw conflict("이미 다른 결제에 사용한 요청 키입니다.");
            return p;
        }
        var plan = plans.get(planId);
        expireDue();
        var p = new Payment();
        p.username = username; p.planId = planId; p.amount = plan.price();
        p.requestKey = requestKey; p.createdAt = Instant.now(clock);
        p.status = success ? Payment.Status.SUCCESS : Payment.Status.FAILED;
        if (success) {
            var s = new Subscription();
            s.username = username; s.planId = plan.id(); s.planName = plan.name(); s.price = plan.price();
            s.startDate = today(); s.endDate = today().plusMonths(plan.months());
            s.status = Subscription.Status.ACTIVE; s.entitlementKey = username + ":" + planId;
            p.subscriptionId = subscriptions.saveAndFlush(s).id;
        }
        return payments.saveAndFlush(p);
    }

    public List<Subscription> listSubscriptions(String username) {
        expireDue();
        return username == null ? subscriptions.findAllByOrderByIdDesc() : subscriptions.findByUsernameOrderByIdDesc(username);
    }
    public Subscription detail(Long id, String username) {
        var s = lockedSubscription(id);
        if (username != null) owner(s.username, username);
        expire(s);
        return s;
    }
    public Subscription cancel(Long id, String username) {
        var s = detail(id, username);
        if (s.status == Subscription.Status.EXPIRED) throw conflict("만료된 구독은 해지할 수 없습니다.");
        s.status = Subscription.Status.CANCELLED;
        return s;
    }
    public Subscription adminStatus(Long id, Subscription.Status target) {
        var s = detail(id, null);
        if (target == Subscription.Status.CANCELLED) return cancel(id, null);
        if (target == Subscription.Status.EXPIRED) {
            if (today().isBefore(s.endDate)) throw conflict("이용 기간이 끝난 구독만 만료할 수 있습니다.");
            expire(s); return s;
        }
        if (s.status != Subscription.Status.ACTIVE) throw conflict("해지·만료된 구독은 재활성화할 수 없습니다. 새 결제가 필요합니다.");
        return s;
    }
    public List<Payment> listPayments(String username) {
        return username == null ? payments.findAllByOrderByIdDesc() : payments.findByUsernameOrderByIdDesc(username);
    }
    public List<Refund> listRefunds(String username) {
        return username == null ? refunds.findAllByOrderByIdDesc() : refunds.findByUsernameOrderByIdDesc(username);
    }
    public Refund requestRefund(Long paymentId, String username, String reason) {
        var p = payments.lockById(paymentId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "결제를 찾을 수 없습니다."));
        owner(p.username, username);
        if (p.status != Payment.Status.SUCCESS) throw conflict("성공한 결제만 환불할 수 있습니다.");
        if (refunds.findByPaymentId(paymentId).isPresent()) throw conflict("이미 환불 요청 내역이 있습니다.");
        var s = detail(p.subscriptionId, username);
        long total = ChronoUnit.DAYS.between(s.startDate, s.endDate);
        long remaining = Math.max(0, ChronoUnit.DAYS.between(today(), s.endDate));
        long amount = p.amount * Math.min(total, remaining) / total;
        if (amount <= 0) throw conflict("남은 이용 기간이 없어 환불할 수 없습니다.");
        var r = new Refund();
        r.username = username; r.paymentId = p.id; r.subscriptionId = s.id;
        r.amount = amount; r.totalDays = total; r.remainingDays = remaining;
        r.requestedDate = today(); r.requestedAt = Instant.now(clock);
        r.reason = reason.trim(); r.status = Refund.Status.REQUESTED;
        return refunds.saveAndFlush(r);
    }
    public Refund processRefund(Long id, boolean approve, String note) {
        var r = refunds.lockById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "환불을 찾을 수 없습니다."));
        if (r.status != Refund.Status.REQUESTED) throw conflict("이미 처리한 환불입니다.");
        if (approve) {
            var p = payments.lockById(r.paymentId).orElseThrow();
            if (p.status != Payment.Status.SUCCESS) throw conflict("환불할 수 없는 결제 상태입니다.");
            var s = lockedSubscription(r.subscriptionId);
            p.status = Payment.Status.REFUNDED;
            s.endDate = today();
            s.status = Subscription.Status.EXPIRED; s.entitlementKey = null;
        }
        r.status = approve ? Refund.Status.APPROVED : Refund.Status.REJECTED;
        r.adminNote = note; r.processedAt = Instant.now(clock);
        return r;
    }

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void expireDue() {
        subscriptions.findDue(today()).forEach(this::expire);
        subscriptions.flush();
    }
}
