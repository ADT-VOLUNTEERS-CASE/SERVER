package org.adt.volunteerscase.unit.service;

import org.adt.volunteerscase.dto.tag.request.TagCreateRequest;
import org.adt.volunteerscase.dto.tag.request.TagUpdateRequest;
import org.adt.volunteerscase.dto.tag.response.TagGetResponse;
import org.adt.volunteerscase.entity.TagEntity;
import org.adt.volunteerscase.exception.TagAlreadyExistsException;
import org.adt.volunteerscase.exception.TagNotFoundException;
import org.adt.volunteerscase.repository.TagRepository;
import org.adt.volunteerscase.service.TagService;
import org.adt.volunteerscase.service.impl.TagServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    private TagService tagService;
    private TagCreateRequest createRequest;
    private TagEntity existingTag;

    @BeforeEach
    void setUp() {
        tagService = new TagServiceImpl(tagRepository);

        createRequest = TagCreateRequest.builder()
                .tagName("education")
                .build();

        existingTag = TagEntity.builder()
                .tagId(1)
                .tagName("volunteer")
                .build();
    }

    @Test
    void getTagEntities_shouldReturnEmptySet_whenTagIdsIsNull() {
        Set<TagEntity> result = tagService.getTagEntities(null);

        assertThat(result).isEmpty();
        verifyNoInteractions(tagRepository);
    }

    @Test
    void getTagEntities_shouldReturnTags_whenAllIdsExist() {
        TagEntity secondTag = TagEntity.builder()
                .tagId(2)
                .tagName("animals")
                .build();

        Set<Integer> tagIds = Set.of(1, 2);

        when(tagRepository.findAllByTagIdIn(tagIds))
                .thenReturn(List.of(existingTag, secondTag));

        Set<TagEntity> result = tagService.getTagEntities(tagIds);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(existingTag, secondTag);

        verify(tagRepository).findAllByTagIdIn(tagIds);
    }

    @Test
    void getTagEntities_shouldThrowException_whenSomeTagsAreMissing() {
        Set<Integer> tagIds = Set.of(1, 2);

        when(tagRepository.findAllByTagIdIn(tagIds))
                .thenReturn(List.of(existingTag));

        assertThatThrownBy(() -> tagService.getTagEntities(tagIds))
                .isInstanceOf(TagNotFoundException.class)
                .hasMessage("Tags not found with IDs: [2]");

        verify(tagRepository).findAllByTagIdIn(tagIds);
    }

    @Test
    void createTag_shouldSaveTagWithRequestData() {
        when(tagRepository.existsByTagName(createRequest.getTagName())).thenReturn(false);

        ArgumentCaptor<TagEntity> tagCaptor = ArgumentCaptor.forClass(TagEntity.class);

        tagService.createTag(createRequest);

        verify(tagRepository).existsByTagName(createRequest.getTagName());
        verify(tagRepository).save(tagCaptor.capture());

        TagEntity savedTag = tagCaptor.getValue();
        assertThat(savedTag.getTagId()).isNull();
        assertThat(savedTag.getTagName()).isEqualTo("education");
    }

    @Test
    void createTag_shouldThrowException_whenTagAlreadyExists() {
        when(tagRepository.existsByTagName(createRequest.getTagName())).thenReturn(true);

        assertThatThrownBy(() -> tagService.createTag(createRequest))
                .isInstanceOf(TagAlreadyExistsException.class)
                .hasMessage("tag with name - education already exists");

        verify(tagRepository).existsByTagName(createRequest.getTagName());
        verify(tagRepository, never()).save(any(TagEntity.class));
    }

    @Test
    void createTag_shouldThrowException_whenSaveViolatesUniqueConstraint() {
        when(tagRepository.existsByTagName(createRequest.getTagName())).thenReturn(false);
        when(tagRepository.save(any(TagEntity.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThatThrownBy(() -> tagService.createTag(createRequest))
                .isInstanceOf(TagAlreadyExistsException.class)
                .hasMessage("tag with name - education already exists");

        verify(tagRepository).existsByTagName(createRequest.getTagName());
        verify(tagRepository).save(any(TagEntity.class));
    }

    @Test
    void updateTag_shouldUpdateTagName_whenTagExistsAndNameIsFree() {
        TagUpdateRequest request = TagUpdateRequest.builder()
                .tagName("ecology")
                .build();

        when(tagRepository.findByTagId(1)).thenReturn(Optional.of(existingTag));
        when(tagRepository.existsByTagNameAndTagIdNot("ecology", 1)).thenReturn(false);

        tagService.updateTag(request, 1);

        assertThat(existingTag.getTagName()).isEqualTo("ecology");

        verify(tagRepository).findByTagId(1);
        verify(tagRepository).existsByTagNameAndTagIdNot("ecology", 1);
        verify(tagRepository).save(existingTag);
    }

    @Test
    void updateTag_shouldThrowException_whenTagNotFound() {
        TagUpdateRequest request = TagUpdateRequest.builder()
                .tagName("ecology")
                .build();

        when(tagRepository.findByTagId(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.updateTag(request, 99))
                .isInstanceOf(TagNotFoundException.class)
                .hasMessage("tag with id - 99 not found");

        verify(tagRepository).findByTagId(99);
        verify(tagRepository, never()).existsByTagNameAndTagIdNot(anyString(), anyInt());
        verify(tagRepository, never()).save(any(TagEntity.class));
    }

    @Test
    void updateTag_shouldThrowException_whenAnotherTagWithSameNameExists() {
        TagUpdateRequest request = TagUpdateRequest.builder()
                .tagName("animals")
                .build();

        when(tagRepository.findByTagId(1)).thenReturn(Optional.of(existingTag));
        when(tagRepository.existsByTagNameAndTagIdNot("animals", 1)).thenReturn(true);

        assertThatThrownBy(() -> tagService.updateTag(request, 1))
                .isInstanceOf(TagAlreadyExistsException.class)
                .hasMessage("tag with name - animals already exists");

        verify(tagRepository).findByTagId(1);
        verify(tagRepository).existsByTagNameAndTagIdNot("animals", 1);
        verify(tagRepository, never()).save(any(TagEntity.class));
    }

    @Test
    void deleteById_shouldDetachRelationsAndDeleteTag() {
        when(tagRepository.findByTagId(1)).thenReturn(Optional.of(existingTag));

        tagService.deleteById(1);

        InOrder inOrder = inOrder(tagRepository);
        inOrder.verify(tagRepository).findByTagId(1);
        inOrder.verify(tagRepository).deleteUserTagLinksByTagId(1);
        inOrder.verify(tagRepository).deleteEventTagLinksByTagId(1);
        inOrder.verify(tagRepository).delete(existingTag);
    }

    @Test
    void deleteById_shouldThrowException_whenTagNotFound() {
        when(tagRepository.findByTagId(77)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.deleteById(77))
                .isInstanceOf(TagNotFoundException.class)
                .hasMessage("tag with id - 77 not found");

        verify(tagRepository).findByTagId(77);
        verify(tagRepository, never()).deleteUserTagLinksByTagId(anyInt());
        verify(tagRepository, never()).deleteEventTagLinksByTagId(anyInt());
        verify(tagRepository, never()).delete(any(TagEntity.class));
    }

    @Test
    void deleteByName_shouldDetachRelationsAndDeleteTag() {
        when(tagRepository.findByTagName("volunteer")).thenReturn(Optional.of(existingTag));

        tagService.deleteByName("volunteer");

        InOrder inOrder = inOrder(tagRepository);
        inOrder.verify(tagRepository).findByTagName("volunteer");
        inOrder.verify(tagRepository).deleteUserTagLinksByTagId(1);
        inOrder.verify(tagRepository).deleteEventTagLinksByTagId(1);
        inOrder.verify(tagRepository).delete(existingTag);
    }

    @Test
    void getById_shouldReturnResponse_whenTagExists() {
        when(tagRepository.findByTagId(1)).thenReturn(Optional.of(existingTag));

        TagGetResponse response = tagService.getById(1);

        assertThat(response.getTagId()).isEqualTo(1);
        assertThat(response.getTagName()).isEqualTo("volunteer");

        verify(tagRepository).findByTagId(1);
    }

    @Test
    void getByName_shouldThrowException_whenTagNotFound() {
        when(tagRepository.findByTagName("missing-tag")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.getByName("missing-tag"))
                .isInstanceOf(TagNotFoundException.class)
                .hasMessage("tag with name - missing-tag not found");

        verify(tagRepository).findByTagName("missing-tag");
    }

    @Test
    void deleteByName_shouldThrowException_whenTagNotFound() {
        when(tagRepository.findByTagName("missing-tag")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.deleteByName("missing-tag"))
                .isInstanceOf(TagNotFoundException.class)
                .hasMessage("tag with name - missing-tag not found");

        verify(tagRepository).findByTagName("missing-tag");
        verify(tagRepository, never()).deleteUserTagLinksByTagId(anyInt());
        verify(tagRepository, never()).deleteEventTagLinksByTagId(anyInt());
        verify(tagRepository, never()).delete(any(TagEntity.class));
    }

    @Test
    void getByName_shouldReturnResponse_whenTagExists() {
        when(tagRepository.findByTagName("volunteer")).thenReturn(Optional.of(existingTag));

        TagGetResponse response = tagService.getByName("volunteer");

        assertThat(response.getTagId()).isEqualTo(1);
        assertThat(response.getTagName()).isEqualTo("volunteer");

        verify(tagRepository).findByTagName("volunteer");
    }

}
