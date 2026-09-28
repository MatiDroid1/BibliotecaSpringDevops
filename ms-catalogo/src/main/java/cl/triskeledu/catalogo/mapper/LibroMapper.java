package cl.triskeledu.catalogo.mapper;

import cl.triskeledu.catalogo.dto.LibroRequest;
import cl.triskeledu.catalogo.dto.LibroResponse;
import cl.triskeledu.catalogo.model.Libro;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LibroMapper {

    // Transforma el Request (DTO) a la Entidad para guardarla en la BD.
    // Ignoramos 'id' porque lo genera la BD y 'categorias' porque el Service
    // debe asociarlas manualmente buscando las entidades reales por ID.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categorias", ignore = true)
    Libro toEntity(LibroRequest request);

    // Transforma la Entidad a Response para devolver al cliente.
    // Aquí sí queremos que se mapeen las categorías para mostrarlas.
    LibroResponse toResponse(Libro libro);

    List<LibroResponse> toResponseList(List<Libro> libros);

    // Este método realiza una "Actualización sobre el Destino" usando @MappingTarget.
    // En lugar de crear un objeto Libro nuevo, MapStruct copia los datos del 'request'
    // directamente sobre la instancia de 'libro' que ya recuperamos de la base de datos.
    // Es vital para un alumno entender que esto:
    // 1. Mantiene la identidad de la entidad: Al trabajar sobre el mismo objeto, JPA
    //    sabe que debe hacer un UPDATE y no un INSERT.
    // 2. Protege el ID: Se ignora el 'id' del request para que nadie pueda cambiar
    //    la llave primaria del registro por accidente.
    // 3. Respeta la lógica de negocio: Ignoramos 'categorias' porque la relación 
    //    ManyToMany se gestiona manualmente en el Service para asegurar que las 
    //    asociaciones en la tabla de unión sean correctas.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categorias", ignore = true)
    void updateEntity(LibroRequest request, @MappingTarget Libro libro);
}
