package org.adt.volunteerscase.config;

import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.entity.user.UserAuthEntity;
import org.adt.volunteerscase.entity.user.UserEntity;
import org.adt.volunteerscase.exception.UserNotFoundException;
import org.adt.volunteerscase.repository.UserRepository;
import org.adt.volunteerscase.service.security.RefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    @Value("${BASE_USER_PASSWORD}")
    private String baserUserPassword = "password123";

    @Value("${COORDINATOR_PASSWORD}")
    private String coordinatorPassword = "password123";

    @Override
    public void run(String... args) throws Exception {
        createInitialData();
    }

    private void createInitialData() {

        //creating users
        UserEntity admin = createUser("adminFirstname", "adminLastname", "adminPatronymic", "admin@example.com", "+67676767671", false, true);
        UserEntity user = createUser("userFirstname", "userLastname", "userPatronymic", "user@example.com", "+79999999999", false, false);
        UserEntity coordinator = createUser("coordinatorFirstname", "coordinatorLastname", "coordinatorPatronymic", "coordinator@example.com", "+8888888888", true, false);

        //creatingLocations
    }

    private UserEntity createUser(String firstname, String lastname, String patronymic, String email, String phoneNumber, boolean isCoordinator, boolean isAdmin) {
        if (!userRepository.existsByEmail(email) && !userRepository.existsByPhoneNumber(phoneNumber)) {

            UserAuthEntity userAuth = UserAuthEntity.builder()
                    .passwordHash(passwordEncoder.encode(adminPassword))
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
}
