package org.adt.volunteerscase.unit.service;

import org.adt.volunteerscase.dto.auth.AuthenticationRequest;
import org.adt.volunteerscase.dto.auth.AuthenticationResponse;
import org.adt.volunteerscase.dto.auth.RegisterRequest;
import org.adt.volunteerscase.entity.RefreshTokenEntity;
import org.adt.volunteerscase.entity.user.UserAuthEntity;
import org.adt.volunteerscase.entity.user.UserDetailsImpl;
import org.adt.volunteerscase.entity.user.UserEntity;
import org.adt.volunteerscase.exception.InvalidPasswordException;
import org.adt.volunteerscase.exception.UserAlreadyExistsException;
import org.adt.volunteerscase.exception.UserNotFoundException;
import org.adt.volunteerscase.repository.UserRepository;
import org.adt.volunteerscase.service.security.AuthenticationService;
import org.adt.volunteerscase.service.security.JwtService;
import org.adt.volunteerscase.service.security.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

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

    private AuthenticationRequest validAuthRequest;
    private AuthenticationRequest invalidEmailRequest;
    private AuthenticationRequest invalidPasswordRequest;

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

        validAuthRequest = AuthenticationRequest.builder()
                .email("existing@example.com")
                .password("CorrectPassword123!")
                .build();

        invalidEmailRequest = AuthenticationRequest.builder()
                .email("nonexistent@example.com")
                .password("AnyPassword")
                .build();

        invalidPasswordRequest = AuthenticationRequest.builder()
                .email("existing@example.com")
                .password("WrongPassword")
                .build();


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
    void register_shouldThrowException_whenEmailAlreadyExists() {

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
    void register_shouldThrowException_whenPhoneNumberAlreadyExists() {
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
    void register_shouldCreateUserWithCorrectData() {

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
        assertThat(savedUser.isAdmin()).isFalse();
        assertThat(savedUser.isCoordinator()).isFalse();

        assertThat(savedUser.getUserAuth()).isNotNull();
        assertThat(savedUser.getUserAuth().getPasswordHash()).isEqualTo("encodedPass");
        assertThat(savedUser.getUserAuth().getUser()).isSameAs(savedUser);
    }

//----------------------------------   registerCoordinator method   ----------------------------------
    /**
     * Test 1: Successful registration of a new user coordinator
     * We check that the user is created successfully with unique data
     */
    @Test
    void registerCoordinator_shouldReturnTokens_whenUserIsNew() {
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

        AuthenticationResponse response = authenticationService.registerCoordinator(validRegisterRequest);

        assertThat(response.getAccessToken()).isEqualTo("jwtAccessToken");
        //assertThat(response.getRefreshToken()).isNotNull(); // TODO: реализовать

        verify(userRepository).save(any(UserEntity.class));

        verify(passwordEncoder).encode("Password123!");
    }

    /**
     * Test 2: Registering coordinator with an existing email should throw an exception
     * We check the email uniqueness validation
     */
    @Test
    void registerCoordinator_shouldThrowException_whenEmailAlreadyExists() {

        when(userRepository.existsByEmail(existingEmailRequest.getEmail()))
                .thenReturn(true);

        assertThatThrownBy(() ->
                authenticationService.registerCoordinator(existingEmailRequest)
        )
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("User with email " + existingEmailRequest.getEmail() + " already exists");

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    /**
     * Test 3: Registering coordinator with an existing phone should throw an exception
     * We check the validation of the uniqueness of the phone
     */
    @Test
    void registerCoordinator_shouldThrowException_whenPhoneNumberAlreadyExists() {
        when(userRepository.existsByEmail(existingPhoneRequest.getEmail()))
                .thenReturn(false);

        when(userRepository.existsByPhoneNumber(existingPhoneRequest.getPhoneNumber()))
                .thenReturn(true);

        assertThatThrownBy(() ->
                authenticationService.registerCoordinator(existingPhoneRequest)
        )
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("User with phone number " + existingPhoneRequest.getPhoneNumber() + " already exists");

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    /**
     * Test 4: Creating a user with the correct data(isCoordinator = true)
     * We check that the UserEntity is created with the correct fields
     */
    @Test
    void registerCoordinator_shouldCreateUserWithCorrectData() {

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPass");
        when(jwtService.generateAccessToken(any())).thenReturn("token");
        when(refreshTokenService.createRefreshToken(any())).thenReturn(new RefreshTokenEntity());

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);

        authenticationService.registerCoordinator(validRegisterRequest);

        verify(userRepository).save(userCaptor.capture());
        UserEntity savedUser = userCaptor.getValue();

        assertThat(savedUser.getFirstname()).isEqualTo("Иван");
        assertThat(savedUser.getLastname()).isEqualTo("Иванов");
        assertThat(savedUser.getPatronymic()).isEqualTo("Иванович");
        assertThat(savedUser.getEmail()).isEqualTo("newuser@example.com");
        assertThat(savedUser.getPhoneNumber()).isEqualTo("+79161234567");
        assertThat(savedUser.isCoordinator()).isTrue();
        assertThat(savedUser.isAdmin()).isFalse();

        assertThat(savedUser.getUserAuth()).isNotNull();
        assertThat(savedUser.getUserAuth().getPasswordHash()).isEqualTo("encodedPass");
        assertThat(savedUser.getUserAuth().getUser()).isSameAs(savedUser);
    }
//----------------------------------   registerAdmin method   ----------------------------------

    /**
     * Test 1: Successful registration of a new admin user
     * We check that the user is created successfully with unique data
     */
    @Test
    void registerAdmin_shouldReturnTokens_whenUserIsNew() {
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

        AuthenticationResponse response = authenticationService.registerAdmin(validRegisterRequest);

        assertThat(response.getAccessToken()).isEqualTo("jwtAccessToken");
        //assertThat(response.getRefreshToken()).isNotNull(); // TODO: реализовать

        verify(userRepository).save(any(UserEntity.class));

        verify(passwordEncoder).encode("Password123!");
    }

    /**
     * Test 2: Registering admin with an existing email should throw an exception
     * We check the email uniqueness validation
     */
    @Test
    void registerAdmin_shouldThrowException_whenEmailAlreadyExists() {

        when(userRepository.existsByEmail(existingEmailRequest.getEmail()))
                .thenReturn(true);

        assertThatThrownBy(() ->
                authenticationService.registerAdmin(existingEmailRequest)
        )
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("User with email " + existingEmailRequest.getEmail() + " already exists");

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    /**
     * Test 3: Registering admin with an existing phone should throw an exception
     * We check the validation of the uniqueness of the phone
     */
    @Test
    void registerAdmin_shouldThrowException_whenPhoneNumberAlreadyExists() {
        when(userRepository.existsByEmail(existingPhoneRequest.getEmail()))
                .thenReturn(false);

        when(userRepository.existsByPhoneNumber(existingPhoneRequest.getPhoneNumber()))
                .thenReturn(true);

        assertThatThrownBy(() ->
                authenticationService.registerAdmin(existingPhoneRequest)
        )
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("User with phone number " + existingPhoneRequest.getPhoneNumber() + " already exists");

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    /**
     * Test 4: Creating a user with the correct data(isAdmin = true)
     * We check that the UserEntity is created with the correct fields
     */
    @Test
    void registerAdmin_shouldCreateUserWithCorrectData() {

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPass");
        when(jwtService.generateAccessToken(any())).thenReturn("token");
        when(refreshTokenService.createRefreshToken(any())).thenReturn(new RefreshTokenEntity());

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);

        authenticationService.registerAdmin(validRegisterRequest);

        verify(userRepository).save(userCaptor.capture());
        UserEntity savedUser = userCaptor.getValue();

        assertThat(savedUser.getFirstname()).isEqualTo("Иван");
        assertThat(savedUser.getLastname()).isEqualTo("Иванов");
        assertThat(savedUser.getPatronymic()).isEqualTo("Иванович");
        assertThat(savedUser.getEmail()).isEqualTo("newuser@example.com");
        assertThat(savedUser.getPhoneNumber()).isEqualTo("+79161234567");
        assertThat(savedUser.isAdmin()).isTrue();
        assertThat(savedUser.isCoordinator()).isFalse();

        assertThat(savedUser.getUserAuth()).isNotNull();
        assertThat(savedUser.getUserAuth().getPasswordHash()).isEqualTo("encodedPass");
        assertThat(savedUser.getUserAuth().getUser()).isSameAs(savedUser);
    }
//----------------------------------   authenticate method   ----------------------------------


    /**
     * Test 1: Successful authentication with correct email and password
     * We check that if the data is correct, the tokens are returned
     */
    @Test
    void authenticate_shouldReturnTokens_whenCredentialsAreValid() {
        when(userRepository.existsByEmail(validAuthRequest.getEmail()))
                .thenReturn(true);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);

        when(userRepository.findByEmailWithAuth(validAuthRequest.getEmail()))
                .thenReturn(Optional.of(existingUser));

        when(jwtService.generateAccessToken(any(UserDetailsImpl.class)))
                .thenReturn("jwtAccessToken");

        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
                .refreshToken("refreshToken123")
                .build();
        when(refreshTokenService.createRefreshToken(existingUser))
                .thenReturn(refreshTokenEntity);

        AuthenticationResponse response = authenticationService.authenticate(validAuthRequest);

        assertThat(response.getAccessToken()).isEqualTo("jwtAccessToken");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken123"); //TODO: заполнить все поля рефреш токена и делать на них проверку

        ArgumentCaptor<UsernamePasswordAuthenticationToken> authTokenCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(authTokenCaptor.capture());

        UsernamePasswordAuthenticationToken capturedToken = authTokenCaptor.getValue();
        assertThat(capturedToken.getPrincipal()).isEqualTo("existing@example.com");
        assertThat(capturedToken.getCredentials()).isEqualTo("CorrectPassword123!");

        verify(userRepository).findByEmailWithAuth("existing@example.com");
    }

    /**
     * Test 2: Authentication with a non-existent email should throw a UserNotFoundException
     * We check the validation of the email's existence before attempting authentication
     */
    @Test
    void authenticate_shouldThrowUserNotFoundException_whenEmailDoesNotExist() {
        when(userRepository.existsByEmail(invalidEmailRequest.getEmail()))
                .thenReturn(false);

        assertThatThrownBy(() ->
                authenticationService.authenticate(invalidEmailRequest)
        )
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User with email " + invalidEmailRequest.getEmail() + " not found");

        verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmailWithAuth(anyString());
    }


    /**
     * Test 3: Authentication with existing email, but invalid password
     * We check the processing of BadCredentialsException and the conversion to InvalidPasswordException
     */
    @Test
    void authenticate_shouldThrowInvalidPasswordException_whenPasswordIsIncorrect() {

        when(userRepository.existsByEmail(invalidPasswordRequest.getEmail()))
                .thenReturn(true);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() ->
                authenticationService.authenticate(invalidPasswordRequest)
        )
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessage("Invalid password");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        verify(userRepository, never()).findByEmailWithAuth(anyString());
        verify(jwtService, never()).generateAccessToken(any());
        verify(refreshTokenService, never()).createRefreshToken(any());

    }
}