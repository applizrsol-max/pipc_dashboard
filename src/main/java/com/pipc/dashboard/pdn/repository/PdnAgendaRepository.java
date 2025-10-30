package com.pipc.dashboard.pdn.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PdnAgendaRepository extends JpaRepository<PdnAgendaEntity, Long> {

	Optional<PdnAgendaEntity> findBySubmissionYearAndPointOfAgendaAndRecordIdAndNameOfDam(String submissionYear,
			String pointOfAgenda, String recordId, String nameOfDam);

	Page<PdnAgendaEntity> findBySubmissionYear(String projectYear, Pageable pageable);

	
}