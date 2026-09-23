package com.submate.backend.billing;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "subscriptions")
public class Subscription {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Version public long version;
    @Column(nullable = false) public String username;
    @Column(nullable = false) public String planId;
    public String planName;
    public long price;
    public LocalDate startDate;
    // Exclusive end date: access is available while today < endDate.
    public LocalDate endDate;
    @Enumerated(EnumType.STRING) public Status status;
    @Column(unique = true) public String entitlementKey;
    public enum Status { ACTIVE, CANCELLED, EXPIRED }
}
