# Biblioteca Spring Boot

Proyecto académico de **microservicios con Spring Boot** utilizado en el laboratorio de Ingeniería DevOps para practicar control de versiones, documentación de repositorios, Pull Requests, GitHub Actions y despliegue en AWS Academy Learner Lab.

## Propósito

El sistema representa una biblioteca implementada mediante varios módulos Maven. En esta actividad el foco no está en modificar la arquitectura, sino en aplicar un flujo de trabajo DevOps sobre un proyecto real.

## Arquitectura del proyecto

| Módulo | Función | Puerto |
| --- | --- | ---: |
| `eureka` | Servidor de descubrimiento de servicios | 8761 |
| `ms-usuarios` | Gestión de usuarios y autenticación | 9001 |
| `ms-catalogo` | Gestión del catálogo de libros | 9002 |
| `ms-recursos` | Gestión de recursos físicos | 9003 |
| `api-gateway` | Punto de entrada único a los microservicios | 9000 |
| `common` | Código compartido entre microservicios | No aplica |
| `frontend` | Vista web estática publicada por Apache | 80 |

## Requisitos para desarrollo

- Java 21.
- Maven 3.9 o compatible.
- PostgreSQL 16 o compatible.
- Git.

## Compilar el proyecto

Desde la raíz del proyecto:

```bash
mvn clean package -DskipTests
```

El comando compila todos los módulos definidos en el `pom.xml` padre y genera los archivos JAR en las carpetas `target` de cada microservicio.

## Flujo Git utilizado en el laboratorio

```text
dev -> Pull Request -> main -> GitHub Actions -> AWS EC2
```

- `dev`: rama donde se realizan los cambios.
- `main`: rama estable que representa la versión a desplegar.
- Los Pull Requests permiten revisar los cambios antes de integrarlos.
- Un `push` a `main` activa el despliegue automático en AWS.

## Pipeline

El workflow se encuentra en:

```text
.github/workflows/deploy.yml
```

Cuando existe un Pull Request hacia `main`, GitHub Actions valida que el proyecto compile y comprueba que estén presentes los archivos de la vista web. Cuando el Pull Request se fusiona y `main` cambia, el pipeline compila, copia los JAR y el directorio `frontend` a EC2, reinicia los microservicios y publica la vista mediante Apache.

## Variables secretas requeridas en GitHub

| Secret | Uso |
| --- | --- |
| `AWS_HOST` | IPv4 pública de la instancia EC2 |
| `AWS_USER` | Usuario SSH; para Amazon Linux normalmente `ec2-user` |
| `AWS_SSH_KEY` | Contenido completo de la clave privada `.pem` |

> Nunca subas un archivo `.pem`, contraseñas o tokens al repositorio.

## Verificación del despliegue

Después de un despliegue correcto:

```text
http://<IP-PUBLICA>/
http://<IP-PUBLICA>:8761
http://<IP-PUBLICA>:9000/actuator/health
```

El primer enlace muestra la nueva **vista web de Biblioteca Nova**. Eureka y Actuator quedan disponibles como verificaciones técnicas adicionales si sus puertos están habilitados en el Security Group.

## Vista web para el laboratorio

La carpeta `frontend/` contiene una interfaz HTML/CSS/JavaScript sin proceso de compilación adicional. Está pensada para que el cambio realizado por el estudiante sea fácil de observar después del despliegue.

Cambios sugeridos:

- Cambiar `v1.0` por `v2.0` en `frontend/index.html`.
- Modificar el título principal.
- Cambiar el nombre de un libro.
- Agregar una tarjeta al catálogo.

El JavaScript consulta `/gateway-health`. Apache redirige esa ruta internamente al endpoint `http://127.0.0.1:9000/actuator/health`, por lo que la página puede mostrar si el API Gateway está `UP` sin exponer credenciales ni configurar CORS para la IP pública.

## Documentación del repositorio

- [CONTRIBUTING.md](CONTRIBUTING.md): normas para colaborar.
- [CHANGELOG.md](CHANGELOG.md): historial de versiones.
- [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md): normas de convivencia.
- [LICENSE.md](LICENSE.md): condiciones de uso del proyecto.

---

## Semana 2 - Orquestación con Docker Compose

Esta versión conserva la arquitectura original del Proyecto Biblioteca y la orquesta con Docker Compose.

### Servicios

- `postgres`: PostgreSQL 16 con un volumen persistente.
- `eureka`: servidor de descubrimiento.
- `ms-usuarios`: microservicio de usuarios.
- `ms-catalogo`: microservicio del catálogo.
- `ms-recursos`: microservicio de recursos.
- `api-gateway`: punto de entrada a los microservicios.
- `frontend`: Nginx que publica la página y hace reverse proxy hacia el Gateway.

### Levantar todo

```bash
# Construye imágenes y levanta todos los contenedores.
sudo docker compose up -d --build
```

### Revisar estado

```bash
sudo docker compose ps
```

### Seguir logs

```bash
sudo docker compose logs -f
```

### Detener conservando datos

```bash
sudo docker compose down
```

### Detener y eliminar el volumen de PostgreSQL

```bash
# ATENCIÓN: elimina los datos persistentes del laboratorio.
sudo docker compose down -v
```

### URLs del laboratorio

- Frontend: `http://IP_PUBLICA/`
- Eureka (si el Security Group permite 8761): `http://IP_PUBLICA:8761/`

### Requisito de EC2

Debido a que esta versión levanta PostgreSQL y cinco procesos Java además del frontend, se recomienda una instancia con **al menos 2 GB de RAM** para el laboratorio.
