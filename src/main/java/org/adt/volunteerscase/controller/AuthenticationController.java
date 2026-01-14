package org.adt.volunteerscase.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.dto.auth.AuthenticationRequest;
import org.adt.volunteerscase.dto.auth.AuthenticationResponse;
import org.adt.volunteerscase.dto.auth.RegisterRequest;
import org.adt.volunteerscase.dto.auth.TokenRefreshRequest;
import org.adt.volunteerscase.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.adt.volunteerscase.dto.ErrorResponse;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API для аутентификации")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    /**
     * Registers a new user and returns JWT authentication data.
     *
     * @param request registration request containing user credentials and profile details
     * @return an AuthenticationResponse containing access and refresh tokens and associated user information
     */
    @Operation(
            summary = "регистрация нового пользователя",
            description = "Создаёт нового пользователя  и возвращает jwt токены",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешная регистрация"),
                    @ApiResponse(responseCode = "400", description = "Невалидные данные", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Пользователь(с таким email или номером телефона) уже существует", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для регистрации нового пользователя",
                    required = true
            )
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @Operation(
            summary = "регистрация нового пользователя с ролью координатор",
            description = "Создаёт нового пользователя с ролью координатор и возвращает jwt токены",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешная регистрация"),
                    @ApiResponse(responseCode = "400", description = "Невалидные данные", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Пользователь(с таким email или номером телефона) уже существует", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))}
    )
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping("/register/coordinator")
    public ResponseEntity<AuthenticationResponse> registerCoordinator(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для регистрации нового пользователя",
                    required = true
            )
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authenticationService.registerCoordinator(request));
    }
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping("/register/admin")
    public ResponseEntity<AuthenticationResponse> registerAdmin(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для регистрации нового пользователя",
                    required = true
            )
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authenticationService.registerAdmin(request));
    }

    /**
     * Authenticate a user and issue JWT access and refresh tokens.
     *
     * @param request the user's authentication credentials (e.g., email and password)
     * @return an AuthenticationResponse containing issued access and refresh tokens
     */
    @Operation(
            summary = "аутентификация пользователя",
            description = "Возвращает jwt токены, для доступа к api",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешная аутентификация"),
                    @ApiResponse(responseCode = "400", description = "Невалидные данные",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Неверный пароль",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Нет пользователя с таким email",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authentication(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Учетные данные пользователя"
            )
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    /**
     * Refreshes JWT authentication tokens.
     * <p>
     * Exchanges a valid refresh token for a new access token and refresh token pair.
     *
     * @param request the token refresh payload containing the refresh token
     * @return the new access and refresh tokens and related authentication data in an AuthenticationResponse wrapped in a ResponseEntity
     */
    @Operation(
            summary = "Обновление токенов",
            description = "Возвращает новую пару jwt токенов",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешное обновление токенов"),
                    @ApiResponse(responseCode = "400", description = "Невалидные данные",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Переданный refresh token недействителен",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping("/refreshtoken")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Токен обновления пользователя"
            )
            @RequestBody TokenRefreshRequest request
    ) {
        return ResponseEntity.ok(authenticationService.refreshToken(request));
    }
}