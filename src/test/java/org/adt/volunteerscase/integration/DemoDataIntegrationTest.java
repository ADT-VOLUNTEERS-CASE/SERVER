package org.adt.volunteerscase.integration;

import org.adt.volunteerscase.VolunteersCaseApplication;
import org.adt.volunteerscase.dto.userEvent.request.UserEventStatusPatchRequest;
import org.adt.volunteerscase.entity.rating.RatingPeriod;
import org.adt.volunteerscase.exception.EventCapacityExceededException;
import org.adt.volunteerscase.repository.*;
import org.adt.volunteerscase.service.*;
import org.adt.volunteerscase.service.storage.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/** Run only against a disposable PostgreSQL database: both profiles recreate its tables. */
@EnabledIfEnvironmentVariable(named = "DEMO_TEST_DB_URL", matches = "jdbc:postgresql:.*")
class DemoDataIntegrationTest {
    @Configuration(proxyBeanMethods = false)
    static class StorageConfig {
        @Bean @Primary
        ObjectStorageService demoTestStorage() {
            return new ObjectStorageService() {
                public StoredObjectResult uploadCover(String name, String type, byte[] bytes) {
                    String key = "test/" + UUID.randomUUID() + "-" + name;
                    return StoredObjectResult.builder().bucket("test").objectKey(key)
                            .link("https://example.com/" + key).eTag("test").build();
                }
                public void deleteObject(String key) { }
            };
        }
    }

    @Test
    void bothProfilesSeedMobileDebugAccountsAndResetDatabaseOnRestart() {
        for (String profile : List.of("prod", "dev")) {
            Map<String, String> props = new LinkedHashMap<>();
            props.put("spring.profiles.active", profile);
            props.put("server.port", "0");
            props.put("DB_URL", System.getenv("DEMO_TEST_DB_URL"));
            props.put("DB_USERNAME", System.getenv().getOrDefault("DEMO_TEST_DB_USER", "postgres"));
            props.put("DB_USRNAME", props.get("DB_USERNAME"));
            props.put("DB_PASSWORD", System.getenv().getOrDefault("DEMO_TEST_DB_PASSWORD", "test"));
            props.put("SECRET_KEY", Base64.getEncoder().encodeToString(new byte[32]));
            props.put("demo.mobile-debug-login", "true");
            // Deliberately differ from the mobile passwords to verify debug compatibility.
            for (String key : List.of("ADMIN_PASSWORD", "COORDINATOR_PASSWORD", "BASE_USER_PASSWORD",
                    "BASE_USER_TWO_PASSWORD", "BASE_USER_THREE_PASSWORD")) props.put(key, "test-password");
            props.put("S3_ENDPOINT", "http://127.0.0.1:1");
            props.put("S3_REGION", "us-east-1");
            props.put("S3_BUCKET", "test");
            props.put("S3_ACCESS_KEY", "test");
            props.put("S3_SECRET_KEY", "test");
            props.put("S3_PUBLIC_BASE_URL", "https://example.com");
            props.put("logging.level.root", "ERROR");
            props.put("spring.main.banner-mode", "off");
            String[] args = props.entrySet().stream().map(e -> "--" + e.getKey() + "=" + e.getValue()).toArray(String[]::new);
            try (var context = new SpringApplicationBuilder(VolunteersCaseApplication.class, StorageConfig.class).run(args)) {
                var jdbc = context.getBean(JdbcTemplate.class);
                assertThat(jdbc.queryForObject("SELECT count(*) FROM users", Integer.class)).isEqualTo(106);
                assertThat(jdbc.queryForObject("SELECT count(*) FROM event", Integer.class)).isEqualTo(85);
                assertThat(jdbc.queryForObject("SELECT count(*) FROM user_events", Integer.class)).isEqualTo(1547);
                assertThat(jdbc.queryForObject("SELECT count(*) FROM tag WHERE tag_name = 'restart-sentinel'", Integer.class)).isZero();
                assertThat(jdbc.queryForObject("SELECT count(*) FROM event WHERE (status = 'COMPLETED') <> (date_timestamp < localtimestamp)", Integer.class)).isZero();
                assertThat(jdbc.queryForObject("SELECT count(*) FROM user_events ue JOIN event e USING(event_id) JOIN users u USING(user_id) "
                        + "WHERE ue.created_at >= e.date_timestamp OR ue.created_at < u.created_at", Integer.class)).isZero();
                var users = context.getBean(UserRepository.class);
                var encoder = context.getBean(PasswordEncoder.class);
                for (var credentials : Map.of("user@example.com", "base_pass", "coordinator@example.com", "coordinator_pass", "admin@example.com", "admin_pass").entrySet()) {
                    var account = users.findActiveByEmailWithAuth(credentials.getKey()).orElseThrow();
                    assertThat(encoder.matches(credentials.getValue(), account.getUserAuth().getPasswordHash())).isTrue();
                }
                var user = users.findByEmail("user@example.com").orElseThrow();
                int userId = user.getUserId();
                int coordinatorId = users.findByEmail("coordinator@example.com").orElseThrow().getUserId();
                var events = context.getBean(EventRepository.class);
                var page = PageRequest.of(0, 100);
                assertThat(context.getBean(StatisticsService.class).getUserStatistics(userId).getTotalParticipatedEvents()).isGreaterThan(25);
                assertThat(context.getBean(RatingService.class).getUserRating(RatingPeriod.OVERALL, page).getTotalElements()).isGreaterThan(80);
                assertThat(context.getBean(RatingService.class).getCoordinatorRating(RatingPeriod.MONTHLY, page).getTotalElements()).isEqualTo(6);
                assertThat(events.findRecommendedEventIds(userId, LocalDateTime.now(), page).getTotalElements()).isGreaterThan(8);
                assertThat(context.getBean(UserService.class).getCurrentUser(user).getEvents()).hasSizeGreaterThan(10);
                var applications = context.getBean(UserEventService.class);
                assertThat(applications.getMyEventApplicationSummaries(coordinatorId, page).getTotalElements()).isEqualTo(61);
                int ecoId = events.findByName("Экофорум: команда волонтёров").orElseThrow().getEventId();
                assertThat(applications.createApplication(ecoId, userId).getStatus()).isEqualTo("PENDING");
                assertThat(applications.updateApplicationStatus(ecoId, userId, UserEventStatusPatchRequest.builder()
                        .status("ACCEPTED").build(), coordinatorId).getStatus()).isEqualTo("ACCEPTED");
                int briefingId = events.findByName("Фотокоманда: практический инструктаж").orElseThrow().getEventId();
                assertThatThrownBy(() -> applications.createApplication(briefingId, userId)).isInstanceOf(EventCapacityExceededException.class);
                if (profile.equals("prod")) {
                    jdbc.update("INSERT INTO tag(tag_name) VALUES ('restart-sentinel')");
                    jdbc.update("UPDATE event SET date_timestamp = '2000-01-01' WHERE event_id = ?", ecoId);
                }
            }
        }
    }
}
