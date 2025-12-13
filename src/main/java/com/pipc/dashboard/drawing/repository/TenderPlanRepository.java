package com.pipc.dashboard.drawing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TenderPlanRepository extends JpaRepository<TenderPlanEntity, Long> {

	List<TenderPlanEntity> findByYear(String year);

	Optional<TenderPlanEntity> findByYearAndRowId(String year, Long rowId);

	Optional<TenderPlanEntity> findByYearAndDeleteId(String year, Long deleteId);
}
