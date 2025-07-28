package com.linkvault.exception;

import com.linkvault.util.LogMessages;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

import static com.linkvault.util.LogUtils.*;
import java.util.List;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFound(
        UserNotFoundException ex, HttpServletRequest request
    ) {
        warn(
            log,
            ExceptionMessages.METHOD_URI_MESSAGE_FORMAT,
            request.getMethod(),
            request.getRequestURI(),
            ex.getMessage()
        );

        return buildErrorResponse(
            HttpStatus.NOT_FOUND,
            ex.getMessage(),
            request
        );
    }

    @ExceptionHandler(LinkNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleLinkNotFound(
        LinkNotFoundException ex, HttpServletRequest request
    ) {
        warn(
            log,
            ExceptionMessages.METHOD_URI_MESSAGE_FORMAT,
            request.getMethod(),
            request.getRequestURI(),
            ex.getMessage()
        );

        return buildErrorResponse(
            HttpStatus.NOT_FOUND,
            ex.getMessage(),
            request
        );
    }

    @ExceptionHandler(LinksNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleLinksNotFound(
        LinksNotFoundException ex, HttpServletRequest request
    ) {
        warn(
            log,
            ExceptionMessages.METHOD_URI_MESSAGE_FORMAT,
            request.getMethod(),
            request.getRequestURI(),
            ex.getMessage()
        );

        return buildErrorResponse(
            HttpStatus.NOT_FOUND,
            ex.getMessage(),
            request
        );
    }

    @ExceptionHandler(LinkSaveException.class)
    public ResponseEntity<ApiErrorResponse> handleLinkSave(
        LinkSaveException ex, HttpServletRequest request
    ) {
        warn(
            log,
            ExceptionMessages.METHOD_URI_MESSAGE_FORMAT,
            request.getMethod(),
            request.getRequestURI(),
            ex.getMessage()
        );
        error(log, LogMessages.STACK_TRACE, ex);

        return buildErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ex.getMessage(),
            request
        );
    }

    @ExceptionHandler(LinkDeleteException.class)
    public ResponseEntity<ApiErrorResponse> handleLinkDelete(
        LinkDeleteException ex, HttpServletRequest request
    ) {
        warn(
            log,
            ExceptionMessages.METHOD_URI_MESSAGE_FORMAT,
            request.getMethod(),
            request.getRequestURI(),
            ex.getMessage()
        );
        error(log, LogMessages.STACK_TRACE, ex);

        return buildErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ex.getMessage(),
            request
        );
    }

    @ExceptionHandler(LinksDeleteException.class)
    public ResponseEntity<ApiErrorResponse> handleLinksDelete(
        LinksDeleteException ex, HttpServletRequest request
    ) {
        warn(
            log,
            ExceptionMessages.METHOD_URI_MESSAGE_FORMAT,
            request.getMethod(),
            request.getRequestURI(),
            ex.getMessage()
        );
        error(log, LogMessages.STACK_TRACE, ex);

        return buildErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ex.getMessage(),
            request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(
        Exception ex, HttpServletRequest request
    ) {
        warn(
            log,
            "Unhandled exception occurred: {} {} - {}",
            request.getMethod(),
            request.getRequestURI(),
            ex.getMessage()
        );
        error(log, LogMessages.STACK_TRACE, ex);

        return buildErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ex.getMessage(),
            request
        );
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(
        UnauthorizedAccessException ex, HttpServletRequest request
    ) {
       warn(
           log,
           "Unauthorized access attempt: {} {} - {}",
           request.getMethod(),
           request.getRequestURI(),
           ex.getMessage()
       );

        return buildErrorResponse(
            HttpStatus.FORBIDDEN,
            ex.getMessage(),
            request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(
        MethodArgumentNotValidException ex, HttpServletRequest request
    ) {
        List<String> validationMessages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .toList();

        warn(
            log,
            "Validation error(s) on [{} {}]: {}",
            request.getMethod(),
            request.getRequestURI(),
            validationMessages
        );
        return ResponseEntity.badRequest().body(new ApiErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ExceptionMessages.INVALID_FIELDS,
            validationMessages,
            Instant.now().toString(),
            request.getRequestURI()
            )
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolationErrors(
        ConstraintViolationException ex, HttpServletRequest request
    ) {
        List<String> errors = ex.getConstraintViolations()
            .stream()
            .map(violation ->
                violation.getPropertyPath() + ": " + violation.getMessage())
            .toList();

        warn(
            log,
            "Constraint violation(s): {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            errors
        );

        return ResponseEntity.badRequest().body(new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ExceptionMessages.INVALID_FIELDS,
                errors,
                Instant.now().toString(),
                request.getRequestURI()
            )
        );
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleUsernameAlreadyExists(
        UsernameAlreadyExistsException ex, HttpServletRequest request
    ) {
        warn(
            log,
            ExceptionMessages.FAILED_TO_CREATE_USER_FORMAT,
            request.getMethod(),
            request.getRequestURI(),
            ex.getMessage()
        );
        error(log, LogMessages.STACK_TRACE, ex);

        return buildErrorResponse(
            HttpStatus.CONFLICT,
            ex.getMessage(),
            request
        );
    }

    @ExceptionHandler(RegistrationFailedException.class)
    public ResponseEntity<ApiErrorResponse> handleRegistrationFailure(
        RegistrationFailedException ex, HttpServletRequest request
    ) {
        warn(
            log,
            ExceptionMessages.FAILED_TO_CREATE_USER_FORMAT,
            request.getMethod(),
            request.getRequestURI(),
            ex.getMessage()
        );
        error(log, LogMessages.STACK_TRACE, ex);

        return buildErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ex.getMessage(),
            request
        );
    }

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(
        HttpStatus status, String message, HttpServletRequest request
    ) {
        return ResponseEntity.status(status)
            .body(new ApiErrorResponse(
                status.value(),
                message,
                null,
                Instant.now().toString(),
                request.getRequestURI()
            )
        );
    }


}
