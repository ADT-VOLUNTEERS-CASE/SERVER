package org.adt.volunteerscase.dto.cover;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoverEntityDTO {
    private Integer coverId;
    private String link;
    private String metadata;
    private Long createdAt;
    private Long deletedAt;
    private CoverMetadataDTO fileMetadata;
}


