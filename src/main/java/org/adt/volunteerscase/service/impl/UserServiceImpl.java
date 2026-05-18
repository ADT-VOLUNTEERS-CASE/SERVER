package org.adt.volunteerscase.service.impl;

import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.dto.page.response.PageResponse;
import org.adt.volunteerscase.dto.tag.TagEntityDTO;
import org.adt.volunteerscase.dto.user.request.UpdateCoordinatorRequest;
import org.adt.volunteerscase.dto.user.response.GetUserResponse;
import org.adt.volunteerscase.dto.user.response.GetUserV2Response;
import org.adt.volunteerscase.dto.user.response.RegisteredEventResponse;
import org.adt.volunteerscase.dto.user.response.UserEventShortResponse;
import org.adt.volunteerscase.entity.CoordinatorEntity;
import org.adt.volunteerscase.entity.TagEntity;
import org.adt.volunteerscase.entity.UserEventEntity;
import org.adt.volunteerscase.entity.event.EventEntity;
import org.adt.volunteerscase.entity.user.UserEntity;
import org.adt.volunteerscase.exception.*;
import org.adt.volunteerscase.repository.CoordinatorRepository;
import org.adt.volunteerscase.repository.EventRepository;
import org.adt.volunteerscase.repository.UserEventRepository;
import org.adt.volunteerscase.repository.UserRepository;
import org.adt.volunteerscase.service.UserService;
import org.adt.volunteerscase.service.security.RefreshTokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.adt.volunteerscase.entity.rating.RatingPeriod;
import org.adt.volunteerscase.service.RatingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final CoordinatorRepository coordinatorRepository;
    private final EventRepository eventRepository;
    private final UserEventRepository userEventRepository;
    private final RatingService ratingService;

    @Override
    @Transactional
    public GetUserResponse updateCoordinatorById(UpdateCoordinatorRequest request, Integer userId) {

        UserEntity userEntity = userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException("user with id - " + userId + " not found"));

        return applyCoordinatorUpdate(request, userEntity);
    }

    @Override
    @Transactional
    public GetUserResponse updateCoordinatorByEmail(UpdateCoordinatorRequest request, String email) {
        UserEntity userEntity = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UserNotFoundException("user with email - " + email + " not found"));

        return applyCoordinatorUpdate(request, userEntity);
    }

    private GetUserResponse applyCoordinatorUpdate(UpdateCoordinatorRequest request, UserEntity userEntity){
        if (!userEntity.isCoordinator()) {
            throw new UserNotCoordinatorException("The user that you want to change is not a coordinator");
        }

        CoordinatorEntity coordinatorEntity = coordinatorRepository.findById(userEntity.getUserId())
                .orElseThrow(() -> new CoordinatorNotFoundException(
                        "coordinator with user id - " + userEntity.getUserId() + " not found"
                ));


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

        if (request.getWorkLocation() != null) {
            coordinatorEntity.setWorkLocation(request.getWorkLocation());
        }


        userEntity.setUpdatedAt(LocalDateTime.now());
        userRepository.save(userEntity);
        coordinatorRepository.save(coordinatorEntity);

        return convertToResponse(userEntity, coordinatorEntity);
    }

    @Override
    @Transactional
    public void deleteCoordinatorById(Integer userId) {

        UserEntity userEntity = userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException("user with id - " + userId + " not found"));

        if (!userEntity.isCoordinator()) {
            throw new UserNotCoordinatorException("The user with id - " + userId + " that you want to delete is not a coordinator");
        }

        CoordinatorEntity coordinatorEntity = coordinatorRepository.findById(userEntity.getUserId())
                .orElseThrow(() -> new CoordinatorNotFoundException(
                        "coordinator with user id - " + userEntity.getUserId() + " not found"
                ));

        if (eventRepository.existsByCoordinator(coordinatorEntity)) {
            throw new CoordinatorInUseException(
                    "coordinator with user id - " + userEntity.getUserId() + " is assigned to one or more events"
            );
        }

        refreshTokenService.deleteAllByUser(userEntity);
        coordinatorRepository.delete(coordinatorEntity);
        softDeleteUser(userEntity);

    }

    @Override
    @Transactional
    public void deleteCoordinatorByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UserNotFoundException("user with email - " + email + " not found"));

        if (!userEntity.isCoordinator()) {
            throw new UserNotCoordinatorException("The user with email - " + email + " that you want to delete is not a coordinator");
        }

        CoordinatorEntity coordinatorEntity = coordinatorRepository.findById(userEntity.getUserId())
                .orElseThrow(() -> new CoordinatorNotFoundException(
                        "coordinator with user id - " + userEntity.getUserId() + " not found"
                ));

        if (eventRepository.existsByCoordinator(coordinatorEntity)) {
            throw new CoordinatorInUseException(
                    "coordinator with user id - " + userEntity.getUserId() + " is assigned to one or more events"
            );
        }

        refreshTokenService.deleteAllByUser(userEntity);
        coordinatorRepository.delete(coordinatorEntity);
        softDeleteUser(userEntity);
    }


    @Override
    @Transactional(readOnly = true)
    public GetUserResponse getCurrentUser(UserEntity currentUser) {
        UserEntity freshUser = getFreshUser(currentUser);
        CoordinatorEntity coordinatorEntity = getCoordinatorEntityIfNeeded(freshUser);


        return convertToResponse(freshUser, coordinatorEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public GetUserV2Response getCurrentUserV2(UserEntity currentUser) {
        UserEntity freshUser = getFreshUser(currentUser);
        CoordinatorEntity coordinatorEntity = getCoordinatorEntityIfNeeded(freshUser);

        return convertToV2Response(freshUser, coordinatorEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RegisteredEventResponse> getRegisteredEvents(UserEntity currentUser, Pageable pageable) {
        UserEntity freshUser = getFreshUser(currentUser);
        Page<UserEventEntity> registeredEventsPage =
                userEventRepository.findRegisteredEventsByUserId(freshUser.getUserId(), pageable);

        return PageResponse.of(registeredEventsPage.map(this::convertToRegisteredEventResponse));
    }

    private UserEntity getFreshUser(UserEntity currentUser) {
        return userRepository.findByUserIdAndDeletedAtIsNull(currentUser.getUserId())
                .orElseThrow(() -> new UserNotFoundException("user with id - " + currentUser.getUserId() + " not found"));
    }

    private CoordinatorEntity getCoordinatorEntityIfNeeded(UserEntity userEntity) {
        if (!userEntity.isCoordinator()) {
            return null;
        }

        return coordinatorRepository.findById(userEntity.getUserId())
                .orElseThrow(() -> new CoordinatorNotFoundException(
                        "coordinator with user id - " + userEntity.getUserId() + " not found"
                ));
    }

    private GetUserResponse convertToResponse(UserEntity userEntity, CoordinatorEntity coordinatorEntity) {
        LocalDateTime now = LocalDateTime.now();
        return GetUserResponse.builder()
                .id(userEntity.getUserId())
                .email(userEntity.getEmail())
                .phoneNumber(userEntity.getPhoneNumber())
                .isAdmin(userEntity.isAdmin())
                .isCoordinator(userEntity.isCoordinator())
                .firstname(userEntity.getFirstname())
                .lastname(userEntity.getLastname())
                .patronymic(userEntity.getPatronymic())
                .workLocation(coordinatorEntity != null ? coordinatorEntity.getWorkLocation() : null)
                .tags(convertTagsToTagsDTO(userEntity.getTags()))
                .events(getActiveUpcomingEvents(userEntity, now))
                .build();
    }

    private GetUserV2Response convertToV2Response(UserEntity userEntity, CoordinatorEntity coordinatorEntity) {
        LocalDateTime now = LocalDateTime.now();
        return GetUserV2Response.builder()
                .id(userEntity.getUserId())
                .email(userEntity.getEmail())
                .phoneNumber(userEntity.getPhoneNumber())
                .isAdmin(userEntity.isAdmin())
                .isCoordinator(userEntity.isCoordinator())
                .firstname(userEntity.getFirstname())
                .lastname(userEntity.getLastname())
                .patronymic(userEntity.getPatronymic())
                .workLocation(coordinatorEntity != null ? coordinatorEntity.getWorkLocation() : null)
                .monthlyRating(ratingService.getUserRatingPosition(userEntity.getUserId(), RatingPeriod.MONTHLY))
                .overallRating(ratingService.getUserRatingPosition(userEntity.getUserId(), RatingPeriod.OVERALL))
                .tags(convertTagsToTagsDTO(userEntity.getTags()))
                .events(getActiveUpcomingEvents(userEntity, now))
                .build();
    }

    @Transactional(readOnly = true)
    private List<UserEventShortResponse> getActiveUpcomingEvents(UserEntity userEntity, LocalDateTime now) {

        List<EventEntity> events = userEventRepository.findActiveUpcomingEventsByUserId(userEntity.getUserId(), now);
        return events.stream()
                .map(this::convertEventToUserEventShortResponse)
                .collect(Collectors.toList());
    }

    private UserEventShortResponse convertEventToUserEventShortResponse(EventEntity event) {
        return UserEventShortResponse.builder()
                .eventId(event.getEventId())
                .name(event.getName())
                .status(String.valueOf(event.getStatus()))
                .maxCapacity(event.getMaxCapacity())
                .dateTimestamp(event.getDateTimestamp())
                .build();
    }

    private RegisteredEventResponse convertToRegisteredEventResponse(UserEventEntity userEvent) {
        EventEntity event = userEvent.getEvent();
        CoordinatorEntity coordinator = event.getCoordinator();
        UserEntity coordinatorUser = coordinator != null ? coordinator.getUser() : null;

        return RegisteredEventResponse.builder()
                .eventId(event.getEventId())
                .name(event.getName())
                .eventStatus(String.valueOf(event.getStatus()))
                .applicationStatus(resolveApplicationStatus(userEvent))
                .maxCapacity(event.getMaxCapacity())
                .weightMinutes(event.getWeightMinutes())
                .dateTimestamp(event.getDateTimestamp())
                .locationAddress(event.getLocation() != null ? event.getLocation().getAddress() : null)
                .coordinatorId(coordinator != null ? coordinator.getUserId() : null)
                .coordinatorFullName(coordinatorUser != null ? fullName(coordinatorUser) : null)
                .build();
    }

    private String resolveApplicationStatus(UserEventEntity userEvent) {
        if (userEvent.isRevoked()) {
            return "REVOKED";
        }
        if (userEvent.isRejected()) {
            return "REJECTED";
        }
        if (userEvent.isAccepted()) {
            return "ACCEPTED";
        }
        return "PENDING";
    }

    private String fullName(UserEntity user) {
        StringBuilder builder = new StringBuilder();

        appendNamePart(builder, user.getLastname());
        appendNamePart(builder, user.getFirstname());
        appendNamePart(builder, user.getPatronymic());

        return builder.isEmpty() ? null : builder.toString();
    }

    private void appendNamePart(StringBuilder builder, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append(" ");
        }
        builder.append(value);
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

    private void softDeleteUser(UserEntity userEntity) {
        LocalDateTime now = LocalDateTime.now();

        userEntity.setDeletedAt(now);

        List<UserEventEntity> activeUserEvents = userEventRepository.findAllByUserAndDeletedAtIsNull(userEntity);
        activeUserEvents.forEach(userEvent -> userEvent.setDeletedAt(now));

        userEventRepository.saveAll(activeUserEvents);
        userRepository.save(userEntity);
    }


}
