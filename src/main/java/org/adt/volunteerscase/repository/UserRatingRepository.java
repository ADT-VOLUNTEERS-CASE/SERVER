package org.adt.volunteerscase.repository;

import org.adt.volunteerscase.entity.rating.RatingPeriod;
import org.adt.volunteerscase.entity.rating.UserRatingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRatingRepository extends JpaRepository<UserRatingEntity, Long> {

    Page<UserRatingEntity> findByPeriodOrderByRatingPositionAscUserUserIdAsc(
            RatingPeriod period,
            Pageable pageable
    );

    Optional<UserRatingEntity> findByPeriodAndUserUserId(
            RatingPeriod period,
            Integer userId
    );

    @Modifying
    @Query("DELETE FROM UserRatingEntity rating WHERE rating.period = :period")
    void deleteByPeriod(@Param("period") RatingPeriod period);
}
