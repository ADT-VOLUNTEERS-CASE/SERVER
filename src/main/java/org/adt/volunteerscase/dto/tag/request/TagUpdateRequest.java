package org.adt.volunteerscase.dto.tag.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TagUpdateRequest {

    @NotBlank(message = "tag is blank")
    private String tagName;

}

