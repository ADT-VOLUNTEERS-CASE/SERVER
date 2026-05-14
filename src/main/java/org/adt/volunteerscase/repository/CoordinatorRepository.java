package org.adt.volunteerscase.repository;

import org.adt.volunteerscase.entity.CoordinatorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface CoordinatorRepository extends JpaRepository<CoordinatorEntity, Integer> {

    List<CoordinatorEntity> findAllByUserIdIn(Collection<Integer> userIds);
}
