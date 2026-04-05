package org.adt.volunteerscase.unit.service;

import org.adt.volunteerscase.dto.cover.request.CoverCreateRequest;
import org.adt.volunteerscase.dto.cover.request.CoverPatchRequest;
import org.adt.volunteerscase.dto.cover.response.CoverResponse;
import org.adt.volunteerscase.entity.CoverEntity;
import org.adt.volunteerscase.exception.CoverInUseException;
import org.adt.volunteerscase.exception.CoverNotFoundException;
import org.adt.volunteerscase.repository.CoverRepository;
import org.adt.volunteerscase.repository.EventRepository;
import org.adt.volunteerscase.service.CoverService;
import org.adt.volunteerscase.service.impl.CoverServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class CoverServiceTest {

    @Mock
    private CoverRepository coverRepository;

    @Mock
    private EventRepository eventRepository;

    private CoverService coverService;
    private CoverCreateRequest createRequest;
    private CoverEntity existingCover;


    @BeforeEach
    void setUp() {
        coverService = new CoverServiceImpl(coverRepository, eventRepository);

        createRequest = CoverCreateRequest.builder()
                .link("link 1")
                .width(1200)
                .height(630)
                .build();

        existingCover = CoverEntity.builder()
                .coverId(1)
                .link("link old")
                .width(800)
                .height(600)
                .build();
    }

    @Test
    void coverCreateRequest_shouldSaveCoverWithRequestData() {
        ArgumentCaptor<CoverEntity> coverCaptor = ArgumentCaptor.forClass(CoverEntity.class);

        coverService.coverCreateRequest(createRequest);

        verify(coverRepository).save(coverCaptor.capture());
        CoverEntity savedCover = coverCaptor.getValue();

        assertThat(savedCover.getCoverId()).isNull();
        assertThat(savedCover.getLink()).isEqualTo(createRequest.getLink());
        assertThat(savedCover.getWidth()).isEqualTo(createRequest.getWidth());
        assertThat(savedCover.getHeight()).isEqualTo(createRequest.getHeight());

        verifyNoInteractions(eventRepository);
    }

    @Test
    void updateCover_shouldUpdateOnlyProvidedFieldsAndReturnResponse() {
        CoverPatchRequest patchRequest = CoverPatchRequest.builder()
                .link("new link")
                .width(1920)
                .build();

        when(coverRepository.findByCoverId(1)).thenReturn(Optional.of(existingCover));
        when(coverRepository.save(any(CoverEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CoverResponse response = coverService.updateCover(patchRequest, 1);

        assertThat(existingCover.getLink()).isEqualTo("new link");
        assertThat(existingCover.getWidth()).isEqualTo(1920);
        assertThat(existingCover.getHeight()).isEqualTo(600);

        assertThat(response.getCoverId()).isEqualTo(1);
        assertThat(response.getLink()).isEqualTo("new link");
        assertThat(response.getWidth()).isEqualTo(1920);
        assertThat(response.getHeight()).isEqualTo(600);

        verify(coverRepository).findByCoverId(1);
        verify(coverRepository).save(existingCover);
        verifyNoInteractions(eventRepository);
    }

    @Test
    void updateCover_shouldThrowException_whenCoverNotFound() {
        when(coverRepository.findByCoverId(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> coverService.updateCover(CoverPatchRequest.builder().build(), 99))
                .isInstanceOf(CoverNotFoundException.class)
                .hasMessage("cover with id - 99 not found");

        verify(coverRepository).findByCoverId(99);
        verify(coverRepository, never()).save(any(CoverEntity.class));
        verifyNoInteractions(eventRepository);
    }

    @Test
    void deleteCoverById_shouldDeleteCover_whenCoverExistsAndNotUsedByEvent() {
        when(coverRepository.findByCoverId(1)).thenReturn(Optional.of(existingCover));
        when(eventRepository.existsByCover(existingCover)).thenReturn(false);

        coverService.deleteCoverById(1);

        verify(coverRepository).findByCoverId(1);
        verify(eventRepository).existsByCover(existingCover);
        verify(coverRepository).delete(existingCover);
    }

    @Test
    void deleteCoverById_shouldThrowException_whenCoverNotFound() {
        when(coverRepository.findByCoverId(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> coverService.deleteCoverById(99))
                .isInstanceOf(CoverNotFoundException.class)
                .hasMessage("cover with id - 99 not found");

        verify(coverRepository).findByCoverId(99);
        verify(eventRepository, never()).existsByCover(any(CoverEntity.class));
        verify(coverRepository, never()).delete(any(CoverEntity.class));
    }

    @Test
    void deleteCoverById_shouldThrowException_whenCoverIsUsedByEvent() {
        when(coverRepository.findByCoverId(1)).thenReturn(Optional.of(existingCover));
        when(eventRepository.existsByCover(existingCover)).thenReturn(true);

        assertThatThrownBy(() -> coverService.deleteCoverById(1))
                .isInstanceOf(CoverInUseException.class)
                .hasMessage("cover with id - 1 is used by event");

        verify(coverRepository).findByCoverId(1);
        verify(eventRepository).existsByCover(existingCover);
        verify(coverRepository, never()).delete(any(CoverEntity.class));
    }

    @Test
    void getCoverById_shouldReturnCoverResponse_whenCoverExists() {
        when(coverRepository.findByCoverId(1)).thenReturn(Optional.of(existingCover));

        CoverResponse response = coverService.getCoverById(1);

        assertThat(response.getCoverId()).isEqualTo(1);
        assertThat(response.getLink()).isEqualTo("link old");
        assertThat(response.getWidth()).isEqualTo(800);
        assertThat(response.getHeight()).isEqualTo(600);

        verify(coverRepository).findByCoverId(1);
        verifyNoInteractions(eventRepository);
    }

    @Test
    void getCoverById_shouldThrowException_whenCoverNotFound() {
        when(coverRepository.findByCoverId(77)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> coverService.getCoverById(77))
                .isInstanceOf(CoverNotFoundException.class)
                .hasMessage("cover with id - 77 not found");

        verify(coverRepository).findByCoverId(77);
        verifyNoInteractions(eventRepository);
    }

}
