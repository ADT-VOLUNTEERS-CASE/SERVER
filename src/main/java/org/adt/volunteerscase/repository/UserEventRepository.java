package org.adt.volunteerscase.repository;

import org.adt.volunteerscase.entity.UserEventEntity;
import org.adt.volunteerscase.entity.UserEventId;
import org.adt.volunteerscase.entity.event.EventEntity;
import org.adt.volunteerscase.entity.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}