package com.pipc.dashboard.login.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pipc.dashboard.login.entities.LoggingAuditEntity;

@Repository
public interface LoggingAuditRepository extends JpaRepository<LoggingAuditEntity, Long> {

}