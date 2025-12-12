package com.pipc.dashboard.pdn.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IrrigationRepositoryRevised extends JpaRepository<IrrigationCapacityEntityRevised, Long> {

	Optional<IrrigationCapacityEntityRevised> findByYearAndDateAndRowId(String year, String dateVal, Integer deleteId);

	Optional<IrrigationCapacityEntityRevised> findByYearAndDateAndDeleteId(String year, String date, Integer deleteId);

	List<IrrigationCapacityEntityRevised> findByYearAndDate(String year, String date);

}