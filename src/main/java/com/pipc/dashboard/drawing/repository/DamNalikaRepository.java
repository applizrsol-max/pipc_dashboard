package com.pipc.dashboard.drawing.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DamNalikaRepository extends JpaRepository<DamNalikaEntity, Integer> {

	Optional<DamNalikaEntity> findByDepartmentKeyAndRowIdAndYearAndMonthAndPeriod(String departmentKey, Integer rowId,
			String year, String month, String period);

	@Query("SELECT d FROM DamNalikaEntity d WHERE d.period = :period AND d.flag <> :flag")
	Page<DamNalikaEntity> findByPeriodAndFlagNot(@Param("period") String period, @Param("flag") String flag,
			Pageable pageable);

	@Query("SELECT d FROM DamNalikaEntity d WHERE d.period = :period AND d.departmentKey = :departmentKey AND d.flag <> :flag")
	Page<DamNalikaEntity> findByPeriodAndDepartmentKeyAndFlagNot(@Param("period") String period,
			@Param("departmentKey") String departmentKey, @Param("flag") String flag, Pageable pageable);

	@Query("SELECT d FROM DamNalikaEntity d WHERE d.period = :period AND d.flag <> 'D'")
	Page<DamNalikaEntity> findByPeriod(@Param("period") String period, Pageable pageable);

	@Query("SELECT d FROM DamNalikaEntity d WHERE d.period = :period AND d.departmentKey = :deptKey AND d.flag <> 'D'")
	Page<DamNalikaEntity> findByPeriodAndDepartment(@Param("period") String period, @Param("deptKey") String deptKey,
			Pageable pageable);

	Optional<DamNalikaEntity> findByDepartmentKeyAndDeleteIdAndYearAndMonthAndPeriod(String deptKey, Long deleteId,
			String year, String month, String period);

	List<DamNalikaEntity> findByPeriodOrderByDepartmentKeyAscRowIdAsc(String period);

	List<DamNalikaEntity> findByPeriodOrderByRowIdAsc(String period);

}
