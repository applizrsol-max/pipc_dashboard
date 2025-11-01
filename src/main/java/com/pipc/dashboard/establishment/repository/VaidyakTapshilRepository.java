package com.pipc.dashboard.establishment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VaidyakTapshilRepository extends JpaRepository<VaidyakTapshilEntity, Long> {
}