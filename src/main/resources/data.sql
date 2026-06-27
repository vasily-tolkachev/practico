INSERT INTO topic (id, name) VALUES
    (1, 'Spring'),
    (2, 'Java Core');

INSERT INTO concept (id, topic_id, name) VALUES
    (1, 1, 'Dependency Injection'),
    (2, 1, 'Spring Boot Autoconfiguration'),
    (3, 2, 'Collections Framework');

INSERT INTO question (id, concept_id, text, difficulty) VALUES
    (1, 1, 'What problem does dependency injection solve in application design?', 'EASY'),
    (2, 1, 'How does constructor injection improve testability compared to field injection?', 'MEDIUM'),
    (3, 2, 'How does Spring Boot decide which beans to autoconfigure?', 'HARD'),
    (4, 3, 'When would you choose HashMap over TreeMap in production code?', 'MEDIUM');

SELECT setval(pg_get_serial_sequence('topic', 'id'), (SELECT COALESCE(MAX(id), 1) FROM topic), true);
SELECT setval(pg_get_serial_sequence('concept', 'id'), (SELECT COALESCE(MAX(id), 1) FROM concept), true);
SELECT setval(pg_get_serial_sequence('question', 'id'), (SELECT COALESCE(MAX(id), 1) FROM question), true);
