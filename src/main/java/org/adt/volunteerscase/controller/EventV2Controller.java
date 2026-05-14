package org.adt.volunteerscase.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.dto.ErrorResponse;
import org.adt.volunteerscase.dto.event.request.EventCreateV2Request;
import org.adt.volunteerscase.dto.event.response.EventV2Response;
import org.adt.volunteerscase.dto.page.response.PageResponse;
import org.adt.volunteerscase.service.EventService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/event")
@RequiredArgsConstructor
@Validated
@Tag(name = "Event v2", description = "API мероприятий с весом в минутах")
public class EventV2Controller {

    private final EventService eventService;

    @Operation(
            summary = "создание мероприятия v2",
            description = "Создаёт новое мероприятие. В отличие от v1 принимает weightMinutes - вес мероприятия в минутах.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "мероприятие успешно создано"),
                    @ApiResponse(responseCode = "400", description = "невалидные данные", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "пользователь не координатор", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "локация, координатор, обложка или тег не найдены", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "локация или обложка уже заняты", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping("/create")
    public ResponseEntity<?> createEvent(
            @Valid @RequestBody EventCreateV2Request request
    ) {
        eventService.createEventV2(request);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "получение мероприятия v2 по id",
            description = "Возвращает мероприятие вместе с weightMinutes.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "мероприятие успешно получено", content = @Content(schema = @Schema(implementation = EventV2Response.class))),
                    @ApiResponse(responseCode = "401", description = "пользователь не авторизован",content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "мероприятие не найдено", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/{eventId}")
    public ResponseEntity<EventV2Response> getEventById(
            @Parameter(description = "ID мероприятия", example = "1")
            @PathVariable Integer eventId
    ) {
        return ResponseEntity.ok(eventService.getEventV2ById(eventId));
    }

    @Operation(
            summary = "получение всех мероприятий v2",
            description = "Возвращает страницу мероприятий с weightMinutes. Сортировка по дате мероприятия от новых к старым.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "мероприятия успешно получены"),
                    @ApiResponse(responseCode = "400", description = "невалидные параметры пагинации", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/all")
    public ResponseEntity<PageResponse<EventV2Response>> getAllEvents(
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
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "dateTimestamp")
        );

        return ResponseEntity.ok(eventService.getAllEventsV2(pageable));
    }
}