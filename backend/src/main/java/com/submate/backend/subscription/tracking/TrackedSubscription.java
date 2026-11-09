package com.submate.backend.subscription.tracking;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;

@Entity
@Table(name = "tracked_subscriptions")
@Getter
public class TrackedSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private BillingCycle billingCycle;

    @Column(nullable = false)
    private LocalDate billingDate;

    @Column(nullable = false)
    private boolean active;

    private Integer reminderDays;

    @Column(length = 100)
    private String receiptId;

    @Version
    private long version;

    protected TrackedSubscription() {}

    TrackedSubscription(long memberId, TrackedSubscriptionInput input, String receiptId) {
        this.memberId = memberId;
        this.receiptId = receiptId;
        update(input);
    }

    void update(TrackedSubscriptionInput input) {
        name = input.name().strip();
        amount = input.amount().longValueExact();
        billingCycle = input.billingCycle();
        billingDate = input.billingDate();
        active = input.active();
        reminderDays = input.reminderDays();
    }
}
