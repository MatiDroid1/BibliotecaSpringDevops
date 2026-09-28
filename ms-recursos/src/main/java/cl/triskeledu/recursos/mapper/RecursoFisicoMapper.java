package cl.triskeledu.recursos.mapper;

import cl.triskeledu.recursos.dto.RecursoFisicoRequest;
import cl.triskeledu.recursos.dto.RecursoFisicoResponse;
import cl.triskeledu.recursos.model.RecursoFisico;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecursoFisicoMapper {

    // Transforma el Request (DTO) a la Entidad para guardarla en la BD.
    // Ignoramos 'id', la relación 'libro' (se resuelve en el service buscando
    // la proyección por ISBN) y las colecciones de historial y mantenimientos.
    @Mapping(target = "id",              ignore = true)
    @Mapping(target = "libro",           ignore = true)
    @Mapping(target = "historial",       ignore = true)
    @Mapping(target = "mantenimientos",  ignore = true)
    RecursoFisico toEntity(RecursoFisicoRequest request);

    // Transforma la Entidad a Response aplanando la relación con LibroProyeccion.
    // Si el recurso no es un Libro, libro será null y los campos isbn/titulo quedarán null.
    @Mapping(target = "isbn",   source = "libro.isbn")
    @Mapping(target = "titulo", source = "libro.titulo")
    RecursoFisicoResponse toResponse(RecursoFisico recurso);

    List<RecursoFisicoResponse> toResponseList(List<RecursoFisico> recursos);

    // Actualización sobre el destino: copia los datos del request en el
    // objeto persistido para que JPA reconozca el UPDATE sin cambiar el ID.
    @Mapping(target = "id",              ignore = true)
    @Mapping(target = "libro",           ignore = true)
    @Mapping(target = "historial",       ignore = true)
    @Mapping(target = "mantenimientos",  ignore = true)
    void updateEntity(RecursoFisicoRequest request, @MappingTarget RecursoFisico recurso);
}
