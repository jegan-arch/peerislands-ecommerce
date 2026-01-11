package com.peerislands.ecommerce.exception;

import com.peerislands.ecommerce.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode ec = e.getErrorCode();

        ErrorResponse response = new ErrorResponse(
                ec.getCode(),
                e.getMessage(),
                ec.getHttpStatus().value(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(response, ec.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> response = new HashMap<>();
        response.put("code", ErrorCode.INVALID_REQUEST.getCode());
        response.put("message", "Validation Failed");
        response.put("details", errors);
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        ErrorResponse response = new ErrorResponse(
                ErrorCode.INTERNAL_ERROR.getCode(),
                "An unexpected error occurred: " + e.getMessage(),
                ErrorCode.INTERNAL_ERROR.getHttpStatus().value(),
                LocalDateTime.now()
        );
        return ResponseEntity.internalServerError().body(response);
    }
}