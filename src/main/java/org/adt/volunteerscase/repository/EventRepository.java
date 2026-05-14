package org.adt.volunteerscase.repository;

import jakarta.persistence.LockModeType;
import org.adt.volunteerscase.dto.rating.RatingAggregateDTO;
import org.adt.volunteerscase.entity.CoordinatorEntity;
import org.adt.volunteerscase.entity.CoverEntity;
import org.adt.volunteerscase.entity.LocationEntity;
import org.adt.volunteerscase.entity.TagEntity;
import org.adt.volunteerscase.entity.event.EventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.adt.volunteerscase.entity.event.EventStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Integer> {

    Optional<EventEntity> findByEventId(Integer eventId);

    Optional<EventEntity> findByName(String name);

    Optional<EventEntity> findByCover(CoverEntity coverEntity);

    boolean existsByLocation(LocationEntity locationEntity);

    boolean existsByCover(CoverEntity coverEntity);

    boolean existsByName(String name);

    boolean existsByLocationAndEventIdNot(LocationEntity location, Integer eventId);

    boolean existsByCoverAndEventIdNot(CoverEntity cover, Integer eventId);

    boolean existsByTagsContaining(TagEntity tag);

    boolean existsByDateTimestamp(LocalDateTime dateTimestamp);

    boolean existsByLocationAndDateTimestamp(LocationEntity location, LocalDateTime dateTimestamp);

    boolean existsByLocationAndDateTimestampAndEventIdNot(LocationEntity location, LocalDateTime dateTimestamp, Integer eventId);

    boolean existsByCoordinator(CoordinatorEntity coordinator);

    Page<EventEntity> findAllByOrderByDateTimestampDesc(Pageable pageable);

    @Query(
            value = """
                    SELECT e.event_id
                    FROM event e
                    LEFT JOIN event_tags et
                        ON et.event_id = e.event_id
                    LEFT JOIN user_tags ut
                        ON ut.tag_id = et.tag_id
                       AND ut.user_id = :userId
                    LEFT JOIN user_events ue_popularity
                        ON ue_popularity.event_id = e.event_id
                       AND ue_popularity.revoked = FALSE
                       AND ue_popularity.deleted_at IS NULL
                    WHERE e.status <> 'COMPLETED'
                      AND e.date_timestamp > :now
                      AND NOT EXISTS (
                          SELECT 1
                          FROM user_events ue_self
                          WHERE ue_self.event_id = e.event_id
                            AND ue_self.user_id = :userId
                            AND ue_self.deleted_at IS NULL
                      )
                    GROUP BY e.event_id, e.date_timestamp
                    ORDER BY COUNT(DISTINCT ut.tag_id) DESC,
                             COUNT(DISTINCT ue_popularity.user_id) DESC,
                             e.date_timestamp DESC,
                             e.event_id DESC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM event e
                    WHERE e.status <> 'COMPLETED'
                      AND e.date_timestamp > :now
                      AND NOT EXISTS (
                          SELECT 1
                          FROM user_events ue_self
                          WHERE ue_self.event_id = e.event_id
                            AND ue_self.user_id = :userId
                            AND ue_self.deleted_at IS NULL
                      )
                    """,
            nativeQuery = true
    )
    Page<Integer> findRecommendedEventIds(
            @Param("userId") Integer userId,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"cover", "coordinator", "location", "tags" })
    @Query("SELECT DISTINCT e FROM EventEntity e WHERE e.eventId IN :eventIds")
    List<EventEntity> findDetailedByEventIdIn(@Param("eventIds") Collection<Integer> eventIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from EventEntity e where e.eventId = :eventId")
    Optional<EventEntity> findByEventIdForUpdate(@Param("eventId") Integer eventId);

    @Query("SELECT e FROM EventEntity e WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%')) ESCAPE '\\'")
    Page<EventEntity> searchByName(@Param("name") String name, Pageable pageable);

    @Query("""
          SELECT COUNT(e)
          FROM EventEntity e
          WHERE e.coordinator.userId = :coordinatorId
             AND e.status = org.adt.volunteerscase.entity.event.EventStatus.COMPLETED
          """)
    long countCompletedEventsByCoordinatorId(
            @Param("coordinatorId") Integer coordinatorId,
            @Param("eventStatus") EventStatus eventStatus
    );

    @Query("""
          SELECT COALESCE(SUM(e.weightMinutes), 0)
          FROM EventEntity e
          WHERE e.coordinator.userId = :coordinatorId
            AND e.status = :eventStatus
          """)
    long sumCompletedEventWeightMinutesByCoordinatorId(
            @Param("coordinatorId") Integer coordinatorId
    );

    @Query("""
          SELECT new org.adt.volunteerscase.dto.rating.RatingAggregateDTO(
              e.coordinator.userId,
              COALESCE(SUM(e.weightMinutes), 0)
          )
          FROM EventEntity e
          WHERE e.coordinator.user.deletedAt IS NULL
            AND e.status = org.adt.volunteerscase.entity.event.EventStatus.COMPLETED
          GROUP BY e.coordinator.userId
          HAVING COALESCE(SUM(e.weightMinutes), 0) > 0
          ORDER BY COALESCE(SUM(e.weightMinutes), 0) DESC, e.coordinator.userId ASC
          """)
    List<RatingAggregateDTO> findOverallCoordinatorRatingAggregates();

    @Query("""
          SELECT new org.adt.volunteerscase.dto.rating.RatingAggregateDTO(
              e.coordinator.userId,
              COALESCE(SUM(e.weightMinutes), 0)
          )
          FROM EventEntity e
          WHERE e.coordinator.user.deletedAt IS NULL
            AND e.status = org.adt.volunteerscase.entity.event.EventStatus.COMPLETED
            AND e.dateTimestamp >= :from
          GROUP BY e.coordinator.userId
          HAVING COALESCE(SUM(e.weightMinutes), 0) > 0
          ORDER BY COALESCE(SUM(e.weightMinutes), 0) DESC, e.coordinator.userId ASC
          """)
    List<RatingAggregateDTO> findMonthlyCoordinatorRatingAggregates(@Param("from") LocalDateTime from);
}
