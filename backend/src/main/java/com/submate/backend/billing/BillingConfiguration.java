package com.submate.backend.billing;

import org.springframework.context.annotation.*;
import java.time.*;

@Configuration
public class BillingConfiguration {
    @Bean public Clock billingClock() { return Clock.system(ZoneId.of("Asia/Seoul")); }
}
