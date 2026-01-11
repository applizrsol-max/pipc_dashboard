package com.pipc.dashboard.establishment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VivranPatraASummaryRepository extends JpaRepository<VivranPatraASummaryEntity, Long> {

	Optional<VivranPatraASummaryEntity> findByDistrictAndYear(String district, String year);

	List<VivranPatraASummaryEntity> findByYearOrderByDistrictAsc(String year);

	List<VivranPatraASummaryEntity> findByYear(String year);
}
