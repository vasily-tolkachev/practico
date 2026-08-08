CREATE TABLE core.micro_concept_content
(
    id                    BIGSERIAL PRIMARY KEY,
    program_id            BIGINT      NOT NULL REFERENCES core.learning_program (id),
    micro_concept_id      BIGINT      NOT NULL REFERENCES core.micro_concept (id),
    status                VARCHAR(32) NOT NULL,
    question_payload      TEXT,
    learning_card_payload TEXT,
    practice_payload      TEXT,
    quick_check_payload   TEXT,
    retry_payload         TEXT,
    generated_at          TIMESTAMPTZ,
    updated_at            TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_micro_concept_content_program_micro UNIQUE (program_id, micro_concept_id)
);

CREATE TABLE core.micro_concept_generation_job
(
    id               BIGSERIAL PRIMARY KEY,
    program_id       BIGINT        NOT NULL REFERENCES core.learning_program (id),
    micro_concept_id BIGINT        NOT NULL REFERENCES core.micro_concept (id),
    status           VARCHAR(32)   NOT NULL,
    progress_percent INTEGER       NOT NULL,
    status_message   VARCHAR(2000),
    requested_by     VARCHAR(128),
    created_at       TIMESTAMPTZ   NOT NULL,
    updated_at       TIMESTAMPTZ   NOT NULL
);

CREATE INDEX idx_micro_concept_content_program_id ON core.micro_concept_content (program_id);
CREATE INDEX idx_micro_concept_content_micro_concept_id ON core.micro_concept_content (micro_concept_id);
CREATE INDEX idx_micro_concept_generation_job_program_micro_created
    ON core.micro_concept_generation_job (program_id, micro_concept_id, created_at DESC);
CREATE INDEX idx_micro_concept_generation_job_status
    ON core.micro_concept_generation_job (status);
