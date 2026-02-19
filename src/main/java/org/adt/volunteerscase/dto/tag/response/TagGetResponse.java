package org.adt.volunteerscase.dto.tag.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TagGetResponse {

    private Integer tagId;
    private String tagName;

}
