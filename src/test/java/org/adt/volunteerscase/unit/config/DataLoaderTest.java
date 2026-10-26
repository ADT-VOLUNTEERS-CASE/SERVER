package org.adt.volunteerscase.unit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.adt.volunteerscase.config.DataLoader;
import org.adt.volunteerscase.dto.cover.CoverMapper;
import org.adt.volunteerscase.entity.*;
import org.adt.volunteerscase.entity.event.EventEntity;
import org.adt.volunteerscase.entity.event.EventStatus;
import org.adt.volunteerscase.entity.user.UserEntity;
import org.adt.volunteerscase.repository.*;
import org.adt.volunteerscase.service.security.RefreshTokenService;
import org.adt.volunteerscase.service.storage.ObjectStorageService;
import org.adt.volunteerscase.service.storage.StoredObjectResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataLoaderTest {
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private LocationRepository locationRepository;
    @Mock private CoverRepository coverRepository;
    @Mock private TagRepository tagRepository;
    @Mock private EventRepository eventRepository;
    @Mock private CoordinatorRepository coordinatorRepository;
    @Mock private UserEventRepository userEventRepository;
    @Mock private ObjectStorageService storage;

    private final CoverMapper coverMapper = new CoverMapper(new ObjectMapper());
    private final Map<String, UserEntity> users = new LinkedHashMap<>();
    private final Map<String, LocationEntity> locations = new LinkedHashMap<>();
    private final Map<String, EventEntity> events = new LinkedHashMap<>();
    private final Map<UserEventId, UserEventEntity> applications = new LinkedHashMap<>();
    private int nextTagId = 1;
    private int nextCoverId = 1;

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userRepository.save(any())).thenAnswer(call -> {
            UserEntity user = call.getArgument(0);
            if (user.getUserId() == null) {
                user.setUserId(users.size() + 1);
            }
            users.put(user.getEmail(), user);
            return user;
        });
        when(coordinatorRepository.save(any())).thenAnswer(call -> {
            CoordinatorEntity coordinator = call.getArgument(0);
            coordinator.setUserId(coordinator.getUser().getUserId());
            return coordinator;
        });
        when(locationRepository.save(any())).thenAnswer(call -> {
            LocationEntity location = call.getArgument(0);
            location.setLocationId(locations.size() + 1);
            locations.put(location.getAddress(), location);
            return location;
        });
        when(locationRepository.findByAddress(anyString())).thenAnswer(call ->
                Optional.ofNullable(locations.get(call.getArgument(0))));
        when(tagRepository.save(any())).thenAnswer(call -> {
            TagEntity tag = call.getArgument(0);
            tag.setTagId(nextTagId++);
            return tag;
        });
        when(storage.uploadCover(anyString(), eq("image/png"), any(byte[].class))).thenAnswer(call ->
                StoredObjectResult.builder()
                        .bucket("test-covers")
                        .objectKey("covers/" + nextCoverId + "-" + call.getArgument(0))
                        .link("https://example.com/covers/" + nextCoverId)
                        .eTag("test-etag")
                        .build());
        when(coverRepository.save(any())).thenAnswer(call -> {
            CoverEntity cover = call.getArgument(0);
            cover.setCoverId(nextCoverId++);
            return cover;
        });
        when(eventRepository.saveAndFlush(any())).thenAnswer(call -> {
            EventEntity event = call.getArgument(0);
            event.setEventId(events.size() + 1);
            events.put(event.getName(), event);
            return event;
        });
        when(userEventRepository.saveAndFlush(any())).thenAnswer(call -> {
            UserEventEntity application = call.getArgument(0);
            applications.put(application.getId(), application);
            return application;
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-10-26T12:00:00", "2026-12-31T23:59:59",
            "2027-01-01T00:00:01", "2028-02-29T23:59:00", "2028-03-01T00:00:00"})
    void seedsConsistentDatesAndApplicationsAcrossCalendarBoundaries(String timestamp) throws Exception {
        LocalDateTime now = LocalDateTime.parse(timestamp);
        load(now);

        assertThat(events).hasSize(13);
        assertThat(events.values()).filteredOn(e -> e.getStatus() == EventStatus.ONGOING)
                .hasSize(7).allSatisfy(e -> {
                    assertThat(e.getDateTimestamp()).isAfter(now).isBefore(now.plusDays(15));
                });
        assertThat(events.values()).filteredOn(e -> e.getStatus() == EventStatus.COMPLETED)
                .hasSize(6).allSatisfy(e -> assertThat(e.getDateTimestamp()).isBefore(now));

        assertThat(applications).hasSize(29);
        assertThat(applications.values()).allSatisfy(a -> {
            assertThat(a.getCreatedAt()).isAfterOrEqualTo(a.getUser().getCreatedAt())
                    .isBefore(a.getEvent().getDateTimestamp()).isBeforeOrEqualTo(now);
            int statusFlags = (a.isAccepted() ? 1 : 0) + (a.isRejected() ? 1 : 0) + (a.isRevoked() ? 1 : 0);
            assertThat(statusFlags).isLessThanOrEqualTo(1);
            assertThat(a.getRejectedAt() != null).isEqualTo(a.isRejected());
            assertThat(a.getRejectReason() != null).isEqualTo(a.isRejected());
            assertThat(a.getRevokedAt() != null).isEqualTo(a.isRevoked());
            for (LocalDateTime action : new LocalDateTime[]{a.getRejectedAt(), a.getRevokedAt(), a.getDeletedAt()}) {
                if (action != null) {
                    assertThat(action).isAfterOrEqualTo(a.getCreatedAt())
                            .isBefore(a.getEvent().getDateTimestamp()).isBeforeOrEqualTo(now);
                }
            }
            if (a.getEvent().getStatus() == EventStatus.COMPLETED) {
                assertThat(a.isAccepted() || a.isRejected() || a.isRevoked()).isTrue();
            }
        });
    }

    @Test
    void providesUsableVolunteerProfilesHistoryRecommendationsAndCapacityScenarios() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 10, 26, 12, 0);
        load(now);

        for (String email : List.of("user@example.com", "user.two@example.com", "user.three@example.com")) {
            UserEntity user = users.get(email);
            List<UserEventEntity> upcoming = applications.values().stream()
                    .filter(a -> a.getUser() == user && a.getDeletedAt() == null)
                    .filter(a -> a.getEvent().getDateTimestamp().isAfter(now)).toList();
            assertThat(upcoming).anyMatch(UserEventEntity::isAccepted)
                    .anyMatch(a -> !a.isAccepted() && !a.isRejected() && !a.isRevoked());
            assertThat(events.values()).anyMatch(e -> e.getDateTimestamp().isAfter(now)
                    && applications.values().stream().noneMatch(a -> a.getUser() == user
                    && a.getEvent() == e && a.getDeletedAt() == null));
            assertThat(applications.values()).anyMatch(a -> a.getUser() == user && a.isAccepted()
                    && a.getEvent().getStatus() == EventStatus.COMPLETED
                    && a.getEvent().getDateTimestamp().isAfter(now.minusMonths(1)));
        }

        List<YearMonth> historyMonths = applications.values().stream()
                .filter(a -> a.getUser() == users.get("user@example.com") && a.isAccepted())
                .filter(a -> a.getEvent().getStatus() == EventStatus.COMPLETED)
                .map(a -> YearMonth.from(a.getEvent().getDateTimestamp())).distinct().toList();
        for (int i = 0; i <= 4; i++) {
            assertThat(historyMonths).contains(YearMonth.from(now).minusMonths(i));
        }
        assertThat(applications.values()).anyMatch(UserEventEntity::isRejected)
                .anyMatch(UserEventEntity::isRevoked).anyMatch(a -> a.getDeletedAt() != null);
        assertThat(applications.values()).noneMatch(a -> a.getUser().isAdmin() || a.getUser().isCoordinator());
        assertThat(events.values()).anyMatch(e -> applications.values().stream().noneMatch(a -> a.getEvent() == e));
        assertThat(events.values()).anyMatch(e -> e.getMaxCapacity() == activeApplications(e));
        assertThat(events.values()).allSatisfy(e -> assertThat(activeApplications(e)).isLessThanOrEqualTo(e.getMaxCapacity()));

        assertThat(events.values()).allSatisfy(e -> {
            assertThat(e.getCover()).isNotNull();
            assertThat(e.getWeightMinutes()).isPositive();
            assertThat(e.getTags()).isNotEmpty();
            var metadata = coverMapper.decodeMetadata(e.getCover().getMetadata());
            assertThat(metadata.getWidth()).isPositive();
            assertThat(metadata.getHeight()).isPositive();
            assertThat(metadata.getSize()).isPositive();
        });
        assertThat(events.values().stream().map(e -> e.getCover().getCoverId())).doesNotHaveDuplicates();
        assertThat(events.values().stream()
                .map(e -> coverMapper.decodeMetadata(e.getCover().getMetadata()).getOriginalFileName()).distinct())
                .containsExactlyInAnyOrder("park-cleanup.png", "animal-shelter.png", "park_sokolniky.png",
                        "theater_square.png", "photo-marathon.png", "eco-forum.png");
    }

    private long activeApplications(EventEntity event) {
        return applications.values().stream().filter(a -> a.getEvent() == event
                && a.getDeletedAt() == null && !a.isRejected() && !a.isRevoked()).count();
    }

    private void load(LocalDateTime now) throws Exception {
        ZoneId zone = ZoneId.of("Europe/Moscow");
        Clock clock = Clock.fixed(now.atZone(zone).toInstant(), zone);
        DataLoader loader = new DataLoader(userRepository, passwordEncoder, refreshTokenService,
                locationRepository, coverRepository, tagRepository, eventRepository,
                coordinatorRepository, userEventRepository, storage, coverMapper, clock);
        for (String field : List.of("adminPassword", "baseUserPassword", "baseUserTwoPassword",
                "baseUserThreePassword", "coordinatorPassword")) {
            ReflectionTestUtils.setField(loader, field, "test-password");
        }
        loader.run();
    }
}
