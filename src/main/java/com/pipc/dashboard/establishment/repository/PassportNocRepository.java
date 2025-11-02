package com.pipc.dashboard.establishment.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PassportNocRepository extends JpaRepository<PassportNocEntity, Long> {
	Optional<PassportNocEntity> findByRowId(Long rowId);

	Page<PassportNocEntity> findByYearContainingIgnoreCaseAndMonthContainingIgnoreCaseAndEmployeeNameContainingIgnoreCase(
			String year, String month, String employeeName, Pageable pageable);
}