package org.adt.volunteerscase.repository;

import org.adt.volunteerscase.entity.UserEventEntity;
import org.adt.volunteerscase.entity.UserEventId;
import org.adt.volunteerscase.entity.event.EventEntity;
import org.adt.volunteerscase.entity.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserEventRepository extends JpaRepository<UserEventEntity, UserEventId> {

    Optional<UserEventEntity> findByUserAndEvent(UserEntity user, EventEntity event);

    boolean existsByUserAndEvent(UserEntity user, EventEntity event);

    List<UserEventEntity> findAllByUserAndDeletedAtIsNull(UserEntity user);

    Optional<UserEventEntity> findByUserAndEventAndDeletedAtIsNull(UserEntity user, EventEntity event);

    boolean existsByUserAndEventAndDeletedAtIsNull(UserEntity user, EventEntity event);

}

