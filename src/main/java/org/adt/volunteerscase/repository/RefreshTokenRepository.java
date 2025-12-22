package org.adt.volunteerscase.repository;

import org.adt.volunteerscase.entity.RefreshTokenEntity;
import org.adt.volunteerscase.entity.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> getFirstByUser(UserEntity user);

    Optional<RefreshTokenEntity> findByRefreshToken(String refreshToken);

    void deleteAllByUser(UserEntity user);
}
