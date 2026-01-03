package com.pipc.dashboard.utility;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pipc.dashboard.login.entities.LoggingAuditEntity;
import com.pipc.dashboard.login.repository.LoggingAuditRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {
	private final LoggingAuditRepository repo;

	public List<LoggingAuditEntity> getFullTrace(String correlationId) {

		log.info("Fetching FULL TRACE | corrId={}", correlationId);

		return repo.findByCorrelationIdOrderByCreatedAtAsc(correlationId);
	}

}
