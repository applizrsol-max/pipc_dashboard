package com.pipc.dashboard.logging.aspect;

import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipc.dashboard.login.entities.LoggingAuditEntity;
import com.pipc.dashboard.login.repository.LoggingAuditRepository;
import com.pipc.dashboard.utility.SensitiveDataSanitizer;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RequestResponseLoggingAspect {

	private final LoggingAuditRepository repo;
	private final ObjectMapper mapper;
	private final SensitiveDataSanitizer sensitiveDataSanitizer;

	@Around("within(@org.springframework.web.bind.annotation.RestController *)")
	public Object logController(ProceedingJoinPoint pjp) throws Throwable {

		String user = Optional.ofNullable(MDC.get("user")).filter(s -> !s.isBlank()).orElse("SYSTEM");

		long start = System.currentTimeMillis();

		LoggingAuditEntity entity = new LoggingAuditEntity();

		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest();

		entity.setApiName(pjp.getSignature().toShortString());
		entity.setServiceName(pjp.getTarget().getClass().getSimpleName());
		entity.setHttpMethod(request.getMethod());
		entity.setRequestUrl(request.getRequestURI());
		entity.setUserId(user);

		// REQUEST LOG
		JsonNode reqNode = mapper.valueToTree(pjp.getArgs());
		entity.setRequest(sensitiveDataSanitizer.sanitize(reqNode));

		Object response = null;

		try {

			// Execute API
			response = pjp.proceed();
			entity.setStatus("SUCCESS");

			// ---------------------------------------------------
			// 🔥 FILE DOWNLOAD RESPONSE LOGGING SKIP
			// ---------------------------------------------------
			boolean isFileResponse = response instanceof InputStreamResource || response instanceof byte[]
					|| (response instanceof ResponseEntity<?> re && re.getBody() instanceof InputStreamResource);

			if (isFileResponse) {

				// DO NOT LOG RESPONSE BODY
				entity.setResponse(
						mapper.createObjectNode().put("message", "File download response skipped from logging"));

				entity.setExecutionTimeMs(System.currentTimeMillis() - start);
				repo.save(entity);

				return response; // IMPORTANT
			}

			// ---------------------------------------------------
			// NORMAL JSON RESPONSE → LOG FULL BODY
			// ---------------------------------------------------
			JsonNode resNode = mapper.valueToTree(response);
			entity.setResponse(sensitiveDataSanitizer.sanitize(resNode));

			entity.setExecutionTimeMs(System.currentTimeMillis() - start);
			repo.save(entity);

			return response;

		} catch (Exception ex) {

			entity.setStatus("ERROR");
			entity.setResponse(mapper.createObjectNode().put("exception", ex.getMessage()));
			entity.setExecutionTimeMs(System.currentTimeMillis() - start);
			repo.save(entity);

			throw ex;
		}
	}
}
