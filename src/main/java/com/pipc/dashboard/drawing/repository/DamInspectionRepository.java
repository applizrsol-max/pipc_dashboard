package com.pipc.dashboard.drawing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DamInspectionRepository extends JpaRepository<DamInspectionEntity, Integer> {

	Optional<DamInspectionEntity> findByRowIdAndYearAndDepartmentKeyAndTitleAndPeriod(Integer rowId, String year,
			String departmentKey, String title, String period);

	Page<DamInspectionEntity> findByPeriodContaining(String period, Pageable pageable);

	List<DamInspectionEntity> findByPeriodContaining(String period);

	Optional<DamInspectionEntity> findByDepartmentKeyAndRowIdAndYearAndMonth(String departmentKey, Integer rowId,
			String year, String month);

	Optional<DamInspectionEntity> findByTitleAndDepartmentKeyAndRowIdAndYearAndMonthAndPeriod(String title,
			String departmentKey, Integer rowId, String year, String month, String period);

	

	// For all departments (no dept filter)
	Page<DamInspectionEntity> findByYearAndPeriod(String year, String period, Pageable pageable);
	
	@Query("SELECT DISTINCT d.departmentKey FROM DamInspectionEntity d WHERE d.year = :year AND d.period = :period")
	List<String> findDistinctDepartmentKeys(@Param("year") String year, @Param("period") String period);

	Page<DamInspectionEntity> findByYearAndPeriodAndDepartmentKey(String year, String period, String departmentKey, Pageable pageable);

	List<DamInspectionEntity> findByPeriodOrderByDepartmentKeyAscRowIdAsc(String period);

	

}