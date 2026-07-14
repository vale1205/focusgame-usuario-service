-- Esquema inicial del microservicio de gamificacion (PostgreSQL: db_gamificacion)
CREATE TABLE eventos_gamificacion (
    id          BIGSERIAL PRIMARY KEY,
    sesion_id   BIGINT    NOT NULL,
    usuario_id  BIGINT    NOT NULL,
    xp_otorgado INT       NOT NULL,
    fecha       TIMESTAMP NOT NULL
);
