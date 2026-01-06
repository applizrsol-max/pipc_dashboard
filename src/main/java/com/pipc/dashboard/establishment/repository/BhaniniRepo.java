package com.pipc.dashboard.establishment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BhaniniRepo extends JpaRepository<BhaniniEntity, Long> {

	Optional<BhaniniEntity> findByEmployeeNameAndYear(String employeeName, String year);

	void deleteByEmployeeNameAndYear(String employeeName, String year);

	
}