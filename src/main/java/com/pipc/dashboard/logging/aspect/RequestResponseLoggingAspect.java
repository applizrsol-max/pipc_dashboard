package com.pipc.dashboard.logging.aspect;

import java.lang.reflect.Method;
import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipc.dashboard.login.entities.LoggingAuditEntity;
import com.pipc.dashboard.login.repository.LoggingAuditRepository;
import com.pipc.dashboard.utility.SensitiveDataSanitizer;

import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RequestResponseLoggingAspect {

	private final LoggingAuditRepository auditRepo;
	private final ObjectMapper objectMapper;
	private final SensitiveDataSanitizer sanitizer;

	@Around("within(@org.springframework.web.bind.annotation.RestController *)")
	public Object logController(ProceedingJoinPoint pjp) throws Throwable {

		MethodSignature signature = (MethodSignature) pjp.getSignature();
		Method method = signature.getMethod();

		/* ===================== SKIP LOGGING ===================== */
		if (method.isAnnotationPresent(SkipLogging.class)) {
			return pjp.proceed();
		}

		long startTime = System.currentTimeMillis();

		String user = Optional.ofNullable(MDC.get("user")).filter(u -> !u.isBlank()).orElse("SYSTEM");

		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest();

		LoggingAuditEntity audit = new LoggingAuditEntity();
		audit.setApiName(signature.toShortString());
		audit.setServiceName(pjp.getTarget().getClass().getSimpleName());
		audit.setHttpMethod(request.getMethod());
		audit.setRequestUrl(request.getRequestURI());
		audit.setUserId(user);

		/* ---------- Correlation IDs ---------- */
		audit.setCorrelationId(MDC.get("correlationId"));
		audit.setBusinessCorrelationId(MDC.get("businessCorrelationId"));

		log.debug("AUDIT START | api={} | corrId={}", audit.getApiName(), audit.getCorrelationId());

		/* ===================== REQUEST LOGGING ===================== */
		audit.setRequest(buildRequestPayload(pjp.getArgs()));

		Object response;
		try {
			/* ===================== CONTROLLER EXECUTION ===================== */
			response = pjp.proceed();
			audit.setStatus("SUCCESS");

			Object responseBody = (response instanceof ResponseEntity<?> re) ? re.getBody() : response;

			audit.setResponse(buildResponsePayload(responseBody));

			audit.setExecutionTimeMs(System.currentTimeMillis() - startTime);
			auditRepo.save(audit);

			log.info("AUDIT SUCCESS | api={} | time={}ms | corrId={}", audit.getApiName(), audit.getExecutionTimeMs(),
					audit.getCorrelationId());

			return response;

		} catch (Exception ex) {

			audit.setStatus("ERROR");
			audit.setResponse(objectMapper.createObjectNode().put("exception", ex.getMessage()));
			audit.setExecutionTimeMs(System.currentTimeMillis() - startTime);

			auditRepo.save(audit);

			log.error("AUDIT ERROR | api={} | corrId={}", audit.getApiName(), audit.getCorrelationId(), ex);

			throw ex; // ❗ logic unchanged
		}
	}

	/* ===================== HELPERS ===================== */

	private JsonNode buildRequestPayload(Object[] args) {
		for (Object arg : args) {
			if (arg instanceof org.springframework.web.multipart.MultipartFile || arg instanceof ServletResponse) {
				return objectMapper.createObjectNode().put("message", "Multipart/Servlet request skipped from logging");
			}
		}

		JsonNode node = objectMapper.valueToTree(args);
		return sanitizer.sanitize(node);
	}

	private JsonNode buildResponsePayload(Object responseBody) {

		if (responseBody == null) {
			return objectMapper.createObjectNode().put("message", "Empty response");
		}

		if (isBinaryResponse(responseBody)) {
			return objectMapper.createObjectNode().put("message", "Binary / Streaming response skipped from logging");
		}

		JsonNode node = objectMapper.valueToTree(responseBody);
		return sanitizer.sanitize(node);
	}

	private boolean isBinaryResponse(Object body) {
		return body instanceof InputStreamResource || body instanceof Resource || body instanceof byte[]
				|| body instanceof ServletResponse || body.getClass().getName().startsWith("org.apache.catalina");
	}
}
