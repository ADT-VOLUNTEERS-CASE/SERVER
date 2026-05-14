package org.adt.volunteerscase.unit.service;

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
import org.adt.volunteerscase.service.impl.RatingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private UserEventRepository userEventRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CoordinatorRepository coordinatorRepository;

    @Mock
    private UserRatingRepository userRatingRepository;

    @Mock
    private CoordinatorRatingRepository coordinatorRatingRepository;

    private RatingService ratingService;
    private UserEntity firstUser;
    private UserEntity secondUser;
    private CoordinatorEntity firstCoordinator;
    private CoordinatorEntity secondCoordinator;

    @BeforeEach
    void setUp() {
        ratingService = new RatingServiceImpl(
                userEventRepository,
                eventRepository,
                userRepository,
                coordinatorRepository,
                userRatingRepository,
                coordinatorRatingRepository
        );

        firstUser = UserEntity.builder()
                .userId(1)
                .firstname("Иван")
                .lastname("Первый")
                .patronymic("Иванович")
                .email("first@example.com")
                .phoneNumber("+79990000001")
                .build();

        secondUser = UserEntity.builder()
                .userId(2)
                .firstname("Пётр")
                .lastname("Второй")
                .patronymic("Петрович")
                .email("second@example.com")
                .phoneNumber("+79990000002")
                .build();

        UserEntity firstCoordinatorUser = UserEntity.builder()
                .userId(10)
                .firstname("Анна")
                .lastname("Координатор")
                .patronymic("Олеговна")
                .email("coordinator.one@example.com")
                .phoneNumber("+79990000010")
                .isCoordinator(true)
                .build();

        UserEntity secondCoordinatorUser = UserEntity.builder()
                .userId(11)
                .firstname("Мария")
                .lastname("Организатор")
                .patronymic("Сергеевна")
                .email("coordinator.two@example.com")
                .phoneNumber("+79990000011")
                .isCoordinator(true)
                .build();

        firstCoordinator = CoordinatorEntity.builder()
                .userId(10)
                .user(firstCoordinatorUser)
                .workLocation("Main office")
                .build();

        secondCoordinator = CoordinatorEntity.builder()
                .userId(11)
                .user(secondCoordinatorUser)
                .workLocation("Branch office")
                .build();
    }

    @Test
    void rebuildUserRatings_shouldReplaceMonthlySnapshotWithRankedUsers() {
        when(userEventRepository.findMonthlyUserRatingAggregates(any(LocalDateTime.class)))
                .thenReturn(List.of(
                        new RatingAggregateDTO(1, 300L),
                        new RatingAggregateDTO(99, 200L),
                        new RatingAggregateDTO(2, 120L)
                ));
        when(userRepository.findAllByUserIdInAndDeletedAtIsNull(List.of(1, 99, 2)))
                .thenReturn(List.of(firstUser, secondUser));

        ratingService.rebuildUserRatings(RatingPeriod.MONTHLY);

        ArgumentCaptor<Iterable<UserRatingEntity>> captor = iterableCaptor();
        verify(userRatingRepository).saveAll(captor.capture());
        List<UserRatingEntity> savedRatings = toList(captor.getValue());

        assertThat(savedRatings).hasSize(2);
        assertThat(savedRatings.get(0).getPeriod()).isEqualTo(RatingPeriod.MONTHLY);
        assertThat(savedRatings.get(0).getUser()).isSameAs(firstUser);
        assertThat(savedRatings.get(0).getWorkedMinutes()).isEqualTo(300L);
        assertThat(savedRatings.get(0).getRatingPosition()).isEqualTo(1);
        assertThat(savedRatings.get(0).getCalculatedAt()).isNotNull();

        assertThat(savedRatings.get(1).getUser()).isSameAs(secondUser);
        assertThat(savedRatings.get(1).getWorkedMinutes()).isEqualTo(120L);
        assertThat(savedRatings.get(1).getRatingPosition()).isEqualTo(2);

        InOrder inOrder = inOrder(userRatingRepository);
        inOrder.verify(userRatingRepository).deleteByPeriod(RatingPeriod.MONTHLY);
        inOrder.verify(userRatingRepository).flush();
        inOrder.verify(userRatingRepository).saveAll(any());
        verify(userRepository, never()).findByUserIdAndDeletedAtIsNull(anyInt());
    }

    @Test
    void rebuildUserRatings_shouldUseNullStartForOverallPeriod() {
        when(userEventRepository.findOverallUserRatingAggregates()).thenReturn(List.of());

        ratingService.rebuildUserRatings(RatingPeriod.OVERALL);

        verify(userEventRepository).findOverallUserRatingAggregates();
        verify(userEventRepository, never()).findMonthlyUserRatingAggregates(any(LocalDateTime.class));
        verify(userRepository, never()).findAllByUserIdInAndDeletedAtIsNull(any());
        verify(userRatingRepository).deleteByPeriod(RatingPeriod.OVERALL);
        verify(userRatingRepository).flush();
        verify(userRatingRepository).saveAll(argThat(iterable -> !iterable.iterator().hasNext()));
    }

    @Test
    void rebuildCoordinatorRatings_shouldReplaceMonthlySnapshotWithRankedCoordinators() {
        when(eventRepository.findMonthlyCoordinatorRatingAggregates(any(LocalDateTime.class)))
                .thenReturn(List.of(
                        new RatingAggregateDTO(10, 600L),
                        new RatingAggregateDTO(404, 400L),
                        new RatingAggregateDTO(11, 180L)
                ));
        when(coordinatorRepository.findAllByUserIdIn(List.of(10, 404, 11)))
                .thenReturn(List.of(firstCoordinator, secondCoordinator));

        ratingService.rebuildCoordinatorRatings(RatingPeriod.MONTHLY);

        ArgumentCaptor<Iterable<CoordinatorRatingEntity>> captor = iterableCaptor();
        verify(coordinatorRatingRepository).saveAll(captor.capture());
        List<CoordinatorRatingEntity> savedRatings = toList(captor.getValue());

        assertThat(savedRatings).hasSize(2);
        assertThat(savedRatings.get(0).getPeriod()).isEqualTo(RatingPeriod.MONTHLY);
        assertThat(savedRatings.get(0).getCoordinator()).isSameAs(firstCoordinator);
        assertThat(savedRatings.get(0).getTotalWeightMinutes()).isEqualTo(600L);
        assertThat(savedRatings.get(0).getRatingPosition()).isEqualTo(1);
        assertThat(savedRatings.get(0).getCalculatedAt()).isNotNull();

        assertThat(savedRatings.get(1).getCoordinator()).isSameAs(secondCoordinator);
        assertThat(savedRatings.get(1).getTotalWeightMinutes()).isEqualTo(180L);
        assertThat(savedRatings.get(1).getRatingPosition()).isEqualTo(2);

        InOrder inOrder = inOrder(coordinatorRatingRepository);
        inOrder.verify(coordinatorRatingRepository).deleteByPeriod(RatingPeriod.MONTHLY);
        inOrder.verify(coordinatorRatingRepository).flush();
        inOrder.verify(coordinatorRatingRepository).saveAll(any());
        verify(coordinatorRepository, never()).findById(anyInt());
    }

    @Test
    void getUserRating_shouldReturnMappedPageResponse() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<UserRatingEntity> ratingPage = new PageImpl<>(
                List.of(
                        UserRatingEntity.builder()
                                .period(RatingPeriod.MONTHLY)
                                .user(firstUser)
                                .workedMinutes(300L)
                                .ratingPosition(1)
                                .calculatedAt(LocalDateTime.now())
                                .build(),
                        UserRatingEntity.builder()
                                .period(RatingPeriod.MONTHLY)
                                .user(secondUser)
                                .workedMinutes(120L)
                                .ratingPosition(2)
                                .calculatedAt(LocalDateTime.now())
                                .build()
                ),
                pageable,
                2
        );

        when(userRatingRepository.findByPeriodOrderByRatingPositionAscUserUserIdAsc(RatingPeriod.MONTHLY, pageable))
                .thenReturn(ratingPage);

        PageResponse<UserRatingResponse> response = ratingService.getUserRating(RatingPeriod.MONTHLY, pageable);

        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getContent().get(0).getPeriod()).isEqualTo("monthly");
        assertThat(response.getContent().get(0).getRatingPosition()).isEqualTo(1);
        assertThat(response.getContent().get(0).getUserId()).isEqualTo(1);
        assertThat(response.getContent().get(0).getFirstname()).isEqualTo("Иван");
        assertThat(response.getContent().get(0).getWorkedMinutes()).isEqualTo(300L);
        assertThat(response.getContent().get(1).getRatingPosition()).isEqualTo(2);
        assertThat(response.getContent().get(1).getUserId()).isEqualTo(2);
        assertThat(response.getContent().get(1).getWorkedMinutes()).isEqualTo(120L);

        verify(userRatingRepository).findByPeriodOrderByRatingPositionAscUserUserIdAsc(RatingPeriod.MONTHLY, pageable);
    }

    @Test
    void getCoordinatorRating_shouldReturnMappedPageResponse() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<CoordinatorRatingEntity> ratingPage = new PageImpl<>(
                List.of(
                        CoordinatorRatingEntity.builder()
                                .period(RatingPeriod.OVERALL)
                                .coordinator(firstCoordinator)
                                .totalWeightMinutes(600L)
                                .ratingPosition(1)
                                .calculatedAt(LocalDateTime.now())
                                .build(),
                        CoordinatorRatingEntity.builder()
                                .period(RatingPeriod.OVERALL)
                                .coordinator(secondCoordinator)
                                .totalWeightMinutes(180L)
                                .ratingPosition(2)
                                .calculatedAt(LocalDateTime.now())
                                .build()
                ),
                pageable,
                2
        );

        when(coordinatorRatingRepository.findByPeriodOrderByRatingPositionAscCoordinatorUserIdAsc(
                RatingPeriod.OVERALL,
                pageable
        )).thenReturn(ratingPage);

        PageResponse<CoordinatorRatingResponse> response =
                ratingService.getCoordinatorRating(RatingPeriod.OVERALL, pageable);

        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getContent().get(0).getPeriod()).isEqualTo("overall");
        assertThat(response.getContent().get(0).getRatingPosition()).isEqualTo(1);
        assertThat(response.getContent().get(0).getCoordinatorId()).isEqualTo(10);
        assertThat(response.getContent().get(0).getFirstname()).isEqualTo("Анна");
        assertThat(response.getContent().get(0).getWorkLocation()).isEqualTo("Main office");
        assertThat(response.getContent().get(0).getTotalWeightMinutes()).isEqualTo(600L);
        assertThat(response.getContent().get(1).getRatingPosition()).isEqualTo(2);
        assertThat(response.getContent().get(1).getCoordinatorId()).isEqualTo(11);
        assertThat(response.getContent().get(1).getTotalWeightMinutes()).isEqualTo(180L);

        verify(coordinatorRatingRepository).findByPeriodOrderByRatingPositionAscCoordinatorUserIdAsc(
                RatingPeriod.OVERALL,
                pageable
        );
    }

    @Test
    void getUserRatingPosition_shouldReturnPosition_whenSnapshotExists() {
        when(userRatingRepository.findByPeriodAndUserUserId(RatingPeriod.MONTHLY, 1))
                .thenReturn(Optional.of(UserRatingEntity.builder()
                        .ratingPosition(4)
                        .build()));

        Integer position = ratingService.getUserRatingPosition(1, RatingPeriod.MONTHLY);

        assertThat(position).isEqualTo(4);
    }

    @Test
    void getUserRatingPosition_shouldReturnNull_whenSnapshotDoesNotExist() {
        when(userRatingRepository.findByPeriodAndUserUserId(RatingPeriod.MONTHLY, 1))
                .thenReturn(Optional.empty());

        Integer position = ratingService.getUserRatingPosition(1, RatingPeriod.MONTHLY);

        assertThat(position).isNull();
    }

    @Test
    void getCoordinatorRatingPosition_shouldReturnPosition_whenSnapshotExists() {
        when(coordinatorRatingRepository.findByPeriodAndCoordinatorUserId(RatingPeriod.OVERALL, 10))
                .thenReturn(Optional.of(CoordinatorRatingEntity.builder()
                        .ratingPosition(2)
                        .build()));

        Integer position = ratingService.getCoordinatorRatingPosition(10, RatingPeriod.OVERALL);

        assertThat(position).isEqualTo(2);
    }

    @SuppressWarnings("unchecked")
    private <T> ArgumentCaptor<Iterable<T>> iterableCaptor() {
        return ArgumentCaptor.forClass(Iterable.class);
    }

    private <T> List<T> toList(Iterable<T> iterable) {
        List<T> result = new ArrayList<>();
        iterable.forEach(result::add);
        return result;
    }
}
