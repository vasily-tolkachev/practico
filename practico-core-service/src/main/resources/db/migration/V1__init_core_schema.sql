CREATE SCHEMA IF NOT EXISTS core;

CREATE TABLE core.learning_profile
(
    id           UUID PRIMARY KEY,
    display_name VARCHAR(255) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL
);

CREATE TABLE core.topic
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE core.concept
(
    id       BIGSERIAL PRIMARY KEY,
    topic_id BIGINT       NOT NULL REFERENCES core.topic (id),
    name     VARCHAR(255) NOT NULL
);

CREATE TABLE core.micro_concept
(
    id         BIGSERIAL PRIMARY KEY,
    concept_id BIGINT       NOT NULL REFERENCES core.concept (id),
    name       VARCHAR(255) NOT NULL,
    sort_order INTEGER      NOT NULL
);

CREATE TABLE core.question
(
    id               BIGSERIAL PRIMARY KEY,
    micro_concept_id BIGINT NOT NULL REFERENCES core.micro_concept (id),
    text             TEXT,
    difficulty       VARCHAR(32),
    question_type    VARCHAR(32),
    expected_answer  TEXT,
    explanation      TEXT
);

CREATE TABLE core.learning_program
(
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(255)  NOT NULL,
    description VARCHAR(4000) NOT NULL,
    status      VARCHAR(32)   NOT NULL,
    origin      VARCHAR(32)   NOT NULL,
    created_at  TIMESTAMPTZ   NOT NULL,
    updated_at  TIMESTAMPTZ   NOT NULL
);

CREATE TABLE core.learning_program_topic
(
    id          BIGSERIAL PRIMARY KEY,
    program_id  BIGINT  NOT NULL REFERENCES core.learning_program (id),
    topic_id    BIGINT  NOT NULL REFERENCES core.topic (id),
    order_index INTEGER NOT NULL,
    CONSTRAINT uk_learning_program_topic_order UNIQUE (program_id, order_index),
    CONSTRAINT uk_learning_program_topic_topic UNIQUE (program_id, topic_id)
);

CREATE TABLE core.goal
(
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(255)  NOT NULL,
    description VARCHAR(2000) NOT NULL,
    status      VARCHAR(32)   NOT NULL,
    created_at  TIMESTAMPTZ   NOT NULL
);

CREATE TABLE core.goal_program_link
(
    id          BIGSERIAL PRIMARY KEY,
    goal_id     BIGINT      NOT NULL,
    program_id  BIGINT      NOT NULL REFERENCES core.learning_program (id),
    source_type VARCHAR(32) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_goal_program_link_goal UNIQUE (goal_id)
);

CREATE TABLE core.answer
(
    id          BIGSERIAL PRIMARY KEY,
    profile_id  UUID        NOT NULL REFERENCES core.learning_profile (id),
    question_id BIGINT      NOT NULL,
    answer      TEXT        NOT NULL,
    score       INTEGER     NOT NULL,
    feedback    TEXT        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL
);

CREATE TABLE core.user_concept_progress
(
    id              BIGSERIAL PRIMARY KEY,
    profile_id      UUID        NOT NULL REFERENCES core.learning_profile (id),
    concept_id      BIGINT      NOT NULL REFERENCES core.concept (id),
    status          VARCHAR(32) NOT NULL,
    correct_answers INTEGER     NOT NULL,
    total_answers   INTEGER     NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_user_concept_progress_profile_concept UNIQUE (profile_id, concept_id)
);

CREATE INDEX idx_concept_topic_id ON core.concept (topic_id);
CREATE INDEX idx_micro_concept_concept_id ON core.micro_concept (concept_id);
CREATE INDEX idx_question_micro_concept_id ON core.question (micro_concept_id);
CREATE INDEX idx_learning_program_topic_program_id ON core.learning_program_topic (program_id);
CREATE INDEX idx_learning_program_topic_topic_id ON core.learning_program_topic (topic_id);
CREATE INDEX idx_goal_program_link_program_id ON core.goal_program_link (program_id);
CREATE INDEX idx_answer_profile_id ON core.answer (profile_id);
CREATE INDEX idx_answer_question_id ON core.answer (question_id);
CREATE INDEX idx_user_concept_progress_profile_id ON core.user_concept_progress (profile_id);
CREATE INDEX idx_user_concept_progress_concept_id ON core.user_concept_progress (concept_id);
