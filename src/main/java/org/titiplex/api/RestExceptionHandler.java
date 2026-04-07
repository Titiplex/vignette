package org.titiplex.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.titiplex.api.dto.ApiError;

import java.time.Instant;

@SuppressWarnings("JvmTaintAnalysis")
@RestControllerAdvice
public class RestExceptionHandler {


}