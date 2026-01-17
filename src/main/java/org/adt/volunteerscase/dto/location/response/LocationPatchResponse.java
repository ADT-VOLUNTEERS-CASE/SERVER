package org.adt.volunteerscase.dto.location.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationPatchResponse {

    private Integer locationId;
    private String address;
    private Double longitude;
    private Double latitude;
    private String additionalNotes;

}
