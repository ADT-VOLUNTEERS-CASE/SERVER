package org.adt.volunteerscase.dto.auth;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Firstname is null")
    @Size(max = 100, message = "First name max length is 100")
    private String firstname;

    @NotBlank(message = "Lastname is null")
    @Size(max = 100, message = "Last name max length is 100")
    private String lastname;

    @Size(max = 100, message = "Patronymic max length is 100")
    private String patronymic;

    @NotBlank(message = "Phone number is null")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Phone must be in E.164 format")
    private String phoneNumber;

    @NotBlank(message = "Email is null")
    @Email(message = "incorrect email format")
    @Size(max = 255, message = "Email max length is 255")
    private String email;

    @NotBlank(message = "Password is blank")
    private String password;

}
