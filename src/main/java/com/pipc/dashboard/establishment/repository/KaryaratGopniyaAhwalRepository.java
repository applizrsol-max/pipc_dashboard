package com.pipc.dashboard.establishment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface KaryaratGopniyaAhwalRepository extends JpaRepository<KaryaratGopniyaAhwalEntity, Long> {

	Optional<KaryaratGopniyaAhwalEntity> findByYearAndDivisionIdAndRowId(String year, Long divisionId, Long rowId);

	void deleteByYearAndDivisionIdAndDeleteId(String year, Long divisionId, Long deleteId);

	List<KaryaratGopniyaAhwalEntity> findAllByYearOrderByDivisionIdAscRowIdAsc(String year);

}
