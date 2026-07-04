package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.ConceptJpaEntity;
import com.myproject.practico.adapter.out.persistence.entity.LearningProgramTopicJpaEntity;
import com.myproject.practico.adapter.out.persistence.entity.MicroConceptJpaEntity;
import com.myproject.practico.application.port.out.ProgramTreeReadPort;
import com.myproject.practico.application.program.ProgramConcept;
import com.myproject.practico.application.program.ProgramMicroConcept;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProgramTreeReadAdapter implements ProgramTreeReadPort {

    private final LearningProgramTopicJpaRepository learningProgramTopicJpaRepository;
    private final ConceptJpaRepository conceptJpaRepository;
    private final MicroConceptJpaRepository microConceptJpaRepository;

    @Override
    public List<ProgramConcept> findConceptTreeByProgramId(Long programId) {
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
        List<MicroConceptJpaEntity> microConcepts = microConceptJpaRepository.findByConcept_IdInOrderByConcept_IdAscSortOrderAscIdAsc(conceptIds);
        Map<Long, List<ProgramMicroConcept>> microByConcept = new LinkedHashMap<>();
        for (MicroConceptJpaEntity micro : microConcepts) {
            microByConcept.computeIfAbsent(micro.getConcept().getId(), key -> new ArrayList<>())
                    .add(new ProgramMicroConcept(
                            micro.getId(),
                            micro.getName(),
                            micro.getSortOrder(),
                            false,
                            false,
                            true
                    ));
        }

        List<ProgramConcept> tree = new ArrayList<>();
        for (ConceptJpaEntity concept : concepts) {
            List<ProgramMicroConcept> conceptMicros = microByConcept.getOrDefault(concept.getId(), List.of());
            tree.add(new ProgramConcept(
                    concept.getId(),
                    concept.getName(),
                    "Generated concept",
                    Math.max(15, conceptMicros.size() * 10),
                    estimateDifficulty(conceptMicros.size()),
                    List.of(),
                    conceptMicros
            ));
        }
        return tree;
    }

    private String estimateDifficulty(int microCount) {
        if (microCount >= 6) {
            return "HARD";
        }
        if (microCount >= 3) {
            return "MEDIUM";
        }
        return "EASY";
    }
}
