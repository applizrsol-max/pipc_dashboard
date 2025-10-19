package com.pipc.dashboard.store.repository;

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

}