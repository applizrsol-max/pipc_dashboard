package com.pipc.dashboard.drawing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
public interface PralambitBhusampadanRepository extends JpaRepository<PralambitBhusampadanEntity, Long> {

	@Query("SELECT p FROM PralambitBhusampadanEntity p WHERE p.period = :period AND p.flag <> 'D'")
	List<PralambitBhusampadanEntity> findByPeriod(@Param("period") String period);

	@Query("SELECT p FROM PralambitBhusampadanEntity p WHERE p.period = :period AND p.star = :star AND p.flag <> 'D'")
	Page<PralambitBhusampadanEntity> findByPeriodAndStar(@Param("period") String period, @Param("star") String star,
			Pageable pageable);

	Optional<PralambitBhusampadanEntity> findByPeriodAndKramankAndSubIdAndStar(String period, Integer kramank,
			Integer subId, String star);

	Optional<PralambitBhusampadanEntity> findByPeriodAndKramankAndSubIdAndStar(String period, int kramank, int subId,
			String star);

	// ✅ Delete one record by its custom deleteId
	@Transactional
	@Modifying
	@Query("DELETE FROM PralambitBhusampadanEntity e WHERE e.deleteId = :deleteId")
	void deleteByCustomDeleteId(@Param("deleteId") long deleteId);

	// ✅ Delete all records having same overall delete ID
	@Transactional
	@Modifying
	@Query("DELETE FROM PralambitBhusampadanEntity e WHERE e.overAllDeleteId = :overAllDeleteId")
	void deleteByCustomOverAllDeleteId(@Param("overAllDeleteId") long overAllDeleteId);

	List<PralambitBhusampadanEntity> findByPeriodOrderByKramankAscSubIdAsc(String period);
}