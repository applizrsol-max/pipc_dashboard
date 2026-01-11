package com.pipc.dashboard.establishment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VivranPatraDRepository extends JpaRepository<VivranPatraDEntity, Long> {

	Optional<VivranPatraDEntity> findByRowIdAndYearAndKaryalayacheNav(Long rowId, String year, String office);

	Optional<VivranPatraDEntity> findByDeleteIdAndYearAndKaryalayacheNav(Long deleteId, String year, String office);

	List<VivranPatraDEntity> findByYearOrderByKaryalayacheNavAscRowIdAsc(String year);

	List<VivranPatraDEntity> findByYearOrderByRowIdAsc(String year);
}
