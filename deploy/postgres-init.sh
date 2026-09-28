#!/usr/bin/env bash
# ============================================================
# INICIALIZACIÓN DE POSTGRESQL PARA DOCKER COMPOSE
# ============================================================
# La imagen oficial de PostgreSQL ejecuta este archivo solo cuando
# el volumen de datos está vacío (primer arranque).
# ============================================================
set -Eeuo pipefail

# Crear las bases definidas por el proyecto.
psql -v ON_ERROR_STOP=1 \
  --username "$POSTGRES_USER" \
  --dbname "$POSTGRES_DB" \
  --file /sql/00-create_dbs.sql

# Cargar tablas y datos en la base correcta de cada microservicio.
psql -v ON_ERROR_STOP=1 \
  --username "$POSTGRES_USER" \
  --dbname usuarios \
  --file /sql/01-usuarios.sql

psql -v ON_ERROR_STOP=1 \
  --username "$POSTGRES_USER" \
  --dbname catalogo \
  --file /sql/02-catalogo.sql

psql -v ON_ERROR_STOP=1 \
  --username "$POSTGRES_USER" \
  --dbname recursos \
  --file /sql/03-recursos.sql

echo "PostgreSQL inicializado correctamente para Biblioteca."
