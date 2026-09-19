package com.example.BajriX.config;

import com.example.BajriX.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Why this class exists:
 * Instead of letting raw Java exceptions crash or leak stack traces to the frontend,
 * this centralized advice catches exceptions across ALL controllers and formats them into
 * clean, consistent, human-readable JSON responses.
 *
 * HTTP Status Mapping:
 * - 400 Bad Request: Form validation failures, invalid inputs (e.g. negative price).
 * - 403 Forbidden: Ownership violations (e.g. Seller A attempting to edit Seller B's listing).
 * - 404 Not Found: Requested product or listing doesn't exist.
 * - 409 Conflict: Optimistic locking version mismatch, or duplicate listing attempt.
 * - 500 Internal Server Error: Catch-all for unexpected issues, with safe generic message.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Catches @Valid annotation failures (e.g. @NotBlank, @Min, @DecimalMin)
    // Returns field-by-field error messages so the frontend can highlight specific inputs in red.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, "Validation failed: please check the highlighted fields.", errors));
    }

    // 2. Catches business validation failures (e.g. "MOQ cannot exceed available stock")
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // 3. Catches state conflicts (e.g. duplicate listing or optimistic locking version conflict)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // 4. Catches unauthorized attempts to modify someone else's data
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // 5. Catch-all fallback for any unexpected system errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred. Please try again later."));
    }
}