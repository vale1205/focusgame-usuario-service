# gamificacion-service

Motor de reglas de gamificacion de FocusGame. No tiene un CRUD propio de
negocio: orquesta a `productividad-service`, `progreso-service` y
`recompensa-service` para convertir una sesion de enfoque completada en
XP, nivel y recompensas. Mantiene una tabla de auditoria
(`eventos_gamificacion`) con cada sesion procesada.

## Puerto

`8087` (configurable con la variable de entorno `SERVER_PORT`).

## Endpoints

| Metodo | Ruta | Descripcion |
|---|---|---|
| POST | `/api/gamificacion/procesar-sesion/{sesionId}` | Procesa una sesion COMPLETADA: calcula XP (1 XP por minuto de duracion), lo suma en `progreso-service`, evalua reglas de recompensas (racha de 5 sesiones, subida de nivel) y devuelve un resumen `{ xpOtorgado, nivelActual, recompensasOtorgadas }` |
| GET | `/api/gamificacion/eventos/usuario/{usuarioId}` | Historial de sesiones procesadas para un usuario |

### Reglas de negocio

- **XP otorgado**: `duracionMinutos` de la sesion (25 minutos completos = 25 XP).
- **Racha de 5 sesiones**: si el `totalSesiones` del progreso actualizado es
  multiplo de 5, se otorga la recompensa "Racha de 5 sesiones".
- **Subida de nivel**: si el nivel del usuario despues de sumar XP es mayor
  al nivel que tenia antes, se otorga la recompensa "Subida de nivel X".
- Si la sesion no existe o no esta `COMPLETADA`, o el usuario no tiene
  progreso registrado, la operacion se rechaza (404 o 409) y no se otorga XP.

Toda la API (excepto `/swagger-ui/**` y `/v3/api-docs/**`) requiere un JWT
valido emitido por `usuario-service` en el header `Authorization: Bearer <token>`.

Documentacion interactiva: `http://localhost:8087/swagger-ui.html`

## Variables de entorno

| Variable | Default | Descripcion |
|---|---|---|
| `DB_HOST` | `postgres` | Host de PostgreSQL |
| `DB_USER` | `postgres` | Usuario de la base de datos |
| `DB_PASS` | `postgres` | Password de la base de datos |
| `SERVER_PORT` | `8087` | Puerto HTTP del servicio |
| `PRODUCTIVIDAD_SERVICE_URL` | `http://localhost:8082` | URL base de `productividad-service` (Feign) |
| `PROGRESO_SERVICE_URL` | `http://localhost:8085` | URL base de `progreso-service` (Feign) |
| `RECOMPENSA_SERVICE_URL` | `http://localhost:8086` | URL base de `recompensa-service` (Feign) |
| `JWT_SECRET` | clave compartida de FocusGame | Secreto usado para validar el JWT |

## Levantar el servicio

### Con Docker Compose (recomendado, junto al resto de FocusGame)

```bash
docker compose up --build gamificacion-service
```

### Localmente con Maven

Requiere una instancia de PostgreSQL accesible en `localhost:5432` con la
base `db_gamificacion` ya creada, y que `productividad-service`,
`progreso-service` y `recompensa-service` esten corriendo.

```bash
./mvnw spring-boot:run
```

## Tests

```bash
./mvnw test
```

El reporte de cobertura JaCoCo queda en `target/site/jacoco/index.html`.
