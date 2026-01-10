package com.pipc.dashboard.establishment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeputyReturnBRepository extends JpaRepository<DeputyReturnBEntity, Long> {

	Optional<DeputyReturnBEntity> findByRowIdAndYearAndKaryalayacheNav(Long rowId, String year, String office);

	Optional<DeputyReturnBEntity> findByDeleteIdAndYearAndKaryalayacheNav(Long deleteId, String year, String office);

	List<DeputyReturnBEntity> findByYearOrderByKaryalayacheNavAscRowIdAsc(String year);

	void deleteByYearAndDeleteId(String year, Long deleteId);
}
