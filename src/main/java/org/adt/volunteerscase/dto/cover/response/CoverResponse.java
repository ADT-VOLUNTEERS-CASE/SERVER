package org.adt.volunteerscase.dto.cover.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoverResponse {

    private Integer coverId;

    private String link;

    private Integer width;

    private Integer height;

}
