package com.submate.backend.refund.controller;
import com.submate.backend.refund.dto.*;
import com.submate.backend.refund.service.RefundService;
import com.submate.backend.subscription.dto.PageResponse;
import com.submate.backend.subscription.support.CurrentMember;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/refunds")
public class RefundController {
    private final RefundService refunds;
    private final CurrentMember current;
    public RefundController(RefundService refunds, CurrentMember current) { this.refunds = refunds; this.current = current; }
    @PostMapping
    public ResponseEntity<RefundResponse> request(@Valid @RequestBody RefundRequest request) {
        return ResponseEntity.status(201).body(refunds.request(current.memberId(), request));
    }
    @GetMapping
    public PageResponse<RefundResponse> list(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return refunds.list(current.memberId(), page, size);
    }
}
