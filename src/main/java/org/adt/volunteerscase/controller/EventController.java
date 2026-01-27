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
import org.adt.volunteerscase.dto.event.request.EventCreateRequest;
import org.adt.volunteerscase.dto.event.request.EventPatchRequest;
import org.adt.volunteerscase.dto.event.request.EventStatusPatchRequest;
import org.adt.volunteerscase.dto.event.response.GetAllResponse;
import org.adt.volunteerscase.dto.event.response.PatchResponse;
import org.adt.volunteerscase.dto.page.response.PageResponse;
import org.adt.volunteerscase.service.EventService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
                    @ApiResponse(responseCode = "204", description = "успешно создано"),
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
        return ResponseEntity.noContent().build();
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
                    @ApiResponse(responseCode = "204", description = "статус успешно изменён"),
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
        return ResponseEntity.noContent().build();
    }


    @Operation(
            summary = "эндпоинт для удаления мероприятия",
            responses = {
                    @ApiResponse(responseCode = "204", description = "успешно удалено"),
                    @ApiResponse(responseCode = "404", description = "по данному айди мероприятие не найдено", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @DeleteMapping("/delete/{eventId}")
    public ResponseEntity<?> deleteEvent(@PathVariable Integer eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }


    @Operation(
            summary = "получение всех событий",
            description = "реализована пагинация, выводятся самые новые(по дате) события",
            responses = {
                    @ApiResponse(responseCode = "200", description = "данные получены"),
                    @ApiResponse(responseCode = "400", description = "невалидные данные")
            }
    )
    @GetMapping("/all")
    @SecurityRequirement(name = "jwtAuth")
    public ResponseEntity<PageResponse<GetAllResponse>> getAllEvents(

            @Parameter(description = "Номер страницы, начиная с 0", example = "0")
            @Min(value = 0, message = "Page number must be greater than or equal to 0")
            @RequestParam(defaultValue = "0")
            int page,

            @Parameter(description = "Количество элементов на странице", example = "10")
            @Min(value = 1, message = "Page size must be at least 1")
            @Max(value = 100, message = "Page size must not exceed 100")
            @RequestParam(defaultValue = "10")
            int size
    ){
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC,"dateTimestamp")
        );

        return ResponseEntity.ok().body(eventService.getAllEvents(pageable));
    }
}
