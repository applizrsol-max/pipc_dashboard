package com.pipc.dashboard.establishment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeePostingRepository extends JpaRepository<EmployeePostingEntity, Long> {

	Optional<EmployeePostingEntity> findByRowId(Long rowId);

	@Query("SELECT e FROM EmployeePostingEntity e "
			+ "WHERE (:name IS NULL OR LOWER(e.adhikariKarmacharyacheNav) LIKE LOWER(CONCAT('%', :name, '%'))) "
			+ "AND (:year IS NULL OR e.year = :year)")
	List<EmployeePostingEntity> findByNameAndYear(@Param("name") String name, @Param("year") String year);
}
