#!/usr/bin/env bash
# ============================================================
# COMPATIBILIDAD CON LA VERSIÓN ANTERIOR DEL PROYECTO
# ============================================================
# Desde la semana de Docker Compose ya no iniciamos JAR manualmente.
# Toda la aplicación es administrada por docker compose.
# ============================================================
set -Eeuo pipefail

cd /opt/biblioteca/current

sudo docker compose up -d --build --remove-orphans

sudo docker compose ps
