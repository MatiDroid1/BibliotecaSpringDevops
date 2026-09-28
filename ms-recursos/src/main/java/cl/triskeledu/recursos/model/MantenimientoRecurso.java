package cl.triskeledu.recursos.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "mantenimiento_recursos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MantenimientoRecurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recurso_id", nullable = false)
    private RecursoFisico recurso;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "estado", nullable = false, length = 40)
    private String estado;

    @Column(name = "observacion", length = 200)
    private String observacion;
}
