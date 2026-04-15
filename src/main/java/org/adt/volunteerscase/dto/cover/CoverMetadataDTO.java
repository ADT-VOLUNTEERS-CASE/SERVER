package org.adt.volunteerscase.dto.cover;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoverMetadataDTO {
    private String originalFileName;
    private String contentType;
    private Long size;
    private Integer width;
    private Integer height;
    private String bucket;
    private String objectKey;
    private String eTag;
}
