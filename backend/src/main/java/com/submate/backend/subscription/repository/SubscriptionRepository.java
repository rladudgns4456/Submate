package com.submate.backend.subscription.repository;

import com.submate.backend.subscription.entity.*;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Page<Subscription> findByMemberId(Long memberId, Pageable pageable);
    boolean existsByMemberIdAndProductIdAndStatusInAndEndDateGreaterThan(
            Long memberId, Long productId, Collection<SubscriptionStatus> statuses, LocalDate today);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Subscription s where s.subscriptionId = :id")
    Optional<Subscription> findLockedById(@Param("id") Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Subscription s where s.status in :statuses and s.endDate <= :today order by s.subscriptionId")
    java.util.List<Subscription> findDue(@Param("statuses") Collection<SubscriptionStatus> statuses,
                                        @Param("today") LocalDate today, Pageable pageable);
}
