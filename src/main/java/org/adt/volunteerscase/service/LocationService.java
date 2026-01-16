package org.adt.volunteerscase.service;

import org.adt.volunteerscase.dto.location.request.LocationCreateRequest;

public interface LocationService {

    void createLocation(LocationCreateRequest request);
}
