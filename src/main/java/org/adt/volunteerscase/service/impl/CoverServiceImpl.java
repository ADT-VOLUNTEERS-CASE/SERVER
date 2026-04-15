package org.adt.volunteerscase.service.impl;

import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.dto.cover.CoverMapper;
import org.adt.volunteerscase.dto.cover.CoverMetadataDTO;
import org.adt.volunteerscase.dto.cover.request.CoverCreateRequest;
import org.adt.volunteerscase.dto.cover.request.CoverPatchRequest;
import org.adt.volunteerscase.dto.cover.response.CoverResponse;
import org.adt.volunteerscase.entity.CoverEntity;
import org.adt.volunteerscase.exception.CoverInUseException;
import org.adt.volunteerscase.exception.CoverNotFoundException;
import org.adt.volunteerscase.exception.CoverUploadException;
import org.adt.volunteerscase.exception.InvalidCoverFileException;
import org.adt.volunteerscase.repository.CoverRepository;
import org.adt.volunteerscase.repository.EventRepository;
import org.adt.volunteerscase.service.CoverService;
import org.adt.volunteerscase.service.storage.ObjectStorageService;
import org.adt.volunteerscase.service.storage.StoredObjectResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CoverServiceImpl implements CoverService {

    private final CoverRepository coverRepository;
    private final EventRepository eventRepository;
    private final ObjectStorageService objectStorageService;
    private final CoverMapper coverMapper;

    @Override
    @Transactional
    public CoverResponse createCover(CoverCreateRequest request) {
        MultipartFile file = request.getFile();
        validateFile(file);

        byte[] content = readBytes(file);
        BufferedImage image = readImage(content);
        StoredObjectResult uploaded = objectStorageService.uploadCover(
                file.getOriginalFilename(),
                file.getContentType(),
                content
        );

        CoverMetadataDTO metadata = buildMetadata(file, image, uploaded);

        CoverEntity saved = coverRepository.save(
                CoverEntity.builder()
                        .link(uploaded.getLink())
                        .metadata(coverMapper.encodeMetadata(metadata))
                        .createdAt(Instant.now().toEpochMilli())
                        .deletedAt(null)
                        .build()
        );

        return coverMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CoverResponse updateCover(CoverPatchRequest request, Integer coverId) {
        CoverEntity coverEntity = getActiveCover(coverId);

        MultipartFile file = request.getFile();
        validateFile(file);

        byte[] content = readBytes(file);
        BufferedImage image = readImage(content);
        CoverMetadataDTO previousMetadata =
                coverMapper.decodeMetadata(coverEntity.getMetadata());

        StoredObjectResult uploaded = objectStorageService.uploadCover(
                file.getOriginalFilename(),
                file.getContentType(),
                content
        );

        CoverMetadataDTO newMetadata = buildMetadata(file, image, uploaded);
        String previousObjectKey = previousMetadata != null ? previousMetadata.getObjectKey() :
                null;

        try {
            coverEntity.setLink(uploaded.getLink());
            coverEntity.setMetadata(coverMapper.encodeMetadata(newMetadata));

            CoverEntity updated = coverRepository.save(coverEntity);

            if (StringUtils.hasText(previousObjectKey)) {
                objectStorageService.deleteObject(previousObjectKey);
            }

            return coverMapper.toResponse(updated);
        } catch (RuntimeException ex) {
            objectStorageService.deleteObject(uploaded.getObjectKey());
            throw ex;
        }
    }

    @Override
    @Transactional
    public void deleteCoverById(Integer coverId) {
        CoverEntity coverEntity = getActiveCover(coverId);

        if (eventRepository.existsByCover(coverEntity)) {
            throw new CoverInUseException("cover with id - " + coverId + " is used by event");
        }

        CoverMetadataDTO metadata = coverMapper.decodeMetadata(coverEntity.getMetadata());
        if (metadata != null) {
            objectStorageService.deleteObject(metadata.getObjectKey());
        }

        coverEntity.setDeletedAt(Instant.now().toEpochMilli());
        coverRepository.save(coverEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public CoverResponse getCoverById(Integer coverId) {
        return coverMapper.toResponse(getActiveCover(coverId));
    }

    private CoverEntity getActiveCover(Integer coverId) {
        return coverRepository.findByCoverIdAndDeletedAtIsNull(coverId)
                .orElseThrow(() -> new CoverNotFoundException("cover with id - " + coverId + " not found"));
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidCoverFileException("cover file is empty");
        }

        if (!StringUtils.hasText(file.getContentType()) || !
                file.getContentType().startsWith("image/")) {
            throw new InvalidCoverFileException("cover file must be an image");
        }
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new CoverUploadException("cannot read uploaded cover", ex);
        }
    }

    private BufferedImage readImage(byte[] content) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(content)) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new InvalidCoverFileException("uploaded file is not a valid image");
            }
            return image;
        } catch (IOException ex) {
            throw new InvalidCoverFileException("uploaded file is not a valid image");
        }
    }

    private CoverMetadataDTO buildMetadata(MultipartFile file, BufferedImage image,
                                           StoredObjectResult uploaded) {
        return CoverMetadataDTO.builder()
                .originalFileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .width(image.getWidth())
                .height(image.getHeight())
                .bucket(uploaded.getBucket())
                .objectKey(uploaded.getObjectKey())
                .eTag(uploaded.getETag())
                .build();
    }
}
