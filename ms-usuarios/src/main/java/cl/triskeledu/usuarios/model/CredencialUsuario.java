package cl.triskeledu.usuarios.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "credenciales_usuarios",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_credenciales_usuario_email", columnNames = "usuario_email")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CredencialUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_email", referencedColumnName = "email", nullable = false)
    private Usuario usuario;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @Column(name = "bloqueado", nullable = false)
    private Boolean bloqueado;

    @Column(name = "intentos_fallidos", nullable = false)
    private Integer intentosFallidos;
}
