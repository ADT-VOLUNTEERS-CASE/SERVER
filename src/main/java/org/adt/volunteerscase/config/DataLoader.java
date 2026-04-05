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

        LocalDateTime seedNow = LocalDateTime.now().withSecond(0).withNano(0);

        //creating users
        UserEntity admin = createUser(
                "adminFirstname",
                "adminLastname",
                "adminPatronymic",
                "admin@example.com",
                "+67676767671",
                false,
                true,
                adminPassword
        );

        UserEntity coordinatorUser = createUser(
                "coordinatorFirstname",
                "coordinatorLastname",
                "coordinatorPatronymic",
                "coordinator@example.com",
                "+8888888888",
                true,
                false,
                coordinatorPassword
        );

        UserEntity user = createUser(
                "userFirstname",
                "userLastname",
                "userPatronymic",
                "user@example.com",
                "+79999999999",
                false,
                false,
                baseUserPassword
        );

        UserEntity userTwo = createUser(
                "secondUserFirstname",
                "secondUserLastname",
                "secondUserPatronymic",
                "user.two@example.com",
                "+79999999998",
                false,
                false,
                baseUserTwoPassword
        );
        UserEntity userThree = createUser(
                "thirdUserFirstname",
                "thirdUserLastname",
                "thirdUserPatronymic",
                "user.three@example.com",
                "+79999999997",
                false,
                false,
                baseUserThreePassword
        );

        CoordinatorEntity coordinator = createCoordinatorProfile(coordinatorUser, "Main office");

        //creating locations
        LocationEntity firstLocation = createLocation(
                "Театральная площадь, Москва",
                "театральная площадь в Москве",
                55.7589,
                37.6185
        );
        LocationEntity secondLocation = createLocation(
                "Королёва 15к1, Москва",
                "Останкинская телебашня",
                55.819682,
                37.611663
        );
        LocationEntity thirdLocation = createLocation(
                "Парк Горького, Москва",
                "центральный вход в парк",
                55.729876,
                37.603674
        );
        LocationEntity fourthLocation = createLocation(
                "Лужники, Москва",
                "волонтёрский штаб у большой спортивной арены",
                55.715765,
                37.553696
        );
        LocationEntity fifthLocation = createLocation(
                "Сокольники, Москва",
                "павильон для регистраций и выдачи инвентаря",
                55.793220,
                37.679993
        );


        //creating covers
        CoverEntity firstCover = createCover(

                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSDqaqlQ9fOyfjdxC23m2wqxdf-aKIlEDHyJQ&s",
                564,
                800
        );
        CoverEntity secondCover = createCover(
                "https://uxwing.com/wp-content/themes/uxwing/download/sport-and-awards/second-icon.png",
                512,
                512
        );
        CoverEntity thirdCover = createCover("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRs-01jMpdHl7jsfth1ZE8igfpl1ijFkdPaGA&s", 564, 800);
        CoverEntity fourthCover = createCover("https://storage.ettip.com/Upload/Images/Vocabulary/Official/4830.png", 300, 300);
        CoverEntity fifthCover = createCover("https://lingolandedu.com/en/english-english-dictionary/fifth", 400, 390);
        CoverEntity sixthCover = createCover("link six", 100, 100);
        CoverEntity seventhCover = createCover("lin seven", 100, 100);
        CoverEntity eighthCover = createCover("link eight", 100, 100);
        CoverEntity ninthCover = createCover("link ninth", 100, 100);
        CoverEntity tenthCover = createCover("link ten", 100, 100);


        //creating tags
        Set<TagEntity> adminTags = new HashSet<>();
        TagEntity adminTag1 = createTag("adminTag1", admin);
        TagEntity adminTag2 = createTag("adminTag2", admin);
        TagEntity adminTag3 = createTag("adminTag3", admin);
        adminTags.add(adminTag1);
        adminTags.add(adminTag2);
        adminTags.add(adminTag3);

        Set<TagEntity> userTags = new HashSet<>();
        TagEntity userTag1 = createTag("userTag1", user);
        TagEntity userTag2 = createTag("userTag2", user);
        TagEntity userTag3 = createTag("userTag3", user);
        userTags.add(userTag1);
        userTags.add(userTag2);
        userTags.add(userTag3);

        Set<TagEntity> coordinatorTags = new HashSet<>();
        TagEntity coordinatorTag1 = createTag("coordinatorTag1", coordinatorUser);
        TagEntity coordinatorTag2 = createTag("coordinatorTag2", coordinatorUser);
        TagEntity coordinatorTag3 = createTag("coordinatorTag3", coordinatorUser);
        coordinatorTags.add(coordinatorTag1);
        coordinatorTags.add(coordinatorTag2);
        coordinatorTags.add(coordinatorTag3);

        Set<TagEntity> userTwoTags = new HashSet<>();
        TagEntity userTwoTag1 = createTag("userTwoTag1", userTwo);
        TagEntity userTwoTag2 = createTag("userTwoTag2", userTwo);
        TagEntity userTwoTag3 = createTag("userTwoTag3", userTwo);
        userTwoTags.add(userTwoTag1);
        userTwoTags.add(userTwoTag2);
        userTwoTags.add(userTwoTag3);

        Set<TagEntity> userThreeTags = new HashSet<>();
        TagEntity userThreeTag1 = createTag("userThreeTag1", userThree);
        TagEntity userThreeTag2 = createTag("userThreeTag2", userThree);
        TagEntity userThreeTag3 = createTag("userThreeTag3", userThree);
        userThreeTags.add(userThreeTag1);
        userThreeTags.add(userThreeTag2);
        userThreeTags.add(userThreeTag3);

        setTagsToUser(adminTags, admin);
        setTagsToUser(userTags, user);
        setTagsToUser(coordinatorTags, coordinatorUser);
        setTagsToUser(userTwoTags, userTwo);
        setTagsToUser(userThreeTags, userThree);

        Set<TagEntity> firstEventTags = new HashSet<>(Set.of(adminTag1, coordinatorTag1, coordinatorTag2));
        Set<TagEntity> secondEventTags = new HashSet<>(Set.of(adminTag2, userTag1, coordinatorTag2));
        Set<TagEntity> parkCleanupTags = new HashSet<>(Set.of(userTag1, userTag2, coordinatorTag1));
        Set<TagEntity> animalShelterTags = new HashSet<>(Set.of(userTwoTag1, userTwoTag2, coordinatorTag2));
        Set<TagEntity> medicalSupportTags = new HashSet<>(Set.of(userThreeTag1, userThreeTag2, coordinatorTag3));
        Set<TagEntity> logisticsHubTags = new HashSet<>(Set.of(userTag2, userThreeTag2, adminTag2));
        Set<TagEntity> photoMarathonTags = new HashSet<>(Set.of(userTag3, userTwoTag3, adminTag1));
        Set<TagEntity> futureCompletedTags = new HashSet<>(Set.of(userTag1, userThreeTag1, adminTag3));
        Set<TagEntity> pastInProgressTags = new HashSet<>(Set.of(userTag2, userTwoTag2, coordinatorTag1));
        Set<TagEntity> ecoForumTags = new HashSet<>(Set.of(userTwoTag1, userThreeTag3, coordinatorTag2));


        //creationEvents
        EventEntity firstEvent = createEvent(
                EventStatus.COMPLETED,
                "first event",
                "first event, completed",
                firstCover,
                coordinator,
                100,
                seedNow.minusDays(60),
                firstLocation,
                firstEventTags
        );
        EventEntity secondEvent = createEvent(
                EventStatus.COMPLETED,
                "second event",
                "second event, completed",
                secondCover,
                coordinator,
                67,
                seedNow.minusDays(61),
                secondLocation,
                secondEventTags
        );

        EventEntity parkCleanupEvent = createEvent(
                EventStatus.ONGOING,
                "park cleanup day",
                "future event for the base user: two matching tags and high popularity",
                thirdCover,
                coordinator,
                120,
                seedNow.plusDays(7),
                thirdLocation,
                parkCleanupTags
        );
        EventEntity animalShelterEvent = createEvent(
                EventStatus.IN_PROGRESS,
                "animal shelter weekend",
                "future event for the second base user: two matching tags and high popularity",
                fourthCover,
                coordinator,
                80,
                seedNow.plusDays(10),
                fourthLocation,
                animalShelterTags
        );
        EventEntity medicalSupportEvent = createEvent(
                EventStatus.ONGOING,
                "medical support shift",
                "future event for the third base user: two matching tags and active applications",
                fifthCover,
                coordinator,
                50,
                seedNow.plusDays(14),
                fifthLocation,
                medicalSupportTags
        );
        EventEntity logisticsHubEvent = createEvent(
                EventStatus.IN_PROGRESS,
                "logistics hub day",
                "future event with mixed tags for recommendation tie checks",
                sixthCover,
                coordinator,
                90,
                seedNow.plusDays(18),
                firstLocation,
                logisticsHubTags
        );
        EventEntity photoMarathonEvent = createEvent(
                EventStatus.ONGOING,
                "photo marathon",
                "future event with lower popularity for ranking checks",
                seventhCover,
                coordinator,
                70,
                seedNow.plusDays(21),
                secondLocation,
                photoMarathonTags
        );
        EventEntity futureCompletedEvent = createEvent(
                EventStatus.COMPLETED,
                "future completed event",
                "should be excluded by status even though its date is in the future",
                eighthCover,
                coordinator,
                40,
                seedNow.plusDays(30),
                thirdLocation,
                futureCompletedTags
        );
        EventEntity pastInProgressEvent = createEvent(
                EventStatus.IN_PROGRESS,
                "past in progress event",
                "should be excluded by date even though its status is not completed",
                ninthCover,
                coordinator,
                40,
                seedNow.minusDays(5),
                fourthLocation,
                pastInProgressTags
        );
        EventEntity ecoForumEvent = createEvent(
                EventStatus.IN_PROGRESS,
                "eco forum booth",
                "future event with zero active popularity for sorting checks",
                tenthCover,
                coordinator,
                60,
                seedNow.plusDays(35),
                fifthLocation,
                ecoForumTags
        );

        createUserEvent(admin, parkCleanupEvent, true, false, false, null);
        createUserEvent(userTwo, parkCleanupEvent, false, false, false, null);
        createUserEvent(userThree, parkCleanupEvent, false, false, false, null);

        createUserEvent(admin, animalShelterEvent, true, false, false, null);
        createUserEvent(user, animalShelterEvent, false, false, false, null);
        createUserEvent(userThree, animalShelterEvent, false, false, false, null);

        createUserEvent(user, medicalSupportEvent, true, false, false, null);
        createUserEvent(userTwo, medicalSupportEvent, false, false, false, null);
        createUserEvent(admin, medicalSupportEvent, false, false, true, null);

        createUserEvent(admin, logisticsHubEvent, true, false, false, null);
        createUserEvent(userTwo, logisticsHubEvent, false, false, false, null);
        createUserEvent(user, logisticsHubEvent, false, false, false, seedNow.minusDays(1));

        createUserEvent(admin, photoMarathonEvent, true, false, false, null);
        createUserEvent(userThree, photoMarathonEvent, false, false, false, null);

        createUserEvent(admin, futureCompletedEvent, true, false, false, null);
        createUserEvent(userTwo, futureCompletedEvent, false, false, false, null);

        createUserEvent(admin, pastInProgressEvent, true, false, false, null);
        createUserEvent(userThree, pastInProgressEvent, false, false, false, null);

        createUserEvent(admin, ecoForumEvent, false, false, true, null);

    }

    private UserEntity createUser(
            String firstname,
            String lastname,
            String patronymic,
            String email,
            String phoneNumber,
            boolean isCoordinator,
            boolean isAdmin,
            String password
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

    private CoverEntity createCover(String link, Integer width, Integer height) {
        if (!coverRepository.existsByLink(link)) {

            CoverEntity coverEntity = CoverEntity.builder()
                    .link(link)
                    .width(width)
                    .height(height)
                    .build();

            coverRepository.save(coverEntity);
        }
        return coverRepository.findByLink(link)
                .orElseThrow(() -> new CoverNotFoundException("cover with link - " + link + " not found"));
    }

    private TagEntity createTag(String tagName, UserEntity currentUser) {

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
        currentUser.setTags(tags);
        userRepository.save(currentUser);
    }

    private EventEntity createEvent(
            EventStatus status,
            String name,
            String description,
            CoverEntity cover,
            CoordinatorEntity coordinator,
            Integer maxCapacity,
            LocalDateTime dateTimestamp,
            LocationEntity location,
            Set<TagEntity> tags
    ) {
        EventEntity existingByName = eventRepository.findByName(name).orElse(null);
        if (existingByName != null) {
            return existingByName;
        }

        if (cover != null) {
            EventEntity existingByCover = eventRepository.findByCover(cover).orElse(null);
            if (existingByCover != null) {
                throw new IllegalStateException(
                        "seed event '" + name + "' cannot be created: cover with id "
                                + cover.getCoverId()
                                + " is already used by event '" + existingByCover.getName() + "'"
                );
            }
        }

        if (location != null && eventRepository.existsByLocationAndDateTimestamp(location, dateTimestamp)) {
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
                .dateTimestamp(dateTimestamp)
                .location(location)
                .tags(tags)
                .build();

        return eventRepository.saveAndFlush(event);
    }


    private UserEventEntity createUserEvent(
            UserEntity user,
            EventEntity event,
            boolean accepted,
            boolean rejected,
            boolean revoked,
            LocalDateTime deletedAt
    ) {
        if (user == null) {
            throw new IllegalStateException("createUserEvent: user is null");
        }
        if (event == null) {
            throw new IllegalStateException("createUserEvent: event is null");
        }
        if (user.getUserId() == null) {
            throw new IllegalStateException("createUserEvent: userId is null for " + user.getEmail());
        }
        if (event.getEventId() == null) {
            throw new IllegalStateException("createUserEvent: eventId is null for event " + event.getName());
        }

        UserEventEntity userEvent = userEventRepository.findByUserAndEvent(user, event)
                .orElseGet(() -> UserEventEntity.builder()
                        .id(UserEventId.builder()
                                .userId(user.getUserId())
                                .eventId(event.getEventId())
                                .build())
                        .user(user)
                        .event(event)
                        .accepted(false)
                        .rejected(false)
                        .revoked(false)
                        .build());

        LocalDateTime actionTime = LocalDateTime.now().withSecond(0).withNano(0);

        userEvent.setAccepted(accepted);
        userEvent.setRejected(rejected);
        userEvent.setRevoked(revoked);
        userEvent.setRejectReason(rejected ? "seed reject" : null);
        userEvent.setRejectedAt(rejected ? actionTime : null);
        userEvent.setRevokedAt(revoked ? actionTime : null);
        userEvent.setDeletedAt(deletedAt);

        return userEventRepository.saveAndFlush(userEvent);
    }



}
