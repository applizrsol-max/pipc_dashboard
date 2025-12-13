package com.pipc.dashboard.drawing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TenderTargetRepository extends JpaRepository<TenderTargetEntity, Long> {

	List<TenderTargetEntity> findByYearAndMonth(String year, String month);

	Optional<TenderTargetEntity> findByYearAndMonthAndRowId(String year, String month, Long rowId);

	Optional<TenderTargetEntity> findByYearAndMonthAndDeleteId(String year, String month, Long deleteId);
}
