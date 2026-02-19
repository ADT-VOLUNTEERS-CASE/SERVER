package org.adt.volunteerscase.dto.user.request;

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
public class UpdateCoordinatorRequest {

    @Size(max = 100, message = "First name max length is 100")
    private String firstname;

    @Size(max = 100, message = "Last name max length is 100")
    private String lastname;

    @Size(max = 100, message = "Patronymic max length is 100")
    private String patronymic;

    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Phone must be in E.164 format")
    private String phoneNumber;

    @Email(message = "incorrect email format")
    @Size(max = 255, message = "Email max length is 255")
    private String email;

}
