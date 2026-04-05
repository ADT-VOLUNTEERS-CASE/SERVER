package org.adt.volunteerscase.repository;

import org.adt.volunteerscase.entity.CoordinatorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoordinatorRepository extends JpaRepository<CoordinatorEntity, Integer> {
}
