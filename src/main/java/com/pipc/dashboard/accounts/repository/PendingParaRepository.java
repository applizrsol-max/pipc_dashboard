package com.pipc.dashboard.accounts.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PendingParaRepository extends JpaRepository<PendingParaEntity, Long> {

	Optional<PendingParaEntity> findByRowIdAndYear(Long rowId, Integer year);

	void deleteByDeleteIdAndYear(Long deleteId, Integer year);

	List<PendingParaEntity> findAllByYear(Integer year);
}
