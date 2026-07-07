# ADR-0001: Auth/Core Boundary

Status: Accepted

## Decision

- `practico-auth-service` and `practico-core-service` are independent bounded contexts.
- `practico-core-service` must not call `practico-auth-service` directly at compile-time or runtime.
- Shared surface is limited to `practico-auth-contract` and JWT claims.
- Auth owns identity lifecycle, sessions, login/refresh/logout and token issuance.
- Core owns learning domain (`LearningProfile`, goals, runtime, learning state, answers, progress).
- Only Auth creates users; Core may lazily create `LearningProfile` for an existing `UserId`.

## Consequences

- Core keeps only JWT validation and current-user extraction from token claims.
- Provider-specific code (Telegram/Google/OAuth details) is forbidden in Core.
- Module dependencies must remain:
  - `core -> contract`
  - `auth -> contract`
  - no `core -> auth`
