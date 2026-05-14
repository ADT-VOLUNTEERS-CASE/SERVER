
package org.adt.volunteerscase.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.dto.ErrorResponse;
import org.adt.volunteerscase.dto.page.response.PageResponse;
import org.adt.volunteerscase.dto.rating.response.CoordinatorRatingResponse;
import org.adt.volunteerscase.dto.rating.response.UserRatingResponse;
import org.adt.volunteerscase.entity.rating.RatingPeriod;
import org.adt.volunteerscase.service.RatingService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/user")
@RequiredArgsConstructor
@Validated
@Tag(name = "Rating", description = "API рейтингов пользователей и координаторов")
public class RatingController {

    private final RatingService ratingService;

    @Operation(
            summary = "получение рейтинга пользователей",
            description = "Возвращает отсортированный рейтинг пользователей по количеству отработанных минут. period может быть monthly или overall.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "рейтинг пользователей успешно получен"),
                    @ApiResponse(responseCode = "400", description = "невалидные параметры запроса", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/rating")
    public ResponseEntity<PageResponse<UserRatingResponse>> getUserRating(
            @Parameter(description = "Период рейтинга: monthly или overall", example = "monthly")
            @RequestParam(defaultValue = "monthly")
            String period,

            @Parameter(description = "Номер страницы, начиная с 0", example = "0")
            @Min(value = 0, message = "Page number must be greater than or equal to 0")
            @RequestParam(defaultValue = "0")
            int page,

            @Parameter(description = "Количество элементов на странице", example = "10")
            @Min(value = 1, message = "Page size must be at least 1")
            @Max(value = 100, message = "Page size must not exceed 100")
            @RequestParam(defaultValue = "10")
            int size
    ) {
        RatingPeriod ratingPeriod = RatingPeriod.fromRequest(period);
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(ratingService.getUserRating(ratingPeriod, pageable));
    }

    @Operation(
            summary = "получение рейтинга координаторов",
            description = "Возвращает отсортированный рейтинг координаторов по суммарному весу завершённых мероприятий. period может быть monthly или overall.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "рейтинг координаторов успешно получен"),
                    @ApiResponse(responseCode = "400", description = "невалидные параметры запроса", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/coordinator/rating")
    public ResponseEntity<PageResponse<CoordinatorRatingResponse>> getCoordinatorRating(
            @Parameter(description = "Период рейтинга: monthly или overall", example = "monthly")
            @RequestParam(defaultValue = "monthly")
            String period,

            @Parameter(description = "Номер страницы, начиная с 0", example = "0")
            @Min(value = 0, message = "Page number must be greater than or equal to 0")
            @RequestParam(defaultValue = "0")
            int page,

            @Parameter(description = "Количество элементов на странице", example = "10")
            @Min(value = 1, message = "Page size must be at least 1")
            @Max(value = 100, message = "Page size must not exceed 100")
            @RequestParam(defaultValue = "10")
            int size
    ) {
        RatingPeriod ratingPeriod = RatingPeriod.fromRequest(period);
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(ratingService.getCoordinatorRating(ratingPeriod, pageable));
    }
}
