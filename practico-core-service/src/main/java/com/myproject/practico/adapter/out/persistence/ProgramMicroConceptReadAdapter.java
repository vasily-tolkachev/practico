package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.ConceptJpaEntity;
import com.myproject.practico.adapter.out.persistence.entity.LearningProgramTopicJpaEntity;
import com.myproject.practico.adapter.out.persistence.entity.MicroConceptJpaEntity;
import com.myproject.practico.application.port.out.ProgramMicroConceptReadPort;
import com.myproject.practico.application.program.ProgramMicroConceptTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProgramMicroConceptReadAdapter implements ProgramMicroConceptReadPort {

    private final LearningProgramTopicJpaRepository learningProgramTopicJpaRepository;
    private final ConceptJpaRepository conceptJpaRepository;
    private final MicroConceptJpaRepository microConceptJpaRepository;
    private final QuestionJpaRepository questionJpaRepository;

    @Override
    public List<ProgramMicroConceptTarget> findByProgramId(Long programId) {
        List<LearningProgramTopicJpaEntity> links = learningProgramTopicJpaRepository.findByProgram_IdOrderByOrderIndexAsc(programId);
        if (links.isEmpty()) {
            return List.of();
        }

        List<Long> topicIds = links.stream().map(link -> link.getTopic().getId()).toList();
        List<ConceptJpaEntity> concepts = conceptJpaRepository.findByTopic_IdInOrderByTopic_IdAscIdAsc(topicIds);
        if (concepts.isEmpty()) {
            return List.of();
        }

        Set<Long> conceptIds = concepts.stream().map(ConceptJpaEntity::getId).collect(Collectors.toSet());
        List<MicroConceptJpaEntity> micros = microConceptJpaRepository.findWithConceptAndTopicByConceptIds(conceptIds);

        List<ProgramMicroConceptTarget> targets = new ArrayList<>();
        for (MicroConceptJpaEntity micro : micros) {
            targets.add(new ProgramMicroConceptTarget(
                    micro.getId(),
                    micro.getConcept().getTopic().getName(),
                    micro.getConcept().getName(),
                    micro.getName()
            ));
        }
        return targets;
    }

    @Override
    public boolean hasQuestions(Long microConceptId) {
        if (microConceptId == null || microConceptId <= 0) {
            return false;
        }
        return questionJpaRepository.existsByMicroConcept_Id(microConceptId);
    }
}
