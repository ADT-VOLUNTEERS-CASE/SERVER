package org.adt.volunteerscase.unit.service;

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
import org.adt.volunteerscase.service.impl.LocationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    private LocationService locationService;
    private LocationCreateRequest createRequest;
    private LocationEntity existingLocation;

    @BeforeEach
    void setUp() {
        locationService = new LocationServiceImpl(locationRepository);

        createRequest = LocationCreateRequest.builder()
                .address("г. Москва, ул. Ленина, 10")
                .additionalNotes("Вход со двора")
                .latitude(55.7558)
                .longitude(37.6173)
                .build();

        existingLocation = LocationEntity.builder()
                .locationId(1)
                .address("г. Москва, ул. Тверская, 1")
                .additionalNotes("Слева от арки")
                .latitude(55.7570)
                .longitude(37.6150)
                .build();
    }

    @Test
    void createLocation_shouldSaveLocationWithRequestData() {
        when(locationRepository.existsByAddress(createRequest.getAddress())).thenReturn(false);

        ArgumentCaptor<LocationEntity> locationCaptor = ArgumentCaptor.forClass(LocationEntity.class);

        locationService.createLocation(createRequest);

        verify(locationRepository).existsByAddress(createRequest.getAddress());
        verify(locationRepository).save(locationCaptor.capture());

        LocationEntity savedLocation = locationCaptor.getValue();
        assertThat(savedLocation.getLocationId()).isNull();
        assertThat(savedLocation.getAddress()).isEqualTo(createRequest.getAddress());
        assertThat(savedLocation.getAdditionalNotes()).isEqualTo(createRequest.getAdditionalNotes());
        assertThat(savedLocation.getLatitude()).isEqualTo(createRequest.getLatitude());
        assertThat(savedLocation.getLongitude()).isEqualTo(createRequest.getLongitude());
    }

    @Test
    void createLocation_shouldThrowException_whenAddressAlreadyExists() {
        when(locationRepository.existsByAddress(createRequest.getAddress())).thenReturn(true);

        assertThatThrownBy(() -> locationService.createLocation(createRequest))
                .isInstanceOf(LocationAlreadyExistsException.class)
                .hasMessage("location with address " + createRequest.getAddress() + " already exists");

        verify(locationRepository).existsByAddress(createRequest.getAddress());
        verify(locationRepository, never()).save(any(LocationEntity.class));
    }

    @Test
    void createLocation_shouldThrowException_whenSaveViolatesUniqueConstraint() {
        when(locationRepository.existsByAddress(createRequest.getAddress())).thenReturn(false);
        when(locationRepository.save(any(LocationEntity.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThatThrownBy(() -> locationService.createLocation(createRequest))
                .isInstanceOf(LocationAlreadyExistsException.class)
                .hasMessage("location with address " + createRequest.getAddress() + " already exists");

        verify(locationRepository).existsByAddress(createRequest.getAddress());
        verify(locationRepository).save(any(LocationEntity.class));
    }

    @Test
    void searchLocations_shouldReturnMappedPageResponse() {
        LocationSearchRequest searchRequest = LocationSearchRequest.builder()
                .address("Москва")
                .build();

        Pageable pageable = PageRequest.of(0, 2);

        LocationEntity secondLocation = LocationEntity.builder()
                .locationId(2)
                .address("г. Москва, ул. Арбат, 12")
                .additionalNotes("Вход через шлагбаум")
                .latitude(55.7522)
                .longitude(37.5927)
                .build();

        Page<LocationEntity> locationPage = new PageImpl<>(
                List.of(existingLocation, secondLocation),
                pageable,
                3
        );

        when(locationRepository.searchByAddress("Москва", pageable)).thenReturn(locationPage);

        PageResponse<LocationResponse> response = locationService.searchLocations(searchRequest, pageable);

        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getPageNumber()).isEqualTo(0);
        assertThat(response.getPageSize()).isEqualTo(2);
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getTotalPages()).isEqualTo(2);
        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isFalse();

        LocationResponse firstLocation = response.getContent().get(0);
        assertThat(firstLocation.getLocationId()).isEqualTo(1);
        assertThat(firstLocation.getAddress()).isEqualTo("г. Москва, ул. Тверская, 1");
        assertThat(firstLocation.getAdditionalNotes()).isEqualTo("Слева от арки");
        assertThat(firstLocation.getLatitude()).isEqualTo(55.7570);
        assertThat(firstLocation.getLongitude()).isEqualTo(37.6150);

        verify(locationRepository).searchByAddress("Москва", pageable);
    }

    @Test
    void updateLocation_shouldUpdateOnlyProvidedFieldsAndReturnResponse() {
        LocationPatchRequest patchRequest = LocationPatchRequest.builder()
                .address("г. Москва, ул. Новый Арбат, 15")
                .longitude(37.6000)
                .build();

        when(locationRepository.findByLocationId(1)).thenReturn(Optional.of(existingLocation));
        when(locationRepository.existsByAddress("г. Москва, ул. Новый Арбат, 15")).thenReturn(false);

        LocationPatchResponse response = locationService.updateLocation(patchRequest, 1);

        assertThat(existingLocation.getAddress()).isEqualTo("г. Москва, ул. Новый Арбат, 15");
        assertThat(existingLocation.getLongitude()).isEqualTo(37.6000);
        assertThat(existingLocation.getAdditionalNotes()).isEqualTo("Слева от арки");
        assertThat(existingLocation.getLatitude()).isEqualTo(55.7570);

        assertThat(response.getLocationId()).isEqualTo(1);
        assertThat(response.getAddress()).isEqualTo("г. Москва, ул. Новый Арбат, 15");
        assertThat(response.getLongitude()).isEqualTo(37.6000);
        assertThat(response.getLatitude()).isEqualTo(55.7570);
        assertThat(response.getAdditionalNotes()).isEqualTo("Слева от арки");

        //verify(locationRepository).findByLocationId(1);
        //verify(locationRepository).existsByAddress("г. Москва, ул. Новый Арбат, 15");
        ArgumentCaptor<LocationEntity> savedCaptor = ArgumentCaptor.forClass(LocationEntity.class);
        verify(locationRepository).save(savedCaptor.capture());
        assertThat(savedCaptor.getValue().getAddress()).isEqualTo("г. Москва, ул. Новый Арбат, 15");
        assertThat(savedCaptor.getValue().getLongitude()).isEqualTo(37.6000);
    }

    @Test
    void updateLocation_shouldThrowException_whenLocationNotFound() {
        when(locationRepository.findByLocationId(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.updateLocation(LocationPatchRequest.builder().build(), 99))
                .isInstanceOf(LocationNotFoundException.class)
                .hasMessage("location with id - 99 not found");

        verify(locationRepository).findByLocationId(99);
        verify(locationRepository, never()).save(any(LocationEntity.class));
    }

    @Test
    void updateLocation_shouldThrowException_whenNewAddressAlreadyExists() {
        LocationPatchRequest patchRequest = LocationPatchRequest.builder()
                .address("г. Москва, ул. Арбат, 12")
                .build();

        when(locationRepository.findByLocationId(1)).thenReturn(Optional.of(existingLocation));
        when(locationRepository.existsByAddress("г. Москва, ул. Арбат, 12")).thenReturn(true);

        assertThatThrownBy(() -> locationService.updateLocation(patchRequest, 1))
                .isInstanceOf(LocationAlreadyExistsException.class)
                .hasMessage("location with address г. Москва, ул. Арбат, 12 already exists");

        verify(locationRepository).findByLocationId(1);
        verify(locationRepository).existsByAddress("г. Москва, ул. Арбат, 12");
        verify(locationRepository, never()).save(any(LocationEntity.class));
    }

    @Test
    void updateLocation_shouldSkipDuplicateCheck_whenAddressIsUnchanged() {
        LocationPatchRequest patchRequest = LocationPatchRequest.builder()
                .address("г. Москва, ул. Тверская, 1")
                .additionalNotes("Обновленная заметка")
                .build();

        when(locationRepository.findByLocationId(1)).thenReturn(Optional.of(existingLocation));

        LocationPatchResponse response = locationService.updateLocation(patchRequest, 1);

        assertThat(existingLocation.getAddress()).isEqualTo("г. Москва, ул. Тверская, 1");
        assertThat(existingLocation.getAdditionalNotes()).isEqualTo("Обновленная заметка");

        assertThat(response.getAddress()).isEqualTo("г. Москва, ул. Тверская, 1");
        assertThat(response.getAdditionalNotes()).isEqualTo("Обновленная заметка");

        verify(locationRepository).findByLocationId(1);
        verify(locationRepository, never()).existsByAddress(anyString());
        verify(locationRepository).save(existingLocation);
    }

    @Test
    void updateLocation_shouldThrowException_whenSaveViolatesUniqueConstraint() {
        LocationPatchRequest patchRequest = LocationPatchRequest.builder()
                .address("г. Москва, ул. Пушкина, 7")
                .build();

        when(locationRepository.findByLocationId(1)).thenReturn(Optional.of(existingLocation));
        when(locationRepository.existsByAddress("г. Москва, ул. Пушкина, 7")).thenReturn(false);
        when(locationRepository.save(any(LocationEntity.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThatThrownBy(() -> locationService.updateLocation(patchRequest, 1))
                .isInstanceOf(LocationAlreadyExistsException.class)
                .hasMessage("location with address г. Москва, ул. Пушкина, 7 already exists");

        verify(locationRepository).findByLocationId(1);
        verify(locationRepository).existsByAddress("г. Москва, ул. Пушкина, 7");
        verify(locationRepository).save(existingLocation);
    }

}

