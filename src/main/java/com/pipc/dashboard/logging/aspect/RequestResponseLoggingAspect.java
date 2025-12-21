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
import jakarta.servlet.http.HttpServletResponse;
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

		/*
		 * =====================================================
		 * SKIP LOGGING IF @SkipLogging USED
		 * =====================================================
		 */
		MethodSignature sig = (MethodSignature) pjp.getSignature();
		Method method = sig.getMethod();

		if (method.isAnnotationPresent(SkipLogging.class)) {
			return pjp.proceed();
		}

		String user = Optional.ofNullable(MDC.get("user")).filter(s -> !s.isBlank()).orElse("SYSTEM");
		long start = System.currentTimeMillis();

		LoggingAuditEntity entity = new LoggingAuditEntity();

		HttpServletRequest request = ((ServletRequestAttributes) 
		    RequestContextHolder.currentRequestAttributes()).getRequest();

		entity.setApiName(pjp.getSignature().toShortString());
		entity.setServiceName(pjp.getTarget().getClass().getSimpleName());
		entity.setHttpMethod(request.getMethod());
		entity.setRequestUrl(request.getRequestURI());
		entity.setUserId(user);

		/*
		 * =====================================================
		 * REQUEST LOGGING
		 * =====================================================
		 */
		boolean skipReq = false;

		for (Object arg : pjp.getArgs()) {
			if (arg instanceof org.springframework.web.multipart.MultipartFile ||
				arg instanceof ServletResponse) {
				skipReq = true;
				break;
			}
		}

		if (skipReq) {
			entity.setRequest(mapper.createObjectNode()
					.put("message", "Multipart/Servlet request skipped from logging"));
		} else {
			JsonNode reqNode = mapper.valueToTree(pjp.getArgs());
			entity.setRequest(sensitiveDataSanitizer.sanitize(reqNode));
		}

		Object response;

		try {
			/*
			 * =====================================================
			 * CONTROLLER EXECUTION
			 * =====================================================
			 */
			response = pjp.proceed();
			entity.setStatus("SUCCESS");

			/*
			 * =====================================================
			 * EXTRACT ACTUAL RESPONSE BODY
			 * =====================================================
			 */
			Object responseBody = response;

			if (response instanceof ResponseEntity<?> re) {
				responseBody = re.getBody();
			}

			/*
			 * =====================================================
			 * SKIP ALL NON-SERIALIZABLE RESPONSES
			 * =====================================================
			 */
			boolean isBinary =
					responseBody instanceof InputStreamResource ||
					responseBody instanceof Resource ||
					responseBody instanceof byte[] ||
					responseBody instanceof HttpServletResponse ||
					responseBody instanceof ServletResponse ||
					responseBody instanceof org.apache.catalina.connector.Response ||
					responseBody instanceof org.apache.catalina.connector.ResponseFacade ||
					responseBody instanceof org.apache.catalina.connector.CoyoteWriter;

			if (isBinary) {
				entity.setResponse(mapper.createObjectNode()
						.put("message", "Binary / Streaming response skipped from logging"));

				entity.setExecutionTimeMs(System.currentTimeMillis() - start);
				repo.save(entity);
				return response;
			}

			/*
			 * =====================================================
			 * SAFE JSON SERIALIZATION
			 * =====================================================
			 */
			if (responseBody == null) {
				entity.setResponse(mapper.createObjectNode().put("message", "Empty response"));
			} else {
				JsonNode resNode = mapper.valueToTree(responseBody);
				entity.setResponse(sensitiveDataSanitizer.sanitize(resNode));
			}

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
