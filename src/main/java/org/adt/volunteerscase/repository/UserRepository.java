package org.adt.volunteerscase.repository;

import org.adt.volunteerscase.entity.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    /**
     * Finds a user by email and eagerly fetches its associated authentication entity.
     *
     * @param email the email address of the user to find
     * @return an Optional containing the matching UserEntity with its `userAuth` loaded, or `Optional.empty()` if none found
     */
    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.userAuth WHERE u.email = :email")
    Optional<UserEntity> findByEmailWithAuth(@Param("email") String email);

    /**
     * Retrieves a user by phone number and includes the user's authentication relation.
     *
     * @param phone the phone number to search for
     * @return an Optional containing the matching UserEntity with its `userAuth` initialized, or empty if no match
     */
    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.userAuth WHERE u.phoneNumber = :phone")
    Optional<UserEntity> findByPhoneNumberWithAuth(@Param("phone") String phone);

    /**
 * Finds a user with the given email.
 *
 * @return an Optional containing the UserEntity with the given email, or an empty Optional if no user matches
 */
Optional<UserEntity> findByEmail(String email);

    /**
 * Checks whether a user with the given email exists.
 *
 * @param email the email address to check for an existing user
 * @return `true` if a user with the given email exists, `false` otherwise
 */
boolean existsByEmail(String email);

    /**
 * Checks whether a user with the given phone number exists.
 *
 * @param phoneNumber the phone number to check for existence
 * @return `true` if a user with the given phone number exists, `false` otherwise
 */
boolean existsByPhoneNumber(String phoneNumber);

}
