package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.ConceptJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ConceptJpaRepository extends JpaRepository<ConceptJpaEntity, Long> {
    List<ConceptJpaEntity> findByTopic_IdInOrderByTopic_IdAscIdAsc(Collection<Long> topicIds);
}
