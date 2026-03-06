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


    @Size(min = 1, max = 100, message = "First name min length is 1, max length is 100")
    private String firstname;

    @Size(min = 1, max = 100, message = "Last name min length is 1, max length is 100")
    private String lastname;

    @Size(min = 1, max = 100, message = "Patronymic min length is 1, max length is 100")
    private String patronymic;

    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Phone must be in E.164 format")
    private String phoneNumber;

    @Size(min = 1, max = 255, message = "Email length min is 1, max is 255")
    @Email(message = "incorrect email format")
    private String email;

}
