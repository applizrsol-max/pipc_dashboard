package com.pipc.dashboard.suprama.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pipc.dashboard.store.repository.StoreEntity;

@Repository
public interface SupremaRepository extends JpaRepository<SupremaEntity, Long> {
    Optional<SupremaEntity> findByProjectNameAndProjectYear(String projectName, String projectYear);
   

}
