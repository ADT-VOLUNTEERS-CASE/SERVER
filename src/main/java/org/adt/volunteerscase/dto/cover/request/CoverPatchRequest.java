package org.adt.volunteerscase.dto.cover.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoverPatchRequest {

    @Pattern(regexp = ".*\\S.*", message = "link must not be blank")
    @Size(max = 500, message = "link max length is 500")
    @URL(message = "invalid link")
    private String link;

    @NotNull(message = "height is null")
    @Min(value = 1, message = "width must be greater than 0")
    private Integer width;

    @NotNull(message = "height is null")
    @Min(value = 1, message = "height must be greater than 0")
    private Integer height;

}
