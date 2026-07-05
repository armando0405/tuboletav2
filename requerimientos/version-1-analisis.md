# Análisis de TuBoleta v1 (tuboleta_scraper_api-main)

> Fuente analizada: `C:\Users\frank\Desktop\tuboleta_scraper_api-main`
> Proyecto sin control de versiones (no es repo git) y sin README — este documento es el único registro escrito de cómo funcionaba v1.
> Objetivo: servir de base de comparación durante el levantamiento de requerimientos de v2.

## 1. Resumen general

- **Nombre**: `tuboleta-scraper` (v0.0.1-SNAPSHOT), paquete `com.armando0405.tuboletascraper`.
- **Propósito**: scraper + monitor de un único show ("Fucks News") en TuBoleta.com, que detecta altas/bajas/cambios y notifica por correo.
- **Stack**: Spring Boot 3.5.5, Java 21, Jsoup 1.17.2 (scraping), H2 (archivo local), SendGrid (prod) / Gmail SMTP (dev), Spring Mail, Actuator, Lombok, commons-codec.
- **Alcance**: proyecto de un solo tenant — no hay concepto de usuarios ni de múltiples búsquedas/proveedores. Todo el sistema gira alrededor de una sola cadena de búsqueda hardcodeada.

## 2. Modelo de datos (JPA sobre H2)

```
ConsultaInstantanea (snapshot de una corrida del scraper)
    └── 1:N → ShowInstantanea (shows encontrados en ese snapshot)

RegistroCambios (log de cambios)
    ├── consultaAnterior → ConsultaInstantanea (nullable, null en la primera corrida)
    └── consultaNueva    → ConsultaInstantanea

Show (DTO en memoria, NO persistido)
ApiResponse (DTO de respuesta, NO persistido)
```

**Show** (POJO): `showUniqueId`, `titulo`, `venue`, `ciudad`, `fechaShow` (LocalDate), `horaShow` (LocalTime, extraído pero nunca parseado/usado), `urlFuente`, `rawHtml` (comentado, solo debug).

**ConsultaInstantanea** (entidad): `id`, `fechaHora`, `totalShows`, `hashContenido` (MD5, UNIQUE), `urlConsulta`, `tiempoEjecucionMs`, `fechaCreacion`, lista `shows` (`@OneToMany`, cascade ALL, lazy).

**ShowInstantanea** (entidad): copia persistida de `Show` + FK `@ManyToOne` a `ConsultaInstantanea`.

**RegistroCambios** (entidad): `resumenCambios` (string con cambios separados por `;`), `totalCambios`, `fechaDeteccion`, FKs a la consulta anterior y nueva.

**Base de datos**: H2 basado en archivo (`./data/tuboleta_scraper_dev` en dev, `/app/data/tuboleta_scraper_prod` en prod), **no en memoria** — pero con `ddl-auto: create` en ambos perfiles (ver deuda técnica).

## 3. Lógica de scraping

**URL objetivo**:
```
https://www.tuboleta.com/es/resultados-de-busqueda?ciudades=All&categorias=All&fecha_inicio=&fecha_final=&s=fucks+news
```

**Selectores CSS (Jsoup)**:
```java
"article.bg-grey-light"                              // contenedor de cada show
".content-info .fs-8.fw-bold.mb-1 span"               // título (con fallback: span:contains(FUCKS NEWS))
".content-info .text-grey span"                       // [0]=venue, [1]=ciudad
".content-date .fs-5.fw-bold.lh-1"                     // día
".content-date .fs-8.fw-bold"                          // mes (fallback: .content-date .fs-7.fw-bold para "DD Mon")
"a.content-link-container"                             // href → urlFuente (si empieza con "/", se antepone baseUrl)
```

**Generación de ID único** (para detectar el mismo show entre corridas):
```java
public void generateUniqueId() {
    String base = titulo + "|" + venue + "|" + ciudad;
    this.showUniqueId = base.toLowerCase()
            .replaceAll("[^a-z0-9|\\s]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("\\|+", "|")
            .replaceAll("^-+|-+$", "");
}
```

