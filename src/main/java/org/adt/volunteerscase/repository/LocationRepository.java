package org.adt.volunteerscase.repository;

import org.adt.volunteerscase.entity.LocationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<LocationEntity, Integer> {

    Optional<LocationEntity> findByLocationId(Integer locationId);
    Optional<LocationEntity> findByAddress(String address);

    boolean existsByAddress(String address);

    boolean existsByLocationId(Integer id);

    @Query("SELECT l FROM LocationEntity l WHERE " +
            "LOWER(l.address) LIKE LOWER(CONCAT('%', :address, '%'))")
    Page<LocationEntity> searchByAddress(@Param("address") String address, Pageable pageable);
}