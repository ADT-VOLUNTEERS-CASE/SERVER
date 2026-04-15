package org.adt.volunteerscase.dto.cover;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.dto.cover.response.CoverResponse;
import org.adt.volunteerscase.entity.CoverEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class CoverMapper {

    private final ObjectMapper objectMapper;

    public String encodeMetadata(CoverMetadataDTO metadata) {
        try {
            return
                    Base64.getEncoder().encodeToString(objectMapper.writeValueAsBytes(metadata));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("cannot encode cover metadata", ex);
        }
    }

    public CoverMetadataDTO decodeMetadata(String encodedMetadata) {
        if (!StringUtils.hasText(encodedMetadata)) {
            return null;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(encodedMetadata);
            return objectMapper.readValue(decoded, CoverMetadataDTO.class);
        } catch (IOException ex) {
            throw new IllegalStateException("cannot decode cover metadata", ex);
        }
    }

    public CoverResponse toResponse(CoverEntity cover) {
        if (cover == null) {
            return null;
        }

        return CoverResponse.builder()
                .coverId(cover.getCoverId())
                .link(cover.getLink())
                .metadata(cover.getMetadata())
                .createdAt(cover.getCreatedAt())
                .deletedAt(cover.getDeletedAt())
                .fileMetadata(decodeMetadata(cover.getMetadata()))
                .build();
    }

    public CoverEntityDTO toDto(CoverEntity cover) {
        if (cover == null) {
            return null;
        }

        return CoverEntityDTO.builder()
                .coverId(cover.getCoverId())
                .link(cover.getLink())
                .metadata(cover.getMetadata())
                .createdAt(cover.getCreatedAt())
                .deletedAt(cover.getDeletedAt())
                .fileMetadata(decodeMetadata(cover.getMetadata()))
                .build();
    }
}
