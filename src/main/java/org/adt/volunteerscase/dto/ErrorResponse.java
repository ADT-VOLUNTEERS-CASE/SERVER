package org.adt.volunteerscase.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Ответ с информацией для ошибки")
public class ErrorResponse {

    @Schema(description = "Код ошибки")
    private String errorCode;

    @Schema(description = "Сообщение об ошибке")
    private String message;

    @Schema(description = "Время, когда произошла ошибка")
    private LocalDateTime time;

}
