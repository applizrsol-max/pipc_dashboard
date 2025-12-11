package com.pipc.dashboard.establishment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgendaSecDRepository extends JpaRepository<AgendaSecEntityGATD, Long> {

	Optional<AgendaSecEntityGATD> findByRowIdAndYearAndTargetDate(Long rowId, String year, String targetDate);

	Optional<AgendaSecEntityGATD> findByDeleteIdAndYearAndTargetDate(Long deleteId, String year, String targetDate);

	List<AgendaSecEntityGATD> findByYearAndTargetDate(String year, String targetDate);
}
