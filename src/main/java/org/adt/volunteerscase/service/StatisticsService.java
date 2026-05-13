package org.adt.volunteerscase.service;

import org.adt.volunteerscase.dto.statistics.response.CoordinatorStatisticsResponse;
import org.adt.volunteerscase.dto.statistics.response.UserStatisticsResponse;

public interface StatisticsService {
    UserStatisticsResponse getUserStatistics(Integer userId);
    CoordinatorStatisticsResponse getCoordinatorStatistics(Integer coordinatorId);
}
