# Changelog

Este archivo registra los cambios relevantes del proyecto organizados por versión.

## [Unreleased]

### Added
- Documentación estándar del repositorio en Markdown.
- Workflow de GitHub Actions para validar Pull Requests y desplegar `main` en AWS EC2.
- Scripts de apoyo para preparar y reiniciar el entorno del laboratorio.
- Vista web `frontend/` para visualizar los cambios desplegados en AWS.
- Publicación automática de la vista web mediante Apache.
- Proxy de Apache hacia el API Gateway para comprobar su estado desde el navegador.

## [0.1.0] - 2026-08-28

### Added
- Proyecto Biblioteca con arquitectura de microservicios Spring Boot.
- Eureka Server.
- Microservicios de usuarios, catálogo y recursos.
- API Gateway.
- Scripts SQL para bases de datos PostgreSQL.

> En un proyecto real se recomienda actualizar este archivo en cada versión publicada y describir cambios que afecten a usuarios, operación o compatibilidad.

## [Semana 2 - Docker Compose]

- Se agrega `docker-compose.yml` para orquestar el proyecto completo.
- Se agrega PostgreSQL 16 como contenedor con volumen persistente.
- Se agregan `healthcheck` y `depends_on` con `condition: service_healthy`.
- Se agrega una red interna para resolución por nombre de servicio.
- Se agregan límites de CPU y memoria para fines didácticos.
- Se agrega Dockerfile Multi-stage reutilizable para los módulos Java.
- Se agrega Nginx como contenedor del frontend y reverse proxy al API Gateway.
- Se actualiza GitHub Actions para validar y desplegar mediante Docker Compose.
- EC2 ahora requiere Docker y Docker Compose; Java/Maven/BD/Nginx quedan dentro de contenedores.
