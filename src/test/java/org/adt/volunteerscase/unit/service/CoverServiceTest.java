package org.adt.volunteerscase.unit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.adt.volunteerscase.dto.cover.CoverMapper;
import org.adt.volunteerscase.dto.cover.CoverMetadataDTO;
import org.adt.volunteerscase.dto.cover.request.CoverCreateRequest;
import org.adt.volunteerscase.dto.cover.request.CoverPatchRequest;
import org.adt.volunteerscase.dto.cover.response.CoverResponse;
import org.adt.volunteerscase.entity.CoverEntity;
import org.adt.volunteerscase.exception.CoverInUseException;
import org.adt.volunteerscase.exception.CoverNotFoundException;
import org.adt.volunteerscase.exception.InvalidCoverFileException;
import org.adt.volunteerscase.repository.CoverRepository;
import org.adt.volunteerscase.repository.EventRepository;
import org.adt.volunteerscase.service.impl.CoverServiceImpl;
import org.adt.volunteerscase.service.storage.ObjectStorageService;
import org.adt.volunteerscase.service.storage.StoredObjectResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoverServiceTest {

    @Mock
    private CoverRepository coverRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ObjectStorageService objectStorageService;

    private CoverServiceImpl coverService;
    private CoverMapper coverMapper;
    private CoverEntity existingCover;

    @BeforeEach
    void setUp() {
        coverMapper = new CoverMapper(new ObjectMapper());
        coverService = new CoverServiceImpl(
                coverRepository,
                eventRepository,
                objectStorageService,
                coverMapper
        );

        existingCover = CoverEntity.builder()
                .coverId(1)
                .link("https://cdn.example.com/covers/old-cover.png")
                .metadata(coverMapper.encodeMetadata(
                        CoverMetadataDTO.builder()
                                .originalFileName("old-cover.png")
                                .contentType("image/png")
                                .size(128L)
                                .width(800)
                                .height(600)
                                .bucket("covers")
                                .objectKey("covers/2026/04/old-cover.png")
                                .eTag("etag-old")
                                .build()
                ))
                .createdAt(Instant.now().toEpochMilli())
                .deletedAt(null)
                .build();
    }

    @Test
    void createCover_shouldUploadSaveAndReturnResponse() throws IOException {
        MockMultipartFile file = imageFile("new-cover.png", 1200, 630);
        CoverCreateRequest request = CoverCreateRequest.builder()
                .file(file)
                .build();

        StoredObjectResult uploadResult = StoredObjectResult.builder()
                .bucket("covers")
                .objectKey("covers/2026/04/new-cover.png")
                .link("https://cdn.example.com/covers/2026/04/new-cover.png")
                .eTag("etag-new")
                .build();

        when(objectStorageService.uploadCover(anyString(), anyString(), any(byte[].class)))
                .thenReturn(uploadResult);
        when(coverRepository.save(any(CoverEntity.class)))
                .thenAnswer(invocation -> {
                    CoverEntity entity = invocation.getArgument(0);
                    entity.setCoverId(10);
                    return entity;
                });

        CoverResponse response = coverService.createCover(request);

        ArgumentCaptor<CoverEntity> captor = ArgumentCaptor.forClass(CoverEntity.class);
        verify(coverRepository).save(captor.capture());
        CoverEntity saved = captor.getValue();
        CoverMetadataDTO metadata = coverMapper.decodeMetadata(saved.getMetadata());

        assertThat(saved.getCoverId()).isEqualTo(10);
        assertThat(saved.getLink()).isEqualTo(uploadResult.getLink());
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getDeletedAt()).isNull();

        assertThat(metadata.getOriginalFileName()).isEqualTo("new-cover.png");
        assertThat(metadata.getContentType()).isEqualTo("image/png");
        assertThat(metadata.getSize()).isEqualTo(file.getSize());
        assertThat(metadata.getWidth()).isEqualTo(1200);
        assertThat(metadata.getHeight()).isEqualTo(630);
        assertThat(metadata.getBucket()).isEqualTo("covers");
        assertThat(metadata.getObjectKey()).isEqualTo("covers/2026/04/new-cover.png");
        assertThat(metadata.getETag()).isEqualTo("etag-new");

        assertThat(response.getCoverId()).isEqualTo(10);
        assertThat(response.getLink()).isEqualTo(uploadResult.getLink());
        assertThat(response.getDeletedAt()).isNull();
        assertThat(response.getFileMetadata().getWidth()).isEqualTo(1200);
        assertThat(response.getFileMetadata().getHeight()).isEqualTo(630);
        assertThat(response.getFileMetadata().getObjectKey()).isEqualTo("covers/2026/04/new-cover.png");

        verify(objectStorageService).uploadCover("new-cover.png", "image/png", file.getBytes());
        verifyNoInteractions(eventRepository);
        verify(objectStorageService, never()).deleteObject(anyString());
    }

    @Test
    void createCover_shouldThrowException_whenFileIsNotImage() {
        CoverCreateRequest request = CoverCreateRequest.builder()
                .file(new MockMultipartFile(
                        "file",
                        "notes.txt",
                        "text/plain",
                        "not-an-image".getBytes()
                ))
                .build();

        assertThatThrownBy(() -> coverService.createCover(request))
                .isInstanceOf(InvalidCoverFileException.class)
                .hasMessage("cover file must be an image");

        verifyNoInteractions(coverRepository, eventRepository, objectStorageService);
    }

    @Test
    void updateCover_shouldReplaceStoredFileAndReturnMappedResponse() throws IOException {
        MockMultipartFile newFile = imageFile("updated-cover.png", 1920, 1080);
        CoverPatchRequest request = CoverPatchRequest.builder()
                .file(newFile)
                .build();

        StoredObjectResult uploadResult = StoredObjectResult.builder()
                .bucket("covers")
                .objectKey("covers/2026/04/updated-cover.png")
                .link("https://cdn.example.com/covers/2026/04/updated-cover.png")
                .eTag("etag-updated")
                .build();

        when(coverRepository.findByCoverIdAndDeletedAtIsNull(1))
                .thenReturn(Optional.of(existingCover));
        when(objectStorageService.uploadCover(anyString(), anyString(), any(byte[].class)))
                .thenReturn(uploadResult);
        when(coverRepository.saveAndFlush(any(CoverEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CoverResponse response = coverService.updateCover(request, 1);

        CoverMetadataDTO metadata = coverMapper.decodeMetadata(existingCover.getMetadata());

        assertThat(existingCover.getLink()).isEqualTo(uploadResult.getLink());
        assertThat(metadata.getOriginalFileName()).isEqualTo("updated-cover.png");
        assertThat(metadata.getWidth()).isEqualTo(1920);
        assertThat(metadata.getHeight()).isEqualTo(1080);
        assertThat(metadata.getObjectKey()).isEqualTo("covers/2026/04/updated-cover.png");

        assertThat(response.getCoverId()).isEqualTo(1);
        assertThat(response.getLink()).isEqualTo(uploadResult.getLink());
        assertThat(response.getFileMetadata().getWidth()).isEqualTo(1920);
        assertThat(response.getFileMetadata().getHeight()).isEqualTo(1080);
        assertThat(response.getFileMetadata().getETag()).isEqualTo("etag-updated");

        verify(coverRepository).findByCoverIdAndDeletedAtIsNull(1);
        verify(coverRepository).saveAndFlush(existingCover);
        verify(objectStorageService).deleteObject("covers/2026/04/old-cover.png");
        verifyNoInteractions(eventRepository);
    }

    @Test
    void updateCover_shouldThrowException_whenCoverNotFound() throws IOException {
        CoverPatchRequest request = CoverPatchRequest.builder()
                .file(imageFile("missing.png", 100, 100))
                .build();

        when(coverRepository.findByCoverIdAndDeletedAtIsNull(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> coverService.updateCover(request, 99))
                .isInstanceOf(CoverNotFoundException.class)
                .hasMessage("cover with id - 99 not found");

        verify(coverRepository).findByCoverIdAndDeletedAtIsNull(99);
        verifyNoInteractions(eventRepository, objectStorageService);
        verify(coverRepository, never()).saveAndFlush(any(CoverEntity.class));
    }

    @Test
    void deleteCoverById_shouldSoftDeleteCoverAndDeleteStoredObject() {
        when(coverRepository.findByCoverIdAndDeletedAtIsNull(1))
                .thenReturn(Optional.of(existingCover));
        when(eventRepository.existsByCover(existingCover)).thenReturn(false);
        when(coverRepository.saveAndFlush(any(CoverEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        coverService.deleteCoverById(1);

        assertThat(existingCover.getDeletedAt()).isNotNull();

        verify(coverRepository).findByCoverIdAndDeletedAtIsNull(1);
        verify(eventRepository).existsByCover(existingCover);
        verify(coverRepository).saveAndFlush(existingCover);
        verify(objectStorageService).deleteObject("covers/2026/04/old-cover.png");
    }

    @Test
    void deleteCoverById_shouldThrowException_whenCoverNotFound() {
        when(coverRepository.findByCoverIdAndDeletedAtIsNull(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> coverService.deleteCoverById(99))
                .isInstanceOf(CoverNotFoundException.class)
                .hasMessage("cover with id - 99 not found");

        verify(coverRepository).findByCoverIdAndDeletedAtIsNull(99);
        verifyNoInteractions(eventRepository, objectStorageService);
    }

    @Test
    void deleteCoverById_shouldThrowException_whenCoverIsUsedByEvent() {
        when(coverRepository.findByCoverIdAndDeletedAtIsNull(1))
                .thenReturn(Optional.of(existingCover));
        when(eventRepository.existsByCover(existingCover)).thenReturn(true);

        assertThatThrownBy(() -> coverService.deleteCoverById(1))
                .isInstanceOf(CoverInUseException.class)
                .hasMessage("cover with id - 1 is used by event");

        verify(coverRepository).findByCoverIdAndDeletedAtIsNull(1);
        verify(eventRepository).existsByCover(existingCover);
        verify(coverRepository, never()).saveAndFlush(any(CoverEntity.class));
        verify(objectStorageService, never()).deleteObject(anyString());
    }

    @Test
    void getCoverById_shouldReturnMappedResponse_whenCoverExists() {
        when(coverRepository.findByCoverIdAndDeletedAtIsNull(1))
                .thenReturn(Optional.of(existingCover));

        CoverResponse response = coverService.getCoverById(1);

        assertThat(response.getCoverId()).isEqualTo(1);
        assertThat(response.getLink()).isEqualTo("https://cdn.example.com/covers/old-cover.png");
        assertThat(response.getCreatedAt()).isEqualTo(existingCover.getCreatedAt());
        assertThat(response.getDeletedAt()).isNull();
        assertThat(response.getFileMetadata().getWidth()).isEqualTo(800);
        assertThat(response.getFileMetadata().getHeight()).isEqualTo(600);
        assertThat(response.getFileMetadata().getObjectKey()).isEqualTo("covers/2026/04/old-cover.png");

        verify(coverRepository).findByCoverIdAndDeletedAtIsNull(1);
        verifyNoInteractions(eventRepository, objectStorageService);
    }

    @Test
    void getCoverById_shouldThrowException_whenCoverNotFound() {
        when(coverRepository.findByCoverIdAndDeletedAtIsNull(77)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> coverService.getCoverById(77))
                .isInstanceOf(CoverNotFoundException.class)
                .hasMessage("cover with id - 77 not found");

        verify(coverRepository).findByCoverIdAndDeletedAtIsNull(77);
        verifyNoInteractions(eventRepository, objectStorageService);
    }

    private MockMultipartFile imageFile(String fileName, int width, int height) throws IOException {
        return new MockMultipartFile(
                "file",
                fileName,
                "image/png",
                pngBytes(width, height)
        );
    }

    private byte[] pngBytes(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return outputStream.toByteArray();
    }
}
