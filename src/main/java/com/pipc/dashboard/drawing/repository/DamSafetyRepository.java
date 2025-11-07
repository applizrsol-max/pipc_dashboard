package com.pipc.dashboard.drawing.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DamSafetyRepository extends JpaRepository<DamSafetyEntity, Long> {
	Optional<DamSafetyEntity> findByRowIdAndYearAndMonthAndProjectName(Integer rowId, String year, String month,
			String projectName);

	@Modifying
	@Query("""
			    UPDATE DamSafetyEntity d
			    SET d.meta.id = :metaId
			    WHERE d.meta.title = :title
			      AND d.meta.period = :period
			      AND d.meta.unit = :unit
			""")
	int updateMetaForAllRows(@Param("title") String title, @Param("period") String period, @Param("unit") String unit,
			@Param("metaId") Long metaId);

	Optional<DamSafetyEntity> findByRowIdAndYearAndMonthAndProjectNameAndMetaId(Integer rowId, String year,
			String month, String projectName, Long metaId);

	Page<DamSafetyEntity> findByMetaId(Long metaId, Pageable pageable);

	Optional<DamSafetyEntity> findByDeleteIdAndYearAndMonthAndProjectNameAndMetaId(Long deleteId, String year,
			String month, String projectName, Long id);

}
