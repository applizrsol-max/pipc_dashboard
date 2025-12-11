package com.pipc.dashboard.establishment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgendaSecBRepository extends JpaRepository<AgendaSecEntityGATB, Long> {

	Optional<AgendaSecEntityGATB> findByRowIdAndYearAndTargetDate(Long rowId, String year, String targetDate);

	Optional<AgendaSecEntityGATB> findByDeleteIdAndYearAndTargetDate(Long deleteId, String year, String targetDate);

	List<AgendaSecEntityGATB> findByYearAndTargetDate(String year, String targetDate);
}
