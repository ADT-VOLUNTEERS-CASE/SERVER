package org.adt.volunteerscase.repository;

import org.adt.volunteerscase.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    boolean existsByTagNameAndTagIdNot(String tagName, Integer tagId);

    void deleteByTagName(String tag);

    @Modifying
    @Query(value = "DELETE FROM user_tags WHERE tag_id = :tagId", nativeQuery = true)
    void deleteUserTagLinksByTagId(@Param("tagId") Integer tagId);

    @Modifying
    @Query(value = "DELETE FROM event_tags WHERE tag_id = :tagId", nativeQuery = true)
    void deleteEventTagLinksByTagId(@Param("tagId") Integer tagId);

}
