package com.pipc.dashboard.establishment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VivranPatraRepository extends JpaRepository<VivranPatraEntity, Long> {

	Optional<VivranPatraEntity> findByYearAndRowIdAndGroupName(String year, Long rowId, String groupName);

	Optional<VivranPatraEntity> findByYearAndDeleteIdAndGroupName(String year, Long deleteId, String groupName);

	List<VivranPatraEntity> findByYearOrderByGroupNameAscRowIdAsc(String year);

	void deleteByYearAndDeleteId(String year, Long deleteId);
}