**Parseo de fecha en español** (día + mes abreviado → `LocalDate`, con heurística "si ya pasó, sumar un año"):
```java
private static final Map<String, String> MESES = Map.ofEntries(
    Map.entry("Ene", "01"), Map.entry("Feb", "02"), Map.entry("Mar", "03"),
    Map.entry("Abr", "04"), Map.entry("May", "05"), Map.entry("Jun", "06"),
    Map.entry("Jul", "07"), Map.entry("Ago", "08"), Map.entry("Sep", "09"),
    Map.entry("Oct", "10"), Map.entry("Nov", "11"), Map.entry("Dic", "12")
);
```

**Configuración** (`ScrapingConfig.java` está **vacía**, es una clase placeholder sin lógica; la config real vive en YAML):
```yaml
scraping:
  tuboleta:
    base-url: "https://www.tuboleta.com"
    search-url: "/es/resultados-de-busqueda?ciudades=All&categorias=All&fecha_inicio=&fecha_final=&s=fucks+news"
    user-agent: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36..."
    rate-limit: 2000   # ms — definido pero NUNCA usado en el código (dead config)
    timeout: 10000 (dev) / 15000 (prod)
```

**Manejo de errores**: excepciones envueltas en `ScrapingException` (RuntimeException); errores al extraer un show individual se loguean como WARN y ese show se descarta (no aborta todo el scraping). Si el scraping completo falla, el ciclo de monitoreo aborta sin fallback a datos anteriores.

## 4. Detección de cambios (`SnapshotService`)

**Hash de contenido** (para saber rápido si algo cambió sin comparar campo a campo):
```java
String contenido = shows.stream()
        .sorted(Comparator.comparing(Show::getShowUniqueId))
        .map(show -> String.format("%s|%s|%s|%s",
                show.getShowUniqueId(), show.getTitulo(), show.getVenue(), show.getFechaShow()))
        .collect(Collectors.joining(","));
return DigestUtils.md5Hex(contenido);   // "empty" si la lista está vacía
```

**Flujo** (`ejecutarMonitoreoCompleto`):
1. Scrapea shows actuales y calcula su hash.
2. Busca el último snapshot (`findTopByOrderByFechaHoraDesc`); si no hay ninguno → rama "primera ejecución".
3. Si el hash es igual al anterior → no hay cambios, responde de inmediato sin más procesamiento.
4. Si difiere → guarda un nuevo `ConsultaInstantanea` + sus `ShowInstantanea`, y corre `detectarCambios(anterior, nueva)`.

**Comparación** vía mapas por `showIdUnico`:
- En **nuevo** pero no en **anterior** → `"Agregado: {titulo} en {ciudad} - {fechaShow}"`
- En **anterior** pero no en **nuevo** → `"Eliminado: {titulo} en {ciudad}"`
- Mismo ID, campo distinto → `"Modificado: ... {campo} cambió de {old} a {new}"`
  - **Solo compara `fechaShow` y `venue`** (no detecta cambios de título, ciudad o URL).

Cada corrida con cambios genera un `RegistroCambios` con el resumen unido por `; ` y el conteo total.

## 5. Scheduling

**MonitoringScheduler** — corre el ciclo completo de scraping+detección:
```java
@Scheduled(
    initialDelayString = "#{${scheduler.monitoring.initial-delay-minutes:2} * 60 * 1000}",
    fixedRateString   = "#{${scheduler.monitoring.interval-minutes:30} * 60 * 1000}"
)
```
- Dev: cada 3 min, sin delay inicial (corre apenas arranca la app).
- Prod: cada 15 min (configurable por `SCHEDULER_INTERVAL`), delay inicial de 2 min.
- Es **fixed-rate**, no cron — no hay control fino de horarios (ej. pausar de noche).
- En primera ejecución dispara `enviarNotificacionPrimeraEjecucion`; si hay cambios, `enviarNotificacionCambios`; si no hay cambios, solo loguea.

**KeepAliveScheduler** — hace ping a su propio `/api/health` cada 1 min (dev) / 10 min (prod) para evitar que Render.com (hosting free-tier) duerma la instancia por inactividad. Resuelve la URL en este orden: env var `RENDER_EXTERNAL_URL` → `keepalive.url` de config → `http://localhost:8080`. Solo se activa si `keepalive.enabled=true` (`@ConditionalOnProperty`). El `/api/health` que llama es un endpoint mock que no verifica nada real (ni DB ni scraping).

