package com.pipc.dashboard.establishment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MahaparRegisterRepository extends JpaRepository<MahaparRegisterEntity, Long> {

	Optional<MahaparRegisterEntity> findByYearAndSectionIdAndRowId(String year, Long sectionId, Long rowId);

	void deleteByYearAndDeleteId(String year, Long deleteId);

	List<MahaparRegisterEntity> findAllByYearOrderBySectionIdAscRowIdAsc(String year);

}
