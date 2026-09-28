package cl.triskeledu.usuarios.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
    name = "perfil_usuarios",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_perfil_usuario_email", columnNames = "usuario_email")
    },
    indexes = {
        @Index(name = "idx_perfil_usuario_email", columnList = "usuario_email")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_email", referencedColumnName = "email", nullable = false)
    private Usuario usuario;

    @Column(name = "telefono", length = 30)
    private String telefono;

    @Column(name = "direccion", length = 180)
    private String direccion;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;
}
