package org.adt.volunteerscase.service.impl;

import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.dto.tag.request.TagCreateRequest;
import org.adt.volunteerscase.dto.tag.request.TagUpdateRequest;
import org.adt.volunteerscase.dto.tag.response.TagGetResponse;
import org.adt.volunteerscase.entity.TagEntity;
import org.adt.volunteerscase.exception.TagAlreadyExistsException;
import org.adt.volunteerscase.exception.TagNotFoundException;
import org.adt.volunteerscase.repository.TagRepository;
import org.adt.volunteerscase.service.TagService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;


    @Override
    public Set<TagEntity> getTagEntities(Set<Integer> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new HashSet<>();
        }

        Set<Integer> cleanedTagIds = tagIds.stream()
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());

        if (cleanedTagIds.isEmpty()) {
            return new HashSet<>();
        }


        List<TagEntity> foundTags = tagRepository.findAllByTagIdIn(cleanedTagIds);

        if (foundTags.size() != cleanedTagIds.size()) {
            Set<Integer> foundTagIds = foundTags.stream()
                    .map(TagEntity::getTagId)
                    .collect(Collectors.toSet());

            Set<Integer> missingTagIds = cleanedTagIds.stream()
                    .filter(id -> !foundTagIds.contains(id))
                    .collect(Collectors.toSet());

            throw new TagNotFoundException("Tags not found with IDs: " + missingTagIds);
        }

        return new HashSet<>(foundTags);
    }

    @Override
    @Transactional
    public void createTag(TagCreateRequest request) {
        String tagName = request.getTagName();
        if (tagRepository.existsByTagName(tagName)) {
            throw new TagAlreadyExistsException("tag with name - " + tagName + " already exists");
        }
        TagEntity tagEntity = TagEntity.builder()
                .tagName(request.getTagName())
                .build();
        try {
            tagRepository.save(tagEntity);
        } catch (DataIntegrityViolationException ex) {
            throw new TagAlreadyExistsException("tag with name - " + tagName + " already exists");
        }
    }

    @Override
    @Transactional
    public void updateTag(TagUpdateRequest request, Integer tagId) {


        TagEntity tagEntity = tagRepository.findByTagId(tagId)
                .orElseThrow(() -> new TagNotFoundException("tag with id - " + tagId + " not found"));

        if (tagRepository.existsByTagName(request.getTagName())) {
            throw new TagAlreadyExistsException("tag with name - " + request.getTagName() + " already exists");
        }

        tagEntity.setTagName(request.getTagName());
        tagRepository.save(tagEntity);
    }

    @Override
    @Transactional
    public void deleteById(Integer tagId) {
        if (!tagRepository.existsById(tagId)) {
            throw new TagNotFoundException("tag with id - " + tagId + " not found");
        }
        tagRepository.deleteById(tagId);
    }

    @Override
    @Transactional
    public void deleteByName(String tagName) {
        if (!tagRepository.existsByTagName(tagName)) {
            throw new TagNotFoundException("tag with name - " + tagName + " not found");
        }
        tagRepository.deleteByTagName(tagName);
    }

    @Override
    public TagGetResponse getById(Integer tagId) {
        TagEntity tagEntity = tagRepository.findByTagId(tagId)
                .orElseThrow(() -> new TagNotFoundException("tag with id - " + tagId + " not found"));

        return convertToResponse(tagEntity);
    }

    @Override
    public TagGetResponse getByName(String tagName) {
        TagEntity tagEntity = tagRepository.findByTagName(tagName)
                .orElseThrow(() -> new TagNotFoundException("tag with name - " + tagName + " not found"));

        return convertToResponse(tagEntity);
    }

    private TagGetResponse convertToResponse(TagEntity tagEntity) {
        return TagGetResponse.builder()
                .tagName(tagEntity.getTagName())
                .tagId(tagEntity.getTagId())
                .build();
    }
}
