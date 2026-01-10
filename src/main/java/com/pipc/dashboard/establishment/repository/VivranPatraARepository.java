package com.pipc.dashboard.establishment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VivranPatraARepository extends JpaRepository<VivranPatraAEntity, Long> {

	Optional<VivranPatraAEntity> findByRowIdAndYearAndKaryalayacheNav(Long rowId, String year, String office);

	Optional<VivranPatraAEntity> findByDeleteIdAndYearAndKaryalayacheNav(Long deleteId, String year, String office);

	List<VivranPatraAEntity> findByYearOrderByKaryalayacheNavAscRowIdAsc(String year);
}
