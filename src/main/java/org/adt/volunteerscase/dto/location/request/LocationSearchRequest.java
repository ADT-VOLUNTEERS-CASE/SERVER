package org.adt.volunteerscase.dto.location.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationSearchRequest {

    @NotBlank(message = "address is blank")
    private String address;

}
