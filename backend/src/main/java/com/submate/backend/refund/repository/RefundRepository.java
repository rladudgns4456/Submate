package com.submate.backend.refund.repository;

import com.submate.backend.refund.entity.Refund;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    boolean existsByPaymentId(Long paymentId);
    @Query("select r from Refund r where r.paymentId in (select p.paymentId from Payment p where p.memberId = :memberId)")
    Page<Refund> findByMemberId(@Param("memberId") Long memberId, Pageable pageable);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Refund r where r.refundId = :id")
    Optional<Refund> findLockedById(@Param("id") Long id);
}
