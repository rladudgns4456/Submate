package com.submate.backend.billing;

import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUsernameOrderByIdDesc(String username);
    List<Payment> findAllByOrderByIdDesc();
    Optional<Payment> findByUsernameAndRequestKey(String username, String requestKey);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.id = :id")
    Optional<Payment> lockById(@Param("id") Long id);
}
