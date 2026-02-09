package org.adt.volunteerscase.service;


import org.adt.volunteerscase.dto.tag.request.TagCreateRequest;
import org.adt.volunteerscase.entity.TagEntity;

import java.util.Set;

public interface TagService {

    Set<TagEntity> getTagEntities(Set<Integer> tagIds);
    void createTag(TagCreateRequest request);
}
