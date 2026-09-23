package com.facturadorsri.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Handler global de errores. Nunca expone stack traces ni mensajes internos
 * de excepciones inesperadas (solo se loguean en el servidor).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> manejarNoEncontrado(ResourceNotFoundException ex, HttpServletRequest req) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> manejarNegocio(BusinessException ex, HttpServletRequest req) {
        return construir(HttpStatus.valueOf(422), ex.getMessage(), req);
    }

    @ExceptionHandler(SriIntegrationException.class)
    public ResponseEntity<ApiError> manejarSri(SriIntegrationException ex, HttpServletRequest req) {
        log.warn("Fallo de integracion con el SRI: {}", ex.getMessage());
        return construir(HttpStatus.BAD_GATEWAY, "No se pudo completar la comunicacion con el SRI", req);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiError> manejarRateLimit(RateLimitExceededException ex, HttpServletRequest req) {
        return construir(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), req);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> manejarCredencialesInvalidas(HttpServletRequest req) {
        return construir(HttpStatus.UNAUTHORIZED, "Usuario o contrasena incorrectos", req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> manejarAccesoDenegado(HttpServletRequest req) {
        return construir(HttpStatus.FORBIDDEN, "No tiene permisos para esta operacion", req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> manejarArgumentoInvalido(IllegalArgumentException ex, HttpServletRequest req) {
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> manejarValidacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList();
        ApiError error = ApiError.conDetalles(HttpStatus.BAD_REQUEST.value(), "Solicitud invalida",
                "Uno o mas campos no son validos", req.getRequestURI(), detalles);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> manejarError(Exception ex, HttpServletRequest req) {
        log.error("Error inesperado procesando {}", req.getRequestURI(), ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error inesperado", req);
    }

    private ResponseEntity<ApiError> construir(HttpStatus status, String mensaje, HttpServletRequest req) {
        ApiError error = ApiError.de(status.value(), status.getReasonPhrase(), mensaje, req.getRequestURI());
        return ResponseEntity.status(status).body(error);
    }
}
