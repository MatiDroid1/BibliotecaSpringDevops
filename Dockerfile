# syntax=docker/dockerfile:1.4
# ============================================================
# DOCKERFILE MULTI-STAGE PARA LOS SERVICIOS JAVA
# ============================================================
# Un mismo Dockerfile construye Eureka, los microservicios y
# el API Gateway. Docker Compose indica qué módulo construir
# mediante el argumento MODULE.
#
# NOTA DE OPTIMIZACIÓN: se agregó --mount=type=cache en los pasos
# de Maven para compartir el repositorio .m2 entre TODOS los
# builds (eureka, api-gateway, ms-usuarios, ms-catalogo,
# ms-recursos), incluso cuando corren en paralelo. Esto evita
# volver a descargar las mismas dependencias 5 veces y reduce
# drasticamente el tiempo total de "docker compose build".
# ============================================================


# ------------------------------------------------------------
# ETAPA 1: DEPENDENCIAS
# ------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21 AS dependencies

WORKDIR /workspace

# MODULE se recibe desde docker-compose.yml.
ARG MODULE

# Copiamos primero los POM para aprovechar la caché de Docker.
COPY pom.xml ./pom.xml
COPY common/pom.xml ./common/pom.xml
COPY eureka/pom.xml ./eureka/pom.xml
COPY ms-usuarios/pom.xml ./ms-usuarios/pom.xml
COPY ms-catalogo/pom.xml ./ms-catalogo/pom.xml
COPY ms-recursos/pom.xml ./ms-recursos/pom.xml
COPY api-gateway/pom.xml ./api-gateway/pom.xml

# Descarga anticipadamente las dependencias del módulo y de sus
# dependencias internas. El cache mount comparte el repositorio
# .m2 entre TODOS los módulos y entre corridas del workflow,
# incluso si los 5 builds corren en paralelo (sharing=locked
# evita que se corrompan entre sí al escribir al mismo tiempo).
RUN --mount=type=cache,target=/root/.m2/repository,sharing=locked \
    mvn -B -pl "${MODULE}" -am dependency:go-offline


# ------------------------------------------------------------
# ETAPA 2: BUILD
# ------------------------------------------------------------
FROM dependencies AS build

ARG MODULE

# Ahora sí copiamos el código fuente completo.
COPY common ./common
COPY eureka ./eureka
COPY ms-usuarios ./ms-usuarios
COPY ms-catalogo ./ms-catalogo
COPY ms-recursos ./ms-recursos
COPY api-gateway ./api-gateway

# Compila solo el módulo solicitado y lo que necesita.
# Reutiliza el mismo cache mount para no volver a descargar nada
# que ya haya bajado la etapa de dependencias.
RUN --mount=type=cache,target=/root/.m2/repository,sharing=locked \
    mvn -B -pl "${MODULE}" -am clean package -DskipTests \
    && cp "${MODULE}"/target/*.jar /tmp/app.jar


# ------------------------------------------------------------
# ETAPA 3: RUNTIME
# ------------------------------------------------------------
FROM eclipse-temurin:21-jre-jammy AS runtime

# curl se usa en los healthchecks de Docker Compose.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Solo copiamos el JAR final. Maven y el código fuente no llegan
# a la imagen que se ejecuta en producción.
COPY --from=build /tmp/app.jar /app/app.jar

# Ajustes pequeños para un laboratorio con recursos limitados.
ENV JAVA_TOOL_OPTIONS="-Xms64m -Xmx192m"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]