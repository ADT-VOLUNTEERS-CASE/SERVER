package org.adt.volunteerscase.service.impl;

import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.dto.tag.TagEntityDTO;
import org.adt.volunteerscase.dto.user.request.UpdateCoordinatorRequest;
import org.adt.volunteerscase.dto.user.response.GetUserResponse;
import org.adt.volunteerscase.entity.TagEntity;
import org.adt.volunteerscase.entity.user.UserEntity;
import org.adt.volunteerscase.exception.UserAlreadyExistsException;
import org.adt.volunteerscase.exception.UserNotCoordinatorException;
import org.adt.volunteerscase.exception.UserNotFoundException;
import org.adt.volunteerscase.repository.UserRepository;
import org.adt.volunteerscase.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public GetUserResponse updateCoordinatorById(UpdateCoordinatorRequest request, Integer userId) {

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("user with id - " + userId + " not found"));

        return applyCoordinatorUpdate(request, userEntity);
    }

    @Override
    @Transactional
    public GetUserResponse updateCoordinatorByEmail(UpdateCoordinatorRequest request, String email) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user with id - " + email + " not found"));

        return applyCoordinatorUpdate(request, userEntity);
    }

    private GetUserResponse applyCoordinatorUpdate(UpdateCoordinatorRequest request, UserEntity userEntity){
        if (!userEntity.isCoordinator()) {
            throw new UserNotCoordinatorException("The user that you want to change is not a coordinator");
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
            if (userRepository.existsByEmail(request.getEmail()) && !request.getEmail().equals(userEntity.getEmail())) {
                throw new UserAlreadyExistsException("user with email - " + request.getEmail() + " already exists");
            }

            userEntity.setEmail(request.getEmail());
        }

        if (request.getPhoneNumber() != null) {
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber()) && !request.getPhoneNumber().equals(userEntity.getPhoneNumber())) {
                throw new UserAlreadyExistsException("user with phone number - " + request.getPhoneNumber() + " already exists");
            }

            userEntity.setPhoneNumber(request.getPhoneNumber());
        }

        userRepository.save(userEntity);

        return convertToResponse(userEntity);
    }

    @Override
    @Transactional
    public void deleteCoordinatorById(Integer userId) {

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("user with id - " + userId + " not found"));

        if (!userEntity.isCoordinator()) {
            throw new UserNotCoordinatorException("The user with id - " + userId + " that you want to delete is not a coordinator");        }

        userRepository.delete(userEntity);
    }

    @Override
    @Transactional
    public void deleteCoordinatorByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user with email - " + email + " not found"));

        if (!userEntity.isCoordinator()) {
            throw new UserNotCoordinatorException("The user with email - " + email + " that you want to delete is not a coordinator");        }

        userRepository.delete(userEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public GetUserResponse getCurrentUser(UserEntity currentUser) {
        UserEntity freshUser = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new UserNotFoundException("user with id - " + currentUser.getUserId() + " not found"));
        return convertToResponse(freshUser);
    }

    private GetUserResponse convertToResponse(UserEntity userEntity) {
        return GetUserResponse.builder()
                .id(userEntity.getUserId())
                .email(userEntity.getEmail())
                .phoneNumber(userEntity.getPhoneNumber())
                .isAdmin(userEntity.isAdmin())
                .isCoordinator(userEntity.isCoordinator())
                .firstname(userEntity.getFirstname())
                .lastname(userEntity.getLastname())
                .patronymic(userEntity.getPatronymic())
                .tags(convertTagsToTagsDTO(userEntity.getTags()))
                .build();
    }

    @Transactional(readOnly = true)
    private Set<TagEntityDTO> convertTagsToTagsDTO(Set<TagEntity> tagEntities){
        if (tagEntities == null || tagEntities.isEmpty()) {
            return Collections.emptySet();
        }

        return tagEntities.stream()
                .map(this::convertTagToDTO)
                .collect(Collectors.toSet());
    }
    @Transactional(readOnly = true)
    private TagEntityDTO convertTagToDTO(TagEntity tag){
        return TagEntityDTO.builder()
                .tagId(tag.getTagId())
                .tagName(tag.getTagName()).build();
    }

}
