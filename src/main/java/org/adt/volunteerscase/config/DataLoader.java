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
import java.util.ArrayList;
import java.util.List;
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


    @Value("${demo.mobile-debug-login:true}")
    private boolean mobileDebugLogin;

    @Value("${ADMIN_PASSWORD:admin_pass}")
    private String adminPassword;

    @Value("${BASE_USER_PASSWORD:base_pass}")
    private String baseUserPassword;

    @Value("${BASE_USER_TWO_PASSWORD:base_pass}")
    private String baseUserTwoPassword;

    @Value("${BASE_USER_THREE_PASSWORD:base_pass}")
    private String baseUserThreePassword;


    @Value("${COORDINATOR_PASSWORD:coordinator_pass}")
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
                "admin@example.com", "+67676767671", false, true, mobileDebugLogin ? "admin_pass" : adminPassword, registeredAt);
        UserEntity coordinatorUser = createUser("Мария", "Иванова", "Сергеевна",
                "coordinator@example.com", "+8888888888", true, false, mobileDebugLogin ? "coordinator_pass" : coordinatorPassword, registeredAt);
        UserEntity user = createUser("Анна", "Петрова", "Дмитриевна",
                "user@example.com", "+79999999999", false, false, mobileDebugLogin ? "base_pass" : baseUserPassword, registeredAt);
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
        createPresentationData(seedNow, coordinator, List.of(user, userTwo, userThree),
                List.of(gorkyPark, shelter, sokolniki, theater, ostankino, sokolniki),
                List.of(Set.of(ecology, social), Set.of(animals, social), Set.of(culture, organization),
                        Set.of(culture, education), Set.of(media, sport), Set.of(ecology, education)));
    }

    /** Larger, deterministic dataset for the mobile debug accounts and paginated screens. */
    private void createPresentationData(LocalDateTime now, CoordinatorEntity mainCoordinator,
                                        List<UserEntity> debugVolunteers, List<LocationEntity> locations,
                                        List<Set<TagEntity>> interests) {
        String[] firstnames = {"Александр", "Михаил", "Иван", "Артём", "Максим", "Никита", "Роман", "Павел",
                "Анна", "Мария", "Софья", "Дарья", "Полина", "Алиса", "Виктория", "Ксения"};
        String[] lastnames = {"Морозов", "Лебедев", "Козлов", "Новиков", "Орлов", "Егоров"};
        List<UserEntity> volunteers = new ArrayList<>();
        for (int i = 0; i < 96; i++) {
            boolean female = i % firstnames.length >= 8;
            String lastname = lastnames[i / firstnames.length] + (female ? "а" : "");
            UserEntity volunteer = createUser(firstnames[i % firstnames.length], lastname,
                    female ? "Александровна" : "Александрович", "volunteer.%03d@example.com".formatted(i + 1),
                    "+7900%07d".formatted(i + 1), false, false, baseUserTwoPassword, now.minusMonths(12).plusDays(i % 60));
            setTagsToUser(interests.get(i % interests.size()), volunteer);
            volunteers.add(volunteer);
        }
        List<CoordinatorEntity> coordinators = new ArrayList<>();
        coordinators.add(mainCoordinator);
        for (int i = 0; i < 5; i++) {
            UserEntity owner = createUser(firstnames[i], lastnames[i], "Сергеевич",
                    "coordinator.%02d@example.com".formatted(i + 1), "+7910%07d".formatted(i + 1),
                    true, false, coordinatorPassword, now.minusMonths(12));
            setTagsToUser(interests.get(i), owner);
            coordinators.add(createCoordinatorProfile(owner, "Городской волонтёрский центр, отдел " + (i + 1)));
        }

        String[] titles = {"Зелёный маршрут", "Друзья приюта", "Парк объединяет", "Культурный выходной",
                "Город в объективе", "Экология начинается с нас"};
        String[] images = {"park-cleanup.png", "animal-shelter.png", "park_sokolniky.png",
                "theater_square.png", "photo-marathon.png", "eco-forum.png"};
        String[] descriptions = {
                "Присоединяйтесь к экологической команде: очистим прогулочные дорожки и отсортируем вторсырьё. Инвентарь выдаём, опыт не требуется.",
                "Поможем животным: погуляем с собаками, подготовим корм и наведём порядок. Сотрудники проведут инструктаж по безопасности.",
                "Встречаем гостей семейной программы, помогаем на мастер-классах и отвечаем на вопросы участников. В команде всегда есть опытный наставник.",
                "Помогаем посетителям культурной программы: навигация, регистрация и сопровождение гостей. Перед сменой познакомимся с площадкой.",
                "Собираем фотоистории добрых дел и поддерживаем участников городского маршрута. Для съёмки подойдёт телефон, согласие героев обязательно.",
                "Расскажем гостям о бережном потреблении, поможем спикерам и организуем зону раздельного сбора. До смены пройдём обучение."};
        for (int index = 0; index < 72; index++) {
            boolean completed = index >= 36;
            int shift = index % 36;
            int theme = shift % titles.length;
            LocalDateTime date = completed
                    ? now.toLocalDate().minusDays(shift < 15 ? 2L + shift : 2 + shift * 4L).atTime(10 + theme, 30)
                    : now.toLocalDate().plusDays(1 + shift).atTime(10 + theme, 30);
            CoordinatorEntity owner = shift % 3 != 2 ? mainCoordinator : coordinators.get(1 + shift / 3 % 5);
            EventEntity event = createEvent(completed ? EventStatus.COMPLETED : EventStatus.ONGOING,
                    titles[theme] + " — " + date.toLocalDate(), descriptions[theme]
                            + " Смена длится " + (2 + theme % 4) + " ч. Встречаемся за 15 минут до начала; предусмотрены перерывы и чай.",
                    "images/" + images[theme], owner, 40 + shift % 4 * 10, (2 + theme % 4) * 60,
                    date, locations.get(theme), interests.get(theme));
            LocalDateTime applied = completed ? date.minusDays(8) : now.minusDays(6);
            for (int d = 0; d < debugVolunteers.size(); d++) {
                // Keep a sizeable personalised catalogue free of existing applications.
                if ((shift + d) % 4 == 0) continue;
                ApplicationStatus status = completed ? ApplicationStatus.ACCEPTED
                        : switch ((shift + d) % 7) {
                            case 0 -> ApplicationStatus.REJECTED;
                            case 1 -> ApplicationStatus.REVOKED;
                            case 2, 3 -> ApplicationStatus.PENDING;
                            default -> ApplicationStatus.ACCEPTED;
                        };
                createUserEvent(debugVolunteers.get(d), event, status, applied.plusHours(d));
            }
            int participants = 12 + shift % 16;
            for (int j = 0; j < participants; j++) {
                UserEntity volunteer = volunteers.get((shift * 7 + j * 5) % volunteers.size());
                ApplicationStatus status = switch (j % 10) {
                    case 0 -> ApplicationStatus.REJECTED;
                    case 1 -> ApplicationStatus.REVOKED;
                    case 2, 3 -> completed ? ApplicationStatus.ACCEPTED : ApplicationStatus.PENDING;
                    default -> ApplicationStatus.ACCEPTED;
                };
                createUserEvent(volunteer, event, status, applied.plusHours(j % 12));
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

            CoverEntity cover = coverRepository.save(
                    CoverEntity.builder()
                            .link(uploaded.getLink())
                            .metadata(coverMapper.encodeMetadata(metadata))
                            .createdAt(clock.millis())
                            .deletedAt(null)
                            .build()
            );
            return cover;
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
