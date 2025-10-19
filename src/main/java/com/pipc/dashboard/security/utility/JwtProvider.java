package com.pipc.dashboard.security.utility;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.pipc.dashboard.login.entities.Role;
import com.pipc.dashboard.login.entities.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtProvider {

	@Value("${app.jwt.secret}")
	private String jwtSecret;

	@Value("${app.jwt.access-token-expiration-ms}")
	private long accessTokenExpiryMs;

	private Key key;

	@PostConstruct
	public void init() {
		byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
		this.key = Keys.hmacShaKeyFor(keyBytes);
	}

	// ✅ Generate new Access Token
	public String generateAccessToken(User user) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + accessTokenExpiryMs);

		return Jwts.builder().setSubject(user.getUsername())
				.claim("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
				.setIssuedAt(now).setExpiration(expiry).signWith(key, SignatureAlgorithm.HS256).compact();
	}

	// ✅ Generate Access Token from existing username & roles (used in refresh)
	public String generateAccessToken(String username, List<String> roles) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + accessTokenExpiryMs);

		return Jwts.builder().setSubject(username).claim("roles", roles).setIssuedAt(now).setExpiration(expiry)
				.signWith(key, SignatureAlgorithm.HS256).compact();
	}

	// ✅ Extract username
	public String getUsernameFromToken(String token) {
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
	}

	// ✅ Extract roles (optional, for refresh flow)
	@SuppressWarnings("unchecked")
	public List<String> getRolesFromToken(String token) {
		Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
		return (List<String>) claims.get("roles");
	}

	// ✅ Validate token
	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
			return true;
		} catch (JwtException | IllegalArgumentException ex) {
			return false;
		}
	}
}
  