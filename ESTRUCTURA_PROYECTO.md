# Estructura del Proyecto TuBoleta v2

```
tu-boleta-v2/                                    
│                                               
├── docker-compose.yml                          # Docker Compose para servicios
│
├── tuboleta-backend/                           # Spring Boot (Java 21)
│   ├── mvnw                                    # Maven Wrapper (Linux/Mac)
│   ├── mvnw.cmd                                # Maven Wrapper (Windows)
│   ├── pom.xml                                 # Configuración Maven
│   ├── HELP.md                                 # Documentación
│   │
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── com/
│       │   │       └── tuboleta/
│       │   │           └── backend/
│       │   │               ├── TuboletaBackendApplication.java
│       │   │               │
│       │   │               ├── api/             # Controladores REST
│       │   │               │   ├── controllers/
│       │   │               │   ├── dtos/        # Data Transfer Objects
│       │   │               │   └── responses/
│       │   │               │
│       │   │               ├── domain/         # Entidades JPA
│       │   │               │   └── entities/
│       │   │               │
│       │   │               ├── service/        # Lógica de negocio
│       │   │               │   └── impl/
│       │   │               │
│       │   │               ├── repository/     # Acceso a datos
│       │   │               │
│       │   │               ├── config/         # Configuración
│       │   │               │   ├── security/
│       │   │               │   └── persistence/
│       │   │               │
│       │   │               ├── exception/      # Excepciones personalizadas
│       │   │               │
│       │   │               └── utils/          # Utilidades
│       │   │
│       │   └── resources/
│       │       ├── application.yaml            # Configuración principal
│       │       ├── db/                         # Scripts de base de datos
│       │       │   └── migration/              # Flyway migrations
│       │       ├── static/                     # Recursos estáticos
│       │       └── templates/                  # Plantillas
│       │
│       └── test/
│           └── java/
│               └── com/
│                   └── tuboleta/
│                       └── backend/
│                           └── TuboletaBackendApplicationTests.java
│
│
├── tuboleta-frontend/                          # Vue 3 + TypeScript + Vite
│   ├── index.html                              # Punto de entrada HTML
│   ├── package.json                            # Dependencias Node
│   ├── tsconfig.json                           # Configuración TypeScript
│   ├── tsconfig.vite-config.json               # TS para Vite Config
│   ├── vite.config.ts                          # Configuración Vite
│   ├── eslint.config.mjs                       # ESLint Config
│   ├── env.d.ts                                # Type definitions
│   ├── README.md                               # Documentación
│   │
│   ├── public/
│   │   ├── _redirects                          # Redirecciones Netlify
│   │   └── assets/
│   │       └── images/
│   │           ├── icon/
│   │           ├── logos/
│   │           └── products/
│   │               └── users/
│   │
│   └── src/
│       ├── App.vue                             # Componente raíz
│       ├── main.ts                             # Punto de entrada
│       │
│       ├── components/                         # Componentes Vue
│       │   ├── shared/
│       │   │   └── AppSnackbarQueue.vue        # Notificaciones globales
│       │   │
│       │   ├── ui-components/                  # Componentes reutilizables
│       │   │   ├── cards/
│       │   │   │   └── FloatingCard.vue
│       │   │   ├── loading/
│       │   │   │   └── Loading.vue
│       │   │   └── table/
│       │   │       └── TableDynamic.vue
│       │   │
│       │   └── admin/                          # Componentes administrativos
│       │       ├── MeasurementParameters/
│       │       │   └── MasterTable.vue
│       │       ├── MeasurementSourceType/
│       │       │   └── MeasurementSourceTypeHeader.vue
│       │       ├── NormActivityTypes/
│       │       │   ├── NormActivityTypesHeader.vue
│       │       │   └── NormActivityTypesBody.vue
│       │       ├── NormSectors/
│       │       │   ├── NormSectorsHeader.vue
│       │       │   └── NormSectorsBody.vue
│       │       ├── Resolutions/
│       │       │   ├── ResolutionsHeader.vue
│       │       │   └── ResolutionsBody.vue
│       │       └── SubsourceTypes/
│       │           ├── SubsourceTypes.vue
│       │           └── SourceBySubsource.vue
│       │
│       ├── views/                              # Páginas/Vistas principales
│       │   ├── auth/
│       │   │   └── Login.vue
│       │   ├── dashboard/
│       │   │   └── Index.vue
│       │   ├── admin/
│       │   │   ├── AccountExtraInfo.vue
│       │   │   ├── ClientExtraInfo.vue
│       │   │   ├── DeclarationFormat.vue
│       │   │   ├── DeclarationPeriod.vue
│       │   │   ├── HeaderParameters.vue
│       │   │   ├── Laboratories.vue
│       │   │   ├── LimitTypes.vue
│       │   │   ├── MeasurementParameters.vue
│       │   │   ├── MeasurementSourceType.vue
│       │   │   ├── NormActivityTypes.vue
│       │   │   ├── NormSectors.vue
│       │   │   ├── ParameterRates.vue
│       │   │   └── [más vistas admin...]
│       │   ├── autodeclaration/
│       │   │   └── [vistas de autodeclaración]
│       │   └── pages/
│       │       └── Error404.vue
│       │
│       ├── composables/                        # Composables (Composition API)
│       │   ├── index.ts
│       │   ├── useNotify.ts
│       │   └── admin/
│       │       ├── index.ts
│       │       ├── useMeasurementParameters.ts
│       │       ├── useMeasurementSourceType.ts
│       │       ├── useNormActivityTypes.ts
│       │       ├── [más composables...]
│       │
│       ├── stores/                             # Pinia Stores
│       │   ├── auth.store.ts                   # Autenticación
│       │   └── notify.store.ts                 # Notificaciones
│       │
│       ├── router/                             # Vue Router
│       │   ├── index.ts
│       │   ├── AdminRoute.ts
│       │   ├── AuthRoutes.ts
│       │   ├── HomeRoutes.ts
│       │   └── MainRoutes.ts
│       │
│       ├── layouts/                            # Layouts principales
│       │   ├── blank/                          # Sin layout (login, etc)
│       │   └── full/                           # Con navegación
│       │
│       ├── locales/                            # Internacionalización (i18n)
│       │   ├── index.ts
│       │   └── es.ts                           # Español
│       │
│       ├── plugins/                            # Plugins Vue
│       │   ├── axios.ts                        # Configuración Axios
│       │   ├── i18n.ts                         # i18n
│       │   ├── swal.ts                         # SweetAlert2
│       │   └── vuetify.ts                      # Vuetify UI
│       │
│       ├── theme/                              # Temas
│       │   └── LightTheme.ts
│       │
│       ├── types/                              # TypeScript types
│       │   ├── index.ts
│       │   ├── IsActive.ts
│       │   ├── swal.d.ts
│       │   ├── component/
│       │   ├── services/
│       │   └── themeTypes/
│       │
│       ├── utils/                              # Utilidades
│       │   ├── endpoints/                      # URLs de API
│       │   └── services/                       # Servicios HTTP
│       │
│       ├── scss/                               # Estilos SCSS
│       │   ├── style.scss
│       │   ├── _variables.scss
│       │   ├── _override.scss
│       │   ├── components/
│       │   ├── layout/
│       │   └── pages/
│       │
│       └── assets/                             # Recursos
│           └── images/
│               ├── background/
│               └── logos/
│
│
└── contexto/                                   # Documentación y apuntes
    └── apunte innecesario.txt
```

