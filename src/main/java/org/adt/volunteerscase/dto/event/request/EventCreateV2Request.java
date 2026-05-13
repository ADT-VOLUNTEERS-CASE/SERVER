package org.adt.volunteerscase.dto.event.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventCreateV2Request {

    @Schema(description = "Название мероприятия")
    @NotBlank(message = "name is null")
    private String name;

    @Schema(description = "Статус мероприятия", example = "ONGOING")
    @NotNull(message = "Status cannot be null")
    @Pattern(regexp = "^(ONGOING|IN_PROGRESS|COMPLETED)$",
            message = "Status must be one of: ONGOING, IN_PROGRESS, COMPLETED")
    private String status;

    @Schema(description = "Описание мероприятия")
    @Size(max = 5000, message = "Description max length is 5000")
    private String description;

    @Schema(description = "ID обложки")
    private Integer coverId;

    @Schema(description = "ID координатора")
    @Positive(message = "coordinator id must be positive")
    @NotNull(message = "coordinator id is null")
    private Integer coordinatorId;

    @Schema(description = "Максимальное количество участников")
    @Min(value = 1, message = "Max capacity must be greater than 0")
    @NotNull(message = "maxCapacity is blank")
    private Integer maxCapacity;

    @Schema(description = "Вес мероприятия в минутах", example = "120")
    @Min(value = 1, message = "Weight minutes must be greater than 0")
    @NotNull(message = "weightMinutes is blank")
    private Integer weightMinutes;

    @Schema(description = "Дата и время мероприятия")
    @NotNull(message = "data is null")
    private LocalDateTime dateTimestamp;

    @Schema(description = "ID локации")
    @NotNull(message = "location id is null")
    private Integer locationId;

    @Schema(description = "ID тегов")
    private Set<Integer> tagIds;
}