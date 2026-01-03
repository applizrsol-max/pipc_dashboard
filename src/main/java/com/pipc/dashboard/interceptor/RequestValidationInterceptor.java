package com.pipc.dashboard.interceptor;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.pipc.dashboard.security.utility.JwtProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestValidationInterceptor implements HandlerInterceptor {

	private static final Logger log = LoggerFactory.getLogger(RequestValidationInterceptor.class);

	private final JwtProvider jwtProvider;

	public RequestValidationInterceptor(JwtProvider jwtProvider) {
		this.jwtProvider = jwtProvider;
	}

	/* ---------------- EXCLUDED PATHS ---------------- */
	private static final List<String> EXCLUDED_PATHS = List.of("/pipc/dashboard/onboarding/login",
			"/pipc/dashboard/onboarding/register", "/pipc/dashboard/onboarding/refresh-token",
			"/pipc/dashboard/onboarding/resetPassword", "/pipc/dashboard/onboarding/otpPwdReset",
			"/pipc/dashboard/onboarding/verifyOtpReset");

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws IOException {

		final String uri = request.getRequestURI();

		/* ---------- Skip onboarding APIs ---------- */
		if (isExcluded(uri)) {
			return true;
		}

		/* ---------- Correlation IDs ---------- */
		String correlationId = getOrGenerate(request.getHeader("correlationId"), "COR");
		String businessCorrelationId = getOrGenerate(request.getHeader("businessCorrelationId"), "BIZ");

		/* ---------- Authorization Header ---------- */
		String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			unauthorized(response, "Missing or invalid Authorization header");
			log.warn("Unauthorized request | uri={} | reason=Missing Authorization | corrId={}", uri, correlationId);
			return false;
		}

		String token = authHeader.substring(7);

		/* ---------- JWT Validation ---------- */
		if (!jwtProvider.validateToken(token)) {
			unauthorized(response, "Invalid or expired token");
			log.warn("Unauthorized request | uri={} | reason=Invalid token | corrId={}", uri, correlationId);
			return false;
		}

		/* ---------- Extract user ---------- */
		String username = jwtProvider.getUsernameFromToken(token);
		if (username == null || username.isBlank()) {
			username = "SYSTEM";
		}

		/* ---------- MDC (logging context) ---------- */
		MDC.put("user", username);
		MDC.put("correlationId", correlationId);
		MDC.put("businessCorrelationId", businessCorrelationId);

		/* ---------- Request attributes ---------- */
		request.setAttribute("loggedInUser", username);
		request.setAttribute("correlationId", correlationId);
		request.setAttribute("businessCorrelationId", businessCorrelationId);

		log.info("Authorized request | uri={} | user={} | corrId={} | bizCorrId={}", uri, username, correlationId,
				businessCorrelationId);

		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		MDC.clear();
	}

	/* ===================== HELPERS ===================== */

	private boolean isExcluded(String uri) {
		return EXCLUDED_PATHS.stream().anyMatch(uri::startsWith);
	}

	private String getOrGenerate(String headerValue, String prefix) {
		return (headerValue == null || headerValue.isBlank()) ? prefix + "-" + UUID.randomUUID() : headerValue;
	}

	private void unauthorized(HttpServletResponse response, String message) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json");
		response.getWriter().write(message);
	}
}
