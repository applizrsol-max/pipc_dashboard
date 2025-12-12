package com.pipc.dashboard.establishment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppealRequestRepository extends JpaRepository<AppealRequestEntity, Long> {

    Optional<AppealRequestEntity> findByRowId(Long rowId);

    List<AppealRequestEntity> findByYearAndArjdarNavPattaAndArjachaNondaniKramank(
            String year,
            String arjdarNavPatta,
            String arjachaNondaniKramank
    );

    Page<AppealRequestEntity> findByYear(String year, Pageable pageable);

    Optional<AppealRequestEntity> findByDeleteIdAndDate(Long deleteId, String date);

    List<AppealRequestEntity> findByYear(String year);
}
