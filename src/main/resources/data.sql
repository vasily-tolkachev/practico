INSERT INTO topic (id, name) VALUES
    (1, 'Spring'),
    (2, 'Java Core');

INSERT INTO concept (id, topic_id, name) VALUES
    (1, 1, 'Why DI exists'),
    (2, 1, 'Constructor Injection'),
    (3, 1, 'Setter Injection'),
    (4, 1, 'Field Injection'),
    (5, 1, 'DI and Testing'),
    (6, 1, 'IoC Container'),
    (7, 1, 'Spring Boot Autoconfiguration'),
    (8, 2, 'Collections Framework');

INSERT INTO question (id, concept_id, text, difficulty) VALUES
    (1, 1, 'What main problem does dependency injection solve?', 'EASY'),
    (2, 1, 'How does dependency injection reduce tight coupling between classes?', 'MEDIUM'),
    (3, 2, 'What is constructor injection?', 'EASY'),
    (4, 2, 'Why is constructor injection usually preferred in Spring?', 'MEDIUM'),
    (5, 3, 'When is setter injection useful?', 'EASY'),
    (6, 4, 'Why is field injection generally discouraged?', 'EASY'),
    (7, 5, 'How does dependency injection make unit testing easier?', 'EASY'),
    (8, 6, 'What does the IoC container do in Spring?', 'MEDIUM'),
    (9, 7, 'How does Spring Boot decide which beans to autoconfigure?', 'HARD'),
    (10, 8, 'When would you choose HashMap over TreeMap in production code?', 'MEDIUM');

SELECT setval(pg_get_serial_sequence('topic', 'id'), (SELECT COALESCE(MAX(id), 1) FROM topic), true);
SELECT setval(pg_get_serial_sequence('concept', 'id'), (SELECT COALESCE(MAX(id), 1) FROM concept), true);
SELECT setval(pg_get_serial_sequence('question', 'id'), (SELECT COALESCE(MAX(id), 1) FROM question), true);
