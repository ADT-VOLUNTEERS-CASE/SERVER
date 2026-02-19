package org.adt.volunteerscase.service;

import org.adt.volunteerscase.dto.user.request.UpdateCoordinatorRequest;
import org.adt.volunteerscase.dto.user.response.GetUserResponse;
import org.adt.volunteerscase.entity.user.UserEntity;

public interface UserService {
    GetUserResponse updateCoordinatorById(UpdateCoordinatorRequest request, Integer userId);

    GetUserResponse updateCoordinatorByEmail(UpdateCoordinatorRequest request, String email);

    void deleteCoordinatorById(Integer userId);

    void deleteCoordinatorByEmail(String email);

    GetUserResponse getCurrentUser(UserEntity currentUser);
}
