--liquibase formatted sql

--changeset ilyin:sandbox context:ddl
CREATE TABLE foo
(
  id   BIGSERIAL PRIMARY KEY,
  name VARCHAR(128) NOT NULL
);

CREATE TABLE bar
(
  id     BIGSERIAL PRIMARY KEY,
  name   VARCHAR(128) NOT NULL,
  foo_id BIGINT NOT NULL REFERENCES foo (id)
);
