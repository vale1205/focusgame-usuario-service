-- Esquema inicial del microservicio de usuarios (PostgreSQL: db_usuarios)
CREATE TABLE usuarios (
    id            BIGSERIAL    PRIMARY KEY,
    email         VARCHAR(150) NOT NULL UNIQUE,
    username      VARCHAR(80)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
);
