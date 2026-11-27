package com.submate.backend.subscription.controller;

import com.submate.backend.payment.dto.*;
import com.submate.backend.payment.service.PaymentService;
import com.submate.backend.subscription.dto.*;
import com.submate.backend.subscription.integration.CatalogGateway;
import com.submate.backend.subscription.service.SubscriptionService;
import com.submate.backend.subscription.support.CurrentMember;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {
    private final SubscriptionService subscriptions;
    private final PaymentService payments;
    private final CurrentMember current;
    public SubscriptionController(SubscriptionService subscriptions, PaymentService payments, CurrentMember current) {
        this.subscriptions = subscriptions; this.payments = payments; this.current = current;
    }
    @PostMapping
    public ResponseEntity<CheckoutResponse> create(@Valid @RequestBody CheckoutRequest request) {
        var response = payments.checkout(current.memberId(), request);
        return ResponseEntity.status(response.replayed() ? 200 : 201).body(response);
    }
    @GetMapping
    public PageResponse<SubscriptionResponse> list(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return subscriptions.list(current.memberId(), page, size);
    }
    @GetMapping("/quote")
    public CatalogGateway.ProductReference quote(@RequestParam Long productId) {
        current.memberId();
        return subscriptions.quote(productId);
    }
    @GetMapping("/{id}")
    public SubscriptionResponse detail(@PathVariable Long id) { return subscriptions.detail(current.memberId(), id); }
    @PatchMapping("/{id}/cancel")
    public SubscriptionResponse cancel(@PathVariable Long id) { return subscriptions.cancel(current.memberId(), id); }
}