## 6. Notificaciones por email

**Proveedor por entorno**: Gmail SMTP (dev, puerto 465) vs SendGrid API (prod, puerto 587, vía `${SENDGRID_API_KEY}`).

**Triggers**:
1. `enviarNotificacionPrimeraEjecucion(totalShows)` — solo la primera vez que corre (no existe snapshot previo).
2. `enviarNotificacionCambios(cambios, totalShows)` — cuando el hash cambió y se detectaron diffs.
3. `enviarCorreoDePrueba()` — manual, vía endpoint de test.

**Destinatario y remitente están hardcodeados** en todos los perfiles (`to: iu443805@gmail.com`); no hay modelo de usuario ni preferencias de notificación.

**Reintentos** (fijo, sin backoff exponencial):
```java
for (int intento = 1; intento <= maxRetryAttempts; intento++) {   // 3 intentos
    try {
        Response response = sg.api(request);
        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) return;
    } catch (IOException e) {
        if (intento == maxRetryAttempts) throw e;
        Thread.sleep(retryDelaySeconds * 1000L);   // 30s
    }
}
```

Plantillas HTML inline con colores por tipo de cambio (verde=agregado, azul=modificado, rojo=eliminado). Prefijo de asunto tiene un typo consistente: `"[ FUCKS NEW  ] "` (falta la "S").

## 7. Superficie de API (`ShowController`, base `/api`)

| Método | Endpoint | Propósito |
|---|---|---|
| GET | `/api/scraping` | Scraping manual (no compara con historial) |
| GET | `/api/monitor` | Ciclo completo: scraping + snapshot + detección de cambios |
| GET | `/api/health` | Health check (usado por KeepAlive) |
| GET | `/api/test-email` | Enviar correo de prueba |
| GET | `/api/test-primera-ejecucion` | Simula el email de primera ejecución |
| GET | `/api/test-cambios-email` | Simula el email de cambios detectados |

Ninguno de estos endpoints tiene autenticación — cualquiera que conozca la URL desplegada puede disparar scraping o emails de prueba.

## 8. Configuración por entorno

| Aspecto | Dev | Prod |
|---|---|---|
| Ruta H2 | `./data/tuboleta_scraper_dev` | `/app/data/tuboleta_scraper_prod` |
| DDL Hibernate | `create` | `create` ⚠️ |
| Consola H2 | habilitada en `/h2-console` | habilitada ⚠️ (`web-allow-others: true`) |
| Email | Gmail SMTP (password en texto plano) | SendGrid (`${SENDGRID_API_KEY}`) |
| Intervalo scheduler | 3 min | 15 min (env var) |
| Delay inicial | 0 | 2 min (env var) |
| KeepAlive | cada 1 min | cada 10 min |
| Logging | DEBUG | INFO |

## 9. Despliegue (Dockerfile)

- Base: `eclipse-temurin:21.0.7_6-jdk-alpine`.
- Cachea el wrapper de Maven y descarga dependencias (`dependency:go-offline`) antes de copiar el código fuente, para aprovechar cache de capas Docker.
- Build con `./mvnw clean package -DskipTests`.
- Crea `/app/data` para persistencia del archivo H2.
- Expone puerto 8080; arranca siempre con `-Dspring.profiles.active=prod`.
- Variables de entorno esperadas en runtime: `SENDGRID_API_KEY`, `EMAIL_FROM`, `EMAIL_TO`, `PORT`, `RENDER_EXTERNAL_URL`, `SCHEDULER_ENABLED`, `SCHEDULER_INTERVAL`.

## 10. Deuda técnica y limitaciones detectadas

**Críticas**:
1. `ddl-auto: create` en **prod** — borra y recrea las tablas en cada reinicio/deploy, con pérdida total del historial de `RegistroCambios`.
2. Consola H2 expuesta en prod con `web-allow-others: true` — cualquiera podría leer/modificar la BD vía navegador.
3. Password de app de Gmail en texto plano dentro de `application-dev.yml`.

