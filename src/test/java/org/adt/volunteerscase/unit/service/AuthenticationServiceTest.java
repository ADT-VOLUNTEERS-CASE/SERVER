package org.adt.volunteerscase.unit.service;

import org.adt.volunteerscase.dto.auth.AuthenticationRequest;
import org.adt.volunteerscase.dto.auth.AuthenticationResponse;
import org.adt.volunteerscase.dto.auth.RegisterRequest;
import org.adt.volunteerscase.dto.auth.TokenRefreshRequest;
import org.adt.volunteerscase.entity.CoordinatorEntity;
import org.adt.volunteerscase.entity.RefreshTokenEntity;
import org.adt.volunteerscase.entity.user.UserAuthEntity;
import org.adt.volunteerscase.entity.user.UserEntity;
import org.adt.volunteerscase.exception.InvalidPasswordException;
import org.adt.volunteerscase.exception.RefreshTokenException;
import org.adt.volunteerscase.exception.UserAlreadyExistsException;
import org.adt.volunteerscase.exception.UserNotFoundException;
import org.adt.volunteerscase.repository.CoordinatorRepository;
import org.adt.volunteerscase.repository.UserRepository;
import org.adt.volunteerscase.service.security.AuthenticationService;
import org.adt.volunteerscase.service.security.JwtService;
import org.adt.volunteerscase.service.security.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private CoordinatorRepository coordinatorRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenService refreshTokenService;

    private AuthenticationService authenticationService;

    private RegisterRequest validRegisterRequest;
    private RegisterRequest existingEmailRequest;
    private RegisterRequest existingPhoneRequest;

    private AuthenticationRequest validAuthRequest;
    private AuthenticationRequest invalidEmailRequest;
    private AuthenticationRequest invalidPasswordRequest;

    private TokenRefreshRequest validRefreshRequest;

    private UserEntity existingUser;
    private UserAuthEntity existingUserAuth;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(
                userRepository,
                passwordEncoder,
                jwtService,
                coordinatorRepository,
                authenticationManager,
                refreshTokenService
        );

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
                .patronymic("Петрович")
                .phoneNumber("+79169876543")
                .email("existing@example.com")
                .password("Password123!")
                .build();

        existingPhoneRequest = RegisterRequest.builder()
                .firstname("Сергей")
                .lastname("Сергеев")
                .patronymic("Сергеевич")
                .phoneNumber("+79161112233")
                .email("newemail@example.com")
                .password("Password123!")
                .build();

        validAuthRequest = AuthenticationRequest.builder()
                .email("existing@example.com")
                .password("CorrectPassword123!")
                .build();

        invalidEmailRequest = AuthenticationRequest.builder()
                .email("missing@example.com")
                .password("AnyPassword123!")
                .build();

        invalidPasswordRequest = AuthenticationRequest.builder()
                .email("existing@example.com")
                .password("WrongPassword123!")
                .build();

        validRefreshRequest = TokenRefreshRequest.builder()
                .refreshToken("old-refresh-token")
                .build();

        existingUserAuth = UserAuthEntity.builder()
                .passwordHash("encodedPassword")
                .build();

        existingUser = UserEntity.builder()
                .userId(1)
                .firstname("Существующий")
                .lastname("Пользователь")
                .patronymic("Тестович")
                .phoneNumber("+79161112233")
                .email("existing@example.com")
                .userAuth(existingUserAuth)
                .build();

        existingUserAuth.setUser(existingUser);
    }

    private RefreshTokenEntity buildRefreshToken(String token, UserEntity user) {
        return RefreshTokenEntity.builder()
                .refreshToken(token)
                .user(user)
                .build();
    }

    @Test
    void register_shouldReturnTokensAndPersistUser_whenRequestIsValid() {
        when(userRepository.existsByEmail(validRegisterRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(validRegisterRequest.getPhoneNumber())).thenReturn(false);
        when(passwordEncoder.encode(validRegisterRequest.getPassword())).thenReturn("encodedPassword123");
        when(jwtService.generateAccessToken(any())).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(any(UserEntity.class)))
                .thenReturn(buildRefreshToken("refresh-token", null));

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);

        AuthenticationResponse response = authenticationService.register(validRegisterRequest);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");

        verify(userRepository).save(userCaptor.capture());
        UserEntity savedUser = userCaptor.getValue();

        assertThat(savedUser.getFirstname()).isEqualTo("Иван");
        assertThat(savedUser.getLastname()).isEqualTo("Иванов");
        assertThat(savedUser.getPatronymic()).isEqualTo("Иванович");
        assertThat(savedUser.getPhoneNumber()).isEqualTo("+79161234567");
        assertThat(savedUser.getEmail()).isEqualTo("newuser@example.com");
        assertThat(savedUser.isAdmin()).isFalse();
        assertThat(savedUser.isCoordinator()).isFalse();
        assertThat(savedUser.getUserAuth()).isNotNull();
        assertThat(savedUser.getUserAuth().getPasswordHash()).isEqualTo("encodedPassword123");
        assertThat(savedUser.getUserAuth().getUser()).isSameAs(savedUser);

        verify(refreshTokenService).createRefreshToken(savedUser);
        verifyNoInteractions(coordinatorRepository);
    }

    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail(existingEmailRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.register(existingEmailRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User with email existing@example.com already exists");

        verify(userRepository).existsByEmail(existingEmailRequest.getEmail());
        verify(userRepository, never()).existsByPhoneNumber(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
        verifyNoInteractions(passwordEncoder, jwtService, coordinatorRepository, authenticationManager, refreshTokenService);
    }

    @Test
    void register_shouldThrowException_whenPhoneAlreadyExists() {
        when(userRepository.existsByEmail(existingPhoneRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(existingPhoneRequest.getPhoneNumber())).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.register(existingPhoneRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User with phone number +79161112233 already exists");

        verify(userRepository).existsByEmail(existingPhoneRequest.getEmail());
        verify(userRepository).existsByPhoneNumber(existingPhoneRequest.getPhoneNumber());
        verify(userRepository, never()).save(any(UserEntity.class));
        verifyNoInteractions(passwordEncoder, jwtService, coordinatorRepository, authenticationManager, refreshTokenService);
    }

    @Test
    void registerCoordinator_shouldReturnTokensAndPersistCoordinator_whenRequestIsValid() {
        when(userRepository.existsByEmail(validRegisterRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(validRegisterRequest.getPhoneNumber())).thenReturn(false);
        when(passwordEncoder.encode(validRegisterRequest.getPassword())).thenReturn("encodedCoordinatorPassword");
        when(jwtService.generateAccessToken(any())).thenReturn("coordinator-access-token");
        when(refreshTokenService.createRefreshToken(any(UserEntity.class)))
                .thenReturn(buildRefreshToken("coordinator-refresh-token", null));

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        ArgumentCaptor<CoordinatorEntity> coordinatorCaptor = ArgumentCaptor.forClass(CoordinatorEntity.class);

        AuthenticationResponse response = authenticationService.registerCoordinator(validRegisterRequest);

        assertThat(response.getAccessToken()).isEqualTo("coordinator-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("coordinator-refresh-token");

        InOrder inOrder = inOrder(userRepository, coordinatorRepository);
        inOrder.verify(userRepository).save(userCaptor.capture());
        inOrder.verify(coordinatorRepository).save(coordinatorCaptor.capture());

        UserEntity savedUser = userCaptor.getValue();
        CoordinatorEntity savedCoordinator = coordinatorCaptor.getValue();

        assertThat(savedUser.isCoordinator()).isTrue();
        assertThat(savedUser.isAdmin()).isFalse();
        assertThat(savedUser.getUserAuth().getPasswordHash()).isEqualTo("encodedCoordinatorPassword");
        assertThat(savedCoordinator.getUser()).isSameAs(savedUser);
        assertThat(savedCoordinator.getWorkLocation()).isNull();

        verify(refreshTokenService).createRefreshToken(savedUser);
    }

    @Test
    void registerCoordinator_shouldThrowException_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail(existingEmailRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.registerCoordinator(existingEmailRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User with email existing@example.com already exists");

        verify(userRepository).existsByEmail(existingEmailRequest.getEmail());
        verify(userRepository, never()).existsByPhoneNumber(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
        verifyNoInteractions(coordinatorRepository, passwordEncoder, jwtService, authenticationManager, refreshTokenService);
    }

    @Test
    void registerCoordinator_shouldThrowException_whenPhoneAlreadyExists() {
        when(userRepository.existsByEmail(existingPhoneRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(existingPhoneRequest.getPhoneNumber())).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.registerCoordinator(existingPhoneRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User with phone number +79161112233 already exists");

        verify(userRepository).existsByEmail(existingPhoneRequest.getEmail());
        verify(userRepository).existsByPhoneNumber(existingPhoneRequest.getPhoneNumber());
        verify(userRepository, never()).save(any(UserEntity.class));
        verifyNoInteractions(coordinatorRepository, passwordEncoder, jwtService, authenticationManager, refreshTokenService);
    }

    @Test
    void registerAdmin_shouldReturnTokensAndPersistAdmin_whenRequestIsValid() {
        when(userRepository.existsByEmail(validRegisterRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(validRegisterRequest.getPhoneNumber())).thenReturn(false);
        when(passwordEncoder.encode(validRegisterRequest.getPassword())).thenReturn("encodedAdminPassword");
        when(jwtService.generateAccessToken(any())).thenReturn("admin-access-token");
        when(refreshTokenService.createRefreshToken(any(UserEntity.class)))
                .thenReturn(buildRefreshToken("admin-refresh-token", null));

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);

        AuthenticationResponse response = authenticationService.registerAdmin(validRegisterRequest);

        assertThat(response.getAccessToken()).isEqualTo("admin-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("admin-refresh-token");

        verify(userRepository).save(userCaptor.capture());
        UserEntity savedUser = userCaptor.getValue();

        assertThat(savedUser.isAdmin()).isTrue();
        assertThat(savedUser.isCoordinator()).isFalse();
        assertThat(savedUser.getUserAuth().getPasswordHash()).isEqualTo("encodedAdminPassword");

        verify(refreshTokenService).createRefreshToken(savedUser);
        verifyNoInteractions(coordinatorRepository);
    }

    @Test
    void registerAdmin_shouldThrowException_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail(existingEmailRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.registerAdmin(existingEmailRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User with email existing@example.com already exists");

        verify(userRepository).existsByEmail(existingEmailRequest.getEmail());
        verify(userRepository, never()).existsByPhoneNumber(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
        verifyNoInteractions(coordinatorRepository, passwordEncoder, jwtService, authenticationManager, refreshTokenService);
    }

    @Test
    void registerAdmin_shouldThrowException_whenPhoneAlreadyExists() {
        when(userRepository.existsByEmail(existingPhoneRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(existingPhoneRequest.getPhoneNumber())).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.registerAdmin(existingPhoneRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User with phone number +79161112233 already exists");

        verify(userRepository).existsByEmail(existingPhoneRequest.getEmail());
        verify(userRepository).existsByPhoneNumber(existingPhoneRequest.getPhoneNumber());
        verify(userRepository, never()).save(any(UserEntity.class));
        verifyNoInteractions(coordinatorRepository, passwordEncoder, jwtService, authenticationManager, refreshTokenService);
    }

    @Test
    void refreshToken_shouldReturnRotatedTokens_whenRefreshTokenExists() {
        RefreshTokenEntity oldToken = buildRefreshToken("old-refresh-token", existingUser);
        RefreshTokenEntity newToken = buildRefreshToken("new-refresh-token", existingUser);

        when(refreshTokenService.findByToken("old-refresh-token")).thenReturn(Optional.of(oldToken));
        when(jwtService.generateAccessToken(any())).thenReturn("new-access-token");
        when(refreshTokenService.rotateRefreshToken(oldToken)).thenReturn(newToken);

        AuthenticationResponse response = authenticationService.refreshToken(validRefreshRequest);

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");

        verify(refreshTokenService).findByToken("old-refresh-token");
        verify(jwtService).generateAccessToken(any());
        verify(refreshTokenService).rotateRefreshToken(oldToken);
        verifyNoInteractions(userRepository, coordinatorRepository, passwordEncoder, authenticationManager);
    }

    @Test
    void refreshToken_shouldThrowException_whenRefreshTokenNotFound() {
        when(refreshTokenService.findByToken("old-refresh-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.refreshToken(validRefreshRequest))
                .isInstanceOf(RefreshTokenException.class)
                .hasMessage("Refresh token not found");

        verify(refreshTokenService).findByToken("old-refresh-token");
        verify(refreshTokenService, never()).rotateRefreshToken(any());
        verify(jwtService, never()).generateAccessToken(any());
        verifyNoInteractions(userRepository, coordinatorRepository, passwordEncoder, authenticationManager);
    }

    @Test
    void refreshToken_shouldThrowException_whenRefreshTokenIsExpired() {
        RefreshTokenEntity oldToken = buildRefreshToken("old-refresh-token", existingUser);

        when(refreshTokenService.findByToken("old-refresh-token")).thenReturn(Optional.of(oldToken));
        when(jwtService.generateAccessToken(any())).thenReturn("new-access-token");
        when(refreshTokenService.rotateRefreshToken(oldToken))
                .thenThrow(new RefreshTokenException("Refresh token expired"));

        assertThatThrownBy(() -> authenticationService.refreshToken(validRefreshRequest))
                .isInstanceOf(RefreshTokenException.class)
                .hasMessage("Refresh token expired");

        verify(refreshTokenService).findByToken("old-refresh-token");
        verify(jwtService).generateAccessToken(any());
        verify(refreshTokenService).rotateRefreshToken(oldToken);
        verifyNoInteractions(userRepository, coordinatorRepository, passwordEncoder, authenticationManager);
    }

    @Test
    void authenticate_shouldReturnTokens_whenCredentialsAreValid() {
        when(userRepository.findActiveByEmailWithAuth(validAuthRequest.getEmail()))
                .thenReturn(Optional.of(existingUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(jwtService.generateAccessToken(any())).thenReturn("jwtAccessToken");
        when(refreshTokenService.createRefreshToken(existingUser))
                .thenReturn(buildRefreshToken("refreshToken123", existingUser));

        AuthenticationResponse response = authenticationService.authenticate(validAuthRequest);

        assertThat(response.getAccessToken()).isEqualTo("jwtAccessToken");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken123");

        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);

        verify(authenticationManager).authenticate(authCaptor.capture());
        UsernamePasswordAuthenticationToken capturedToken = authCaptor.getValue();

        assertThat(capturedToken.getPrincipal()).isEqualTo("existing@example.com");
        assertThat(capturedToken.getCredentials()).isEqualTo("CorrectPassword123!");

        verify(userRepository).findActiveByEmailWithAuth("existing@example.com");
        verify(refreshTokenService).createRefreshToken(existingUser);
        verifyNoInteractions(coordinatorRepository);
    }

    @Test
    void authenticate_shouldThrowUserNotFoundException_whenActiveUserNotFound() {
        when(userRepository.findActiveByEmailWithAuth(invalidEmailRequest.getEmail()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.authenticate(invalidEmailRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User with email missing@example.com not found");

        verify(userRepository).findActiveByEmailWithAuth("missing@example.com");
        verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateAccessToken(any());
        verify(refreshTokenService, never()).createRefreshToken(any());
        verifyNoInteractions(coordinatorRepository, passwordEncoder);
    }

    @Test
    void authenticate_shouldThrowInvalidPasswordException_whenPasswordIsIncorrect() {
        when(userRepository.findActiveByEmailWithAuth(invalidPasswordRequest.getEmail()))
                .thenReturn(Optional.of(existingUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authenticationService.authenticate(invalidPasswordRequest))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessage("Invalid password");

        verify(userRepository).findActiveByEmailWithAuth("existing@example.com");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateAccessToken(any());
        verify(refreshTokenService, never()).createRefreshToken(any());
        verifyNoInteractions(coordinatorRepository);
    }
}