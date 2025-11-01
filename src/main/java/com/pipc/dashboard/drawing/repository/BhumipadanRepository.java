package com.pipc.dashboard.drawing.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BhumipadanRepository extends JpaRepository<BhumipadanEntity, Long> {

	@Query("SELECT b FROM BhumipadanEntity b WHERE b.period = :period AND b.flag <> 'D'")
	Page<BhumipadanEntity> findByPeriod(@Param("period") String period, Pageable pageable);

	@Query("SELECT b FROM BhumipadanEntity b WHERE b.period = :period AND b.star = :star AND b.flag <> 'D'")
	Page<BhumipadanEntity> findByPeriodAndStar(@Param("period") String period, @Param("star") String star,
			Pageable pageable);

	Optional<BhumipadanEntity> findByPeriodAndKramankAndSubIdAndStar(String period, Integer kramank, Integer subId,
			String star);
}