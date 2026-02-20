package org.adt.volunteerscase.repository;

import org.adt.volunteerscase.entity.CoverEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;

@Repository
public interface CoverRepository extends JpaRepository<CoverEntity, Integer> {
    Optional<CoverEntity> findByCoverId(Integer coverId);

    boolean existsByLink(String link);

    Optional<CoverEntity> findByLink(String link);
}
