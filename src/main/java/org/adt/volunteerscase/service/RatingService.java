package org.adt.volunteerscase.service;

import org.adt.volunteerscase.dto.page.response.PageResponse;
import org.adt.volunteerscase.dto.rating.response.CoordinatorRatingResponse;
import org.adt.volunteerscase.dto.rating.response.UserRatingResponse;
import org.adt.volunteerscase.entity.rating.RatingPeriod;
import org.springframework.data.domain.Pageable;

public interface RatingService {

    void rebuildAllRatings();

    void rebuildUserRatings(RatingPeriod period);

    void rebuildCoordinatorRatings(RatingPeriod period);

    PageResponse<UserRatingResponse> getUserRating(RatingPeriod period, Pageable pageable);

    PageResponse<CoordinatorRatingResponse> getCoordinatorRating(RatingPeriod period, Pageable pageable);

    Integer getUserRatingPosition(Integer userId, RatingPeriod period);

    Integer getCoordinatorRatingPosition(Integer coordinatorId, RatingPeriod period);
}