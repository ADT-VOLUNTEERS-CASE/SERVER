package org.adt.volunteerscase.repository;

import org.adt.volunteerscase.entity.UserEventEntity;
import org.adt.volunteerscase.entity.UserEventId;
import org.adt.volunteerscase.entity.event.EventEntity;
import org.adt.volunteerscase.entity.event.EventStatus;
import org.adt.volunteerscase.entity.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.adt.volunteerscase.dto.userEvent.response.CoordinatorEventApplicationsSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.adt.volunteerscase.dto.rating.RatingAggregateDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserEventRepository extends JpaRepository<UserEventEntity, UserEventId> {

    Optional<UserEventEntity> findByUserAndEvent(UserEntity user, EventEntity event);

    boolean existsByUserAndEvent(UserEntity user, EventEntity event);

    List<UserEventEntity> findAllByUserAndDeletedAtIsNull(UserEntity user);

    Optional<UserEventEntity> findByUserAndEventAndDeletedAtIsNull(UserEntity user, EventEntity event);

    boolean existsByUserAndEventAndDeletedAtIsNull(UserEntity user, EventEntity event);

    long countByEventAndDeletedAtIsNullAndRejectedFalseAndRevokedFalse(EventEntity event);

    long countByEventAndDeletedAtIsNullAndAcceptedTrueAndRevokedFalse(EventEntity event);

    @Query("""
            SELECT e
            FROM UserEventEntity ue
            JOIN ue.event e
            WHERE ue.user.userId = :userId
              AND ue.deletedAt IS NULL
              AND ue.revoked = false
              AND ue.rejected = false
              AND e.dateTimestamp > :now
              AND e.status <> org.adt.volunteerscase.entity.event.EventStatus.COMPLETED
            ORDER BY e.dateTimestamp ASC, e.eventId ASC
            """)
    List<EventEntity> findActiveUpcomingEventsByUserId(
            @Param("userId") Integer userId,
            @Param("now") LocalDateTime now
    );

    @Query(
            value = """
                  SELECT new
  org.adt.volunteerscase.dto.userEvent.response.CoordinatorEventApplicationsSummaryResponse(
                      e.eventId,
                      e.name,
                      e.status,
                      e.dateTimestamp,
                      e.maxCapacity,
                      COUNT(ue),
                      SUM(CASE WHEN ue.accepted = false AND ue.rejected = false AND ue.revoked = false
  THEN 1 ELSE 0 END),
                      SUM(CASE WHEN ue.accepted = true AND ue.revoked = false THEN 1 ELSE 0 END),
                      SUM(CASE WHEN ue.rejected = true AND ue.revoked = false THEN 1 ELSE 0 END),
                      SUM(CASE WHEN ue.revoked = true THEN 1 ELSE 0 END)
                  )
                  FROM EventEntity e
                  LEFT JOIN e.userEvents ue ON ue.deletedAt IS NULL
                  WHERE e.coordinator.userId = :coordinatorId
                  GROUP BY e.eventId, e.name, e.status, e.dateTimestamp, e.maxCapacity
                  ORDER BY e.dateTimestamp DESC, e.eventId DESC
  """,
            countQuery = """
                  SELECT COUNT(e)
                  FROM EventEntity e
                  WHERE e.coordinator.userId = :coordinatorId
                  """
    )
    Page<CoordinatorEventApplicationsSummaryResponse> findCoordinatorEventApplicationSummaries(
            @Param("coordinatorId") Integer coordinatorId,
            Pageable pageable
    );

    @Query(
            value = """
                  SELECT ue
                  FROM UserEventEntity ue
                  JOIN FETCH ue.user u
                  JOIN FETCH ue.event e
                  WHERE e.eventId = :eventId
                    AND e.coordinator.userId = :coordinatorId
                    AND ue.deletedAt IS NULL
                    AND (
                        :status IS NULL
                        OR (:status = 'PENDING' AND ue.accepted = false AND ue.rejected = false AND
  ue.revoked = false)
                        OR (:status = 'ACCEPTED' AND ue.accepted = true AND ue.revoked = false)
                        OR (:status = 'REJECTED' AND ue.rejected = true AND ue.revoked = false)
                        OR (:status = 'REVOKED' AND ue.revoked = true)
                    )
                  ORDER BY ue.createdAt DESC, u.userId ASC
  """,
            countQuery = """
                  SELECT COUNT(ue)
                  FROM UserEventEntity ue
                  JOIN ue.event e
                  WHERE e.eventId = :eventId
                    AND e.coordinator.userId = :coordinatorId
                    AND ue.deletedAt IS NULL
                    AND (
                        :status IS NULL
                        OR (:status = 'PENDING' AND ue.accepted = false AND ue.rejected = false AND
  ue.revoked = false)
                        OR (:status = 'ACCEPTED' AND ue.accepted = true AND ue.revoked = false)
                        OR (:status = 'REJECTED' AND ue.rejected = true AND ue.revoked = false)
                        OR (:status = 'REVOKED' AND ue.revoked = true)
                    )
  """
    )
    Page<UserEventEntity> findApplicationsByCoordinatorAndEvent(
            @Param("eventId") Integer eventId,
            @Param("coordinatorId") Integer coordinatorId,
            @Param("status") String status,
            Pageable pageable
    );

    @Query("""
          SELECT COUNT(ue)
          FROM UserEventEntity ue
          JOIN ue.event e
          WHERE ue.user.userId = :userId
            AND ue.deletedAt IS NULL
            AND ue.accepted = true
            AND ue.revoked = false
            AND e.status = org.adt.volunteerscase.entity.event.EventStatus.COMPLETED
          """)
    long countCompletedParticipationsByUserId(
            @Param("userId") Integer userId
    );

    @Query("""
          SELECT COUNT(ue)
          FROM UserEventEntity ue
          JOIN ue.event e
          WHERE ue.user.userId = :userId
            AND ue.deletedAt IS NULL
            AND ue.accepted = true
            AND ue.revoked = false
            AND e.status = :eventStatus
            AND e.dateTimestamp >= :from
            AND e.dateTimestamp < :to
          """)
    long countCompletedParticipationsByUserIdBetween(
            @Param("userId") Integer userId,
            @Param("eventStatus") EventStatus eventStatus,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
          SELECT COALESCE(SUM(e.weightMinutes), 0)
          FROM UserEventEntity ue
          JOIN ue.event e
          WHERE ue.user.userId = :userId
            AND ue.deletedAt IS NULL
            AND ue.accepted = true
            AND ue.revoked = false
            AND e.status = :eventStatus
          """)
    long sumCompletedParticipationWeightMinutesByUserId(
            @Param("userId") Integer userId,
            @Param("eventStatus") EventStatus eventStatus
    );

    @Query("""
          SELECT COALESCE(SUM(e.weightMinutes), 0)
          FROM UserEventEntity ue
          JOIN ue.event e
          WHERE ue.user.userId = :userId
            AND ue.deletedAt IS NULL
            AND ue.accepted = true
            AND ue.revoked = false
            AND e.status = :eventStatus
            AND e.dateTimestamp >= :from
            AND e.dateTimestamp < :to
          """)
    long sumCompletedParticipationWeightMinutesByUserIdBetween(
            @Param("userId") Integer userId,
            @Param("eventStatus") EventStatus eventStatus,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
          SELECT e.dateTimestamp
          FROM UserEventEntity ue
          JOIN ue.event e
          WHERE ue.user.userId = :userId
            AND ue.deletedAt IS NULL
            AND ue.accepted = true
            AND ue.revoked = false
            AND e.status = :eventStatus
          ORDER BY e.dateTimestamp ASC
          """)
    List<LocalDateTime> findCompletedParticipationDatesByUserId(
            @Param("userId") Integer userId,
            @Param("eventStatus") EventStatus eventStatus
    );

    @Query("""
          SELECT COUNT(ue)
          FROM UserEventEntity ue
          JOIN ue.event e
          WHERE e.coordinator.userId = :coordinatorId
            AND e.status = :eventStatus
            AND ue.deletedAt IS NULL
            AND ue.accepted = true
            AND ue.revoked = false
          """)
    long countCompletedEventParticipantsByCoordinatorId(
            @Param("coordinatorId") Integer coordinatorId,
            @Param("eventStatus") EventStatus eventStatus
    );

    @Query("""
          SELECT new org.adt.volunteerscase.dto.rating.RatingAggregateDTO(
              ue.user.userId,
              COALESCE(SUM(e.weightMinutes), 0)
          )
          FROM UserEventEntity ue
          JOIN ue.event e
          WHERE ue.deletedAt IS NULL
            AND ue.accepted = true
            AND ue.revoked = false
            AND ue.user.deletedAt IS NULL
            AND ue.user.isAdmin = false
            AND ue.user.isCoordinator = false
            AND e.status = org.adt.volunteerscase.entity.event.EventStatus.COMPLETED
          GROUP BY ue.user.userId
          HAVING COALESCE(SUM(e.weightMinutes), 0) > 0
          ORDER BY COALESCE(SUM(e.weightMinutes), 0) DESC, ue.user.userId ASC
          """)
    List<RatingAggregateDTO> findOverallUserRatingAggregates();

    @Query("""
          SELECT new org.adt.volunteerscase.dto.rating.RatingAggregateDTO(
              ue.user.userId,
              COALESCE(SUM(e.weightMinutes), 0)
          )
          FROM UserEventEntity ue
          JOIN ue.event e
          WHERE ue.deletedAt IS NULL
            AND ue.accepted = true
            AND ue.revoked = false
            AND ue.user.deletedAt IS NULL
            AND ue.user.isAdmin = false
            AND ue.user.isCoordinator = false
            AND e.status = org.adt.volunteerscase.entity.event.EventStatus.COMPLETED
            AND e.dateTimestamp >= :from
          GROUP BY ue.user.userId
          HAVING COALESCE(SUM(e.weightMinutes), 0) > 0
          ORDER BY COALESCE(SUM(e.weightMinutes), 0) DESC, ue.user.userId ASC
          """)
    List<RatingAggregateDTO> findMonthlyUserRatingAggregates(@Param("from") LocalDateTime from);

}
