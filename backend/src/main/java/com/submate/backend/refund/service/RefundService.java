package com.submate.backend.refund.service;

import com.submate.backend.payment.entity.*;
import com.submate.backend.payment.repository.PaymentRepository;
import com.submate.backend.refund.dto.*;
import com.submate.backend.refund.entity.*;
import com.submate.backend.refund.repository.RefundRepository;
import com.submate.backend.subscription.dto.PageResponse;
import com.submate.backend.subscription.repository.SubscriptionRepository;
import com.submate.backend.subscription.service.SubscriptionService;
import com.submate.backend.subscription.support.DomainException;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RefundService {
    private final RefundRepository refunds;
    private final PaymentRepository payments;
    private final SubscriptionRepository subscriptions;
    private final Clock clock;
    public RefundService(RefundRepository refunds, PaymentRepository payments,
            SubscriptionRepository subscriptions, Clock clock) {
        this.refunds = refunds; this.payments = payments; this.subscriptions = subscriptions; this.clock = clock;
    }

    @Transactional
    public RefundResponse request(Long memberId, RefundRequest request) {
        Payment payment = payments.findLockedById(request.paymentId()).orElseThrow(() -> DomainException.notFound("결제"));
        if (!payment.getMemberId().equals(memberId)) throw DomainException.notFound("결제");
        if (payment.getStatus() != PaymentStatus.COMPLETED) throw DomainException.conflict("완료된 결제만 환불 요청할 수 있습니다.");
        if (refunds.existsByPaymentId(payment.getPaymentId())) throw DomainException.conflict("이미 환불 요청한 결제입니다.");
        var subscription = subscriptions.findLockedById(payment.getSubscriptionId())
                .orElseThrow(() -> DomainException.notFound("구독"));
        LocalDate today = LocalDate.now(clock);
        long totalDays = ChronoUnit.DAYS.between(subscription.getStartDate(), subscription.getEndDate());
        long remainingDays = Math.min(totalDays, ChronoUnit.DAYS.between(today, subscription.getEndDate()));
        if (totalDays <= 0 || remainingDays <= 0) {
            throw DomainException.conflict("남은 이용 기간이 없어 환불할 수 없습니다.");
        }
        BigDecimal amount = payment.getAmount().multiply(BigDecimal.valueOf(remainingDays))
                .divide(BigDecimal.valueOf(totalDays), 0, RoundingMode.DOWN);
        if (amount.signum() <= 0) throw DomainException.conflict("계산된 환불 금액이 0원입니다.");
        Refund refund = refunds.saveAndFlush(new Refund(payment.getPaymentId(), amount,
                request.reason().strip(), LocalDateTime.now(clock), totalDays, remainingDays));
        return RefundResponse.from(refund);
    }

    @Transactional
    public RefundResponse decide(Long id, RefundDecisionRequest request) {
        Refund refund = refunds.findLockedById(id).orElseThrow(() -> DomainException.notFound("환불"));
        if (refund.getStatus() != RefundStatus.REQUESTED) throw DomainException.conflict("이미 처리된 환불입니다.");
        if (request.decision() == RefundDecisionRequest.Decision.REJECT) {
            refund.reject();
        } else {
            Payment payment = payments.findLockedById(refund.getPaymentId()).orElseThrow(() -> DomainException.notFound("결제"));
            if (payment.getStatus() != PaymentStatus.COMPLETED) throw DomainException.conflict("환불할 수 없는 결제 상태입니다.");
            var subscription = subscriptions.findLockedById(payment.getSubscriptionId()).orElseThrow(() -> DomainException.notFound("구독"));
            LocalDateTime now = LocalDateTime.now(clock);
            refund.complete(now);
            payment.markRefunded();
            // 요청일에 확정한 일할 환불액을 유지하고 승인 시 이용을 종료한다.
            subscription.endForRefund(LocalDate.now(clock), now);
        }
        return RefundResponse.from(refund);
    }

    public PageResponse<RefundResponse> list(Long memberId, int page, int size) {
        return PageResponse.from(refunds.findByMemberId(memberId, SubscriptionService.page(page, size, "refundId")).map(RefundResponse::from));
    }
    public PageResponse<RefundResponse> adminList(int page, int size) {
        return PageResponse.from(refunds.findAll(SubscriptionService.page(page, size, "refundId")).map(RefundResponse::from));
    }
}
