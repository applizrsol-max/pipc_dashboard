package com.pipc.dashboard.bhusmapadan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface Praptra3MasterDataRepository extends JpaRepository<Praptra3MasterDataEntity, Long> {

	// 🔍 FIND FOR UPDATE
	@Query(value = """
			SELECT * FROM praptra3_master_data
			WHERE year = :year
			  AND row_id = :rowId
			  AND data->>'projectName' = :projectName
			""", nativeQuery = true)
	Optional<Praptra3MasterDataEntity> findByYearRowIdAndProjectName(@Param("year") String year,
			@Param("rowId") Long rowId, @Param("projectName") String projectName);

	// 🔴 HARD DELETE (deleteId mandatory)
	@Modifying
	@Transactional
	@Query(value = """
			DELETE FROM praptra3_master_data
			WHERE year = :year
			  AND delete_id = :deleteId
			  AND data->>'projectName' = :projectName
			""", nativeQuery = true)
	void deleteByYearProjectNameAndDeleteId(@Param("year") String year, @Param("projectName") String projectName,
			@Param("deleteId") Long deleteId);

	// 📥 GET – YEAR ONLY
	@Query(value = """
			SELECT * FROM praptra3_master_data
			WHERE year = :year
			ORDER BY row_id
			""", nativeQuery = true)
	List<Praptra3MasterDataEntity> findAllByYear(@Param("year") String year);
}