**Arquitectónicas**:
4. Búsqueda hardcodeada a un solo término ("fucks news") — sin soporte multi-usuario ni multi-búsqueda.
5. Destinatario de email hardcodeado — sin modelo de suscripciones por usuario.
6. `showUniqueId` basado en título+venue+ciudad — si el show cambia de nombre o de venue, se interpreta como un show nuevo en vez de una modificación.
7. `horaShow` se extrae pero nunca se parsea ni se usa.
8. Detección de cambios solo compara `fechaShow` y `venue` — ignora cambios de título, ciudad o URL.

**Operativas / calidad**:
9. `ScrapingConfig.java` es una clase vacía (código muerto).
10. `rate-limit: 2000` configurado pero nunca aplicado en el código.
11. Cero tests reales (solo `contextLoads()` placeholder).
12. Endpoints de test de email sin autenticación.
13. Sin backoff exponencial en reintentos de email; sin estrategia de backup para el archivo H2.
14. `Show` (DTO) y `ShowInstantanea` (entidad) duplican los mismos campos, con conversión manual entre ambos.

## 11. Puntos relevantes para v2

**Lo que el esquema actual de v2 ya resuelve** (`V1__init.sql` de tuboletav2, ver `CLAUDE.md` y `borrador-requerimientos.md`):
- `providers` reemplaza la URL de búsqueda hardcodeada — permite múltiples fuentes activables/desactivables (`is_active`), aunque de momento solo hay un provider (TuBoleta).
- `users` + `searches` reemplazan el término de búsqueda y destinatario hardcodeados — cada usuario puede tener múltiples búsquedas propias.
- `search_notifications` + `notif_channels` generalizan el destinatario único de v1 a múltiples destinos por búsqueda, y dejan preparado (aunque inactivo) soporte para Telegram/WhatsApp.
- `events` con `status ACTIVE/REMOVED` + `hash` (SHA-256 de title+venue+event_date_raw) es una evolución directa del hash MD5 global de v1: en vez de un hash único por snapshot completo, v2 hashea por evento individual, lo que permite detectar altas/bajas/cambios por evento sin recomparar toda la lista.
- Flyway + PostgreSQL reemplaza `ddl-auto: create` + H2 — elimina el riesgo de pérdida de datos en cada despliegue.
- `event_changes` (una fila por campo cambiado) generaliza el `resumenCambios` de texto libre de v1 a un log estructurado y consultable.

**Lo que aún no está implementado en v2 y v1 puede servir de referencia directa**:
- Lógica de scraping en sí (Jsoup, selectores CSS, parseo de fechas en español) — no existe todavía código de scraping en v2; los selectores de v1 son específicos del HTML de TuBoleta y probablemente reutilizables si la estructura del sitio no cambió.
- El scheduler (fixed-rate vs cron) y si conviene mantener un keep-alive (depende del hosting elegido para v2 — si no es un free-tier que duerme, probablemente sobra).
- El servicio de envío de notificaciones (v1 usaba SendGrid/Gmail con reintentos fijos) — v2 tiene el catálogo `notif_channels` pero ningún `NotificationService` implementado aún.
- Estrategia de generación de `external_id`/hash para eventos: v1 generaba un ID propio (título+venue+ciudad normalizado); v2 ya define `external_id` como el campo "link" de la API de TuBoleta, lo cual es más robusto que el enfoque de v1 (evita el problema #6 de la sección de deuda técnica) — confirmar que esta decisión ya está tomada y no hace falta revisarla.

**Decisiones abiertas para definir con el usuario**:
- ¿Se reutiliza el mismo enfoque de scraping con Jsoup, o se evalúa otra estrategia (API no oficial, headless browser, etc.)?
- ¿Cron expressions en vez de fixed-rate para el scheduler de v2, para tener control fino de horarios?
- ¿Qué proveedor de email usar en v2 (SendGrid, Gmail, otro) y cómo gestionar las credenciales (variables de entorno, vault, etc.) en vez de texto plano?
- ¿Se necesita keep-alive en v2, según dónde se vaya a desplegar?
- ¿Qué campos de un evento deben considerarse en la detección de "modificado" (v1 solo miraba fecha y venue) — título, ciudad, ¿algo más?
- ¿Autenticación para endpoints administrativos/de prueba (v1 no tenía ninguna)?
