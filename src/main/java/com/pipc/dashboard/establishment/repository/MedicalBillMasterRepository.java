package com.pipc.dashboard.establishment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalBillMasterRepository extends JpaRepository<MedicalBillMasterEntity, Long> {
	Optional<MedicalBillMasterEntity> findByRowId(Long rowId);

	@Query("SELECT m FROM MedicalBillMasterEntity m "
			+ "WHERE (:employeeName IS NULL OR m.employeeDetails.employeeName = :employeeName) "
			+ "AND (:month IS NULL OR m.month = :month) " + "AND (:year IS NULL OR m.year = :year) "
			+ "AND (:period IS NULL OR m.period = :period) " + "AND (:billDate IS NULL OR m.billDate = :billDate)")
	List<MedicalBillMasterEntity> findByFilters(@Param("employeeName") String employeeName,
			@Param("month") String month, @Param("year") String year, @Param("period") String period,
			@Param("billDate") String billDate);

	Optional<MedicalBillMasterEntity> findByEmployeeDetails_EmployeeNameAndBillDate(String employeeName,
			String billDate);

}
