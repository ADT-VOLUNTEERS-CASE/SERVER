package org.adt.volunteerscase.dto.location;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationEntityDTO {

    private Integer locationId;
    private String address;
    private Double latitude;
    private Double longitude;

}

