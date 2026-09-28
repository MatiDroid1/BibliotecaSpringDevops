package cl.triskeledu.common.exception;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Manejador global de excepciones para toda la API REST.
 *
 * @RestControllerAdvice intercepta todas las excepciones no capturadas en los
 * controladores y las transforma en respuestas HTTP coherentes con el envelope
 * ApiError. Esto desacopla la lógica de manejo de errores de los controladores,
 * centralizando la responsabilidad en un único punto (principio DRY + SRP).
 *
 * Orden de prioridad de los handlers: Spring usa el más específico primero.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─── 401 UNAUTHORIZED ─────────────────────────────────────────────────────

    /**
     * Captura errores de autenticación (token ausente, inválido o expirado).
     * Devuelve 401 UNAUTHORIZED con un mensaje claro para el cliente.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(
            AuthenticationException ex,
            HttpServletRequest request) {

        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message("No autenticado. Debe proporcionar un token JWT válido en el header Authorization.")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // ─── 403 FORBIDDEN ────────────────────────────────────────────────────────

    /**
     * Captura errores de autorización (usuario autenticado pero sin permisos).
     * Devuelve 403 FORBIDDEN indicando que el rol no tiene acceso al recurso.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message("Acceso denegado. Su rol no tiene permisos para acceder a este recurso.")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // ─── 404 NOT FOUND ────────────────────────────────────────────────────────

    /**
     * Captura búsquedas de entidades inexistentes (por ID o ISBN).
     * Devuelve 404 NOT FOUND con el mensaje de la excepción de dominio.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleLibroNotFound(
            EntityNotFoundException ex,
            HttpServletRequest request) {

        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(feign.FeignException.class)
    public ResponseEntity<ApiError> handleFeignException(
            feign.FeignException ex,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.resolve(ex.status());

        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(resolveFeignMessage(ex))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(status).body(error);
    }

    private String resolveFeignMessage(feign.FeignException ex) {

        if (ex.status() == 404) {
            return "El recurso solicitado no existe en el microservicio externo.";
        }

        if (ex.status() == 400) {
            return "Solicitud inválida enviada a microservicio externo.";
        }

        return "Error en comunicación con microservicio externo.";
    }

    /**
     * Captura requests hacia endpoints inexistentes.
     *
     * Ejemplo:
     * GET /api/v1/libro  (incorrecto)
     * GET /api/v1/libros (correcto)
     *
     * En Spring Boot moderno pueden ocurrir dos escenarios:
     *
     * 1) Spring lanza NoHandlerFoundException (si está habilitado)
     * 2) Spring NO lanza excepción y maneja internamente el 404
     *
     * Este handler cubre el caso donde sí se lanza excepción.
     * Para máxima compatibilidad se maneja por tipo en el handler genérico.
     */
    @ExceptionHandler({
        org.springframework.web.servlet.NoHandlerFoundException.class,
        org.springframework.web.servlet.resource.NoResourceFoundException.class
    })
    public ResponseEntity<ApiError> handleEndpointNotFound(
            Exception ex,
            HttpServletRequest request) {

        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message("El endpoint solicitado no existe")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // ─── 409 CONFLICT ─────────────────────────────────────────────────────────

    /**
     * Captura intentos de crear o actualizar recursos (entidades) con valores duplicados.
     * El HTTP 409 CONFLICT es semánticamente más preciso que 400 en este caso,
     * ya que el problema es un conflicto de estado del recurso, no un input inválido.
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleDuplicateResource(
            DuplicateResourceException ex,
            HttpServletRequest request) {

        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // ─── 400 BAD REQUEST (Validación de campos) ───────────────────────────────

    /**
     * Captura errores de validación de Bean Validation disparados por @Valid.
     * Recopila todos los mensajes de error de campo en una lista para mayor
     * claridad hacia el cliente: sabe exactamente qué campo falló y por qué.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        // Recopila todos los mensajes de error de los campos inválidos
        List<String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> "'" + fe.getField() + "': " + fe.getDefaultMessage())
                .collect(Collectors.toList());

        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("La solicitud contiene datos inválidos. Revise el campo 'errors'.")
                .errors(fieldErrors)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    // ─── 500 INTERNAL SERVER ERROR ────────────────────────────────────────────

    /**
     * Captura cualquier excepción no controlada como red de seguridad.
     *
     * IMPORTANTE:
     * Este handler es el más genérico y debe quedar al final, ya que captura
     * cualquier excepción que no haya sido manejada previamente por handlers
     * más específicos.
     *
     * Además, aquí se detectan manualmente casos de errores genéricos 
     * cuando Spring NO lanza excepción específica (comportamiento común en
     * versiones modernas).
     *
     * Devuelve un mensaje genérico al cliente (nunca exponemos detalles internos
     * por seguridad) y el detalle real queda registrado en los logs del servidor.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        // Manejo manual de endpoint inexistente (fallback seguro)
        if (ex instanceof org.springframework.web.servlet.NoHandlerFoundException ||
            ex instanceof org.springframework.web.servlet.resource.NoResourceFoundException) {

            ApiError error = ApiError.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.NOT_FOUND.value())
                    .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                    .message("El endpoint solicitado no existe")
                    .path(request.getRequestURI())
                    .build();

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        // Error genérico real del servidor
        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("Ocurrió un error interno inesperado. Por favor, intente más tarde.")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // ─── 400 BAD REQUEST (JSON Mal formado) ──────────────────────────────────

    /**
     * Captura errores cuando el JSON enviado por el cliente es inválido 
     * (faltan comas, llaves, comillas, etc.).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleJsonMalformed(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        ApiError error = ApiError.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .message("El cuerpo de la solicitud (JSON) no es legible o tiene un formato inválido.")
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

}