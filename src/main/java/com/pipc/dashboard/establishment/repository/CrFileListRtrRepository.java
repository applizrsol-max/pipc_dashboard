package com.pipc.dashboard.establishment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CrFileListRtrRepository extends JpaRepository<CrFileListRtrEntity, Long> {

	Optional<CrFileListRtrEntity> findByYearAndRowId(String year, Long rowId);

	void deleteByYearAndDeleteId(String year, Long deleteId);

	List<CrFileListRtrEntity> findAllByYearOrderByRowId(String year);
}
