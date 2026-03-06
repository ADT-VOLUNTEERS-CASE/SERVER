package org.adt.volunteerscase.config;

import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.entity.user.UserAuthEntity;
import org.adt.volunteerscase.entity.user.UserEntity;
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
    private String adminpassword;

    @Override
    public void run(String... args) throws Exception {
        createInitialData();
    }

    private void createInitialData() {
        createAdmin("adminfirstname", "adminlastname", "adminpatronymic", "admin@example.com", "+67676767671", false);
    }

    private void createAdmin(String firstname, String lastname, String patronymic, String email, String phoneNumber, boolean isCoordinator) {
        if (!userRepository.existsByEmail(email) && !userRepository.existsByPhoneNumber(phoneNumber)) {

            UserAuthEntity userAuth = UserAuthEntity.builder()
                    .passwordHash(passwordEncoder.encode(adminpassword))
                    .build();

            UserEntity admin = UserEntity.builder()
                    .firstname(firstname)
                    .lastname(lastname)
                    .patronymic(patronymic)
                    .email(email)
                    .phoneNumber(phoneNumber)
                    .isAdmin(true)
                    .userAuth(userAuth)
                    .isCoordinator(isCoordinator).build();


            userAuth.setUser(admin);
            userRepository.save(admin);
            refreshTokenService.createRefreshToken(admin);

        }
    }
}
