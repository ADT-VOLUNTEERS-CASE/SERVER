package org.adt.volunteerscase.repository;

import org.adt.volunteerscase.entity.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.userAuth WHERE u.email = :email")
    Optional<UserEntity> findByEmailWithAuth(@Param("email") String email);

    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.userAuth WHERE u.phoneNumber = :phone")
    Optional<UserEntity> findByPhoneNumberWithAuth(@Param("phone") String phone);

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

}

