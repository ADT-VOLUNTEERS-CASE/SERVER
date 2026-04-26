package org.adt.volunteerscase.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.dto.ErrorResponse;
import org.adt.volunteerscase.dto.page.response.PageResponse;
import org.adt.volunteerscase.dto.userEvent.request.CoordinatorApplicationFilterRequest;
import org.adt.volunteerscase.dto.userEvent.request.UserEventStatusPatchRequest;
import org.adt.volunteerscase.dto.userEvent.response.CoordinatorApplicationResponse;
import org.adt.volunteerscase.dto.userEvent.response.CoordinatorEventApplicationsSummaryResponse;
import org.adt.volunteerscase.dto.userEvent.response.UserEventResponse;
import org.adt.volunteerscase.entity.user.UserDetailsImpl;
import org.adt.volunteerscase.service.UserEventService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/user-event")
@RequiredArgsConstructor
@Validated
public class UserEventController {

    private final UserEventService userEventService;

    @Operation(
            summary = "Подача заявки на мероприятие",
            description = "Создаёт заявку текущего авторизованного пользователя на мероприятие.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Заявка успешно создана"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Мероприятие или пользователь не найдены", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Активная заявка уже существует, мероприятие не принимает заявки или достигнут лимит мест", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping("/create/{eventId}")
    public ResponseEntity<UserEventResponse> createApplication(
            @Parameter(description = "ID мероприятия", example = "1")
            @PathVariable Integer eventId,

            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userEventService.createApplication(eventId, currentUser.getUser().getUserId()));
    }

    @Operation(
            summary = "Получение статуса своей заявки на мероприятие",
            description = "Возвращает статус заявки текущего авторизованного пользователя на указанное мероприятие.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Статус заявки успешно получен"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Мероприятие, пользователь или заявка не найдены", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/status/{eventId}")
    public ResponseEntity<UserEventResponse> getMyApplicationStatus(
            @Parameter(description = "ID мероприятия", example = "1")
            @PathVariable Integer eventId,

            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        return ResponseEntity.ok(
                userEventService.getMyApplicationStatus(
                        eventId,
                        currentUser.getUser().getUserId()
                )
        );
    }

    @Operation(
            summary = "Получение мероприятий текущего координатора со сводкой по заявкам",
            description = "Возвращает только те мероприятия, к которым привязан текущий координатор. Для каждого мероприятия возвращается количество заявок по статусам.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список мероприятий координатора успешно получен"),
                    @ApiResponse(responseCode = "400", description = "Некорректные параметры пагинации", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Пользователь не имеет роли координатора", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/coordinator/events")
    public ResponseEntity<PageResponse<CoordinatorEventApplicationsSummaryResponse>>
    getMyCoordinatorEvents(
            @Parameter(description = "Номер страницы, начиная с 0", example = "0")
            @Min(value = 0, message = "Page number must be greater than or equal to 0")
            @RequestParam(defaultValue = "0")
            int page,

            @Parameter(description = "Количество элементов на странице", example = "10")
            @Min(value = 1, message = "Page size must be at least 1")
            @Max(value = 100, message = "Page size must not exceed 100")
            @RequestParam(defaultValue = "10")
            int size,

            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateTimestamp"));

        return ResponseEntity.ok(
                userEventService.getMyEventApplicationSummaries(
                        currentUser.getUser().getUserId(),
                        pageable
                )
        );
    }

    @Operation(
            summary = "Получение заявок на мероприятие текущего координатора",
            description = "Возвращает заявки только по тому мероприятию, к которому привязан текущий координатор. Можно фильтровать заявки по статусу: PENDING, ACCEPTED, REJECTED, REVOKED.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список заявок успешно получен"),
                    @ApiResponse(responseCode = "400", description = "Некорректный статус фильтра или параметры пагинации", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Пользователь не имеет роли координатора или не является координатором указанного мероприятия", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Мероприятие не найдено", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/coordinator/events/{eventId}/applications")
    public ResponseEntity<PageResponse<CoordinatorApplicationResponse>> getApplicationsForMyEvent(
            @Parameter(description = "ID мероприятия", example = "1")
            @PathVariable Integer eventId,

            @Valid @ModelAttribute CoordinatorApplicationFilterRequest filter,

            @Parameter(description = "Номер страницы, начиная с 0", example = "0")
            @Min(value = 0, message = "Page number must be greater than or equal to 0")
            @RequestParam(defaultValue = "0")
            int page,

            @Parameter(description = "Количество элементов на странице", example = "20")
            @Min(value = 1, message = "Page size must be at least 1")
            @Max(value = 100, message = "Page size must not exceed 100")
            @RequestParam(defaultValue = "20")
            int size,

            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(
                userEventService.getApplicationsForMyEvent(
                        eventId,
                        filter,
                        currentUser.getUser().getUserId(),
                        pageable
                )
        );
    }

    @Operation(
            summary = "Изменение статуса заявки координатором",
            description = "Основной endpoint для координатора. Позволяет принять или отклонить заявку на мероприятие. Координатор может менять заявки только на своих мероприятиях.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Статус заявки успешно изменён"),
                    @ApiResponse(responseCode = "400", description = "Некорректное тело запроса: статус не ACCEPTED/REJECTED или причина отказа невалидна", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Пользователь не имеет роли координатора или не является координатором указанного мероприятия", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Мероприятие, пользователь или заявка не найдены", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Заявка отозвана, мероприятие не принимает заявки или достигнут лимит мест", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @PatchMapping("/coordinator/events/{eventId}/applications/{userId}/status")
    public ResponseEntity<UserEventResponse> updateCoordinatorApplicationStatus(
            @Parameter(description = "ID мероприятия", example = "1")
            @PathVariable Integer eventId,

            @Parameter(description = "ID пользователя, подавшего заявку", example = "10")
            @PathVariable Integer userId,

            @Valid @RequestBody UserEventStatusPatchRequest request,

            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        return ResponseEntity.ok(
                userEventService.updateApplicationStatus(
                        eventId,
                        userId,
                        request,
                        currentUser.getUser().getUserId()
                )
        );
    }
}