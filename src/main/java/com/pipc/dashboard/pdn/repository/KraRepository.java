package com.pipc.dashboard.pdn.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KraRepository extends JpaRepository<KraEntity, Long> {

	Optional<KraEntity> findByKraPeriodAndRowId(String kraPeriod, Integer rowId);

	Page<KraEntity> findByKraPeriod(String kraPeriod, Pageable pageable);

}