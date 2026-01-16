package org.adt.volunteerscase.repository;

import org.adt.volunteerscase.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<LocationEntity, Integer> {

    Optional<LocationEntity> findByLocationId(Integer locationId);

    boolean existsByAddress(String address);
    boolean existsByLocationId(Integer id);
}
