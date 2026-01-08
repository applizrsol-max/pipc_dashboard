package com.pipc.dashboard.establishment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JeReturnRepository extends JpaRepository<JeReturnEntity, Long> {

	List<JeReturnEntity> findByYearAndKaryalayacheNavOrderByRowIdAsc(String year, String karyalayacheNav);

	Optional<JeReturnEntity> findByDeleteIdAndYearAndKaryalayacheNav(Long deleteId, String year, String office);

	Optional<JeReturnEntity> findByRowIdAndYearAndKaryalayacheNav(Long rowId, String year, String office);

	List<JeReturnEntity> findByYear(String year);

}
