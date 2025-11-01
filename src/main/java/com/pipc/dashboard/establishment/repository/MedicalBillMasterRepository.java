package com.pipc.dashboard.establishment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalBillMasterRepository extends JpaRepository<MedicalBillMasterEntity, Long> {
	Optional<MedicalBillMasterEntity> findByRowId(Long rowId);
}