package org.adt.volunteerscase.service.impl;

import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.dto.page.response.PageResponse;
import org.adt.volunteerscase.dto.rating.RatingAggregateDTO;
import org.adt.volunteerscase.dto.rating.response.CoordinatorRatingResponse;
import org.adt.volunteerscase.dto.rating.response.UserRatingResponse;
import org.adt.volunteerscase.entity.CoordinatorEntity;
import org.adt.volunteerscase.entity.rating.CoordinatorRatingEntity;
import org.adt.volunteerscase.entity.rating.RatingPeriod;
import org.adt.volunteerscase.entity.rating.UserRatingEntity;
import org.adt.volunteerscase.entity.user.UserEntity;
import org.adt.volunteerscase.repository.CoordinatorRatingRepository;
import org.adt.volunteerscase.repository.CoordinatorRepository;
import org.adt.volunteerscase.repository.EventRepository;
import org.adt.volunteerscase.repository.UserEventRepository;
import org.adt.volunteerscase.repository.UserRatingRepository;
import org.adt.volunteerscase.repository.UserRepository;
import org.adt.volunteerscase.service.RatingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final UserEventRepository userEventRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CoordinatorRepository coordinatorRepository;
    private final UserRatingRepository userRatingRepository;
    private final CoordinatorRatingRepository coordinatorRatingRepository;

    @Override
    @Transactional
    public void rebuildAllRatings() {
        rebuildUserRatings(RatingPeriod.MONTHLY);
        rebuildUserRatings(RatingPeriod.OVERALL);
        rebuildCoordinatorRatings(RatingPeriod.MONTHLY);
        rebuildCoordinatorRatings(RatingPeriod.OVERALL);
    }

    @Override
    @Transactional
    public void rebuildUserRatings(RatingPeriod period) {
        LocalDateTime calculatedAt = LocalDateTime.now();

        List<RatingAggregateDTO> aggregates = findUserRatingAggregates(period);
        Map<Integer, UserEntity> usersById = findUsersByAggregateSubjectIds(aggregates);
        List<UserRatingEntity> ratings = new ArrayList<>();

        int position = 1;
        for (RatingAggregateDTO aggregate : aggregates) {
            UserEntity user = usersById.get(aggregate.getSubjectId());

            if (user == null) {
                continue;
            }

            ratings.add(UserRatingEntity.builder()
                    .period(period)
                    .user(user)
                    .workedMinutes(aggregate.getMinutes())
                    .ratingPosition(position)
                    .calculatedAt(calculatedAt)
                    .build());

            position++;
        }

        userRatingRepository.deleteByPeriod(period);
        userRatingRepository.flush();
        userRatingRepository.saveAll(ratings);
    }

    @Override
    @Transactional
    public void rebuildCoordinatorRatings(RatingPeriod period) {
        LocalDateTime calculatedAt = LocalDateTime.now();

        List<RatingAggregateDTO> aggregates = findCoordinatorRatingAggregates(period);
        Map<Integer, CoordinatorEntity> coordinatorsById = findCoordinatorsByAggregateSubjectIds(aggregates);
        List<CoordinatorRatingEntity> ratings = new ArrayList<>();

        int position = 1;
        for (RatingAggregateDTO aggregate : aggregates) {
            CoordinatorEntity coordinator = coordinatorsById.get(aggregate.getSubjectId());

            if (coordinator == null) {
                continue;
            }

            ratings.add(CoordinatorRatingEntity.builder()
                    .period(period)
                    .coordinator(coordinator)
                    .totalWeightMinutes(aggregate.getMinutes())
                    .ratingPosition(position)
                    .calculatedAt(calculatedAt)
                    .build());

            position++;
        }

        coordinatorRatingRepository.deleteByPeriod(period);
        coordinatorRatingRepository.flush();
        coordinatorRatingRepository.saveAll(ratings);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserRatingResponse> getUserRating(RatingPeriod period, Pageable pageable) {
        Page<UserRatingEntity> ratingPage =
                userRatingRepository.findByPeriodOrderByRatingPositionAscUserUserIdAsc(period, pageable);

        List<UserRatingResponse> content = ratingPage.getContent().stream()
                .map(this::convertToUserRatingResponse)
                .toList();

        return PageResponse.of(new PageImpl<>(content, pageable, ratingPage.getTotalElements()));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CoordinatorRatingResponse> getCoordinatorRating(RatingPeriod period, Pageable pageable) {
        Page<CoordinatorRatingEntity> ratingPage =
                coordinatorRatingRepository.findByPeriodOrderByRatingPositionAscCoordinatorUserIdAsc(period, pageable);

        List<CoordinatorRatingResponse> content = ratingPage.getContent().stream()
                .map(this::convertToCoordinatorRatingResponse)
                .toList();

        return PageResponse.of(new PageImpl<>(content, pageable, ratingPage.getTotalElements()));
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getUserRatingPosition(Integer userId, RatingPeriod period) {
        return userRatingRepository.findByPeriodAndUserUserId(period, userId)
                .map(UserRatingEntity::getRatingPosition)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getCoordinatorRatingPosition(Integer coordinatorId, RatingPeriod period) {
        return coordinatorRatingRepository.findByPeriodAndCoordinatorUserId(period, coordinatorId)
                .map(CoordinatorRatingEntity::getRatingPosition)
                .orElse(null);
    }

    @Scheduled(
            fixedDelayString = "${rating.rebuild.fixed-delay-ms:300000}",
            initialDelayString = "${rating.rebuild.initial-delay-ms:300000}"
    )
    @Transactional
    public void rebuildAllRatingsBySchedule() {
        rebuildAllRatings();
    }

    private List<RatingAggregateDTO> findUserRatingAggregates(RatingPeriod period) {
        if (period == RatingPeriod.MONTHLY) {
            return userEventRepository.findMonthlyUserRatingAggregates(resolveMonthlyStart());
        }

        return userEventRepository.findOverallUserRatingAggregates();
    }

    private List<RatingAggregateDTO> findCoordinatorRatingAggregates(RatingPeriod period) {
        if (period == RatingPeriod.MONTHLY) {
            return eventRepository.findMonthlyCoordinatorRatingAggregates(resolveMonthlyStart());
        }

        return eventRepository.findOverallCoordinatorRatingAggregates();
    }

    private LocalDateTime resolveMonthlyStart() {
        return LocalDateTime.now().minusMonths(1);
    }

    private Map<Integer, UserEntity> findUsersByAggregateSubjectIds(List<RatingAggregateDTO> aggregates) {
        if (aggregates.isEmpty()) {
            return Map.of();
        }

        List<Integer> userIds = aggregates.stream()
                .map(RatingAggregateDTO::getSubjectId)
                .toList();

        return userRepository.findAllByUserIdInAndDeletedAtIsNull(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getUserId, Function.identity()));
    }

    private Map<Integer, CoordinatorEntity> findCoordinatorsByAggregateSubjectIds(List<RatingAggregateDTO> aggregates) {
        if (aggregates.isEmpty()) {
            return Map.of();
        }

        List<Integer> coordinatorIds = aggregates.stream()
                .map(RatingAggregateDTO::getSubjectId)
                .toList();

        return coordinatorRepository.findAllByUserIdIn(coordinatorIds).stream()
                .collect(Collectors.toMap(CoordinatorEntity::getUserId, Function.identity()));
    }

    private UserRatingResponse convertToUserRatingResponse(UserRatingEntity rating) {
        UserEntity user = rating.getUser();

        return UserRatingResponse.builder()
                .period(rating.getPeriod().getValue())
                .ratingPosition(rating.getRatingPosition())
                .userId(user.getUserId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .patronymic(user.getPatronymic())
                .workedMinutes(rating.getWorkedMinutes())
                .build();
    }

    private CoordinatorRatingResponse convertToCoordinatorRatingResponse(CoordinatorRatingEntity rating) {
        CoordinatorEntity coordinator = rating.getCoordinator();
        UserEntity user = coordinator.getUser();

        return CoordinatorRatingResponse.builder()
                .period(rating.getPeriod().getValue())
                .ratingPosition(rating.getRatingPosition())
                .coordinatorId(coordinator.getUserId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .patronymic(user.getPatronymic())
                .workLocation(coordinator.getWorkLocation())
                .totalWeightMinutes(rating.getTotalWeightMinutes())
                .build();
    }
}
