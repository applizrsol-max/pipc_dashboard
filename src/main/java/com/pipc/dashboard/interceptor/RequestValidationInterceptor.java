package com.pipc.dashboard.interceptor;

import java.util.List;
import java.util.UUID;

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

	// Exclude login/register APIs
	private static final List<String> EXCLUDED_PATHS = List.of("/pipc/dashboard/onboarding/login",
			"/pipc/dashboard/onboarding/register", "/pipc/dashboard/onboarding/refresh-token",
			"/pipc/dashboard/onboarding/forgotPassword");

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		String uri = request.getRequestURI();

		// ✅ Skip for login/register etc.
		if (EXCLUDED_PATHS.stream().anyMatch(uri::startsWith)) {
			return true;
		}

		// ✅ Read headers
		String businessCorrelationId = request.getHeader("businessCorrelationId");
		String correlationId = request.getHeader("correlationId");

		// ✅ Generate if missing or empty
		if (businessCorrelationId == null || businessCorrelationId.isBlank()) {
			businessCorrelationId = "BIZ-" + UUID.randomUUID().toString();
			request.setAttribute("businessCorrelationId", businessCorrelationId);
		} else {
			request.setAttribute("businessCorrelationId", businessCorrelationId);
		}

		if (correlationId == null || correlationId.isBlank()) {
			correlationId = "COR-" + UUID.randomUUID().toString();
			request.setAttribute("correlationId", correlationId);
		} else {
			request.setAttribute("correlationId", correlationId);
		}

		// ✅ Token validation (lightweight check)
		String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("Missing or invalid Authorization header");
			return false;
		}

		String token = authHeader.substring(7);

		if (!jwtService.validateToken(token)) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("Invalid or expired token");
			return false;
		}

		// ✅ Log request info for observability
		System.out.printf("✅ Authorized request: [%s] businessCorrelationId=%s | correlationId=%s%n", uri,
				businessCorrelationId, correlationId);

		return true;
	}
}
