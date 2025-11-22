package com.pipc.dashboard.establishment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveRepository extends JpaRepository<LeaveEntity, Long> {

	Optional<LeaveEntity> findByRowId(Long rowId);

	@Query("""
			SELECT e FROM LeaveEntity e
			WHERE (:employeeName IS NULL OR LOWER(e.employeeName) LIKE LOWER(CONCAT('%', :employeeName, '%')))
			  AND (:year IS NULL OR e.year = :year)
			  AND (:month IS NULL OR e.month = :month)
			  AND (:date IS NULL OR e.date = :date)
			ORDER BY e.createdDate DESC
			""")
	List<LeaveEntity> findByFilters(@Param("employeeName") String employeeName, @Param("year") String year,
			@Param("month") String month, @Param("date") String date);

	Optional<LeaveEntity> findByEmployeeNameAndDate(String employeeName, String date);

}