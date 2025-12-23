package org.adt.volunteerscase.service;


import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.dto.auth.AuthenticationRequest;
import org.adt.volunteerscase.dto.auth.AuthenticationResponse;
import org.adt.volunteerscase.dto.auth.RegisterRequest;
import org.adt.volunteerscase.dto.auth.TokenRefreshRequest;
import org.adt.volunteerscase.entity.RefreshTokenEntity;
import org.adt.volunteerscase.entity.user.UserAuthEntity;
import org.adt.volunteerscase.entity.user.UserDetailsImpl;
import org.adt.volunteerscase.entity.user.UserEntity;
import org.adt.volunteerscase.exception.InvalidPasswordException;
import org.adt.volunteerscase.exception.RefreshTokenException;
import org.adt.volunteerscase.exception.UserAlreadyExistsException;
import org.adt.volunteerscase.exception.UserNotFoundException;
import org.adt.volunteerscase.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    private final RefreshTokenService refreshTokenService;

    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new UserAlreadyExistsException("User with phone number " + request.getPhoneNumber() + " already exists");
        }
        var userAuth = UserAuthEntity.builder()
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();
        var user = UserEntity.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .patronymic(request.getPatronymic())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .userAuth(userAuth)
                .build();
        userAuth.setUser(user);
        userRepository.save(user);
        var jwtToken = jwtService.generateAccessToken(new UserDetailsImpl(user, userAuth));

        var refreshToken = refreshTokenService.createRefreshToken(user).getRefreshToken();

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponse refreshToken(TokenRefreshRequest request) {
        RefreshTokenEntity oldToken = refreshTokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RefreshTokenException("Refresh token not found"));

        UserEntity user = oldToken.getUser();

        var jwtToken = jwtService.generateAccessToken(new UserDetailsImpl(user, user.getUserAuth()));

        var refreshToken = refreshTokenService.rotateRefreshToken(oldToken).getRefreshToken();

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        String email = request.getEmail();
        if (!userRepository.existsByEmail(email)) {
            throw new UserNotFoundException("User with email " + email + " not found");
        }
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            request.getPassword()
                    )
            );
            var user = userRepository.findByEmailWithAuth(request.getEmail()).orElseThrow();

            var jwtToken = jwtService.generateAccessToken(new UserDetailsImpl(user, user.getUserAuth()));

            var refreshToken = refreshTokenService.createRefreshToken(user).getRefreshToken();

            return AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .build();
        } catch (BadCredentialsException e) {
            throw new InvalidPasswordException("Invalid password");
        }
    }
}