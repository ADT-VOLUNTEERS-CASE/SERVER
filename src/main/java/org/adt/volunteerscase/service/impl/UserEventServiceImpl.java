package org.adt.volunteerscase.service.impl;

import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.dto.userEvent.request.UserEventStatusPatchRequest;
import org.adt.volunteerscase.dto.userEvent.response.UserEventResponse;
import org.adt.volunteerscase.entity.UserEventEntity;
import org.adt.volunteerscase.entity.UserEventId;
import org.adt.volunteerscase.entity.event.EventEntity;
import org.adt.volunteerscase.entity.event.EventStatus;
import org.adt.volunteerscase.entity.user.UserEntity;
import org.adt.volunteerscase.exception.EventCapacityExceededException;
import org.adt.volunteerscase.exception.EventNotFoundException;
import org.adt.volunteerscase.exception.UserEventAccessDeniedException;
import org.adt.volunteerscase.exception.UserEventAlreadyExistsException;
import org.adt.volunteerscase.exception.UserEventNotFoundException;
import org.adt.volunteerscase.exception.UserEventStateConflictException;
import org.adt.volunteerscase.exception.UserNotFoundException;
import org.adt.volunteerscase.repository.EventRepository;
import org.adt.volunteerscase.repository.UserEventRepository;
import org.adt.volunteerscase.repository.UserRepository;
import org.adt.volunteerscase.service.UserEventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserEventServiceImpl implements UserEventService {

    private final UserEventRepository userEventRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public UserEventResponse createApplication(Integer eventId, Integer currentUserId) {
        UserEntity user = getActiveUser(currentUserId);
        EventEntity event = getEvent(eventId);

        validateEventAcceptsApplications(event);

        Optional<UserEventEntity> existingOptional = userEventRepository.findByUserAndEvent(user,
                event);
        if (existingOptional.isPresent()) {
            UserEventEntity existing = existingOptional.get();
            if (isActiveApplication(existing)) {
                throw new UserEventAlreadyExistsException(
                        "active application for user id - " + currentUserId + " and event id - " +
                                eventId + " already exists"
                );
            }
        }

        ensureActiveCapacityNotReached(event);

        LocalDateTime now = LocalDateTime.now();

        UserEventEntity userEvent = existingOptional.orElseGet(() -> UserEventEntity.builder()
                .id(UserEventId.builder()
                        .userId(user.getUserId())
                        .eventId(event.getEventId())
                        .build())
                .user(user)
                .event(event)
                .build());

        moveToPending(userEvent, now);

        UserEventEntity savedUserEvent = userEventRepository.save(userEvent);
        return convertToResponse(savedUserEvent);
    }

    @Override
    @Transactional
    public UserEventResponse updateApplicationStatus(
            Integer eventId,
            Integer userId,
            UserEventStatusPatchRequest request,
            Integer currentCoordinatorId
    ) {
        EventEntity event = getEvent(eventId);
        ensureCoordinatorOwnsEvent(event, currentCoordinatorId);

        UserEntity user = getActiveUser(userId);
        UserEventEntity userEvent = userEventRepository.findByUserAndEventAndDeletedAtIsNull(user,
                        event)
                .orElseThrow(() -> new UserEventNotFoundException(
                        "application for user id - " + userId + " and event id - " + eventId + " not found"
                ));

        if (userEvent.isRevoked()) {
            throw new UserEventStateConflictException(
                    "application for user id - " + userId + " and event id - " + eventId + " is revoked and cannot be updated"
            );
        }

        if ("ACCEPTED".equals(request.getStatus())) {
            validateEventAcceptsApplications(event);

            if (userEvent.isRejected()) {
                ensureActiveCapacityNotReached(event);
            }

            if (!userEvent.isAccepted()) {
                ensureAcceptedCapacityNotReached(event);
            }

            moveToAccepted(userEvent);
        } else if ("REJECTED".equals(request.getStatus())) {
            moveToRejected(userEvent, request.getRejectReason(), LocalDateTime.now());
        } else {
            throw new UserEventStateConflictException(
                    "unsupported application status - " + request.getStatus()
            );
        }

        UserEventEntity savedUserEvent = userEventRepository.save(userEvent);
        return convertToResponse(savedUserEvent);
    }

    private UserEntity getActiveUser(Integer userId) {
        return userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException("user with id - " + userId + " not found"));
    }

    private EventEntity getEvent(Integer eventId) {
        return eventRepository.findByEventId(eventId)
                .orElseThrow(() -> new EventNotFoundException("event with id - " + eventId + " not found"));
    }

    private void validateEventAcceptsApplications(EventEntity event) {
        if (event.getStatus() == EventStatus.COMPLETED || !
                event.getDateTimestamp().isAfter(LocalDateTime.now())) {
            throw new UserEventStateConflictException(
                    "event with id - " + event.getEventId() + " does not accept applications"
            );
        }
    }

    private void ensureCoordinatorOwnsEvent(EventEntity event, Integer currentCoordinatorId) {
        Integer eventCoordinatorId = event.getCoordinator() != null ?
                event.getCoordinator().getUserId() : null;
        if (!Objects.equals(eventCoordinatorId, currentCoordinatorId)) {
            throw new UserEventAccessDeniedException(
                    "user with id - " + currentCoordinatorId + " is not coordinator of event with id - " + event.getEventId()
            );
        }
    }

    private boolean isActiveApplication(UserEventEntity userEvent) {
        return userEvent.getDeletedAt() == null
                && !userEvent.isRejected()
                && !userEvent.isRevoked();
    }

    private void ensureActiveCapacityNotReached(EventEntity event) {
        long activeApplications = userEventRepository
                .countByEventAndDeletedAtIsNullAndRejectedFalseAndRevokedFalse(event);

        if (activeApplications >= event.getMaxCapacity()) {
            throw new EventCapacityExceededException(
                    "event with id - " + event.getEventId() + " has reached max capacity - " +
                            event.getMaxCapacity()
            );
        }
    }

    private void ensureAcceptedCapacityNotReached(EventEntity event) {
        long acceptedApplications = userEventRepository
                .countByEventAndDeletedAtIsNullAndAcceptedTrueAndRevokedFalse(event);

        if (acceptedApplications >= event.getMaxCapacity()) {
            throw new EventCapacityExceededException(
                    "event with id - " + event.getEventId() + " has reached max capacity - " +
                            event.getMaxCapacity()
            );
        }
    }

    private void moveToPending(UserEventEntity userEvent, LocalDateTime now) {
        userEvent.setAccepted(false);
        userEvent.setRejected(false);
        userEvent.setRevoked(false);
        userEvent.setRejectReason(null);
        userEvent.setRejectedAt(null);
        userEvent.setRevokedAt(null);
        userEvent.setDeletedAt(null);
        userEvent.setCreatedAt(now);
    }

    private void moveToAccepted(UserEventEntity userEvent) {
        userEvent.setAccepted(true);
        userEvent.setRejected(false);
        userEvent.setRevoked(false);
        userEvent.setRejectReason(null);
        userEvent.setRejectedAt(null);
        userEvent.setRevokedAt(null);
        userEvent.setDeletedAt(null);
    }

    private void moveToRejected(UserEventEntity userEvent, String rejectReason, LocalDateTime now) {
        userEvent.setAccepted(false);
        userEvent.setRejected(true);
        userEvent.setRevoked(false);
        userEvent.setRejectReason(rejectReason);
        userEvent.setRejectedAt(now);
        userEvent.setRevokedAt(null);
        userEvent.setDeletedAt(null);
    }

    private UserEventResponse convertToResponse(UserEventEntity userEvent) {
        return UserEventResponse.builder()
                .userId(userEvent.getUser().getUserId())
                .eventId(userEvent.getEvent().getEventId())
                .status(resolveStatus(userEvent))
                .rejectReason(userEvent.getRejectReason())
                .createdAt(userEvent.getCreatedAt())
                .rejectedAt(userEvent.getRejectedAt())
                .revokedAt(userEvent.getRevokedAt())
                .build();
    }

    private String resolveStatus(UserEventEntity userEvent) {
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
}