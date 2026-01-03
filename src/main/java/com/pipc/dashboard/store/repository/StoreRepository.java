package com.pipc.dashboard.store.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<StoreEntity, Long> {

	// ---------------- BASIC ----------------
	List<StoreEntity> findByDepartmentName(String departmentName);

	Optional<StoreEntity> findByDepartmentNameAndRowId(String departmentName, Integer rowId);

	Optional<StoreEntity> findByDepartmentNameAndRowIdAndYear(String departmentName, Integer rowId, String year);

	Optional<StoreEntity> findByDepartmentNameAndDeleteIdAndYear(String departmentName, Long deleteId, String year);

	List<StoreEntity> findAllByDepartmentNameAndYear(String departmentName, String year);

	List<StoreEntity> findByDepartmentNameAndYearOrderByRowIdAsc(String dept, String year);

	// ❗ FIXED: year parameter mandatory
	List<StoreEntity> findByYear(String year);

	// ---------------- DISTINCT ----------------
	@Query("SELECT DISTINCT s.departmentName FROM StoreEntity s ORDER BY s.departmentName")
	List<String> findDistinctDepartmentNames();

	@Query("""
			    SELECT DISTINCT s.departmentName
			    FROM StoreEntity s
			    WHERE s.year = :year
			    ORDER BY s.departmentName
			""")
	List<String> findDistinctDepartmentNamesByYear(@Param("year") String year);

	// ---------------- EKUN (DEPT LEVEL) ----------------
	@Query("""
			    SELECT s
			    FROM StoreEntity s
			    WHERE s.departmentName = :deptName
			      AND s.year = :year
			      AND s.ekun IS NOT NULL
			""")
	Optional<StoreEntity> findExistingEkunForDeptAndYear(@Param("deptName") String deptName,
			@Param("year") String year);

	// ---------------- EKUN EKANDAR (OVERALL) ----------------
	@Query("""
			    SELECT s
			    FROM StoreEntity s
			    WHERE s.year = :year
			      AND s.ekunEkandar IS NOT NULL
			""")
	Optional<StoreEntity> findExistingEkunEkandarByYear(@Param("year") String year);

	@Modifying
	@Query("""
			    UPDATE StoreEntity s
			       SET s.ekunEkandar = :ekunEkandar,
			           s.updatedBy = :updatedBy,
			           s.updatedAt = CURRENT_TIMESTAMP,
			           s.flag = 'U'
			     WHERE s.year = :year
			""")
	int updateEkunEkandarAndTimestampByYear(@Param("ekunEkandar") Integer ekunEkandar,
			@Param("updatedBy") String updatedBy, @Param("year") String year);
}
