package com.pipc.dashboard.pdn.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PdnAgendaRepository extends JpaRepository<PdnAgendaEntity, Long> {

	Page<PdnAgendaEntity> findBySubmissionYear(String projectYear, Pageable pageable);

	List<PdnAgendaEntity> findBySubmissionYear(String year);

	Optional<PdnAgendaEntity> findBySubmissionYearAndPointOfAgendaAndDeleteIdAndNameOfDam(String submissionYear,
			String pointOfAgenda, Long deleteId, String damName);

	Optional<PdnAgendaEntity> findBySubmissionYearAndPointOfAgendaAndRecordIdAndNameOfDam(String submissionYear,
			String pointOfAgenda, Integer rowId, String damName);

	List<PdnAgendaEntity> findBySubmissionYearOrderByRecordIdAsc(String projectYear);

}