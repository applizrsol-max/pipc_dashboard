package com.pipc.dashboard.establishment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AgendaOfficerRepository extends JpaRepository<AgendaOfficerEntity, Long> {

	Optional<AgendaOfficerEntity> findByRowIdAndYearAndTargetDate(long rowId, String year, String targetDate);

	Optional<AgendaOfficerEntity> findByDeleteIdAndYearAndTargetDate(Long deleteId, String year, String targetDate);

	List<AgendaOfficerEntity> findByYearAndTargetDate(String year, String targetDate);
}