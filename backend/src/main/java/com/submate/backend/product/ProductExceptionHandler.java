package com.submate.backend.product;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = {
        "com.submate.backend.product",
        "com.submate.backend.admin.product"
})
public class ProductExceptionHandler {

    public record ErrorResponse(
            int status,
            String message
    ) {
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> illegalArgument(
            IllegalArgumentException ex
    ) {
        return ResponseEntity
                .status(409)
                .body(new ErrorResponse(
                        409,
                        ex.getMessage()
                ));
    }
}