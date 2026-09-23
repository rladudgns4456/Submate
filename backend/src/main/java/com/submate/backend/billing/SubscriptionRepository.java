package com.submate.backend.billing;

import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUsernameOrderByIdDesc(String username);
    List<Subscription> findAllByOrderByIdDesc();
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Subscription s where s.id = :id")
    Optional<Subscription> lockById(@Param("id") Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Subscription s where s.endDate <= :today and s.status <> com.submate.backend.billing.Subscription.Status.EXPIRED")
    List<Subscription> findDue(@Param("today") java.time.LocalDate today);
}
