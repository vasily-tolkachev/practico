CREATE TABLE auth_user (
    id UUID PRIMARY KEY,
    display_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE auth_identity (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth_user (id),
    provider VARCHAR(32) NOT NULL,
    provider_subject VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    display_name VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(1024),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE auth_refresh_session (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth_user (id),
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE
);

CREATE UNIQUE INDEX uk_auth_identity_provider_subject
    ON auth_identity (provider, provider_subject);

CREATE UNIQUE INDEX uk_auth_refresh_session_token_hash
    ON auth_refresh_session (token_hash);

CREATE INDEX idx_auth_identity_user_id
    ON auth_identity (user_id);

CREATE INDEX idx_auth_refresh_session_user_id
    ON auth_refresh_session (user_id);
