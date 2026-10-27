package org.adt.volunteerscase.config;

import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.entity.*;
import org.adt.volunteerscase.entity.event.EventEntity;
import org.adt.volunteerscase.entity.event.EventStatus;
import org.adt.volunteerscase.entity.user.UserAuthEntity;
import org.adt.volunteerscase.entity.user.UserEntity;
import org.adt.volunteerscase.exception.*;
import org.adt.volunteerscase.repository.*;
import org.adt.volunteerscase.service.security.RefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.adt.volunteerscase.dto.cover.CoverMapper;
import org.adt.volunteerscase.dto.cover.CoverMetadataDTO;
import org.adt.volunteerscase.service.storage.ObjectStorageService;
import org.adt.volunteerscase.service.storage.StoredObjectResult;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final LocationRepository locationRepository;
    private final CoverRepository coverRepository;
    private final TagRepository tagRepository;
    private final EventRepository eventRepository;
    private final CoordinatorRepository coordinatorRepository;
    private final UserEventRepository userEventRepository;
    private final ObjectStorageService objectStorageService;
    private final CoverMapper coverMapper;
    private final Clock clock;


    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    @Value("${BASE_USER_PASSWORD}")
    private String baseUserPassword;

    @Value("${BASE_USER_TWO_PASSWORD}")
    private String baseUserTwoPassword;

    @Value("${BASE_USER_THREE_PASSWORD}")
    private String baseUserThreePassword;


    @Value("${COORDINATOR_PASSWORD}")
    private String coordinatorPassword;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        createInitialData();
    }

    private void createInitialData() {
        // One reference time keeps event dates and the application history consistent.
        LocalDateTime seedNow = LocalDateTime.now(clock).withSecond(0).withNano(0);
        LocalDateTime registeredAt = seedNow.minusMonths(6);

        UserEntity admin = createUser("Алексей", "Смирнов", "Андреевич",
                "admin@example.com", "+67676767671", false, true, adminPassword, registeredAt);
        UserEntity coordinatorUser = createUser("Мария", "Иванова", "Сергеевна",
                "coordinator@example.com", "+8888888888", true, false, coordinatorPassword, registeredAt);
        UserEntity user = createUser("Анна", "Петрова", "Дмитриевна",
                "user@example.com", "+79999999999", false, false, baseUserPassword, registeredAt);
        UserEntity userTwo = createUser("Дмитрий", "Соколов", "Алексеевич",
                "user.two@example.com", "+79999999998", false, false, baseUserTwoPassword, registeredAt);
        UserEntity userThree = createUser("Елена", "Волкова", "Михайловна",
                "user.three@example.com", "+79999999997", false, false, baseUserThreePassword, registeredAt);
        CoordinatorEntity coordinator = createCoordinatorProfile(coordinatorUser, "Волонтёрский штаб, Москва");

        LocationEntity theater = createLocation("Театральная площадь, Москва",
                "Сбор у фонтана перед Большим театром. Координатор встретит участников с табличкой.",
                55.7589, 37.6185);
        LocationEntity ostankino = createLocation("Улица Академика Королёва, 15к1, Москва",
                "Сбор у главного входа. Возьмите заряженный телефон и удобную обувь.",
                55.819682, 37.611663);
        LocationEntity gorkyPark = createLocation("Парк Горького, Москва",
                "Встреча у центрального входа. Перчатки, мешки и инвентарь выдаём на месте.",
                55.729876, 37.603674);
        LocationEntity shelter = createLocation("Улица Зорге, 21, Москва",
                "Встреча у входа в приют. Корм и принадлежности передайте координатору.",
                55.7860, 37.5100);
        LocationEntity sokolniki = createLocation("Парк Сокольники, Москва",
                "Волонтёрский штаб у главного входа. Здесь проходят инструктаж и выдача бейджей.",
                55.793220, 37.679993);

        TagEntity ecology = createTag("Экология");
        TagEntity animals = createTag("Помощь животным");
        TagEntity culture = createTag("Культура");
        TagEntity sport = createTag("Спорт");
        TagEntity media = createTag("Медиа и фотография");
        TagEntity organization = createTag("Организация мероприятий");
        TagEntity social = createTag("Социальная помощь");
        TagEntity education = createTag("Образование");

        setTagsToUser(Set.of(ecology, social, organization), admin);
        setTagsToUser(Set.of(ecology, culture, organization, education), coordinatorUser);
        setTagsToUser(Set.of(ecology, social, organization), user);
        setTagsToUser(Set.of(animals, ecology, sport), userTwo);
        setTagsToUser(Set.of(culture, media, education), userThree);

        // Upcoming events have open registration and useful daytime start times.
        EventEntity parkCleanup = createEvent(EventStatus.ONGOING,
                "Чистый парк: волонтёрская уборка",
                "Поможем привести в порядок дорожки и газоны парка Горького. Соберём мусор раздельно, "
                        + "а после уборки обсудим простые экологичные привычки. Опыт не нужен, инвентарь выдадим.",
                "images/park-cleanup.png", coordinator, 25, 180,
                seedNow.toLocalDate().plusDays(1).atTime(11, 0), gorkyPark, Set.of(ecology, social));
        EventEntity animalShelter = createEvent(EventStatus.ONGOING,
                "Добрые лапы: помощь приюту",
                "Погуляем с собаками, поможем убрать вольеры и разобрать корм. Перед началом сотрудники "
                        + "приюта проведут инструктаж. Наденьте одежду, которую не жалко испачкать.",
                "images/animal-shelter.png", coordinator, 15, 240,
                seedNow.toLocalDate().plusDays(3).atTime(10, 0), shelter, Set.of(animals, social));
        EventEntity festival = createEvent(EventStatus.ONGOING,
                "Семейный фестиваль в Сокольниках",
                "Встретим гостей, поможем с навигацией и проведением творческих мастер-классов. "
                        + "Для волонтёров предусмотрены инструктаж, перерывы и горячий чай.",
                "images/park_sokolniky.png", coordinator, 30, 300,
                seedNow.toLocalDate().plusDays(5).atTime(12, 0), sokolniki, Set.of(culture, organization, education));
        EventEntity theaterDay = createEvent(EventStatus.ONGOING,
                "Театральный день: встречаем зрителей",
                "Поможем зрителям найти площадки городской театральной программы, раздадим буклеты "
                        + "и поддержим работу информационной стойки. Перед сменой познакомимся с маршрутом.",
                "images/theater_square.png", coordinator, 20, 240,
                seedNow.toLocalDate().plusDays(7).atTime(10, 0), theater, Set.of(culture, organization));
        EventEntity photoMarathon = createEvent(EventStatus.ONGOING,
                "Фотомарафон добрых дел",
                "Снимем истории городских волонтёров и поможем участникам пройти маршрут фотомарафона. "
                        + "Можно снимать на телефон. Лучшие кадры с согласия участников войдут в итоговый альбом.",
                "images/photo-marathon.png", coordinator, 12, 180,
                seedNow.toLocalDate().plusDays(10).atTime(9, 0), ostankino, Set.of(media, culture, sport));
        createEvent(EventStatus.ONGOING,
                "Экофорум: команда волонтёров",
                "Открыт набор в команду городского экофорума. Будем регистрировать гостей, помогать "
                        + "спикерам и рассказывать о раздельном сборе отходов. Обучение пройдёт перед началом смены.",
                "images/eco-forum.png", coordinator, 40, 360,
                seedNow.toLocalDate().plusDays(14).atTime(11, 0), sokolniki, Set.of(ecology, education, organization));
        EventEntity briefing = createEvent(EventStatus.ONGOING,
                "Фотокоманда: практический инструктаж",
                "Небольшая встреча команды фотомарафона: проверим оборудование, распределим задачи "
                        + "и потренируемся снимать репортаж. Все места в этой группе уже заняты заявками.",
                "images/photo-marathon.png", coordinator, 2, 90,
                seedNow.toLocalDate().plusDays(2).atTime(18, 0), ostankino, Set.of(media, education));

        // Each volunteer has accepted and pending applications, plus events still available to join.
        createUserEvent(user, parkCleanup, ApplicationStatus.ACCEPTED, seedNow.minusDays(4));
        createUserEvent(userTwo, parkCleanup, ApplicationStatus.PENDING, seedNow.minusDays(2));
        createUserEvent(userThree, parkCleanup, ApplicationStatus.REVOKED, seedNow.minusDays(3));
        createUserEvent(user, animalShelter, ApplicationStatus.PENDING, seedNow.minusDays(1));
        createUserEvent(userTwo, animalShelter, ApplicationStatus.ACCEPTED, seedNow.minusDays(3));
        createUserEvent(user, festival, ApplicationStatus.REJECTED, seedNow.minusDays(3));
        createUserEvent(userTwo, festival, ApplicationStatus.REVOKED, seedNow.minusDays(2));
        createUserEvent(userThree, festival, ApplicationStatus.ACCEPTED, seedNow.minusDays(4));
        createUserEvent(userThree, theaterDay, ApplicationStatus.PENDING, seedNow.minusDays(1));
        createUserEvent(userThree, photoMarathon, ApplicationStatus.ACCEPTED, seedNow.minusDays(2));
        createUserEvent(userTwo, briefing, ApplicationStatus.ACCEPTED, seedNow.minusDays(3));
        createUserEvent(userThree, briefing, ApplicationStatus.PENDING, seedNow.minusDays(1));

        // A deleted application is hidden from the user and allows a new application.
        UserEventEntity deleted = createUserEvent(user, theaterDay, ApplicationStatus.REVOKED, seedNow.minusDays(4));
        deleted.setDeletedAt(deleted.getRevokedAt());
        userEventRepository.saveAndFlush(deleted);

        EventEntity recentCleanup = createEvent(EventStatus.COMPLETED,
                "Чистый берег: итоги волонтёрской уборки",
                "Команда собрала мусор у прогулочных дорожек и передала вторсырьё на переработку. "
                        + "Спасибо всем участникам за помощь парку!",
                "images/park-cleanup.png", coordinator, 25, 180,
                seedNow.toLocalDate().minusDays(1).atTime(11, 0), gorkyPark, Set.of(ecology, social));
        createUserEvent(user, recentCleanup, ApplicationStatus.ACCEPTED, recentCleanup.getDateTimestamp().minusDays(7));
        createUserEvent(userTwo, recentCleanup, ApplicationStatus.ACCEPTED, recentCleanup.getDateTimestamp().minusDays(5));
        createUserEvent(userThree, recentCleanup, ApplicationStatus.ACCEPTED, recentCleanup.getDateTimestamp().minusDays(4));

        EventEntity recentFestival = createEvent(EventStatus.COMPLETED,
                "Город добрых дел: завершённый фестиваль",
                "Волонтёры помогли провести семейные мастер-классы, встретили гостей и организовали "
                        + "сбор вещей для благотворительных организаций.",
                "images/park_sokolniky.png", coordinator, 30, 300,
                seedNow.toLocalDate().minusDays(10).atTime(12, 0), sokolniki, Set.of(culture, social, organization));
        createUserEvent(user, recentFestival, ApplicationStatus.ACCEPTED, recentFestival.getDateTimestamp().minusDays(6));
        createUserEvent(userThree, recentFestival, ApplicationStatus.ACCEPTED, recentFestival.getDateTimestamp().minusDays(5));
        createUserEvent(userTwo, recentFestival, ApplicationStatus.REVOKED, recentFestival.getDateTimestamp().minusDays(4));

        // Four previous calendar months populate participation charts and overall ratings.
        for (int monthsAgo = 1; monthsAgo <= 4; monthsAgo++) {
            LocalDateTime date = seedNow.toLocalDate().minusMonths(monthsAgo).withDayOfMonth(10).atTime(10, 0);
            EventEntity past = createEvent(EventStatus.COMPLETED,
                    "Помощь приюту: встреча " + date.toLocalDate(),
                    "Волонтёры выгуляли собак, убрали территорию и подготовили корм. "
                            + "Завершённая смена учтена в истории участия и волонтёрских часах.",
                    "images/animal-shelter.png", coordinator, 15, 120 + monthsAgo * 30,
                    date, shelter, Set.of(animals, social));
            createUserEvent(user, past, ApplicationStatus.ACCEPTED, date.minusDays(7));
            createUserEvent(userTwo, past, ApplicationStatus.ACCEPTED, date.minusDays(6));
            if (monthsAgo <= 2) {
                createUserEvent(userThree, past, ApplicationStatus.ACCEPTED, date.minusDays(5));
            }
        }
    }

    private UserEntity createUser(
            String firstname,
            String lastname,
            String patronymic,
            String email,
            String phoneNumber,
            boolean isCoordinator,
            boolean isAdmin,
            String password,
            LocalDateTime createdAt
    ) {
        UserEntity userByEmail = userRepository.findByEmail(email).orElse(null);
        UserEntity userByPhone = userRepository.findByPhoneNumber(phoneNumber).orElse(null);

        if (userByEmail != null && userByPhone != null
                && !userByEmail.getUserId().equals(userByPhone.getUserId())) {
            throw new UserAlreadyExistsException(
                    "email " + email + " and phone number " + phoneNumber + " belong to different users"
            );
        }

        UserEntity user = userByEmail != null ? userByEmail : userByPhone;

        if (user != null) {
            if (!user.getEmail().equals(email) || !user.getPhoneNumber().equals(phoneNumber)) {
                throw new UserAlreadyExistsException(
                        "existing user data does not match requested seed user: email=" + email + ", phoneNumber=" + phoneNumber
                );
            }

            if (isCoordinator && !user.isCoordinator()) {
                throw new UserNotCoordinatorException(
                        "user with id - " + user.getUserId() + " exists but is not marked as coordinator"
                );
            }

            if (user.getCreatedAt() == null) {
                user.setCreatedAt(createdAt);
                userRepository.save(user);
            }

            return user;
        }

        UserAuthEntity userAuth = UserAuthEntity.builder()
                .passwordHash(passwordEncoder.encode(password))
                .build();

        UserEntity newUser = UserEntity.builder()
                .firstname(firstname)
                .lastname(lastname)
                .patronymic(patronymic)
                .email(email)
                .phoneNumber(phoneNumber)
                .isAdmin(isAdmin)
                .isCoordinator(isCoordinator)
                .userAuth(userAuth)
                .createdAt(createdAt)
                .build();

        userAuth.setUser(newUser);
        userRepository.save(newUser);
        refreshTokenService.createRefreshToken(newUser);

        return newUser;
    }


    private CoordinatorEntity createCoordinatorProfile(UserEntity user, String workLocation) {
        if (!user.isCoordinator()) {
            throw new UserNotCoordinatorException(
                    "user with id - " + user.getUserId() + " is not coordinator"
            );
        }

        return coordinatorRepository.findById(user.getUserId())
                .orElseGet(() -> coordinatorRepository.save(
                        CoordinatorEntity.builder()
                                .user(user)
                                .workLocation(workLocation)
                                .build()
                ));
    }


    private LocationEntity createLocation(String address, String additionalNotes, Double latitude, Double longitude) {
        if (!locationRepository.existsByAddress(address)) {
            LocationEntity location = LocationEntity.builder()
                    .address(address)
                    .additionalNotes(additionalNotes)
                    .latitude(latitude)
                    .longitude(longitude)
                    .build();

            locationRepository.save(location);
        }
        return locationRepository.findByAddress(address)
                .orElseThrow(() -> new LocationNotFoundException("location with address - " + address + " not found"));
    }

    private TagEntity createTag(String tagName) {

        if (tagRepository.existsByTagName(tagName)) {
            return tagRepository.findByTagName(tagName)
                    .orElseThrow(() -> new TagNotFoundException("tag with name - " + tagName + " not found"));
        }

        TagEntity tag = TagEntity.builder()
                .tagName(tagName)
                .build();
        tagRepository.save(tag);
        return tag;
    }

    private void setTagsToUser(Set<TagEntity> tags, UserEntity currentUser) {
        currentUser.setTags(new HashSet<>(tags));
        userRepository.save(currentUser);
    }

    private EventEntity createEvent(
            EventStatus status,
            String name,
            String description,
            String coverResourcePath,
            CoordinatorEntity coordinator,
            Integer maxCapacity,
            Integer weightMinutes,
            LocalDateTime dateTimestamp,
            LocationEntity location,
            Set<TagEntity> tags
    ) {
        EventEntity existingByName = eventRepository.findByName(name).orElse(null);

        if (existingByName != null) {
            if (existingByName.getCover() == null && coverResourcePath != null) {
                CoverEntity cover = createSeedCover(coverResourcePath);
                existingByName.setCover(cover);
                try {
                    return eventRepository.saveAndFlush(existingByName);
                } catch (RuntimeException ex) {
                    deleteSeedCoverObjectQuietly(cover);
                    throw ex;
                }
            }
            return existingByName;
        }

        CoverEntity cover = coverResourcePath != null ? createSeedCover(coverResourcePath) : null;

        if (location != null && eventRepository.existsByLocationAndDateTimestamp(location,
                dateTimestamp)) {
            deleteSeedCoverObjectQuietly(cover);
            throw new IllegalStateException(
                    "seed event '" + name + "' cannot be created: location '"
                            + location.getAddress()
                            + "' is already occupied at " + dateTimestamp
            );
        }

        EventEntity event = EventEntity.builder()
                .status(status)
                .name(name)
                .description(description)
                .cover(cover)
                .coordinator(coordinator)
                .maxCapacity(maxCapacity)
                .weightMinutes(weightMinutes)
                .dateTimestamp(dateTimestamp)
                .location(location)
                .tags(new HashSet<>(tags))
                .build();

        try {
            return eventRepository.saveAndFlush(event);
        } catch (RuntimeException ex) {
            deleteSeedCoverObjectQuietly(cover);
            throw ex;
        }
    }

    private CoverEntity createSeedCover(String resourcePath) {
        Resource resource = new ClassPathResource(resourcePath);

        if (!resource.exists()) {
            throw new IllegalStateException("seed cover resource not found: " + resourcePath);
        }

        StoredObjectResult uploaded = null;

        try (InputStream input = resource.getInputStream()) {
            byte[] content = input.readAllBytes();
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(content));

            if (image == null) {
                throw new IllegalStateException("seed cover is not a valid image: " +
                        resourcePath);
            }

            String originalFileName = resource.getFilename();
            String contentType = "image/png";

            uploaded = objectStorageService.uploadCover(
                    originalFileName,
                    contentType,
                    content
            );

            CoverMetadataDTO metadata = CoverMetadataDTO.builder()
                    .originalFileName(originalFileName)
                    .contentType(contentType)
                    .size((long) content.length)
                    .width(image.getWidth())
                    .height(image.getHeight())
                    .bucket(uploaded.getBucket())
                    .objectKey(uploaded.getObjectKey())
                    .eTag(uploaded.getETag())
                    .build();

            return coverRepository.save(
                    CoverEntity.builder()
                            .link(uploaded.getLink())
                            .metadata(coverMapper.encodeMetadata(metadata))
                            .createdAt(clock.millis())
                            .deletedAt(null)
                            .build()
            );
        } catch (IOException ex) {
            if (uploaded != null) {
                objectStorageService.deleteObject(uploaded.getObjectKey());
            }
            throw new IllegalStateException("cannot read seed cover: " + resourcePath, ex);
        } catch (RuntimeException ex) {
            if (uploaded != null) {
                objectStorageService.deleteObject(uploaded.getObjectKey());
            }
            throw ex;
        }
    }

    private void deleteSeedCoverObjectQuietly(CoverEntity cover) {
        if (cover == null) {
            return;
        }

        try {
            CoverMetadataDTO metadata = coverMapper.decodeMetadata(cover.getMetadata());
            if (metadata != null && metadata.getObjectKey() != null) {
                objectStorageService.deleteObject(metadata.getObjectKey());
            }
        } catch (RuntimeException ignored) {
        }
    }



    private UserEventEntity createUserEvent(
            UserEntity user,
            EventEntity event,
            ApplicationStatus status,
            LocalDateTime createdAt
    ) {
        if (createdAt.isBefore(user.getCreatedAt()) || !createdAt.isBefore(event.getDateTimestamp())) {
            throw new IllegalStateException("seed application must be created after the user and before the event");
        }

        UserEventEntity application = userEventRepository.findByUserAndEvent(user, event)
                .orElseGet(() -> UserEventEntity.builder()
                        .id(UserEventId.builder()
                                .userId(user.getUserId())
                                .eventId(event.getEventId())
                                .build())
                        .user(user)
                        .event(event)
                        .build());

        LocalDateTime actionTime = createdAt.plusHours(2);
        application.setCreatedAt(createdAt);
        application.setAccepted(status == ApplicationStatus.ACCEPTED);
        application.setRejected(status == ApplicationStatus.REJECTED);
        application.setRevoked(status == ApplicationStatus.REVOKED);
        application.setRejectReason(status == ApplicationStatus.REJECTED
                ? "Для этой смены требуется опыт организации мероприятий. Можно подать заявку на другую смену."
                : null);
        application.setRejectedAt(status == ApplicationStatus.REJECTED ? actionTime : null);
        application.setRevokedAt(status == ApplicationStatus.REVOKED ? actionTime : null);
        application.setDeletedAt(null);

        return userEventRepository.saveAndFlush(application);
    }

    private enum ApplicationStatus {
        PENDING, ACCEPTED, REJECTED, REVOKED
    }
}
