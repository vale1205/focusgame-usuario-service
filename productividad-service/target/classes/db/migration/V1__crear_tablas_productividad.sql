-- Esquema inicial del microservicio de productividad (PostgreSQL: db_productividad)
CREATE TABLE tareas (
    id          BIGSERIAL    PRIMARY KEY,
    titulo      VARCHAR(150) NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    estado      VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    usuario_id  BIGINT       NOT NULL   -- referencia logica a usuario-service (sin FK fisica)
);

CREATE TABLE sesiones (
    id               BIGSERIAL   PRIMARY KEY,
    usuario_id       BIGINT      NOT NULL,
    tarea_id         BIGINT      NOT NULL REFERENCES tareas(id),
    duracion_minutos INT         NOT NULL,
    estado           VARCHAR(20) NOT NULL,
    fecha_inicio     TIMESTAMP,
    fecha_fin        TIMESTAMP
);
