package com.linkvault.exception;

public record ApiErrorResponse(
   int status,
   String message,
   String timestamp,
   String requestUri
) {}
