package com.pipc.dashboard.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pipc.dashboard.login.entities.LoggingAuditEntity;
import com.pipc.dashboard.utility.AuditLogService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/pipc/dashboard/logs")
@RequiredArgsConstructor
public class CorrelationLogController {

	private final AuditLogService service;

	@GetMapping("/{correlationId}")
	public ResponseEntity<?> getByCorrelationId(@PathVariable String correlationId) {

		List<LoggingAuditEntity> logs = service.getFullTrace(correlationId);

		if (logs.isEmpty()) {
			return ResponseEntity.status(404).body(Map.of("message", "No logs found", "correlationId", correlationId));
		}

		return ResponseEntity.ok(Map.of("correlationId", correlationId, "totalCalls", logs.size(), "timeline", logs));
	}
}
