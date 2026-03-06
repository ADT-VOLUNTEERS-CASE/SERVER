package org.adt.volunteerscase.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.dto.ErrorResponse;
import org.adt.volunteerscase.dto.user.request.UpdateCoordinatorRequest;
import org.adt.volunteerscase.dto.user.response.GetUserResponse;
import org.adt.volunteerscase.entity.user.UserDetailsImpl;
import org.adt.volunteerscase.entity.user.UserEntity;
import org.adt.volunteerscase.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "обновление информации о координаторе по его id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "успешно обновлено"),
                    @ApiResponse(responseCode = "409", description = "пользователь с таким email или номером телефона уже существует", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "400", description = "некорректный формат json или некорректное заполнение полей json", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "пользователь, данные которого хотят изменить не является координатором", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "пользователь с таким id не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @PatchMapping("/coordinator/id/{userId}")
    public ResponseEntity<GetUserResponse> updateCoordinatorById(
            @Valid @RequestBody UpdateCoordinatorRequest request,
            @PathVariable Integer userId
    ) {
        return ResponseEntity.ok().body(userService.updateCoordinatorById(request, userId));
    }

    @Operation(
            summary = "обновление информации о координаторе по его email",
            responses = {
                    @ApiResponse(responseCode = "200", description = "успешно обновлено"),
                    @ApiResponse(responseCode = "409", description = "пользователь с таким email или номером телефона уже существует", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "400", description = "некорректный формат json или некорректное заполнение полей json", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "пользователь, данные которого хотят изменить не является координатором", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "пользователь с таким email не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @PatchMapping("/coordinator/email/{email}")
    public ResponseEntity<GetUserResponse> updateCoordinatorByEmail(
            @Valid @RequestBody UpdateCoordinatorRequest request,
            @PathVariable String email
    ) {
        return ResponseEntity.ok().body(userService.updateCoordinatorByEmail(request, email));
    }

    @Operation(
            summary = "удаление пользователя(только координатора) по его id",
            responses = {
                    @ApiResponse(responseCode = "204", description = "успешно удалён"),
                    @ApiResponse(responseCode = "404", description = "пользователь с таким id не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "пользователь, данные которого хотят изменить не является координатором", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @DeleteMapping("/coordinator/id/{userId}")
    public ResponseEntity<?> deleteUserById(
            @PathVariable Integer userId
    ) {
        userService.deleteCoordinatorById(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "удаление пользователя(только координатора) по его email",
            responses = {
                    @ApiResponse(responseCode = "204", description = "успешно удалён"),
                    @ApiResponse(responseCode = "404", description = "пользователь с таким email не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "пользователь, данные которого хотят изменить не является координатором", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @DeleteMapping("/coordinator/email/{email}")
    public ResponseEntity<?> deleteUserByEmail(
            @PathVariable String email
    ) {
        userService.deleteCoordinatorByEmail(email);
        return ResponseEntity.noContent().build();
    }


    @Operation(
            summary = "получение информации о текущем пользователе",
            responses = {
                    @ApiResponse(responseCode = "200", description = "успешно")
            }
    )
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/me")
    public ResponseEntity<GetUserResponse> getCurrentUser(
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        return ResponseEntity.ok().body(userService.getCurrentUser(currentUser.getUser()));
    }
}
