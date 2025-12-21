package org.adt.volunteerscase.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequest {

    @Email
    @NotBlank(message = "email is blank")
    @Schema(description = "Email пользователя")
    private String email;

    @NotBlank(message = "password is blank")
    @Schema(description = "пароль пользователя")
    String password;

}
