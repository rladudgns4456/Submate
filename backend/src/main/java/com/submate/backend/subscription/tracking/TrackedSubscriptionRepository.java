package com.submate.backend.subscription.tracking;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface TrackedSubscriptionRepository extends JpaRepository<TrackedSubscription, Long> {
    List<TrackedSubscription> findAllByMemberId(long memberId);

    Optional<TrackedSubscription> findByIdAndMemberId(long id, long memberId);

    boolean existsByMemberIdAndReceiptId(long memberId, String receiptId);
}
