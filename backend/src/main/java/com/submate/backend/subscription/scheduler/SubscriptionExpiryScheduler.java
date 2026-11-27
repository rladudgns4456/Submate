package com.submate.backend.subscription.scheduler;

import com.submate.backend.subscription.service.SubscriptionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "submate.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class SubscriptionExpiryScheduler {
    private final SubscriptionService subscriptions;
    public SubscriptionExpiryScheduler(SubscriptionService subscriptions) { this.subscriptions = subscriptions; }
    @Scheduled(cron = "${submate.scheduler.expiry-cron:0 * * * * *}", zone = "Asia/Seoul")
    public void expireSubscriptions() {
        int count;
        do { count = subscriptions.expireDueBatch(); } while (count == 100);
    }
}
