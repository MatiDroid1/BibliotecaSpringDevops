package cl.triskeledu.common.exception;

public class ReferentialIntegrityException extends RuntimeException {

    /**
     * Constructor genérico para violaciones de integridad referencial.
     * @param entity El nombre de la entidad que se intenta eliminar (ej: "Libro").
     * @param id El identificador de dicha entidad.
     * @param dependencies Descripción de los registros que impiden el borrado (ej: "Categorías").
     */
    public ReferentialIntegrityException(String entity, Object id, String dependencies) {
        super(String.format("No se puede eliminar el registro de %s con ID '%s', "
            + "porque tiene registros asociados en: %s. Para eliminarlo, primero "
            + "debe eliminar o desvincular esos registros relacionados.", 
            entity, id.toString(), dependencies));
    }
}
