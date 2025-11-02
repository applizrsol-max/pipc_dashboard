package com.pipc.dashboard.pdn.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NrldRepository extends JpaRepository<NrldEntity, Long>{
	Optional<NrldEntity> findByRowIdAndDamNameAndYear(String rowId, String damName, String year);
	Page<NrldEntity> findByYear(String year, Pageable pageable);

    Page<NrldEntity> findByYearAndDamNameContainingIgnoreCase(String year, String damName, Pageable pageable);

	List<NrldEntity> findByYear(String year);
}
