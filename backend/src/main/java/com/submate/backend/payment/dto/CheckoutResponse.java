package com.submate.backend.payment.dto;
import com.submate.backend.subscription.dto.SubscriptionResponse;

public record CheckoutResponse(boolean replayed, SubscriptionResponse subscription, PaymentResponse payment) {}
