package com.pipc.dashboard.establishment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RtrGopniyaAhwalRepository extends JpaRepository<RtrGopniyaAhwal, Long> {

	Optional<RtrGopniyaAhwal> findByYearAndDivisionIdAndRowId(String year, Long divisionId, Long rowId);

	void deleteByYearAndDivisionIdAndDeleteId(String year, Long divisionId, Long deleteId);

	List<RtrGopniyaAhwal> findAllByYearOrderByDivisionIdAscRowIdAsc(String year);

}
