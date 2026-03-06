package org.adt.volunteerscase.repository;

import org.adt.volunteerscase.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TagRepository extends JpaRepository<TagEntity, Integer> {

    Optional<TagEntity> findByTagId(Integer tagId);
    Optional<TagEntity> findByTagName(String tagName);


    List<TagEntity> findAllByTagIdIn(Set<Integer> tagIds);

    boolean existsByTagName(String tagName);

    void deleteByTagName(String tag);
}
