package org.adt.volunteerscase.repository;

import org.adt.volunteerscase.entity.CoverEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoverRepository extends JpaRepository<CoverEntity, Integer> {
    Optional<CoverEntity> findByCoverIdAndDeletedAtIsNull(Integer coverId);
    boolean existsByLinkAndDeletedAtIsNull(String link);
    Optional<CoverEntity> findByLinkAndDeletedAtIsNull(String link);
}
