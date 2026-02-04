package org.adt.volunteerscase.dto.tag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TagEntityDTO {

    private Integer tagId;
    private String tagName;

}
