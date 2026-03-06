package org.adt.volunteerscase.service.security;

import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.entity.RefreshTokenEntity;
import org.adt.volunteerscase.entity.user.UserEntity;
import org.adt.volunteerscase.exception.RefreshTokenException;
import org.adt.volunteerscase.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token.expiration.sec}")
    private Integer refreshTokenExpirationSeconds;

    /**
     * Creates and persists a new refresh token for the given user, removing any existing refresh tokens for that user.
     *
     * @param user the user to associate the created refresh token with
     * @return the persisted RefreshTokenEntity containing the token value and expiry timestamp
     */
    public RefreshTokenEntity createRefreshToken(UserEntity user) {
        deleteAllByUser(user);

        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                .createdAt(LocalDateTime.now())
                .expiryAt(LocalDateTime.now().plusSeconds(refreshTokenExpirationSeconds))
                .refreshToken(UUID.randomUUID().toString())
                .user(user)
                .build();

        refreshTokenRepository.save(refreshToken);

        return refreshToken;
    }

    /**
     * Removes all refresh tokens associated with the given user.
     *
     * @param user the user whose refresh tokens will be deleted
     */
    public void deleteAllByUser(UserEntity user) {
        refreshTokenRepository.deleteAllByUser(user);
    }

    /**
     * Locate a refresh token entity by its token string.
     *
     * @param refreshToken the refresh token string to look up
     * @return an Optional containing the matching RefreshTokenEntity if found, or empty if not found
     */
    Optional<RefreshTokenEntity> findByToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken);
    }

    /**
     * Validates that the given refresh token has not expired and removes it if it has.
     *
     * @param refreshToken the refresh token entity to validate
     * @return the provided `refreshToken` when it is not expired
     * @throws RefreshTokenException if the token is expired; the expired token is deleted before the exception is thrown
     */
    public RefreshTokenEntity verifyExpiration(RefreshTokenEntity refreshToken) {
        if (refreshToken.getExpiryAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RefreshTokenException("Refresh token expired");
        }
        return refreshToken;
    }

    /**
     * Replace an existing refresh token with a newly created token for the same user.
     *
     * Verifies the provided token is not expired, deletes it, and issues a new token tied to the same user.
     *
     * @param oldToken the refresh token to rotate
     * @return the newly created refresh token for the same user
     * @throws RefreshTokenException if the provided token has expired
     */
    public RefreshTokenEntity rotateRefreshToken(RefreshTokenEntity oldToken) {

        verifyExpiration(oldToken);

        refreshTokenRepository.delete(oldToken);
        return createRefreshToken(oldToken.getUser());
    }
}