package org.adt.volunteerscase.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.dto.ErrorResponse;
import org.adt.volunteerscase.dto.user.response.GetUserV2Response;
import org.adt.volunteerscase.entity.user.UserDetailsImpl;
import org.adt.volunteerscase.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/user")
@RequiredArgsConstructor
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
}
