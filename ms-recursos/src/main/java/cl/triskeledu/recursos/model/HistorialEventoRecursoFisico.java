package cl.triskeledu.recursos.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
    name = "historial_eventos_recursos_fisicos",
    indexes = {
        @Index(name = "idx_historial_recurso", columnList = "recurso_id"),
        @Index(name = "idx_historial_usuario", columnList = "usuario_email")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialEventoRecursoFisico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_email", nullable = false)
    private UsuarioProyeccion usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recurso_id", nullable = false)
    private RecursoFisico recurso;

    @Column(name = "fecha_evento", nullable = false)
    private LocalDate fechaEvento;

    @Column(name = "estado", nullable = false, length = 50)
    private String estado;
}
