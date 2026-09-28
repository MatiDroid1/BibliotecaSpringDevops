package cl.triskeledu.usuarios.mapper;

import cl.triskeledu.usuarios.dto.UsuarioRequest;
import cl.triskeledu.usuarios.dto.UsuarioResponse;
import cl.triskeledu.usuarios.model.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    // Transforma el Request (DTO) a la Entidad para guardarla en la BD.
    // Ignoramos 'id', 'activo' (se gestiona en el service) y las relaciones
    // OneToOne (perfil y credencial), que se crean en el service cuando corresponde.
    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "activo",     ignore = true)
    @Mapping(target = "perfil",     ignore = true)
    @Mapping(target = "credencial", ignore = true)
    Usuario toEntity(UsuarioRequest request);

    // Transforma la Entidad a Response para devolver al cliente.
    // La contraseña nunca se expone en la respuesta.
    UsuarioResponse toResponse(Usuario usuario);

    List<UsuarioResponse> toResponseList(List<Usuario> usuarios);

    // Actualización sobre el destino: copia los datos del request en el
    // objeto persistido para que JPA reconozca el UPDATE sin cambiar el ID.
    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "activo",     ignore = true)
    @Mapping(target = "perfil",     ignore = true)
    @Mapping(target = "credencial", ignore = true)
    void updateEntity(UsuarioRequest request, @MappingTarget Usuario usuario);
}
