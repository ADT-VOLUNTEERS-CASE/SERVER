package org.adt.volunteerscase.dto.coordinator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoordinatorEntityDTO {

    private Integer userId;
    private String workLocation;

}
