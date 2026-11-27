package com.submate.backend.subscription.support;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SubscriptionConfiguration {
    @Bean
    public Clock subscriptionClock() { return Clock.system(ZoneId.of("Asia/Seoul")); }
}
