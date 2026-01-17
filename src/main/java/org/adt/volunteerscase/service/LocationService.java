package org.adt.volunteerscase.service;

import org.adt.volunteerscase.dto.location.request.LocationCreateRequest;
import org.adt.volunteerscase.dto.location.request.LocationSearchRequest;
import org.adt.volunteerscase.dto.location.response.LocationResponse;
import org.adt.volunteerscase.dto.page.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface LocationService {

    void createLocation(LocationCreateRequest request);
    PageResponse<LocationResponse> searchLocations(LocationSearchRequest request, Pageable pageable);
}
