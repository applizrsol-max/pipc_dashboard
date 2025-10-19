package com.pipc.dashboard.interceptor;

import java.util.List;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.pipc.dashboard.security.utility.JwtProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestValidationInterceptor implements HandlerInterceptor {

	private final JwtProvider jwtService;

	public RequestValidationInterceptor(JwtProvider jwtService) {
		this.jwtService = jwtService;
	}

	// Excluded APIs (no token/correlation validation)
	private static final List<String> EXCLUDED_PATHS = List.of("/pipc/dashboard/onboarding/login",
			"/pipc/dashboard/onboarding/register", "/pipc/dashboard/onboarding/refresh-token",
			"/pipc/dashboard/onboarding/forgotPassword");

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		String uri = request.getRequestURI();

		// ✅ Skip for onboarding endpoints
		if (EXCLUDED_PATHS.stream().anyMatch(uri::startsWith)) {
			return true;
		}

		// ✅ Extract or generate correlation IDs
		String businessCorrelationId = request.getHeader("businessCorrelationId");
		String correlationId = request.getHeader("correlationId");

		if (businessCorrelationId == null || businessCorrelationId.isBlank()) {
			businessCorrelationId = "BIZ-" + UUID.randomUUID();
		}
		if (correlationId == null || correlationId.isBlank()) {
			correlationId = "COR-" + UUID.randomUUID();
		}

		// ✅ Token extraction
		String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("Missing or invalid Authorization header");
			return false;
		}

		String token = authHeader.substring(7);

		// ✅ Validate JWT
		if (!jwtService.validateToken(token)) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("Invalid or expired token");
			return false;
		}

		// ✅ Extract username from token
		String username = jwtService.getUsernameFromToken(token);
		if (username == null || username.isBlank()) {
			username = "system";
		}

		// ✅ Add to MDC (for logs + global context)
		MDC.put("user", username);
		MDC.put("correlationId", correlationId);
		MDC.put("businessCorrelationId", businessCorrelationId);

		// ✅ Add to request attributes (for service/controller access)
		request.setAttribute("loggedInUser", username);
		request.setAttribute("correlationId", correlationId);
		request.setAttribute("businessCorrelationId", businessCorrelationId);

		// ✅ Optional: Log once per request
		System.out.printf("✅ Authorized [%s] user=%s | corrId=%s | bizCorrId=%s%n", uri, username, correlationId,
				businessCorrelationId);

		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		// Clear MDC after request completes
		MDC.clear();
	}
}
