package org.adt.volunteerscase.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.dto.ErrorResponse;
import org.adt.volunteerscase.dto.userEvent.request.UserEventStatusPatchRequest;
import org.adt.volunteerscase.dto.userEvent.response.UserEventResponse;
import org.adt.volunteerscase.entity.user.UserDetailsImpl;
import org.adt.volunteerscase.service.UserEventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/user-event")
@RequiredArgsConstructor
public class UserEventController {

    private final UserEventService userEventService;

    @Operation(
            summary = "подача заявки на мероприятие",
            responses = {
                    @ApiResponse(responseCode = "201", description = "заявка успешно создана"),
                    @ApiResponse(responseCode = "404", description = "мероприятие не найдено", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "заявка уже существует, мероприятие не принимает заявки или достигнут лимит мест", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping("/create/{eventId}")
    public ResponseEntity<UserEventResponse> createApplication(
            @PathVariable Integer eventId,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userEventService.createApplication(eventId, currentUser.getUser().getUserId()));
    }

    @Operation(
            summary = "изменение статуса заявки на мероприятие",
            responses = {
                    @ApiResponse(responseCode = "200", description = "статус заявки успешно изменён"),
                    @ApiResponse(responseCode = "400", description = "невалидные данные", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "текущий пользователь не является координатором этого мероприятия", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "мероприятие, пользователь или заявка не найдены", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "мероприятие не принимает заявки или достигнут лимит мест", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )

    @SecurityRequirement(name = "jwtAuth")
    @PatchMapping("/update/status/{eventId}/{userId}")
    public ResponseEntity<UserEventResponse> updateApplicationStatus(
            @PathVariable Integer eventId,
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

    @Operation(
            summary = "получение статуса своей заявки на мероприятие",
            responses = {
                    @ApiResponse(responseCode = "200", description = "статус заявки успешно получен"),
                    @ApiResponse(responseCode = "404", description = "мероприятие или заявка не найдены", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/status/{eventId}")
    public ResponseEntity<UserEventResponse> getMyApplicationStatus(
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
}