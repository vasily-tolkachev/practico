# Mastery Roadmap Status

| Stage | Status |
| --- | --- |
| Sprint 1 - Headless Runtime | 100% |
| Sprint 2 - Product UI | 100% |
| Sprint 3 - Curriculum | 100% |
| Sprint 4 - Goal Driven Learning + Internal Generation (MVP) | 0% |
| Sprint 5 - Shared Course Library | 0% |
| Sprint 6 - Community Feedback | 0% |
| Sprint 7 - Course Evolution | 0% |
| Sprint 8 - Recommendation Engine | 0% |
| Sprint 9 - Analytics Platform | 0% |
| Sprint 10 - AI Mentor | 0% |

---

# Sprint 4 v2 - Goal Driven Learning + Internal Generation

## Product principle
User sets a goal. The system automatically resolves a course:
1. Find existing suitable course.
2. If not found, generate a new one.
3. Attach resolved course to goal.
4. User continues learning.

No explicit "Generate Course" button in main user flow.

## Commit 1
`feat(goal): introduce Goal domain and basic goal flow`

Definition of done:
- Backend:
  - `Goal` model: `id`, `title`, `description`, `status`, `createdAt`.
  - Use cases: `CreateGoalUseCase`, `ListGoalsUseCase`, `GetGoalUseCase`.
  - REST: `POST /goals`, `GET /goals`, `GET /goals/{id}`.
- Frontend:
  - Real Goals page with create + list + details open.
- End state:
  - User can create and view goals in working UI.

## Commit 2
`feat(goal): introduce course resolution job and status tracking`

Definition of done:
- Backend:
  - On goal creation, start async `ResolveCourseForGoal` job.
  - Job states: `QUEUED`, `SEARCHING_LIBRARY`, `GENERATING`, `COMPLETED`, `FAILED`.
  - REST: `GET /goals/{id}/resolution-status`.
- Frontend:
  - Goal card shows live status and progress label.
- End state:
  - System automatically starts resolving course after goal creation.

## Commit 3
`feat(library): add minimal Course Library and search`

Definition of done:
- Backend:
  - `Course`, `CourseVersion` minimal models.
  - `GET /courses?query=...`.
  - Resolver first attempts library match.
- Frontend:
  - Library page with search and course list.
- End state:
  - Existing course can be reused without generation.

## Commit 4
`feat(generator): add GenerateLearningProgram use case with stub pipeline`

Definition of done:
- Backend:
  - Internal `GenerateLearningProgramUseCase` returning fake but valid curriculum.
  - Resolver fallback: if no good match -> generate via stub.
- Frontend:
  - Goal details page shows generation progress steps.
- End state:
  - Goal always ends with resolved course (reused or generated).

## Commit 5
`feat(generator): connect OpenAI for concept and microconcept generation`

Definition of done:
- Backend:
  - Replace stub concepts with AI generation pipeline:
    - goal -> concepts -> microconcepts.
  - Save generated curriculum as new `CourseVersion`.
- Frontend:
  - Progress steps updated from backend job stage.
- End state:
  - Generated curriculum is AI-based and persisted.

## Commit 6
`feat(generator): generate question bank`

Definition of done:
- Backend:
  - For each microconcept generate:
    - questions,
    - learning cards,
    - practice tasks.
  - Job progress includes produced counts.
- Frontend:
  - Display `generated / total` counters.
- End state:
  - Resolved course is immediately learnable.

## Commit 7
`feat(goal): connect Goal with resolved Course`

Definition of done:
- Backend:
  - Persist link `Goal -> CourseVersion`.
  - REST: `GET /goals/{id}` returns linked course summary.
- Frontend:
  - Goal card: status, course title, question count, "Start Learning".
- End state:
  - User can continue from goal directly to learning.

## Commit 8
`feat(generator): generation history and diagnostics`

Definition of done:
- Backend:
  - `GenerationHistory` per goal.
  - REST: `GET /goals/{id}/history`.
- Frontend:
  - History timeline with timestamps and stage results.
- End state:
  - Every generation attempt is observable.

## Commit 9
`feat(generator): generation settings + preview`

Definition of done:
- Backend:
  - Settings in goal request: difficulty, size.
  - Preview endpoint: concepts/microconcepts/questions/time estimate.
- Frontend:
  - Settings form + preview screen before publish/start.
- End state:
  - User can constrain generation quality/size.

## Commit 10
`chore(generator): polish, reliability, and release checklist`

Definition of done:
- Improve retries/timeouts/error mapping.
- Tighten API validation and UI empty/error states.
- Final E2E smoke flow:
  - create goal -> resolve course -> start learning.
