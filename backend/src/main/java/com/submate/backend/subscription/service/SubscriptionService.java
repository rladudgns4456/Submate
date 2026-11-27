package com.submate.backend.subscription.service;

import com.submate.backend.subscription.dto.*;
import com.submate.backend.subscription.entity.*;
import com.submate.backend.subscription.integration.CatalogGateway;
import com.submate.backend.subscription.repository.SubscriptionRepository;
import com.submate.backend.subscription.support.DomainException;
import java.time.*;
import java.util.List;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SubscriptionService {
    public static final List<SubscriptionStatus> LIVE_STATUSES =
            List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.CANCELLED);
    private final SubscriptionRepository subscriptions;
    private final CatalogGateway catalog;
    private final Clock clock;

    public SubscriptionService(SubscriptionRepository subscriptions, CatalogGateway catalog, Clock clock) {
        this.subscriptions = subscriptions; this.catalog = catalog; this.clock = clock;
    }

    public static Pageable page(int page, int size, String id) {
        if (page < 0 || size < 1 || size > 100) throw new DomainException(400, "page는 0 이상, size는 1~100이어야 합니다.");
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, id));
    }

    public PageResponse<SubscriptionResponse> list(Long memberId, int page, int size) {
        return PageResponse.from(subscriptions.findByMemberId(memberId, page(page, size, "subscriptionId")).map(this::response));
    }
    public PageResponse<SubscriptionResponse> adminList(int page, int size) {
        return PageResponse.from(subscriptions.findAll(page(page, size, "subscriptionId")).map(this::response));
    }
    public SubscriptionResponse detail(Long memberId, Long id) {
        Subscription subscription = find(id);
        requireOwner(subscription, memberId);
        return response(subscription);
    }
    public SubscriptionResponse adminDetail(Long id) { return response(find(id)); }
    public CatalogGateway.ProductReference quote(Long productId) {
        var product = catalog.product(productId);
        if (!"ACTIVE".equals(product.status())) throw DomainException.conflict("현재 구독할 수 없는 상품입니다.");
        return product;
    }

    @Transactional
    public SubscriptionResponse cancel(Long memberId, Long id) {
        Subscription subscription = subscriptions.findLockedById(id).orElseThrow(() -> DomainException.notFound("구독"));
        requireOwner(subscription, memberId);
        cancelActive(subscription);
        return response(subscription);
    }

    @Transactional
    public SubscriptionResponse adminCancel(Long id) {
        Subscription subscription = subscriptions.findLockedById(id).orElseThrow(() -> DomainException.notFound("구독"));
        cancelActive(subscription);
        return response(subscription);
    }

    private void cancelActive(Subscription subscription) {
        if (!LocalDate.now(clock).isBefore(subscription.getEndDate())) {
            throw DomainException.conflict("이용 기간이 끝난 구독은 해지할 수 없습니다.");
        }
        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw DomainException.conflict("ACTIVE 구독만 해지할 수 있습니다.");
        }
        subscription.markCancelled(LocalDateTime.now(clock));
    }

    // 작은 배치별 트랜잭션으로 잠금을 오래 보유하지 않는다.
    @Transactional
    public int expireDueBatch() {
        var due = subscriptions.findDue(LIVE_STATUSES, LocalDate.now(clock), PageRequest.of(0, 100));
        due.forEach(subscription -> subscription.markExpired(LocalDateTime.now(clock)));
        return due.size();
    }

    public SubscriptionResponse response(Subscription subscription) {
        return SubscriptionResponse.from(subscription, catalog.product(subscription.getProductId()).name(), LocalDate.now(clock));
    }
    private Subscription find(Long id) {
        return subscriptions.findById(id).orElseThrow(() -> DomainException.notFound("구독"));
    }
    private void requireOwner(Subscription subscription, Long memberId) {
        if (!subscription.getMemberId().equals(memberId)) throw DomainException.notFound("구독");
    }
}
