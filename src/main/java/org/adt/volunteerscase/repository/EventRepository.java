package org.adt.volunteerscase.repository;

import org.adt.volunteerscase.entity.CoverEntity;
import org.adt.volunteerscase.entity.LocationEntity;
import org.adt.volunteerscase.entity.event.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Integer> {

    Optional<EventEntity> findByEventId(Integer eventId);

    boolean existsByLocation(LocationEntity locationEntity);
    boolean existsByCover(CoverEntity coverEntity);

    boolean existsByLocationAndEventIdNot(LocationEntity location, Integer eventId);
    boolean existsByCoverAndEventIdNot(CoverEntity cover, Integer eventId);
}
