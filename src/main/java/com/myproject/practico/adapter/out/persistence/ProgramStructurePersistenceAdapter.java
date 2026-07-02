package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.ConceptJpaEntity;
import com.myproject.practico.adapter.out.persistence.entity.MicroConceptJpaEntity;
import com.myproject.practico.adapter.out.persistence.entity.TopicJpaEntity;
import com.myproject.practico.application.port.out.LearningProgramTopicPersistencePort;
import com.myproject.practico.application.port.out.ProgramStructurePersistencePort;
import com.myproject.practico.application.program.GeneratedConceptStructure;
import com.myproject.practico.application.program.GeneratedProgramStructure;
import com.myproject.practico.application.program.GeneratedTopicStructure;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProgramStructurePersistenceAdapter implements ProgramStructurePersistencePort {

    private final TopicJpaRepository topicJpaRepository;
    private final ConceptJpaRepository conceptJpaRepository;
    private final MicroConceptJpaRepository microConceptJpaRepository;
    private final LearningProgramTopicPersistencePort learningProgramTopicPersistencePort;

    @Override
    @Transactional
    public void persist(Long programId, GeneratedProgramStructure structure) {
        if (programId == null || programId <= 0 || structure == null || structure.topics().isEmpty()) {
            return;
        }

        List<Long> programTopicIds = new ArrayList<>();
        for (GeneratedTopicStructure topicStructure : structure.topics()) {
            TopicJpaEntity topic = resolveTopic(topicStructure.name());
            programTopicIds.add(topic.getId());

            for (GeneratedConceptStructure conceptStructure : topicStructure.concepts()) {
                ConceptJpaEntity concept = conceptJpaRepository.save(new ConceptJpaEntity(
                        null,
                        topic,
                        conceptStructure.name()
                ));

                int sortOrder = 0;
                for (String microConceptName : conceptStructure.microConcepts()) {
                    microConceptJpaRepository.save(new MicroConceptJpaEntity(
                            null,
                            concept,
                            microConceptName,
                            sortOrder++
                    ));
                }
            }
        }

        learningProgramTopicPersistencePort.attachTopics(programId, programTopicIds);
    }

    private TopicJpaEntity resolveTopic(String topicName) {
        return topicJpaRepository.findByName(topicName)
                .orElseGet(() -> topicJpaRepository.save(new TopicJpaEntity(null, topicName)));
    }
}
