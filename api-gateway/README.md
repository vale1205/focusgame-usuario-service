# api-gateway

Punto de entrada unico de FocusGame. Implementado con Spring Cloud Gateway
(reactivo, WebFlux) y **rutas estaticas** (sin Eureka ni service discovery),
para que sea simple de explicar en la defensa. No valida el JWT: cada
microservicio downstream valida su propio token; el gateway solo reenvia el
header `Authorization` tal cual llega (comportamiento por defecto de
Spring Cloud Gateway) y deja traza de cada peticion en el log.

## Puerto

`8080` (configurable con la variable de entorno `SERVER_PORT`).

## Rutas

Todas bajo el prefijo `/api/**`, redirigidas por path hacia el microservicio
correspondiente:

| Ruta | Servicio destino |
|---|---|
| `/api/usuarios/**`, `/api/auth/**` | usuario-service |
| `/api/tareas/**`, `/api/sesiones/**` | productividad-service |
| `/api/progresos/**` | progreso-service |
| `/api/recompensas/**` | recompensa-service |
| `/api/gamificacion/**` | gamificacion-service |
| `/api/minijuegos/**` | minijuego-service |
| `/api/notificaciones/**` | notificacion-service |
| `/api/estadisticas/**` | estadisticas-service *(pendiente: ruta ya preparada, falta implementar el servicio)* |
| `/api/rachas/**` | racha-service |

## Dos perfiles de URL (Docker vs. local)

Cada ruta usa una URL por defecto pensada para correr **fuera de Docker**
(`localhost:<puerto>`, el mismo puerto por defecto que declara cada
microservicio en su propio `application.yml`), y se puede sobreescribir con
una variable de entorno para apuntar al hostname del contenedor cuando se
corre con Docker Compose:

| Variable de entorno | Default (fuera de Docker) | Valor en docker-compose |
|---|---|---|
| `USUARIO_SERVICE_URL` | `http://localhost:8084` | `http://usuario-service:8081` |
| `PRODUCTIVIDAD_SERVICE_URL` | `http://localhost:8082` | `http://productividad-service:8082` |
| `PROGRESO_SERVICE_URL` | `http://localhost:8085` | `http://progreso-service:8085` |
| `RECOMPENSA_SERVICE_URL` | `http://localhost:8086` | `http://recompensa-service:8086` |
| `GAMIFICACION_SERVICE_URL` | `http://localhost:8087` | `http://gamificacion-service:8087` |
| `MINIJUEGO_SERVICE_URL` | `http://localhost:8088` | `http://minijuego-service:8088` |
| `NOTIFICACION_SERVICE_URL` | `http://localhost:8089` | `http://notificacion-service:8089` |
| `ESTADISTICAS_SERVICE_URL` | `http://localhost:8090` | `http://estadisticas-service:8090` (cuando exista) |
| `RACHA_SERVICE_URL` | `http://localhost:8091` | `http://racha-service:8091` |

> Nota: `usuario-service` usa el puerto 8081 dentro de la red de Docker
> porque asi esta mapeado en `docker-compose.yml` (variable `SERVER_PORT`),
> aunque su propio default fuera de Docker es 8084.

## Levantar el servicio

### Con Docker Compose (recomendado, junto al resto de FocusGame)

```bash
docker compose up --build api-gateway
```

### Localmente con Maven

Requiere que los microservicios de negocio esten corriendo en sus puertos
por defecto (`localhost:808X`).

```bash
./mvnw spring-boot:run
```

## Tests

```bash
./mvnw test
```

El reporte de cobertura JaCoCo queda en `target/site/jacoco/index.html`.
