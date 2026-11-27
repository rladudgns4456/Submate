package com.submate.backend.payment.controller;
import com.submate.backend.payment.dto.PaymentResponse;
import com.submate.backend.payment.service.PaymentService;
import com.submate.backend.subscription.dto.PageResponse;
import com.submate.backend.subscription.support.CurrentMember;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService payments;
    private final CurrentMember current;
    public PaymentController(PaymentService payments, CurrentMember current) { this.payments = payments; this.current = current; }
    @GetMapping
    public PageResponse<PaymentResponse> list(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return payments.list(current.memberId(), page, size);
    }
}
