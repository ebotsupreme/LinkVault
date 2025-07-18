package com.linkvault.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

import static com.linkvault.util.LogUtils.*;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFound(
        UserNotFoundException ex, HttpServletRequest request
    ) {
        warn(log, "User not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                Instant.now().toString(),
                request.getRequestURI()
            ));
    }

    @ExceptionHandler(LinkNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleLinkNotFound(
        LinkNotFoundException ex, HttpServletRequest request
    ) {
        warn(log, "Link not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                Instant.now().toString(),
                request.getRequestURI()
            ));
    }

    @ExceptionHandler(LinksNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleLinksNotFound(
        LinksNotFoundException ex, HttpServletRequest request
    ) {
        warn(log, "Links not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                Instant.now().toString(),
                request.getRequestURI()
            ));
    }

    @ExceptionHandler(LinkSaveException.class)
    public ResponseEntity<ApiErrorResponse> handleLinkSave(
        LinkSaveException ex, HttpServletRequest request
    ) {
        warn(log, "Link failed to save: {}", ex.getMessage());
        error(log, "Stack trace: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                Instant.now().toString(),
                request.getRequestURI()
            ));
    }

    @ExceptionHandler(LinkDeleteException.class)
    public ResponseEntity<ApiErrorResponse> handleLinkDelete(
        LinkDeleteException ex, HttpServletRequest request
    ) {
        warn(log, "Link failed to delete: {}", ex.getMessage());
        error(log, "Stack trace: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                Instant.now().toString(),
                request.getRequestURI()
            ));
    }

    @ExceptionHandler(LinksDeleteException.class)
    public ResponseEntity<ApiErrorResponse> handleLinksDelete(
        LinksDeleteException ex, HttpServletRequest request
    ) {
        warn(log, "Failed to delete links by user ID: {}", ex.getMessage());
        error(log, "Stack trace: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                Instant.now().toString(),
                request.getRequestURI()
            ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(
        Exception ex, HttpServletRequest request
    ) {
        warn(log, "Unhandled exception occurred: {}", ex.getMessage());
        error(log, "Stack trace:", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                Instant.now().toString(),
                request.getRequestURI()
            ));
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(
        UnauthorizedAccessException ex, HttpServletRequest request
    ) {
       warn(log, "Unauthorized access attempt: {}", ex.getMessage());
       return ResponseEntity.status(HttpStatus.FORBIDDEN)
           .body(new ApiErrorResponse(
               HttpStatus.FORBIDDEN.value(),
               ex.getMessage(),
               Instant.now().toString(),
               request.getRequestURI()
           ));
    }
}
