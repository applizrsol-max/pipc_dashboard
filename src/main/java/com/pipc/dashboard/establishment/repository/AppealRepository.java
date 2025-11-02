package com.pipc.dashboard.establishment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppealRepository extends JpaRepository<AppealEntity, Long> {

	Optional<AppealEntity> findByRowId(Long rowId);

	List<AppealEntity> findByYearAndApeelkaracheNavAndApeelArjachaNondaniKramank(String year, String name,
			String registerNumber);

	Page<AppealEntity> findByYear(String year, Pageable pageable);

}