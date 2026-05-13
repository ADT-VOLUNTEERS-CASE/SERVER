package org.adt.volunteerscase.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.dto.ErrorResponse;
import org.adt.volunteerscase.dto.statistics.response.CoordinatorStatisticsResponse;
import org.adt.volunteerscase.dto.statistics.response.UserStatisticsResponse;
import org.adt.volunteerscase.entity.user.UserDetailsImpl;
import org.adt.volunteerscase.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/user")
@RequiredArgsConstructor
@Tag(name = "User statistics", description = "API статистики пользователей и координаторов")
public class UserStatisticsController {

    private final StatisticsService statisticsService;

    @Operation(
            summary = "получение статистики текущего пользователя",
            description = "Возвращает количество участий, отработанные минуты за месяц и за всё время, текущий и максимальный месячный стрик.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "статистика успешно получена",
                            content = @Content(schema = @Schema(implementation = UserStatisticsResponse.class))),
                    @ApiResponse(responseCode = "401", description = "пользователь не авторизован",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "пользователь не найден",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/statistics")
    public ResponseEntity<UserStatisticsResponse> getUserStatistics(
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        return ResponseEntity.ok(statisticsService.getUserStatistics(currentUser.getUser().getUserId()));
    }

    @Operation(
            summary = "получение статистики текущего координатора",
            description = "Возвращает количество проведённых мероприятий, общее количество принятых участников и суммарный вес завершённых мероприятий в минутах.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "статистика успешно получена",
                            content = @Content(schema = @Schema(implementation = CoordinatorStatisticsResponse.class))),
                    @ApiResponse(responseCode = "401", description = "пользователь не авторизован",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "пользователь не координатор",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "координатор не найден",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/coordinator/statistics")
    public ResponseEntity<CoordinatorStatisticsResponse> getCoordinatorStatistics(
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        return ResponseEntity.ok(statisticsService.getCoordinatorStatistics(currentUser.getUser().getUserId()));
    }
}
