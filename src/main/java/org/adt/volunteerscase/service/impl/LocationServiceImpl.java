package org.adt.volunteerscase.service.impl;

import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.dto.location.request.LocationCreateRequest;
import org.adt.volunteerscase.entity.LocationEntity;
import org.adt.volunteerscase.exception.LocationAlreadyExistsException;
import org.adt.volunteerscase.repository.LocationRepository;
import org.adt.volunteerscase.service.LocationService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;

    @Override
    public void createLocation(LocationCreateRequest request) {
        if (locationRepository.existsByAddress(request.getAddress())){
            throw new LocationAlreadyExistsException("location with address " + request.getAddress() + " already exists");
        }
        LocationEntity locationEntity = LocationEntity.builder()
                .address(request.getAddress())
                .additionalNotes(request.getAdditionalNotes())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude()).build();

        locationRepository.save(locationEntity);
    }
}
