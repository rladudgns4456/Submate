package com.submate.backend.subscription.tracking;

import com.submate.backend.subscription.support.CurrentMember;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tracked-subscriptions")
public class TrackedSubscriptionController {
    private final CurrentMember currentMember;
    private final TrackedSubscriptionService service;

    public TrackedSubscriptionController(CurrentMember currentMember, TrackedSubscriptionService service) {
        this.currentMember = currentMember;
        this.service = service;
    }

    public record ReceiptImport(
            @NotNull @Pattern(regexp = "gmail:[a-f0-9]{64}") String receiptId,
            @NotNull @Valid TrackedSubscriptionInput subscription
    ) {}

    @GetMapping
    public TrackedSubscriptionService.Dashboard dashboard() {
        return service.dashboard(currentMember.memberId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrackedSubscriptionService.Entry create(@Valid @RequestBody TrackedSubscriptionInput input) {
        return service.create(currentMember.memberId(), input, null);
    }

    @PostMapping("/imports")
    @ResponseStatus(HttpStatus.CREATED)
    public TrackedSubscriptionService.Entry importReceipt(@Valid @RequestBody ReceiptImport input) {
        return service.create(currentMember.memberId(), input.subscription(), input.receiptId());
    }

    @PutMapping("/{id}")
    public TrackedSubscriptionService.Entry update(@PathVariable long id,
            @Valid @RequestBody TrackedSubscriptionInput input) {
        return service.update(currentMember.memberId(), id, input);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        service.delete(currentMember.memberId(), id);
    }
}
