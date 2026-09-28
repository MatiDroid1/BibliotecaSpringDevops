/* ============================================================
   ARCHIVO: 01-usuarios.sql
   Microservicio: usuarios
   Responsabilidad: administrar cuentas, perfiles y credenciales.
   ============================================================ */

-- 1. ELIMINACIÓN EN JERARQUÍA INVERSA
DROP TABLE IF EXISTS credenciales_usuarios;
DROP TABLE IF EXISTS perfil_usuarios;
DROP TABLE IF EXISTS usuarios;

-- 2. TABLAS MAESTRAS
CREATE TABLE usuarios (
    id                SERIAL       PRIMARY KEY,
    nombre            VARCHAR(150) NOT NULL,
    apellido          VARCHAR(150) NOT NULL,
    email             VARCHAR(150) UNIQUE NOT NULL,
    -- [JJWT] Se debe aumentar el tamaño de la password
    password          VARCHAR(255) NOT NULL, 
    rol               VARCHAR(50)  NOT NULL CHECK (rol IN ('Administrador','Bibliotecario','Cliente')),
    activo            BOOLEAN      DEFAULT TRUE
);

CREATE TABLE perfil_usuarios (
    id                SERIAL       PRIMARY KEY,
    usuario_email     VARCHAR(150) UNIQUE NOT NULL REFERENCES usuarios(email) ON DELETE CASCADE,
    telefono          VARCHAR(30),
    direccion         VARCHAR(180),
    fecha_registro    DATE         NOT NULL DEFAULT CURRENT_DATE
);

CREATE TABLE credenciales_usuarios (
    id                SERIAL       PRIMARY KEY,
    usuario_email     VARCHAR(150) UNIQUE NOT NULL REFERENCES usuarios(email) ON DELETE CASCADE,
    ultimo_acceso     TIMESTAMP,
    bloqueado         BOOLEAN      NOT NULL DEFAULT FALSE,
    intentos_fallidos INT          NOT NULL DEFAULT 0 CHECK (intentos_fallidos >= 0)
);

CREATE INDEX idx_usuarios_rol ON usuarios(rol);
CREATE INDEX idx_usuarios_activo ON usuarios(activo);
CREATE INDEX idx_perfil_usuario_email ON perfil_usuarios(usuario_email);

-- 3. DATOS DE PRUEBA
-- [JJWT-INI] 
-- La contraseña por defecto fue configurada como 'Biblio@2026' para todos los usuarios, y está almacenada como 
-- hash (huella digital) utilizando el algoritmo BCrypt (generado con BCryptPasswordEncoder de Spring Security).
INSERT INTO usuarios (nombre, apellido, email, password, rol) VALUES
('Ana',      'Aguilar',   'ana@administrador.cl',     '$2b$10$1hnbaMR7iTsdn3D0gG5Q8eUw5aSh9O2at2e4u1iAlzdhD6m4dzVZO',  'Administrador'),
('Andrés',   'Acosta',    'andres@administrador.cl',  '$2b$10$1hnbaMR7iTsdn3D0gG5Q8eUw5aSh9O2at2e4u1iAlzdhD6m4dzVZO',  'Administrador'),
('Adrián',   'Álvarez',   'adrian@administrador.cl',  '$2b$10$1hnbaMR7iTsdn3D0gG5Q8eUw5aSh9O2at2e4u1iAlzdhD6m4dzVZO',  'Administrador'),
('Beatriz',  'Bermúdez',  'beatriz@bibliotecario.cl', '$2b$10$1hnbaMR7iTsdn3D0gG5Q8eUw5aSh9O2at2e4u1iAlzdhD6m4dzVZO',  'Bibliotecario'),
('Benito',   'Barrios',   'benito@bibliotecario.cl',  '$2b$10$1hnbaMR7iTsdn3D0gG5Q8eUw5aSh9O2at2e4u1iAlzdhD6m4dzVZO',  'Bibliotecario'),
('Belén',    'Bravo',     'belen@bibliotecario.cl',   '$2b$10$1hnbaMR7iTsdn3D0gG5Q8eUw5aSh9O2at2e4u1iAlzdhD6m4dzVZO',  'Bibliotecario'),
('Carlos',   'Contreras', 'carlos@cliente.cl',        '$2b$10$1hnbaMR7iTsdn3D0gG5Q8eUw5aSh9O2at2e4u1iAlzdhD6m4dzVZO', 'Cliente'),
('Camila',   'Cervantes', 'camila@cliente.cl',        '$2b$10$1hnbaMR7iTsdn3D0gG5Q8eUw5aSh9O2at2e4u1iAlzdhD6m4dzVZO', 'Cliente'),
('Cristian', 'Castro',    'cristian@cliente.cl',      '$2b$10$1hnbaMR7iTsdn3D0gG5Q8eUw5aSh9O2at2e4u1iAlzdhD6m4dzVZO', 'Cliente');
-- [JJWT-FIN] 

INSERT INTO perfil_usuarios (usuario_email, telefono, direccion) VALUES
('ana@administrador.cl',     '+56911111111', 'Sede Central'),
('beatriz@bibliotecario.cl', '+56922222222', 'Sede Providencia'),
('carlos@cliente.cl',        '+56933333333', 'Santiago'),
('camila@cliente.cl',        NULL,           'Valparaíso'),
('cristian@cliente.cl',      '+56955555555', 'La Serena');

INSERT INTO credenciales_usuarios (usuario_email, ultimo_acceso, bloqueado, intentos_fallidos) VALUES
('ana@administrador.cl',     NOW(), FALSE, 0),
('beatriz@bibliotecario.cl', NOW(), FALSE, 0),
('carlos@cliente.cl',        NOW(), FALSE, 1),
('camila@cliente.cl',        NULL,  FALSE, 0),
('cristian@cliente.cl',      NULL,  TRUE,  3);
