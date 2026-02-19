package org.adt.volunteerscase.dto.cover.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoverPatchRequest {

    @Size(min = 1, message = "link is blank")
    private String link;

    @Min(value = 1, message = "width must be greater than 0")
    private Integer width;

    @Min(value = 1, message = "height must be greater than 0")
    private Integer height;

}
