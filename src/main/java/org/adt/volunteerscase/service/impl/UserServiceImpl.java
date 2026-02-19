package org.adt.volunteerscase.service.impl;

import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.dto.user.request.UpdateCoordinatorRequest;
import org.adt.volunteerscase.dto.user.response.GetUserResponse;
import org.adt.volunteerscase.entity.user.UserEntity;
import org.adt.volunteerscase.exception.UserAlreadyExistsException;
import org.adt.volunteerscase.exception.UserNotCoordinatorException;
import org.adt.volunteerscase.exception.UserNotFoundException;
import org.adt.volunteerscase.repository.UserRepository;
import org.adt.volunteerscase.service.UserService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public GetUserResponse updateCoordinatorById(UpdateCoordinatorRequest request, Integer userId) {

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("user with id - " + userId + " not found"));

        if (!userEntity.isCoordinator()) {
            throw new UserNotCoordinatorException("The user with Id - " + userId + " that you want to change is not a coordinator");
        }
        if (request.getFirstname() != null) {
            userEntity.setFirstname(request.getFirstname());
        }

        if (request.getLastname() != null) {
            userEntity.setLastname(request.getLastname());
        }

        if (request.getPatronymic() != null) {
            userEntity.setPatronymic(request.getPatronymic());
        }

        if (request.getEmail() != null) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new UserAlreadyExistsException("user with email - " + request.getEmail() + " already exists");
            }

            userEntity.setEmail(request.getEmail());
        }

        if (request.getPhoneNumber() != null) {
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new UserAlreadyExistsException("user with phone number - " + request.getPhoneNumber() + " already exists");
            }

            userEntity.setPhoneNumber(request.getPhoneNumber());
        }

        userRepository.save(userEntity);

        return convertToResponse(userEntity);
    }

    @Override
    public GetUserResponse updateCoordinatorByEmail(UpdateCoordinatorRequest request, String email) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user with id - " + email + " not found"));

        if (!userEntity.isCoordinator()) {
            throw new UserNotCoordinatorException("The user with Id - " + email + " that you want to change is not a coordinator");
        }
        if (request.getFirstname() != null) {
            userEntity.setFirstname(request.getFirstname());
        }

        if (request.getLastname() != null) {
            userEntity.setLastname(request.getLastname());
        }

        if (request.getPatronymic() != null) {
            userEntity.setPatronymic(request.getPatronymic());
        }

        if (request.getEmail() != null) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new UserAlreadyExistsException("user with email - " + request.getEmail() + " already exists");
            }

            userEntity.setEmail(request.getEmail());
        }

        if (request.getPhoneNumber() != null) {
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new UserAlreadyExistsException("user with phone number - " + request.getPhoneNumber() + " already exists");
            }

            userEntity.setPhoneNumber(request.getPhoneNumber());
        }

        userRepository.save(userEntity);

        return convertToResponse(userEntity);
    }

    @Override
    public void deleteCoordinatorById(Integer userId) {

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("user with id - " + userId + " not found"));

        if (!userEntity.isCoordinator()) {
            throw new UserNotCoordinatorException("The user with Id - " + userId + " that you want to change is not a coordinator");
        }

        userRepository.delete(userEntity);
    }

    @Override
    public void deleteCoordinatorByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user with email - " + email + " not found"));

        if (!userEntity.isCoordinator()) {
            throw new UserNotCoordinatorException("The user with email - " + email + " that you want to change is not a coordinator");
        }

        userRepository.delete(userEntity);
    }

    @Override
    public GetUserResponse getCurrentUser(UserEntity currentUser) {
        return convertToResponse(currentUser);
    }

    private GetUserResponse convertToResponse(UserEntity userEntity) {
        return GetUserResponse.builder()
                .id(userEntity.getUserId())
                .email(userEntity.getEmail())
                .phoneNumber(userEntity.getPhoneNumber())
                .firstname(userEntity.getFirstname())
                .lastname(userEntity.getLastname())
                .patronymic(userEntity.getPatronymic())
                .tags(userEntity.getTags())
                .build();
    }
}
