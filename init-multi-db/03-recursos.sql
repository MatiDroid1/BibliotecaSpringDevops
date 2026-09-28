/* ============================================================
   ARCHIVO: 03-recursos.sql
   Microservicio: recursos
   Responsabilidad: inventario físico, disponibilidad y eventos.
   Proyecciones: usuarios_proyeccion y libros_proyeccion sincronizadas por Feign.
   ============================================================ */

DROP TABLE IF EXISTS mantenimiento_recursos;
DROP TABLE IF EXISTS historial_eventos_recursos_fisicos;
DROP TABLE IF EXISTS recursos_fisicos;
DROP TABLE IF EXISTS usuarios_proyeccion;
DROP TABLE IF EXISTS libros_proyeccion;

-- PROYECCIONES MÍNIMAS LOCALES
CREATE TABLE libros_proyeccion (
    isbn               VARCHAR(20)  PRIMARY KEY,
    titulo             VARCHAR(255) NOT NULL
);

CREATE TABLE usuarios_proyeccion (
    email              VARCHAR(150) PRIMARY KEY,
    nombre             VARCHAR(150) NOT NULL,
    apellido           VARCHAR(150) NOT NULL,
    rol                VARCHAR(50)  NOT NULL
);

-- TABLAS DEL MICROSERVICIO
CREATE TABLE recursos_fisicos (
    id                 SERIAL       PRIMARY KEY,
    sku                VARCHAR(50)  UNIQUE NOT NULL,
    tipo_recurso       VARCHAR(50)  NOT NULL CHECK (tipo_recurso IN ('Libro','Notebook','Tablet','Juego de mesa')),
    isbn               VARCHAR(20)  REFERENCES libros_proyeccion(isbn),
    estado_fisico      VARCHAR(50)  DEFAULT 'Excelente' CHECK (estado_fisico IN ('Excelente','Buen estado','Dañado','En reparación')),
    disponible         BOOLEAN      DEFAULT TRUE,
    CONSTRAINT chk_isbn_segun_tipo CHECK ((tipo_recurso = 'Libro' AND isbn IS NOT NULL) OR (tipo_recurso <> 'Libro'))
);

CREATE TABLE historial_eventos_recursos_fisicos (
    id                 SERIAL       PRIMARY KEY,
    usuario_email      VARCHAR(150) NOT NULL REFERENCES usuarios_proyeccion(email),
    recurso_id         INT          NOT NULL REFERENCES recursos_fisicos(id),
    fecha_evento       DATE         NOT NULL,
    estado             VARCHAR(50)  NOT NULL CHECK (estado IN ('Creado','Reservado','Prestado','Devuelto a tiempo','Devuelto con atraso','Perdido'))
);

CREATE TABLE mantenimiento_recursos (
    id                 SERIAL       PRIMARY KEY,
    recurso_id         INT          NOT NULL REFERENCES recursos_fisicos(id),
    fecha_inicio       DATE         NOT NULL,
    estado             VARCHAR(40)  NOT NULL CHECK (estado IN ('Abierto','Cerrado','Cancelado')),
    observacion        VARCHAR(200)
);

CREATE INDEX idx_recursos_sku ON recursos_fisicos(sku);
CREATE INDEX idx_recursos_disponible ON recursos_fisicos(disponible);
CREATE INDEX idx_historial_recurso ON historial_eventos_recursos_fisicos(recurso_id);
CREATE INDEX idx_historial_usuario ON historial_eventos_recursos_fisicos(usuario_email);

-- DATOS DE PROYECCIÓN
INSERT INTO libros_proyeccion (isbn, titulo) VALUES
('9798344055985', 'Moby Dick'), ('9788437604947', 'Cien años de soledad'),
('9788420651323', 'El Principito'), ('9780141036137', '1984'),
('9788497592208', 'El resplandor'), ('9781537822075', 'Dracula'),
('9788420674209', 'Don Quijote'), ('9781644732076', 'Harry Potter'),
('9788445077412', 'El Hobbit');

INSERT INTO usuarios_proyeccion (email, nombre, apellido, rol) VALUES
('ana@administrador.cl','Ana','Aguilar','Administrador'),
('beatriz@bibliotecario.cl','Beatriz','Bermúdez','Bibliotecario'),
('carlos@cliente.cl','Carlos','Contreras','Cliente'),
('camila@cliente.cl','Camila','Cervantes','Cliente'),
('cristian@cliente.cl','Cristian','Castro','Cliente');

INSERT INTO recursos_fisicos (sku, tipo_recurso, isbn, estado_fisico, disponible) VALUES
('SKU-MOBY-001','Libro','9798344055985','Excelente',TRUE),
('SKU-CIEN-001','Libro','9788437604947','Buen estado',TRUE),
('SKU-PRIN-001','Libro','9788420651323','Excelente',FALSE),
('SKU-DRAC-001','Libro','9781537822075','Dañado',FALSE),
('SKU-HOBB-001','Libro','9788445077412','Excelente',TRUE),
('SKU-NOTE-001','Notebook',NULL,'En reparación',FALSE),
('SKU-TAB-001','Tablet',NULL,'Buen estado',TRUE);

INSERT INTO historial_eventos_recursos_fisicos (usuario_email, recurso_id, fecha_evento, estado) VALUES
('ana@administrador.cl',1,'2025-11-01','Creado'),
('carlos@cliente.cl',3,'2025-11-04','Prestado'),
('camila@cliente.cl',2,'2025-11-15','Devuelto con atraso'),
('cristian@cliente.cl',4,'2025-11-08','Perdido');

INSERT INTO mantenimiento_recursos (recurso_id, fecha_inicio, estado, observacion) VALUES
(6,'2025-11-12','Abierto','Equipo no enciende'),
(4,'2025-11-09','Cerrado','Libro marcado como dañado');
