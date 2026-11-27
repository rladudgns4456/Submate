package com.submate.backend.payment.repository;

import com.submate.backend.payment.entity.Payment;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findByMemberId(Long memberId, Pageable pageable);
    Optional<Payment> findByMemberIdAndRequestKey(Long memberId, String requestKey);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.paymentId = :id")
    Optional<Payment> findLockedById(@Param("id") Long id);
}
