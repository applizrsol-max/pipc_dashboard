package com.pipc.dashboard.establishment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface MasterDataRepository extends JpaRepository<MasterDataEntity, Long> {

	Optional<MasterDataEntity> findByYearAndRowId(String year, Long rowId);

	@Modifying
	@Transactional
	void deleteByYearAndDeleteId(String year, Long deleteId);

	List<MasterDataEntity> findAllByYearOrderByRowId(String year);

	
}