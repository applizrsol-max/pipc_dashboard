package com.pipc.dashboard.establishment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncomeTaxDeductionRepository extends JpaRepository<IncomeTaxDeductionEntity, Long> {

	Optional<IncomeTaxDeductionEntity> findByRowId(Long rowId);

	List<IncomeTaxDeductionEntity> findAll(Specification<IncomeTaxDeductionEntity> spec);

	List<IncomeTaxDeductionEntity> findByYear(String year);

	List<IncomeTaxDeductionEntity> findByMonthContainingIgnoreCase(String month);

	List<IncomeTaxDeductionEntity> findByYearAndMonthContainingIgnoreCase(String year, String month);

}