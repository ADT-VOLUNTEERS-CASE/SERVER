package org.adt.volunteerscase.service;

import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.entity.RefreshTokenEntity;
import org.adt.volunteerscase.entity.user.UserEntity;
import org.adt.volunteerscase.exception.RefreshTokenExcepion;
import org.adt.volunteerscase.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {
    @Autowired
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenEntity createRefreshToken(UserEntity user) {
        deleteAllByUser(user);

        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                .createdAt(LocalDateTime.now())
                .expiryAt(LocalDateTime.now().plusSeconds(60 * 60 * 24 * 7))
                .refreshToken(UUID.randomUUID().toString())
                .user(user)
                .build();

        refreshTokenRepository.save(refreshToken);

        return refreshToken;
    }

    public void deleteAllByUser(UserEntity user) {
        refreshTokenRepository.deleteAllByUser(user);
    }

    Optional<RefreshTokenEntity> findByToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken);
    }

    public RefreshTokenEntity verifyExpiration(RefreshTokenEntity refreshToken) {
        if (refreshToken.getExpiryAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RefreshTokenExcepion("Refresh token expired");
        }
        return refreshToken;
    }

    public RefreshTokenEntity rotateRefreshToken(RefreshTokenEntity oldToken) {

        verifyExpiration(oldToken);

        refreshTokenRepository.delete(oldToken);
        return createRefreshToken(oldToken.getUser());
    }
}
