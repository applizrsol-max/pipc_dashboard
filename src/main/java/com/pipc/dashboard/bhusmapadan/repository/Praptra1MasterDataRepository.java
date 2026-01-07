package com.pipc.dashboard.bhusmapadan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface Praptra1MasterDataRepository extends JpaRepository<Praptra1MasterDataEntity, Long> {

	Optional<Praptra1MasterDataEntity> findByYearAndRowIdAndProjectName(@Param("year") String year,
			@Param("rowId") Long rowId, @Param("projectName") String projectName);

	void deleteByYearAndProjectNameAndDeleteId(@Param("year") String year, @Param("projectName") String projectName,
			@Param("deleteId") Long deleteId);

	@Query(value = """
			SELECT * FROM praptra1_master_data
			WHERE year = :year
			ORDER BY row_id
			""", nativeQuery = true)
	List<Praptra1MasterDataEntity> findAllByYear(@Param("year") String year);

	List<Praptra1MasterDataEntity> findAllByYearAndProjectName(String year, String projectName);

}
