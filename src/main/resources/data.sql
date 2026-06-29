INSERT INTO topic (id, name) VALUES
    (1, 'Spring'),
    (2, 'Java Core');

INSERT INTO concept (id, topic_id, name) VALUES
    (1, 1, 'Dependency Injection'),
    (2, 1, 'Spring Boot Autoconfiguration'),
    (3, 2, 'Collections Framework');

INSERT INTO micro_concept (id, concept_id, name, sort_order) VALUES
    (1, 1, 'Why DI exists', 1),
    (2, 1, 'Constructor Injection', 2),
    (3, 1, 'Setter Injection', 3),
    (4, 1, 'Field Injection', 4),
    (5, 1, 'DI and Testing', 5),
    (6, 1, 'IoC Container', 6),
    (7, 2, 'Autoconfiguration Basics', 1),
    (8, 3, 'HashMap vs TreeMap', 1);

INSERT INTO question (id, micro_concept_id, text, difficulty, question_type) VALUES
    (1, 1, 'What main problem does dependency injection solve?', 'EASY', 'DEFINITION'),
    (2, 1, 'How does dependency injection reduce tight coupling between classes?', 'MEDIUM', 'UNDERSTANDING'),
    (3, 2, 'What is constructor injection?', 'EASY', 'DEFINITION'),
    (4, 2, 'Why is constructor injection usually preferred in Spring?', 'MEDIUM', 'UNDERSTANDING'),
    (5, 3, 'When is setter injection useful?', 'EASY', 'APPLICATION'),
    (6, 4, 'Why is field injection generally discouraged?', 'EASY', 'UNDERSTANDING'),
    (7, 5, 'How does dependency injection make unit testing easier?', 'EASY', 'UNDERSTANDING'),
    (8, 6, 'What does the IoC container do in Spring?', 'MEDIUM', 'DEFINITION'),
    (9, 7, 'How does Spring Boot decide which beans to autoconfigure?', 'HARD', 'UNDERSTANDING'),
    (10, 8, 'When would you choose HashMap over TreeMap in production code?', 'MEDIUM', 'COMPARISON');

SELECT setval(pg_get_serial_sequence('topic', 'id'), (SELECT COALESCE(MAX(id), 1) FROM topic), true);
SELECT setval(pg_get_serial_sequence('concept', 'id'), (SELECT COALESCE(MAX(id), 1) FROM concept), true);
SELECT setval(pg_get_serial_sequence('micro_concept', 'id'), (SELECT COALESCE(MAX(id), 1) FROM micro_concept), true);
SELECT setval(pg_get_serial_sequence('question', 'id'), (SELECT COALESCE(MAX(id), 1) FROM question), true);
