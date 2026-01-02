package org.adt.volunteerscase.repository;

import org.adt.volunteerscase.entity.RefreshTokenEntity;
import org.adt.volunteerscase.entity.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    /**
 * Retrieves the first refresh token associated with the specified user.
 *
 * @param user the user whose refresh token to retrieve
 * @return an Optional containing the first RefreshTokenEntity for the given user, or empty if none exists
 */
Optional<RefreshTokenEntity> getFirstByUser(UserEntity user);

    /**
 * Finds the refresh token entity that matches the given token value.
 *
 * @param refreshToken the refresh token string to look up
 * @return an Optional containing the matching RefreshTokenEntity if present, {@code Optional.empty()} otherwise
 */
Optional<RefreshTokenEntity> findByRefreshToken(String refreshToken);

    /**
 * Deletes all refresh token records associated with the specified user.
 *
 * @param user the user whose refresh tokens will be removed
 */
void deleteAllByUser(UserEntity user);
}