package com.pipc.dashboard.drawing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TenderSummaryRepository extends JpaRepository<TenderSummaryEntity, Long> {

	List<TenderSummaryEntity> findByYear(String year);

	Optional<TenderSummaryEntity> findByYearAndRowId(String year, Long rowId);

	Optional<TenderSummaryEntity> findByYearAndDeleteId(String year, Long deleteId);
}
