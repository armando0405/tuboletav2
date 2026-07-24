# syntax=docker/dockerfile:1
#
# Imagen "todo en uno" de TuBoleta v2: compila el frontend Vue, lo empaqueta
# dentro de los estáticos del backend Spring Boot y produce un solo jar/imagen.
# El backend sirve la SPA y la API desde el mismo origen (sin CORS).
#
# Build:  docker build -t tuboleta .
# Run:    docker run -p 8088:8088 --env-file .env tuboleta
# En Render: Runtime = Docker (usa este Dockerfile automáticamente).

# ─────────────────────────────────────────────────────────────
# Etapa 1 — Compilar el frontend (Vue + Vite)
# ─────────────────────────────────────────────────────────────
FROM node:22-alpine AS frontend
WORKDIR /app
# CI=true: desactiva la apertura del reporte de bundle (visualizer) en headless.
ENV CI=true
# Primero solo los manifiestos para aprovechar la cache de capas de Docker.
COPY tuboleta-frontend/package.json tuboleta-frontend/package-lock.json ./
RUN npm ci
COPY tuboleta-frontend/ ./
RUN npm run build

# ─────────────────────────────────────────────────────────────
# Etapa 2 — Compilar el backend (Spring Boot) con el front adentro
# ─────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /build
# Descarga de dependencias cacheable: primero el pom.
COPY tuboleta-backend/pom.xml ./
RUN mvn -B -q dependency:go-offline
# Código del backend.
COPY tuboleta-backend/src ./src
# El frontend compilado va a los estáticos que Spring sirve (classpath:/static/).
COPY --from=frontend /app/dist/ ./src/main/resources/static/
RUN mvn -B -q clean package -DskipTests \
    && cp target/*.jar app.jar

# ─────────────────────────────────────────────────────────────
# Etapa 3 — Imagen final (solo el runtime de Java)
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
# Usuario no-root por seguridad.
RUN useradd --system --uid 1001 tuboleta
COPY --from=backend /build/app.jar app.jar
USER tuboleta
# Render inyecta PORT; la app lo lee (server.port=${PORT:8088}).
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]
