package cl.triskeledu.recursos.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "recursos_fisicos",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_recursos_sku", columnNames = "sku")
    },
    indexes = {
        @Index(name = "idx_recursos_sku", columnList = "sku"),
        @Index(name = "idx_recursos_disponible", columnList = "disponible")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecursoFisico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku", nullable = false, length = 50, unique = true)
    private String sku;

    @Column(name = "tipo_recurso", nullable = false, length = 50)
    private String tipoRecurso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "isbn")
    private LibroProyeccion libro;

    @Column(name = "estado_fisico", length = 50)
    private String estadoFisico;

    @Column(name = "disponible")
    private Boolean disponible;

    @Builder.Default
    @OneToMany(mappedBy = "recurso", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistorialEventoRecursoFisico> historial = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "recurso", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MantenimientoRecurso> mantenimientos = new ArrayList<>();
}
