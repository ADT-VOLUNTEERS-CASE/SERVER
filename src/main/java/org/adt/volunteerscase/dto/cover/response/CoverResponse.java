package org.adt.volunteerscase.dto.cover.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.adt.volunteerscase.dto.cover.CoverMetadataDTO;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoverResponse {
    private Integer coverId;
    private String link;
    private String metadata;
    private Long createdAt;
    private Long deletedAt;
    private CoverMetadataDTO fileMetadata;
}