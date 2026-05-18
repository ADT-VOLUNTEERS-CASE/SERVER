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
import org.adt.volunteerscase.dto.user.response.GetUserV2Response;
import org.adt.volunteerscase.dto.user.response.RegisteredEventResponse;
import org.adt.volunteerscase.entity.user.UserDetailsImpl;
import org.adt.volunteerscase.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/user")
@RequiredArgsConstructor
@Validated
@Tag(name = "User v2", description = "API пользователя v2")
public class UserV2Controller {

    private final UserService userService;

    @Operation(
            summary = "получение информации о текущем пользователе v2",
            description = "Возвращает данные текущего пользователя, включая monthlyRating и overallRating.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "данные пользователя успешно получены", content = @Content(schema = @Schema(implementation = GetUserV2Response.class))),
                    @ApiResponse(responseCode = "401", description = "пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "пользователь не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/me")
    public ResponseEntity<GetUserV2Response> getCurrentUser(
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        return ResponseEntity.ok(userService.getCurrentUserV2(currentUser.getUser()));
    }

    @Operation(
            summary = "получение мероприятий, на которые зарегистрирован текущий пользователь",
            description = "Возвращает страницу мероприятий, по которым у текущего пользователя есть активная запись в user_events. В ответе есть статус мероприятия и статус заявки пользователя.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "мероприятия пользователя успешно получены"),
                    @ApiResponse(responseCode = "400", description = "невалидные параметры пагинации", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "пользователь не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/registered-events")
    public ResponseEntity<PageResponse<RegisteredEventResponse>> getRegisteredEvents(
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
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(userService.getRegisteredEvents(currentUser.getUser(), pageable));
    }
}
