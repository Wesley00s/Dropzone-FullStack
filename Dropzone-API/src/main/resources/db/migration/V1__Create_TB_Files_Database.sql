CREATE TABLE tb_files
(
    id            UUID NOT NULL,
    original_name VARCHAR(255),
    storage_key   VARCHAR(255),
    content_type  VARCHAR(255),
    size          BIGINT,
    created_at    TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_tb_files PRIMARY KEY (id)
);