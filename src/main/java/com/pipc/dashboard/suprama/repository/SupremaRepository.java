package com.pipc.dashboard.suprama.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupremaRepository extends JpaRepository<SupremaEntity, Long> {

	Optional<SupremaEntity> findByProjectYearAndRowId(String projectYear, Integer rowId);

	Optional<SupremaEntity> findByProjectYearAndRowIdAndProjectName(String projectYear, Integer rowId,
			String projectName);

	Page<SupremaEntity> findByProjectYear(String projectYear, Pageable pageable);

	Optional<SupremaEntity> findByProjectYearAndDeleteIdAndProjectName(String projectYear, Long deleteId,
			String projectName);

	List<SupremaEntity> findByProjectYearOrderByRowIdAsc(String projectYear);

}
