package com.pipc.dashboard.logging.aspect;

import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
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

		// REQUEST JSON → SANITIZE
		JsonNode reqNode = mapper.valueToTree(pjp.getArgs());
		entity.setRequest(sensitiveDataSanitizer.sanitize(reqNode));

		entity.setUserId(user);

		Object response = null;

		try {
			response = pjp.proceed();
			entity.setStatus("SUCCESS");

		} catch (Exception ex) {
			entity.setStatus("ERROR");

			JsonNode errorNode = mapper.createObjectNode().put("exception", ex.getMessage());
			entity.setResponse(errorNode);
			entity.setExecutionTimeMs(System.currentTimeMillis() - start);

			repo.save(entity);
			throw ex;
		}

		// RESPONSE JSON → SANITIZE
		JsonNode resNode = mapper.valueToTree(response);
		entity.setResponse(sensitiveDataSanitizer.sanitize(resNode));

		entity.setExecutionTimeMs(System.currentTimeMillis() - start);

		repo.save(entity);

		return response;
	}

}
