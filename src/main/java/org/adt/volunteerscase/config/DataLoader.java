package org.adt.volunteerscase.config;

import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.entity.CoverEntity;
import org.adt.volunteerscase.entity.LocationEntity;
import org.adt.volunteerscase.entity.TagEntity;
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

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    @Value("${BASE_USER_PASSWORD:password123}")
    private String baseUserPassword;

    @Value("${COORDINATOR_PASSWORD:password123}")
    private String coordinatorPassword;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        createInitialData();
    }

    private void createInitialData() {


        //creating users
        UserEntity admin = createUser("adminFirstname", "adminLastname", "adminPatronymic", "admin@example.com", "+67676767671", false, true, adminPassword);
        UserEntity user = createUser("userFirstname", "userLastname", "userPatronymic", "user@example.com", "+79999999999", false, false, baseUserPassword);
        UserEntity coordinator = createUser("coordinatorFirstname", "coordinatorLastname", "coordinatorPatronymic", "coordinator@example.com", "+8888888888", true, false, coordinatorPassword);

        //creating locations
        LocationEntity firstLocation = createLocation("Театральная площадь, Москва", "театральная площадь в Москве", 55.7589, 37.6185);
        LocationEntity secondLocation = createLocation("Королёва 15к1", "Останкинская телебашня", 55.819682, 37.611663);


        //creating covers
        CoverEntity firstCover = createCover("https://resize.tripster.ru/ESwneRzbn3q3LDrOdHQYRjLH9fs=/fit-in/1220x600/filters:no_upscale()/https://cdn.tripster.ru/photos/6036295a-43ba-4583-951f-e8d695243f6f.jpg", 1220, 600);
        CoverEntity secondCover = createCover("https://liveinmsk.ru/up/photos/album/4north2/4217.jpg", 667, 1000);


        //creating tags
        Set<TagEntity> adminTags = new HashSet<>();
        TagEntity adminTag1 = createTag("adminTag1", admin);
        adminTags.add(adminTag1);
        TagEntity adminTag2 = createTag("adminTag2", admin);
        adminTags.add(adminTag2);
        TagEntity adminTag3 = createTag("adminTag3", admin);
        adminTags.add(adminTag3);

        Set<TagEntity> userTags = new HashSet<>();
        TagEntity userTag1 = createTag("userTag1", user);
        userTags.add(userTag1);
        TagEntity userTag2 = createTag("userTag2", user);
        userTags.add(userTag2);
        TagEntity userTag3 = createTag("userTag3", user);
        userTags.add(userTag3);

        Set<TagEntity> coordinatorTags = new HashSet<>();
        TagEntity coordinatorTag1 = createTag("coordinatorTag1", coordinator);
        coordinatorTags.add(coordinatorTag1);
        TagEntity coordinatorTag2 = createTag("coordinatorTag2", coordinator);
        coordinatorTags.add(coordinatorTag2);
        TagEntity coordinatorTag3 = createTag("coordinatorTag3", coordinator);
        coordinatorTags.add(coordinatorTag3);

        setTagsToUser(adminTags, admin);
        setTagsToUser(userTags, user);
        setTagsToUser(coordinatorTags, coordinator);

        Set<TagEntity> firstEventTags = new HashSet<>();
        firstEventTags.add(adminTag1);
        firstEventTags.add(coordinatorTag1);
        firstEventTags.add(coordinatorTag2);

        Set<TagEntity> secondEventTags = new HashSet<>();
        secondEventTags.add(adminTag2);
        secondEventTags.add(userTag1);
        secondEventTags.add(coordinatorTag2);

//        Set<TagEntity> thirdEventTags = new HashSet<>();
//        thirdEventTags.add(adminTag3);
//        thirdEventTags.add(adminTag2);
//        thirdEventTags.add(coordinatorTag2);
//        thirdEventTags.add(coordinatorTag3);
//        thirdEventTags.add(userTag1);


        //creationEvents
        EventEntity firstEvent = createEvent(
                EventStatus.COMPLETED,
                "first event",
                "first event, completed",
                firstCover,
                "+67676767671",
                100,
                LocalDateTime.of(2025, 2, 19, 12, 0),
                firstLocation,
                firstEventTags
        );
        EventEntity secondEvent = createEvent(
                EventStatus.COMPLETED,
                "second event",
                "second event, completed",
                secondCover,
                "coordinator@example.com",
                67,
                LocalDateTime.of(2025, 2, 18, 12, 0),
                secondLocation,
                secondEventTags
        );
//        EventEntity thirdEvent = createEvent();
    }

    private UserEntity createUser(String firstname, String lastname, String patronymic, String email, String phoneNumber, boolean isCoordinator, boolean isAdmin, String password) {
        if (!userRepository.existsByEmail(email) && !userRepository.existsByPhoneNumber(phoneNumber)) {

            UserAuthEntity userAuth = UserAuthEntity.builder()
                    .passwordHash(passwordEncoder.encode(password))
                    .build();

            UserEntity user = UserEntity.builder()
                    .firstname(firstname)
                    .lastname(lastname)
                    .patronymic(patronymic)
                    .email(email)
                    .phoneNumber(phoneNumber)
                    .isAdmin(isAdmin)
                    .userAuth(userAuth)
                    .isCoordinator(isCoordinator).build();


            userAuth.setUser(user);
            userRepository.save(user);
            refreshTokenService.createRefreshToken(user);
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user with email - " + email + " not found"));
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

    private EventEntity createEvent(EventStatus status, String name, String description, CoverEntity cover, String coordinatorContact, Integer maxCapacity, LocalDateTime dateTimestamp, LocationEntity location, Set<TagEntity> tags) {

        if (eventRepository.existsByName(name)) {
            return eventRepository.findByName(name)
                    .orElseThrow(() -> new EventNotFoundException("event with name - " + name + " not found"));
        }

        EventEntity event = EventEntity.builder()
                .status(status)
                .name(name)
                .description(description)
                .cover(cover)
                .coordinatorContact(coordinatorContact)
                .maxCapacity(maxCapacity)
                .dateTimestamp(dateTimestamp)
                .location(location)
                .tags(tags)
                .build();

        return eventRepository.save(event);
    }
}
