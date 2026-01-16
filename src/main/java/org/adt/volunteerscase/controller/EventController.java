package org.adt.volunteerscase.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.dto.ErrorResponse;
import org.adt.volunteerscase.dto.event.request.EventCreateRequest;
import org.adt.volunteerscase.dto.event.request.EventPatchRequest;
import org.adt.volunteerscase.dto.event.request.EventStatusPatchRequest;
import org.adt.volunteerscase.dto.event.response.PatchResponse;
import org.adt.volunteerscase.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/event")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @Operation(
            summary = "эндпоинт для создания нового мероприятия",
            responses = {
                    @ApiResponse(responseCode = "200", description = "успешно создано"),
                    @ApiResponse(responseCode = "404", description = "по данным id не найдена запись мероприятия или обложка, подробнее в сообщении ошибки", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "400", description = "невалидные данные", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "введённые локация или обложка уже заняты", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping("/create")
    public ResponseEntity<?> createEvent(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "запрос для создания нового мероприятия"
            )
            @RequestBody EventCreateRequest request) {
        eventService.createEvent(request);
        return ResponseEntity.ok().build();
    }


    @Operation(
            summary = "эндпоинт для обновления полей мероприятия",
            description = "обновить только поля, которым были переданы данные",
            responses = {
                    @ApiResponse(responseCode = "200", description = "данные успешно обновлены", content = @Content(schema = @Schema(implementation = PatchResponse.class))),
                    @ApiResponse(responseCode = "404", description = "по данному id не найдено мероприятие или обложка, или теги, подробнее смотрите в полученной ошибке", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "400", description = "невалидные данные", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "введённые локация или обложка уже заняты", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))

            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @PatchMapping("/update/{eventId}")
    public ResponseEntity<?> updateEvent(
            @PathVariable Integer eventId,
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "данные для обновления полей мероприятия, если какое-то поле пустое, оно не будет изменено"
            )
            @RequestBody EventPatchRequest request
    ) {
        return ResponseEntity.ok(eventService.patchEvent(eventId, request));
    }

    @Operation(
            summary = "эндпоинт для обновления статуса мероприятия",
            responses = {
                    @ApiResponse(responseCode = "200", description = "статус успешно изменён"),
                    @ApiResponse(responseCode = "404", description = "по данному айди не найдено мероприятие", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "400", description = "невалидные данные", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @PatchMapping("/update/status/{eventId}")
    public ResponseEntity<?> updateEventStatus(
            @PathVariable Integer eventId,
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "запрос для обновления статуса"
            ) @RequestBody EventStatusPatchRequest request
    ) {
        eventService.updateStatus(eventId, request);
        return ResponseEntity.ok().build();
    }


    @Operation(
            summary = "эндпоинт для удаления мероприятия",
            responses = {
                    @ApiResponse(responseCode = "200", description = "успешно удалено"),
                    @ApiResponse(responseCode = "404", description = "по данному айди мероприятие не найдено", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @DeleteMapping("/delete/{eventId}")
    public ResponseEntity<?> deleteEvent(@PathVariable Integer eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.ok().build();
    }
}
