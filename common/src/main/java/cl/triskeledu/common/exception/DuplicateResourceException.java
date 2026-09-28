package cl.triskeledu.common.exception;

public class DuplicateResourceException extends RuntimeException {

    /**
     * Constructor genérico para registros duplicados.
     * @param entity El nombre de la entidad (ej: "Libro", "Usuario").
     * @param field El nombre del campo duplicado (ej: "ISBN", "Email").
     * @param value El valor que causó el conflicto.
     * @param description La descripción del recurso duplicado.
     */
    public DuplicateResourceException(String entity, String field, Object value, String description) {
        super(String.format("%s con %s igual a '%s' ya existe en el sistema, descrito por '%s'.", 
              entity, field, (value != null ? value.toString() : "N/A"), description));
    }
}
