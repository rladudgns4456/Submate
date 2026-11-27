package com.submate.backend.payment.service;

import com.submate.backend.payment.dto.*;
import com.submate.backend.payment.entity.Payment;
import com.submate.backend.payment.repository.PaymentRepository;
import com.submate.backend.subscription.dto.PageResponse;
import com.submate.backend.subscription.entity.Subscription;
import com.submate.backend.subscription.integration.CatalogGateway;
import com.submate.backend.subscription.repository.SubscriptionRepository;
import com.submate.backend.subscription.service.SubscriptionService;
import com.submate.backend.subscription.support.DomainException;
import java.time.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PaymentService {
    private final PaymentRepository payments;
    private final SubscriptionRepository subscriptions;
    private final CatalogGateway catalog;
    private final SubscriptionService subscriptionService;
    private final Clock clock;
    public PaymentService(PaymentRepository payments, SubscriptionRepository subscriptions,
            CatalogGateway catalog, SubscriptionService subscriptionService, Clock clock) {
        this.payments = payments; this.subscriptions = subscriptions;
        this.catalog = catalog; this.subscriptionService = subscriptionService; this.clock = clock;
    }

    @Transactional
    public CheckoutResponse checkout(Long memberId, CheckoutRequest request) {
        // 같은 회원의 요청을 직렬화하여 중복 클릭 및 다른 키를 사용한 중복 구독도 차단한다.
        catalog.lockMember(memberId);
        var existing = payments.findByMemberIdAndRequestKey(memberId, request.requestKey());
        if (existing.isPresent()) {
            Payment payment = existing.get();
            Subscription subscription = subscriptions.findById(payment.getSubscriptionId()).orElseThrow();
            if (!subscription.getProductId().equals(request.productId())
                    || payment.getPaymentMethod() != request.paymentMethod() || request.simulateFailure()) {
                throw DomainException.conflict("같은 요청 키를 다른 결제에 사용할 수 없습니다.");
            }
            return new CheckoutResponse(true, subscriptionService.response(subscription), PaymentResponse.from(payment));
        }
        var product = catalog.lockProduct(request.productId());
        if (!"ACTIVE".equals(product.status())) throw DomainException.conflict("현재 구독할 수 없는 상품입니다.");
        if (product.price().signum() <= 0) throw DomainException.conflict("상품 가격이 올바르지 않습니다.");
        LocalDate today = LocalDate.now(clock);
        if (subscriptions.existsByMemberIdAndProductIdAndStatusInAndEndDateGreaterThan(
                memberId, product.productId(), SubscriptionService.LIVE_STATUSES, today)) {
            throw DomainException.conflict("아직 이용 기간이 남아 있는 구독입니다.");
        }
        if (request.simulateFailure()) {
            throw new DomainException(400, "가상 결제에 실패했습니다. 결제 및 구독이 생성되지 않았습니다.");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        Subscription subscription = subscriptions.saveAndFlush(
                new Subscription(memberId, product.productId(), today, product.endDate(today), now));
        Payment payment = payments.saveAndFlush(new Payment(subscription.getSubscriptionId(), memberId,
                request.requestKey(), product.price(), request.paymentMethod(), now));
        return new CheckoutResponse(false, subscriptionService.response(subscription), PaymentResponse.from(payment));
    }

    public PageResponse<PaymentResponse> list(Long memberId, int page, int size) {
        return PageResponse.from(payments.findByMemberId(memberId, SubscriptionService.page(page, size, "paymentId"))
                .map(PaymentResponse::from));
    }
    public PageResponse<PaymentResponse> adminList(int page, int size) {
        return PageResponse.from(payments.findAll(SubscriptionService.page(page, size, "paymentId")).map(PaymentResponse::from));
    }
}
