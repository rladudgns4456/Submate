package com.submate.backend.admin.subscription.controller;
import com.submate.backend.payment.dto.PaymentResponse;
import com.submate.backend.payment.service.PaymentService;
import com.submate.backend.refund.dto.*;
import com.submate.backend.refund.service.RefundService;
import com.submate.backend.subscription.dto.*;
import com.submate.backend.subscription.service.SubscriptionService;
import com.submate.backend.subscription.support.CurrentMember;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class SubscriptionAdminController {
    private final SubscriptionService subscriptions;
    private final PaymentService payments;
    private final RefundService refunds;
    private final CurrentMember current;
    public SubscriptionAdminController(SubscriptionService subscriptions, PaymentService payments,
            RefundService refunds, CurrentMember current) {
        this.subscriptions = subscriptions; this.payments = payments; this.refunds = refunds; this.current = current;
    }
    @GetMapping("/subscriptions")
    public PageResponse<SubscriptionResponse> subscriptions(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "20") int size) {
        current.requireAdmin(); return subscriptions.adminList(page, size);
    }
    @GetMapping("/subscriptions/{id}")
    public SubscriptionResponse detail(@PathVariable Long id) {
        current.requireAdmin(); return subscriptions.adminDetail(id);
    }
    @PatchMapping("/subscriptions/{id}/cancel")
    public SubscriptionResponse cancel(@PathVariable Long id) {
        current.requireAdmin(); return subscriptions.adminCancel(id);
    }
    @GetMapping("/payments")
    public PageResponse<PaymentResponse> payments(@RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        current.requireAdmin(); return payments.adminList(page, size);
    }
    @GetMapping("/refunds")
    public PageResponse<RefundResponse> refunds(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        current.requireAdmin(); return refunds.adminList(page, size);
    }
    @PatchMapping("/refunds/{id}")
    public RefundResponse decide(@PathVariable Long id, @Valid @RequestBody RefundDecisionRequest request) {
        current.requireAdmin(); return refunds.decide(id, request);
    }
}
