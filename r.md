app_user

goal

learning_program

program_topic

topic

concept

micro_concept

question

answer

user_concept_progress


Согласен с направлением. Это правильная декомпозиция Sprint 5: не “LLM-демо”, а foundation для библиотеки курсов.

Предлагаю зафиксировать это как **Sprint 5.0 (обязательный минимум)** и **Sprint 5.x (расширение)**, чтобы не сорвать срок.

**Sprint 5.0 (в рамках одного спринта, рекомендую)**
1. `learning_program` как реальный aggregate + статусы `CREATED/GENERATING/READY/FAILED`.
2. `goal_program_link.program_id -> learning_program.id` (реальный FK).
3. `learning_program_topic(program_id, topic_id, order_index)`.
4. AI pipeline только для структуры: `topics -> concepts -> micro_concepts` (без вопросов).
5. Статусная асинхронная генерация + retry для failed шагов.
6. API: `GET /programs/{id}`, `GET /programs/{id}/tree`, `GET /programs/{id}/status`.
7. Frontend: дерево программы + polling статуса + `Generating/Ready/Failed`.
8. Runtime читает persisted `learning_program` без изменения публичного контракта.

**Sprint 5.x (сразу после 5.0)**
1. Генерация `questions/answers/explanations` по micro-concept.
2. Частичный retry на уровне micro-concept.
3. Метрики генерации (latency, fail-rate, token/cost per stage).

Почему так: вопросы и explanations сильно увеличивают время, стоимость и вероятность падений. Если включить их в “must-have” Sprint 5, риск недозавершить базовую архитектуру высокий.

Если ок, я подготовлю следующий шаг как рабочий execution plan:
- точные миграции (DDL),
- изменения портов/адаптеров,
- контракты JSON для AI,
- порядок внедрения по коммитам (backend -> frontend -> e2e).