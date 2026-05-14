package org.adt.volunteerscase.repository;

import org.adt.volunteerscase.entity.rating.CoordinatorRatingEntity;
import org.adt.volunteerscase.entity.rating.RatingPeriod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CoordinatorRatingRepository extends JpaRepository<CoordinatorRatingEntity, Long> {

    Page<CoordinatorRatingEntity> findByPeriodOrderByRatingPositionAscCoordinatorUserIdAsc(
            RatingPeriod period,
            Pageable pageable
    );

    Optional<CoordinatorRatingEntity> findByPeriodAndCoordinatorUserId(
            RatingPeriod period,
            Integer coordinatorId
    );

    @Modifying
    @Query("DELETE FROM CoordinatorRatingEntity rating WHERE rating.period = :period")
    void deleteByPeriod(@Param("period") RatingPeriod period);
}
