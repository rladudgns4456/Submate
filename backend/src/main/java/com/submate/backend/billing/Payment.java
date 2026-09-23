package com.submate.backend.billing;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "payments", uniqueConstraints = @UniqueConstraint(columnNames = {"username", "requestKey"}))
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    public String username;
    public String planId;
    public long amount;
    public Long subscriptionId;
    @Column(nullable = false, length = 80) public String requestKey;
    @Enumerated(EnumType.STRING) public Status status;
    public Instant createdAt;
    public enum Status { SUCCESS, FAILED, REFUNDED }
}
