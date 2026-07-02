package com.myproject.practico.application.port.out;

import java.util.List;

public interface LearningProgramTopicPersistencePort {

    void attachTopics(Long programId, List<Long> topicIdsInOrder);

    List<Long> findTopicIds(Long programId);
}
