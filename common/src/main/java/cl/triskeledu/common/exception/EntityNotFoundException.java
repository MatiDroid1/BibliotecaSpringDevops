package cl.triskeledu.common.exception;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String entity, String key, Object value) {
        super(String.format("No se encontraron registros en %s con el %s '%s'", 
            entity, key, (value != null ? value.toString() : "N/A")));
    }
}
