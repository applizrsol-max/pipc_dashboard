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
	@Query("""
			SELECT p FROM Praptra3MasterDataEntity p
			WHERE p.year = :year
			  AND p.rowId = :rowId
			  AND p.projectName = :projectName
			""")
	Optional<Praptra3MasterDataEntity> findByYearRowIdAndProjectName(@Param("year") String year,
			@Param("rowId") Long rowId, @Param("projectName") String projectName);

	@Modifying
	@Transactional
	@Query("""
			DELETE FROM Praptra3MasterDataEntity p
			WHERE p.year = :year
			  AND p.projectName = :projectName
			  AND p.deleteId = :deleteId
			""")
	void deleteByYearProjectNameAndDeleteId(@Param("year") String year, @Param("projectName") String projectName,
			@Param("deleteId") Long deleteId);

	List<Praptra3MasterDataEntity> findAllByYearAndProjectName(String year, String projectName);
}
