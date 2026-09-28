package cl.triskeledu.catalogo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LibroProyeccionSyncRequest {

    private String isbn;
    private String titulo;
}
