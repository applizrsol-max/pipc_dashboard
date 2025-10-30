package com.pipc.dashboard.store.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<StoreEntity, Long> {

	// 🔹 Find single department record (for JSON update or check)
	List<StoreEntity> findByDepartmentName(String departmentName);

	@Modifying
	@Query("""
			    UPDATE StoreEntity s
			    SET s.ekunEkandar = :total,
			        s.updatedAt = CURRENT_TIMESTAMP,
			        s.updatedBy = :updatedBy,
			        s.flag = CASE WHEN s.flag = 'C' THEN 'U' ELSE s.flag END
			    WHERE s.ekunEkandar IS NULL OR s.ekunEkandar <> :total
			""")
	void updateEkunEkandarAndTimestamp(@Param("total") Integer total, @Param("updatedBy") String updatedBy);

	// 🔹 Get latest ekunEkandar
	@Query("SELECT MAX(s.ekunEkandar) FROM StoreEntity s")
	Integer findCurrentEkunEkandar();

	// 🔹 Find by combination (used for update checks)
	Optional<StoreEntity> findByDepartmentNameAndEkunEkandarAndRowId(String departmentName, Integer ekunEkandar,
			Integer rowId);

	Optional<StoreEntity> findByDepartmentNameAndRowIdAndEkunAndEkunEkandar(String departmentName, Integer rowId,
			Integer ekun, Integer ekunEkandar);

	Optional<StoreEntity> findByDepartmentNameAndRowId(String departmentName, Integer rowId);

	@Query("SELECT DISTINCT s.ekun FROM StoreEntity s WHERE s.departmentName = :departmentName")
	Optional<Integer> findExistingEkunForDept(@Param("departmentName") String departmentName);

	List<StoreEntity> findAllByDepartmentName(String departmentName);

	@Query("SELECT DISTINCT s.ekunEkandar FROM StoreEntity s WHERE s.ekunEkandar IS NOT NULL")
	Optional<Integer> findExistingEkunEkandar();

	@Query("SELECT DISTINCT s.departmentName FROM StoreEntity s ORDER BY s.departmentName")
	List<String> findDistinctDepartmentNames();

	Page<StoreEntity> findByDepartmentName(String departmentName, Pageable pageable);

}
