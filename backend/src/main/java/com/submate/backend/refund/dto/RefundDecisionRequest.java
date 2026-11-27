package com.submate.backend.refund.dto;
import jakarta.validation.constraints.NotNull;
public record RefundDecisionRequest(@NotNull Decision decision) {
    public enum Decision { APPROVE, REJECT }
}
