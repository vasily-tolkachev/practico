package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.MicroConceptJpaEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface MicroConceptJpaRepository extends JpaRepository<MicroConceptJpaEntity, Long> {
    List<MicroConceptJpaEntity> findByConcept_IdInOrderByConcept_IdAscSortOrderAscIdAsc(Collection<Long> conceptIds);

    @Query("""
            select mc
            from MicroConceptJpaEntity mc
            join fetch mc.concept c
            join fetch c.topic t
            where c.id in :conceptIds
            order by c.id asc, mc.sortOrder asc, mc.id asc
            """)
    List<MicroConceptJpaEntity> findWithConceptAndTopicByConceptIds(@Param("conceptIds") Collection<Long> conceptIds);
}
