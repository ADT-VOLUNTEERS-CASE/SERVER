package org.adt.volunteerscase.service;


import jakarta.validation.Valid;
import org.adt.volunteerscase.dto.tag.request.TagCreateRequest;
import org.adt.volunteerscase.dto.tag.request.TagUpdateRequest;
import org.adt.volunteerscase.dto.tag.response.TagGetResponse;
import org.adt.volunteerscase.entity.TagEntity;

import java.util.Set;

public interface TagService {

    Set<TagEntity> getTagEntities(Set<Integer> tagIds);
    void createTag(TagCreateRequest request);

    void updateTag(TagUpdateRequest request, Integer tagId);

    void deleteById(Integer tagId);

    void deleteByName(String tagName);

    TagGetResponse getById(Integer tagId);

    TagGetResponse getByName(String tagName);
}
