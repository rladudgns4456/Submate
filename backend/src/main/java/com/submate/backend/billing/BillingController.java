package com.submate.backend.billing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BillingController {
    private final BillingService service;
    private final PlanCatalog plans;
    public BillingController(BillingService service, PlanCatalog plans) { this.service = service; this.plans = plans; }
    public record Checkout(@NotBlank String planId, @NotBlank @Size(max = 80) String requestKey, @NotNull Boolean success) {}
    public record RefundRequest(@NotNull @Positive Long paymentId, @NotBlank @Size(max = 500) String reason) {}
    public record Decision(@NotNull Boolean approve, @Size(max = 500) String note) {}
    public record StatusChange(@NotNull Subscription.Status status) {}

    @GetMapping("/plans") public List<PlanCatalog.Plan> plans() { return plans.list(); }
    @GetMapping("/me") public java.util.Map<String, Object> me(org.springframework.security.core.Authentication user) {
        return java.util.Map.of("username", user.getName(), "admin", user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }
    // A successful virtual payment creates the subscription atomically.
    @PostMapping("/subscriptions") public Payment checkout(@Valid @RequestBody Checkout request, Principal user) {
        return service.checkout(user.getName(), request.planId(), request.requestKey(), request.success());
    }
    @GetMapping("/subscriptions") public List<Subscription> subscriptions(Principal user) { return service.listSubscriptions(user.getName()); }
    @GetMapping("/subscriptions/{id}") public Subscription detail(@PathVariable Long id, Principal user) { return service.detail(id, user.getName()); }
    @PostMapping("/subscriptions/{id}/cancel") public Subscription cancel(@PathVariable Long id, Principal user) { return service.cancel(id, user.getName()); }
    @GetMapping("/payments") public List<Payment> payments(Principal user) { return service.listPayments(user.getName()); }
    @GetMapping("/refunds") public List<Refund> refunds(Principal user) { return service.listRefunds(user.getName()); }
    @PostMapping("/refunds") public Refund refund(@Valid @RequestBody RefundRequest request, Principal user) {
        return service.requestRefund(request.paymentId(), user.getName(), request.reason());
    }
    @GetMapping("/admin/subscriptions") public List<Subscription> allSubscriptions() { return service.listSubscriptions(null); }
    @GetMapping("/admin/subscriptions/{id}") public Subscription adminDetail(@PathVariable Long id) { return service.detail(id, null); }
    @PatchMapping("/admin/subscriptions/{id}/status") public Subscription status(@PathVariable Long id, @Valid @RequestBody StatusChange request) {
        return service.adminStatus(id, request.status());
    }
    @GetMapping("/admin/payments") public List<Payment> allPayments() { return service.listPayments(null); }
    @GetMapping("/admin/refunds") public List<Refund> allRefunds() { return service.listRefunds(null); }
    @PostMapping("/admin/refunds/{id}/decision") public Refund decision(@PathVariable Long id, @Valid @RequestBody Decision request) {
        return service.processRefund(id, request.approve(), request.note());
    }
}
