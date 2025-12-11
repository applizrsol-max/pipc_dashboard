package com.pipc.dashboard.establishment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AgendaThirteenRepository extends JpaRepository<AgendaThirteenEntity, Long> {

    Optional<AgendaThirteenEntity> findByRowIdAndYearAndTargetDate(long rowId, String year, String targetDate);

    Optional<AgendaThirteenEntity> findByDeleteIdAndYearAndTargetDate(long deleteId, String year, String targetDate);

    List<AgendaThirteenEntity> findByYearAndTargetDate(String year, String targetDate);
}
