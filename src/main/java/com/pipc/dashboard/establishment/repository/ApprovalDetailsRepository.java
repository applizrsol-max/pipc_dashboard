package com.pipc.dashboard.establishment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApprovalDetailsRepository extends JpaRepository<ApprovalDetailsEntity, Long> {
}