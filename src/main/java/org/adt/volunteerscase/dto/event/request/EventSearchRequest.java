package org.adt.volunteerscase.dto.event.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventSearchRequest {

    @NotBlank(message = "name is blank")
    @Size(max = 255, message = "name max length is 255")
    private String name;

}