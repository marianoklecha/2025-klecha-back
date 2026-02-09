package com.medibook.api.config;

import com.medibook.api.dto.ErrorResponseDTO;
import com.medibook.api.util.ErrorResponseUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return ErrorResponseUtil.createBadRequestResponse(
                "Error de validación: " + errors, 
                request.getRequestURI()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleMalformedRequest(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return ErrorResponseUtil.createBadRequestResponse(
            "Cuerpo de solicitud mal formado o JSON inválido", 
            request.getRequestURI()
        );
    }

    @ExceptionHandler({AccessDeniedException.class, AuthenticationException.class})
    public ResponseEntity<ErrorResponseDTO> handleSecurityExceptions(
            Exception ex, HttpServletRequest request) {
        log.warn("Security Error: {}", ex.getMessage());
        return ErrorResponseUtil.createErrorResponse(
            "ACCESS_DENIED",
            "No tienes permisos para acceder a este recurso",
            HttpStatus.FORBIDDEN,
            request.getRequestURI()
        );
    }

    @ExceptionHandler({EntityNotFoundException.class})
    public ResponseEntity<ErrorResponseDTO> handleNotFound(
            Exception ex, HttpServletRequest request) {
        return ErrorResponseUtil.createNotFoundResponse(ex.getMessage(), request.getRequestURI());
    }
    
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(
            NoResourceFoundException ex, HttpServletRequest request) {
        return ErrorResponseUtil.createNotFoundResponse("El recurso solicitado no existe", request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        return ErrorResponseUtil.createBadRequestResponse(ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleAllUncaughtExceptions(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled Exception caught: ", ex);
        
        return ErrorResponseUtil.createInternalServerErrorResponse(
            "Ha ocurrido un error interno inesperado.",
            request.getRequestURI()
        );
    }
}