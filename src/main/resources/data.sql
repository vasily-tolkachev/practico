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
    (1, 1, 'Why is creating dependencies with new inside a class considered a design problem?', 'EASY', 'UNDERSTANDING'),
    (2, 1, 'How does dependency injection reduce coupling and make components easier to replace?', 'MEDIUM', 'UNDERSTANDING'),
    (3, 2, 'Why is constructor injection generally preferred over field injection?', 'EASY', 'COMPARISON'),
    (4, 2, 'When does constructor injection improve fail-fast behavior and testability?', 'MEDIUM', 'APPLICATION'),
    (5, 3, 'When is setter injection a better choice than constructor injection?', 'EASY', 'COMPARISON'),
    (6, 4, 'What risks does field injection introduce for object design and testing?', 'EASY', 'UNDERSTANDING'),
    (7, 5, 'How does dependency injection make unit tests simpler and more reliable?', 'EASY', 'UNDERSTANDING'),
    (8, 6, 'How does the IoC container control object creation and wiring better than manual new?', 'MEDIUM', 'UNDERSTANDING'),
    (9, 7, 'How does Spring Boot decide whether to create an auto-configured bean, and how can you override it?', 'HARD', 'APPLICATION'),
    (10, 8, 'When would you choose HashMap over TreeMap, and what trade-off are you accepting?', 'MEDIUM', 'COMPARISON');

SELECT setval(pg_get_serial_sequence('topic', 'id'), (SELECT COALESCE(MAX(id), 1) FROM topic), true);
SELECT setval(pg_get_serial_sequence('concept', 'id'), (SELECT COALESCE(MAX(id), 1) FROM concept), true);
SELECT setval(pg_get_serial_sequence('micro_concept', 'id'), (SELECT COALESCE(MAX(id), 1) FROM micro_concept), true);
SELECT setval(pg_get_serial_sequence('question', 'id'), (SELECT COALESCE(MAX(id), 1) FROM question), true);
