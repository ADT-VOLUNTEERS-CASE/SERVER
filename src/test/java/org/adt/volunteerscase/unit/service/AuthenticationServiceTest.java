package org.adt.volunteerscase.unit.service;

import org.adt.volunteerscase.dto.auth.AuthenticationResponse;
import org.adt.volunteerscase.dto.auth.RegisterRequest;
import org.adt.volunteerscase.entity.RefreshTokenEntity;
import org.adt.volunteerscase.entity.user.UserAuthEntity;
import org.adt.volunteerscase.entity.user.UserDetailsImpl;
import org.adt.volunteerscase.entity.user.UserEntity;
import org.adt.volunteerscase.exception.UserAlreadyExistsException;
import org.adt.volunteerscase.repository.UserRepository;
import org.adt.volunteerscase.service.AuthenticationService;
import org.adt.volunteerscase.service.JwtService;
import org.adt.volunteerscase.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthenticationService.
 *
 * @ExtendWith(MockitoExtension.class) - Enables Mockito support for JUnit 5.
 * This allows you to use @Mock annotations, @InjectMocks, and automatic mock creation.
 */
@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private RegisterRequest validRegisterRequest;
    private RegisterRequest existingEmailRequest;
    private RegisterRequest existingPhoneRequest;

    // An existing user for tests with duplicates
    private UserEntity existingUser;

    @BeforeEach
    void setUp() {
        validRegisterRequest = RegisterRequest.builder()
                .firstname("Иван")
                .lastname("Иванов")
                .patronymic("Иванович")
                .phoneNumber("+79161234567")
                .email("newuser@example.com")
                .password("Password123!")
                .build();

        existingEmailRequest = RegisterRequest.builder()
                .firstname("Петр")
                .lastname("Петров")
                .phoneNumber("+79169876543")
                .email("existing@example.com")
                .password("Password123!")
                .build();

        existingPhoneRequest = RegisterRequest.builder()
                .firstname("Сергей")
                .lastname("Сергеев")
                .phoneNumber("+79161112233")
                .email("newemail@example.com")
                .password("Password123!")
                .build();

        UserAuthEntity existingUserAuth = UserAuthEntity.builder()
                .passwordHash("encodedPassword")
                .build();

        existingUser = UserEntity.builder()
                .userId(1)
                .firstname("Существующий")
                .lastname("Пользователь")
                .phoneNumber("+79161112233")
                .email("existing@example.com")
                .userAuth(existingUserAuth)
                .build();

        existingUserAuth.setUser(existingUser);
    }


//----------------------------------   register method   ----------------------------------
    /**
     * Test 1: Successful registration of a new user
     * We check that the user is created successfully with unique data
     */
    @Test
    void register_shouldReturnTokens_whenUserIsNew() {
        when(userRepository.existsByEmail(validRegisterRequest.getEmail()))
                .thenReturn(false);

        when(userRepository.existsByPhoneNumber(validRegisterRequest.getPhoneNumber()))
                .thenReturn(false);

        when(passwordEncoder.encode(validRegisterRequest.getPassword()))
                .thenReturn("encodedPassword123");

        when(jwtService.generateAccessToken(any(UserDetailsImpl.class)))
                .thenReturn("jwtAccessToken");

        when(refreshTokenService.createRefreshToken(any(UserEntity.class)))
                .thenReturn(new RefreshTokenEntity());

        AuthenticationResponse response = authenticationService.register(validRegisterRequest);

        assertThat(response.getAccessToken()).isEqualTo("jwtAccessToken");
        //assertThat(response.getRefreshToken()).isNotNull(); // TODO: реализовать

        verify(userRepository).save(any(UserEntity.class));

        verify(passwordEncoder).encode("Password123!");
    }

    /**
     * Test 2: Registering with an existing email should throw an exception
     * We check the email uniqueness validation
     */
    @Test
    void register_shouldThrowException_whenEmailAlreadyExists(){

        when(userRepository.existsByEmail(existingEmailRequest.getEmail()))
                .thenReturn(true);

        assertThatThrownBy(() ->
                authenticationService.register(existingEmailRequest)
        )
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("User with email " + existingEmailRequest.getEmail() + " already exists");

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    /**
     * Test 3: Registering with an existing phone should throw an exception
     * We check the validation of the uniqueness of the phone
     */
    @Test
    void register_shouldThrowException_whenPhoneNumberAlreadyExists(){
        when(userRepository.existsByEmail(existingPhoneRequest.getEmail()))
                .thenReturn(false);

        when(userRepository.existsByPhoneNumber(existingPhoneRequest.getPhoneNumber()))
                .thenReturn(true);

        assertThatThrownBy(() ->
                authenticationService.register(existingPhoneRequest)
        )
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("User with phone number " + existingPhoneRequest.getPhoneNumber() + " already exists");

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    /**
     * Test 4: Creating a user with the correct data
     * We check that the UserEntity is created with the correct fields
     */
    @Test
    void register_shouldCreateUserWithCorrectData(){

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPass");
        when(jwtService.generateAccessToken(any())).thenReturn("token");
        when(refreshTokenService.createRefreshToken(any())).thenReturn(new RefreshTokenEntity());

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);

        authenticationService.register(validRegisterRequest);

        verify(userRepository).save(userCaptor.capture());
        UserEntity savedUser = userCaptor.getValue();

        assertThat(savedUser.getFirstname()).isEqualTo("Иван");
        assertThat(savedUser.getLastname()).isEqualTo("Иванов");
        assertThat(savedUser.getPatronymic()).isEqualTo("Иванович");
        assertThat(savedUser.getEmail()).isEqualTo("newuser@example.com");
        assertThat(savedUser.getPhoneNumber()).isEqualTo("+79161234567");

        assertThat(savedUser.getUserAuth()).isNotNull();
        assertThat(savedUser.getUserAuth().getPasswordHash()).isEqualTo("encodedPass");
        assertThat(savedUser.getUserAuth().getUser()).isSameAs(savedUser);
    }
}

