package org.adt.volunteerscase.dto.cover.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CoverCreateRequest {

    @NotBlank(message = "link is blank")
    private String link;

    @NotNull(message = "width is blank")
    @Min(value = 1, message = "width must be greater than 0")
    private Integer width;

    @NotNull(message = "height is blank")
    @Min(value = 1, message = "height must be greater than 0")
    private Integer height;
}
