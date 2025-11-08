package com.pipc.dashboard.drawing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DamMetaRepository extends JpaRepository<DamMetaEntity, Long> {
	Optional<DamMetaEntity> findByTitleAndPeriodAndUnit(String title, String period, String unit);

	Optional<DamMetaEntity> findByTitleAndPeriod(String title, String period);

	List<DamMetaEntity> findByPeriodContaining(String year);

	Optional<DamMetaEntity> findFirstByPeriod(String period);

}