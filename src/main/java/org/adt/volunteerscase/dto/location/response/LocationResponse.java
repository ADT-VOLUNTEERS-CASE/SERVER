package org.adt.volunteerscase.dto.location.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationResponse {

    private Integer locationId;
    private String address;
    private String additionalNotes;
    private Double latitude;
    private Double longitude;

}
