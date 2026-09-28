package cl.triskeledu.common.exception;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiError {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;

    /**
     * Lista de errores de campo para respuestas de validación (HTTP 400).
     * Vacía o ausente en errores no relacionados con validación de campos.
     */
    private List<String> errors;

    /** Ruta URI que originó la petición con error. */
    private String path;
}
