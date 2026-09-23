package com.submate.backend.billing;

import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    List<Refund> findByUsernameOrderByIdDesc(String username);
    List<Refund> findAllByOrderByIdDesc();
    Optional<Refund> findByPaymentId(Long paymentId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Refund r where r.id = :id")
    Optional<Refund> lockById(@Param("id") Long id);
}
