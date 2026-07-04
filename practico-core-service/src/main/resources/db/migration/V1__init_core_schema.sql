CREATE TABLE IF NOT EXISTS learning_profile (
    id UUID PRIMARY KEY,
    display_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS topic (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS concept (
    id BIGSERIAL PRIMARY KEY,
    topic_id BIGINT NOT NULL REFERENCES topic (id),
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS micro_concept (
    id BIGSERIAL PRIMARY KEY,
    concept_id BIGINT NOT NULL REFERENCES concept (id),
    name VARCHAR(255) NOT NULL,
    sort_order INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS question (
    id BIGSERIAL PRIMARY KEY,
    micro_concept_id BIGINT NOT NULL REFERENCES micro_concept (id),
    text TEXT,
    difficulty VARCHAR(32),
    question_type VARCHAR(32),
    expected_answer TEXT,
    explanation TEXT
);

CREATE TABLE IF NOT EXISTS learning_program (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(4000) NOT NULL,
    status VARCHAR(32) NOT NULL,
    origin VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS learning_program_topic (
    id BIGSERIAL PRIMARY KEY,
    program_id BIGINT NOT NULL REFERENCES learning_program (id),
    topic_id BIGINT NOT NULL REFERENCES topic (id),
    order_index INTEGER NOT NULL,
    CONSTRAINT uk_learning_program_topic_order UNIQUE (program_id, order_index),
    CONSTRAINT uk_learning_program_topic_topic UNIQUE (program_id, topic_id)
);

CREATE TABLE IF NOT EXISTS goal (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS goal_program_link (
    id BIGSERIAL PRIMARY KEY,
    goal_id BIGINT NOT NULL,
    program_id BIGINT NOT NULL REFERENCES learning_program (id),
    source_type VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_goal_program_link_goal UNIQUE (goal_id)
);

CREATE TABLE IF NOT EXISTS answer (
    id BIGSERIAL PRIMARY KEY,
    profile_id UUID NOT NULL REFERENCES learning_profile (id),
    question_id BIGINT NOT NULL,
    answer TEXT NOT NULL,
    score INTEGER NOT NULL,
    feedback TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS user_concept_progress (
    id BIGSERIAL PRIMARY KEY,
    profile_id UUID NOT NULL REFERENCES learning_profile (id),
    concept_id BIGINT NOT NULL REFERENCES concept (id),
    status VARCHAR(32) NOT NULL,
    correct_answers INTEGER NOT NULL,
    total_answers INTEGER NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_user_concept_progress_profile_concept UNIQUE (profile_id, concept_id)
);

CREATE INDEX IF NOT EXISTS idx_concept_topic_id ON concept (topic_id);
CREATE INDEX IF NOT EXISTS idx_micro_concept_concept_id ON micro_concept (concept_id);
CREATE INDEX IF NOT EXISTS idx_question_micro_concept_id ON question (micro_concept_id);
CREATE INDEX IF NOT EXISTS idx_learning_program_topic_program_id ON learning_program_topic (program_id);
CREATE INDEX IF NOT EXISTS idx_learning_program_topic_topic_id ON learning_program_topic (topic_id);
CREATE INDEX IF NOT EXISTS idx_goal_program_link_program_id ON goal_program_link (program_id);
CREATE INDEX IF NOT EXISTS idx_answer_profile_id ON answer (profile_id);
CREATE INDEX IF NOT EXISTS idx_answer_question_id ON answer (question_id);
CREATE INDEX IF NOT EXISTS idx_user_concept_progress_profile_id ON user_concept_progress (profile_id);
CREATE INDEX IF NOT EXISTS idx_user_concept_progress_concept_id ON user_concept_progress (concept_id);
