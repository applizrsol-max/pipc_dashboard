package com.pipc.dashboard.establishment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeputyReturnARepository extends JpaRepository<DeputyReturnAEntity, Long> {

	Optional<DeputyReturnAEntity> findByYearAndRowId(String year, Long rowId);

	Optional<DeputyReturnAEntity> findByYearAndDeleteId(String year, Long deleteId);

	List<DeputyReturnAEntity> findByYearOrderByRowIdAsc(String year);

	List<DeputyReturnAEntity> findByYearOrderByKaryalayacheNavAscRowIdAsc(String year);

	void deleteByYearAndDeleteId(String year, Long deleteId);

	Optional<DeputyReturnAEntity> findByDeleteIdAndYearAndKaryalayacheNav(Long deleteId, String year, String office);

	Optional<DeputyReturnAEntity> findByRowIdAndYearAndKaryalayacheNav(Long rowId, String year, String office);

	List<DeputyReturnAEntity> findByYear(String year);
}
