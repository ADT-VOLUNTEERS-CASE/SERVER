package org.adt.volunteerscase.dto.user.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.adt.volunteerscase.dto.tag.TagEntityDTO;
import org.adt.volunteerscase.dto.tag.response.TagGetResponse;
import org.adt.volunteerscase.entity.TagEntity;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetUserResponse {

    private Integer id;

    private String firstname;
    private String lastname;
    private String patronymic;

    private String phoneNumber;
    private String email;

    private boolean isAdmin;
    private boolean isCoordinator;

    private Set<TagEntityDTO> tags;
}
