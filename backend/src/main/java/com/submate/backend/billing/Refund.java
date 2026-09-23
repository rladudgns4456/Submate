package com.submate.backend.billing;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "refunds")
public class Refund {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    public String username;
    @Column(unique = true, nullable = false) public Long paymentId;
    public Long subscriptionId;
    public long amount;
    public long remainingDays;
    public long totalDays;
    public LocalDate requestedDate;
    public Instant requestedAt;
    public Instant processedAt;
    @Column(length = 500) public String reason;
    @Column(length = 500) public String adminNote;
    @Enumerated(EnumType.STRING) public Status status;
    public enum Status { REQUESTED, APPROVED, REJECTED }
}
