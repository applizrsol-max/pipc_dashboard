package com.pipc.dashboard.bhusmapadan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
public interface PraptraMasterDataRepository extends JpaRepository<PraptraMasterDataEntity, Long> {

	@Query(value = """
			SELECT * FROM praptra_master_data
			WHERE year = :year
			  AND row_id = :rowId
			  AND data->>'projectName' = :projectName
			""", nativeQuery = true)
	Optional<PraptraMasterDataEntity> findByYearRowIdAndProjectName(@Param("year") String year,
			@Param("rowId") Long rowId, @Param("projectName") String projectName);

	@Modifying
	@Transactional
	@Query(value = """
			DELETE FROM praptra_master_data
			WHERE year = :year
			  AND delete_id = :deleteId
			  AND data->>'projectName' = :projectName
			""", nativeQuery = true)
	void deleteByYearDeleteIdAndProjectName(@Param("year") String year, @Param("deleteId") Long deleteId,
			@Param("projectName") String projectName);

	@Query(value = """
			SELECT * FROM praptra_master_data
			WHERE year = :year
			ORDER BY row_id
			""", nativeQuery = true)
	List<PraptraMasterDataEntity> findAllByYear(@Param("year") String year);

}
