package com.pipc.dashboard.establishment.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncomeTaxDeductionRepository extends JpaRepository<IncomeTaxDeductionEntity, Long> {

	Optional<IncomeTaxDeductionEntity> findByRowId(Long rowId);

	Page<IncomeTaxDeductionEntity> findAll(Specification<IncomeTaxDeductionEntity> spec, Pageable pageable);

	Page<IncomeTaxDeductionEntity> findByYear(String year, Pageable pageable);

	Page<IncomeTaxDeductionEntity> findByMonthContainingIgnoreCase(String month, Pageable pageable);

	Page<IncomeTaxDeductionEntity> findByYearAndMonthContainingIgnoreCase(String year, String month, Pageable pageable);

}