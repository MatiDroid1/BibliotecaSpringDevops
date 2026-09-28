package cl.triskeledu.recursos.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuarios_proyeccion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioProyeccion {

    @Id
    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 150)
    private String apellido;

    @Column(name = "rol", nullable = false, length = 50)
    private String rol;
}