---

## Información Técnica

### Backend - Spring Boot
- **Framework**: Spring Boot 4.0.5
- **Java Version**: 21
- **Build Tool**: Maven
- **Dependencias principales**:
  - Spring Boot Starter Security
  - Spring Boot Starter Data JPA
  - Spring Boot Starter Web
  - Spring Boot Starter Validation
  - Flyway (Migraciones de BD)
  - Actuator (Health checks)

### Frontend - Vue 3
- **Framework**: Vue 3
- **Language**: TypeScript
- **Build Tool**: Vite
- **UI Framework**: Vuetify 4.0.4
- **State Management**: Pinia
- **HTTP Client**: Axios
- **Routing**: Vue Router 5.0.4
- **Internacionalización**: Vue i18n 11.3.0
- **Notificaciones**: SweetAlert2
- **Icons**: Iconify Vue

### Base de Datos
- Controlada con Flyway migrations
- Ubicadas en: `tuboleta-backend/src/main/resources/db/`

### Deployment
- Docker Compose para orquestar servicios
- Frontend puede ser desplegado en Netlify (incluye `_redirects`)

---

## Directorios Excluidos (según solicitud)
- ❌ `node_modules/` (dependencias frontend)
- ❌ `target/` (compilados y artifacts Maven del backend)
- ❌ `.git/` (repositorio)

---

**Última actualización**: 31 de marzo de 2026
