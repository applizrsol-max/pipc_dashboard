package com.pipc.dashboard.drawing.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PralambitBhusampadanRepository extends JpaRepository<PralambitBhusampadanEntity, Long> {

	@Query("SELECT p FROM PralambitBhusampadanEntity p WHERE p.period = :period AND p.flag <> 'D'")
	Page<PralambitBhusampadanEntity> findByPeriod(@Param("period") String period, Pageable pageable);

	@Query("SELECT p FROM PralambitBhusampadanEntity p WHERE p.period = :period AND p.star = :star AND p.flag <> 'D'")
	Page<PralambitBhusampadanEntity> findByPeriodAndStar(@Param("period") String period, @Param("star") String star,
			Pageable pageable);

	Optional<PralambitBhusampadanEntity> findByPeriodAndKramankAndSubIdAndStar(String period, Integer kramank,
			Integer subId, String star);
}