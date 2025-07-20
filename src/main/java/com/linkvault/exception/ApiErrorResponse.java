package com.linkvault.exception;

import java.util.List;

public record ApiErrorResponse(
   int status,
   String message,
   List<String> errors,
   String timestamp,
   String requestUri
) {}
