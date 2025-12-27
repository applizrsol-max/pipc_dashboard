package com.pipc.dashboard.establishment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CrFileListRepository extends JpaRepository<CrFileListEntity, Long> {

	Optional<CrFileListEntity> findByYearAndRowId(String year, Long rowId);

	void deleteByYearAndDeleteId(String year, Long deleteId);

	List<CrFileListEntity> findAllByYearOrderByRowId(String year);
}
