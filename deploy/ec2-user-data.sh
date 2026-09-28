#!/usr/bin/env bash
# ============================================================
# PREPARACIÓN DE EC2 - AMAZON LINUX / AWS LEARNER LAB
# ============================================================
# Instala únicamente lo que necesita el HOST:
#   - Docker
#   - Docker Compose
#   - Git y curl para diagnóstico
#
# Java, Maven, PostgreSQL y Nginx vivirán dentro de contenedores.
# ============================================================
set -Eeuo pipefail

BASE=/opt/biblioteca
READY="$BASE/.bootstrap-ready"
FAILED="$BASE/.bootstrap-failed"

on_error() {
  rc=$?
  echo "ERROR preparando EC2 (código $rc)."
  sudo mkdir -p "$BASE"
  sudo touch "$FAILED"
  sudo rm -f "$READY"
  exit "$rc"
}
trap on_error ERR

# Actualizar metadatos e instalar Docker y utilidades.
sudo yum makecache -y
sudo yum install -y docker git curl

# Iniciar Docker y dejarlo habilitado para reinicios de la EC2.
sudo systemctl enable docker
sudo systemctl start docker

# Permitir que ec2-user use Docker en futuras sesiones.
sudo usermod -aG docker ec2-user || true

# Esperar a que Docker realmente responda.
for i in {1..30}; do
  if sudo docker info >/dev/null 2>&1; then
    break
  fi
  echo "Esperando Docker... intento $i/30"
  sleep 2
done
sudo docker info >/dev/null

# ------------------------------------------------------------
# DOCKER COMPOSE
# ------------------------------------------------------------
# Primero comprobamos si el plugin ya viene con la instalación.
if ! sudo docker compose version >/dev/null 2>&1; then

  # Intentar instalar el paquete disponible en algunos Amazon Linux.
  sudo yum install -y docker-compose-plugin || true
fi

# Si todavía no existe, instalar el plugin oficial como fallback.
if ! sudo docker compose version >/dev/null 2>&1; then
  sudo mkdir -p /usr/local/lib/docker/cli-plugins
  sudo curl -fL \
    "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-x86_64" \
    -o /usr/local/lib/docker/cli-plugins/docker-compose
  sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
fi

# Comprobaciones finales.
sudo docker --version
sudo docker compose version

# Directorio donde GitHub Actions copiará el repositorio.
sudo mkdir -p "$BASE/current"
sudo chown -R ec2-user:ec2-user "$BASE"

# Marcar la instancia como preparada.
touch "$READY"
rm -f "$FAILED"

echo "EC2 preparada correctamente para Docker Compose."
