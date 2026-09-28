/* ============================================================
   ARCHIVO: 02-catalogo.sql
   Microservicio: catalogo
   Responsabilidad: administrar libros y clasificación local.
   ============================================================ */

-- 1. ELIMINACIÓN EN JERARQUÍA INVERSA
DROP TABLE IF EXISTS libro_categoria;
DROP TABLE IF EXISTS libros;
DROP TABLE IF EXISTS categorias;

-- 2. TABLAS MAESTRAS
CREATE TABLE categorias (
    id                SERIAL       PRIMARY KEY,
    nombre            VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE libros (
    id                SERIAL       PRIMARY KEY,
    isbn              VARCHAR(20)  UNIQUE NOT NULL,
    titulo            VARCHAR(255) NOT NULL,
    editorial         VARCHAR(100) NOT NULL,
    anio_publicacion  INT          NOT NULL CHECK (anio_publicacion BETWEEN 1450 AND 2100),
    autor             VARCHAR(150) NOT NULL
);

CREATE TABLE libro_categoria (
    id                SERIAL       PRIMARY KEY,
    libro_id          INT          NOT NULL REFERENCES libros(id) ON DELETE CASCADE,
    categoria_id      INT          NOT NULL REFERENCES categorias(id) ON DELETE RESTRICT,
    UNIQUE (libro_id, categoria_id)
);

CREATE INDEX idx_libros_isbn ON libros(isbn);
CREATE INDEX idx_libros_titulo ON libros(titulo);
CREATE INDEX idx_libro_categoria_libro ON libro_categoria(libro_id);
CREATE INDEX idx_libro_categoria_categoria ON libro_categoria(categoria_id);

-- 3. DATOS DE PRUEBA
INSERT INTO categorias (nombre) VALUES
('Novela'), ('Terror'), ('Fantasía'), ('Aventura'), ('Clásico'), ('Drama'), ('Distopía'), ('Infantil');

INSERT INTO libros (isbn, titulo, editorial, anio_publicacion, autor) VALUES
('9798344055985', 'Moby Dick',            'Elderwand',  2024, 'Herman Melville'),
('9788437604947', 'Cien años de soledad', 'Cátedra',    2007, 'G. García Márquez'),
('9788420651323', 'El Principito',        'Alianza',    1943, 'A. de Saint-Exupéry'),
('9780141036137', '1984',                 'Penguin',    2008, 'George Orwell'),
('9788497592208', 'El resplandor',        'Debolsillo', 2012, 'Stephen King'),
('9781537822075', 'Dracula',              'Feltrinelli',2011, 'Bram Stoker'),
('9788420674209', 'Don Quijote',          'Alianza',    2011, 'M. de Cervantes'),
('9781644732076', 'Harry Potter',         'Pottermore', 1997, 'J.K. Rowling'),
('9788445077412', 'El Hobbit',            'Minotauro',  2012, 'J.R.R. Tolkien');

INSERT INTO libro_categoria (libro_id, categoria_id) VALUES
(1, 4), (1, 5), (2, 1), (2, 5), (3, 8), (3, 5), (4, 1), (4, 7),
(5, 2), (5, 6), (6, 2), (6, 5), (7, 4), (7, 5), (8, 3), (8, 4),
(9, 3), (9, 4);
