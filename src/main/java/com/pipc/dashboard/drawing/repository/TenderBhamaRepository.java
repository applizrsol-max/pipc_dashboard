package com.pipc.dashboard.drawing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TenderBhamaRepository extends JpaRepository<TenderBhamaEntity, Long> {

	List<TenderBhamaEntity> findByYearAndMonthAndDate(String year, String month, String date);

	Optional<TenderBhamaEntity> findByYearAndMonthAndDateAndRowId(String year, String month, String date, Long rowId);

	Optional<TenderBhamaEntity> findByYearAndMonthAndDateAndDeleteId(String year, String month, String date,
			Long deleteId);
}