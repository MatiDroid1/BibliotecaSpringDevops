package cl.triskeledu.recursos.mapper;

import cl.triskeledu.recursos.dto.LibroProyeccionResponse;
import cl.triskeledu.recursos.model.LibroProyeccion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LibroProyeccionMapper {

    @Mapping(target = "isbn", source = "isbn")
    @Mapping(target = "titulo", source = "titulo")
    LibroProyeccionResponse toResponse(LibroProyeccion libroProyeccion);

    List<LibroProyeccionResponse> toResponseList(List<LibroProyeccion> libroProyecciones);

}
