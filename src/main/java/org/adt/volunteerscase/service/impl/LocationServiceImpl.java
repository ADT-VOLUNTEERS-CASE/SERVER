package org.adt.volunteerscase.service.impl;

import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.dto.location.request.LocationCreateRequest;
import org.adt.volunteerscase.dto.location.request.LocationPatchRequest;
import org.adt.volunteerscase.dto.location.request.LocationSearchRequest;
import org.adt.volunteerscase.dto.location.response.LocationPatchResponse;
import org.adt.volunteerscase.dto.location.response.LocationResponse;
import org.adt.volunteerscase.dto.page.response.PageResponse;
import org.adt.volunteerscase.entity.LocationEntity;
import org.adt.volunteerscase.exception.LocationAlreadyExistsException;
import org.adt.volunteerscase.exception.LocationNotFoundException;
import org.adt.volunteerscase.repository.LocationRepository;
import org.adt.volunteerscase.service.LocationService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;

    @Override
    public void createLocation(LocationCreateRequest request) {
        if (locationRepository.existsByAddress(request.getAddress())) {
            throw new LocationAlreadyExistsException("location with address " + request.getAddress() + " already exists");
        }
        LocationEntity locationEntity = LocationEntity.builder()
                .address(request.getAddress())
                .additionalNotes(request.getAdditionalNotes())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude()).build();

        try {
            locationRepository.save(locationEntity);
        } catch (DataIntegrityViolationException ex) {
            throw new LocationAlreadyExistsException("location with address " + request.getAddress() + " already exists");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LocationResponse> searchLocations(LocationSearchRequest request, Pageable pageable) {
        Page<LocationEntity> locationPage = locationRepository.searchByAddress(request.getAddress(), pageable);

        List<LocationResponse> content = locationPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PageResponse.of(new PageImpl<>(content, pageable, locationPage.getTotalElements()));
    }

    @Override
    public LocationPatchResponse updateLocation(LocationPatchRequest request, Integer locationId) {
        LocationEntity locationEntity = locationRepository.findByLocationId(locationId)
                .orElseThrow(() -> new LocationNotFoundException("location with id - " + locationId + " not found"));

        if (request.getAddress() != null){
            if (locationRepository.existsByAddress(request.getAddress())){
                throw new LocationAlreadyExistsException("location with address " + request.getAddress() + " already exists");
            }
            locationEntity.setAddress(request.getAddress());
        }

        if (request.getAdditionalNotes() != null){
            locationEntity.setAdditionalNotes(request.getAdditionalNotes());
        }

        if (request.getLatitude() != null){
            locationEntity.setLatitude(request.getLatitude());
        }

        if (request.getLongitude() != null){
            locationEntity.setLongitude(request.getLongitude());
        }

        try {
            locationRepository.save(locationEntity);
        } catch (DataIntegrityViolationException ex) {
            throw new LocationAlreadyExistsException("location with address " + request.getAddress() + " already exists");
        }
        return LocationPatchResponse.builder()
                .locationId(locationEntity.getLocationId())
                .address(locationEntity.getAddress())
                .additionalNotes(locationEntity.getAdditionalNotes())
                .longitude(locationEntity.getLongitude())
                .latitude(locationEntity.getLatitude()).build();
    }


    @Transactional(readOnly = true)
    public LocationResponse convertToResponse(LocationEntity location) {
        if (location == null) {
            return null;
        }
        return LocationResponse.builder()
                .locationId(location.getLocationId())
                .address(location.getAddress())
                .additionalNotes(location.getAdditionalNotes())
                .longitude(location.getLongitude())
                .latitude(location.getLatitude())
                .build();
    }
}
