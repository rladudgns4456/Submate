package com.submate.backend.subscription.support;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(basePackages = {
        "com.submate.backend.subscription",
        "com.submate.backend.payment",
        "com.submate.backend.refund",
        "com.submate.backend.admin.subscription",
        "com.submate.backend.admin.member",
        "com.submate.backend.product"
})
public class SubscriptionExceptionHandler {
    public record ErrorResponse(int status, String message) {}
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> business(DomainException ex) {
        return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(ex.getStatus(), ex.getMessage()));
    }
    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> invalid(Exception ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(400, "요청 값과 필수 항목을 확인해 주세요."));
    }
    @ExceptionHandler({DataIntegrityViolationException.class, ConcurrencyFailureException.class})
    public ResponseEntity<ErrorResponse> conflict(Exception ex) {
        return ResponseEntity.status(409).body(new ErrorResponse(409, "동시에 처리된 요청과 충돌했습니다. 내역을 새로 조회해 주세요."));
    }
}
