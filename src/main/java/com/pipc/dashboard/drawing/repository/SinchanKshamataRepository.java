package com.pipc.dashboard.drawing.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SinchanKshamataRepository extends JpaRepository<SinchanKshamataEntity, Long> {

	List<SinchanKshamataEntity> findByPeriodOrderBySectionTitleAscRowIdAsc(String period);

	Optional<SinchanKshamataEntity> findByPeriodAndSectionTitleAndDeleteId(String period, String sectionTitle,
			Long deleteId);

	List<SinchanKshamataEntity> findByPeriodAndDateOrderBySectionTitleAscRowIdAsc(String period, LocalDate filterDate);

	List<SinchanKshamataEntity> findByPeriodAndDate(String period, LocalDate localDate);

	List<SinchanKshamataEntity> findByPeriod(String period);
}
