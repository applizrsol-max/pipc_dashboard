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

	Optional<StoreEntity> findByDeptName(String deptName);

	@Modifying
	@Query("UPDATE StoreEntity s SET s.ekunEkandar = :total, s.updatedAt = CURRENT_TIMESTAMP WHERE s.ekunEkandar <> :total")
	void updateEkunEkandarAndTimestamp(@Param("total") Integer total);

	@Query("SELECT MAX(s.ekunEkandar) FROM StoreEntity s")
	Integer findCurrentEkunEkandar();

	 Optional<StoreEntity> findByDeptNameAndEkunEkandarAndRowId(String deptName, Integer ekunEkandar, Integer rowId);
	 Optional<StoreEntity> findByDeptNameAndRowIdAndEkunAndEkunEkandar(
		        String deptName, Integer rowId, Integer ekun, Integer ekunEkandar
		);
	// Find any existing overall total
	 @Query("SELECT DISTINCT s.ekunEkandar FROM StoreEntity s")
	 Optional<Integer> findExistingEkunEkandar();

	 // Find existing department total
	 @Query("SELECT DISTINCT s.ekun FROM StoreEntity s WHERE s.deptName = :deptName")
	 Optional<Integer> findExistingEkunForDept(@Param("deptName") String deptName);

	 // Find all rows of a department
	 List<StoreEntity> findAllByDeptName(String deptName);

	 // Find a row by dept + rowId
	 Optional<StoreEntity> findByDeptNameAndRowId(String deptName, Integer rowId);

}