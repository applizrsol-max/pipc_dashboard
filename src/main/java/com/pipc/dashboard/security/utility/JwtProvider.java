package com.pipc.dashboard.security.utility;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.pipc.dashboard.login.entities.Role;
import com.pipc.dashboard.login.entities.User;

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

	public String generateAccessToken(User user) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + accessTokenExpiryMs);

		return Jwts.builder().setSubject(user.getUsername())
				.claim("roles", user.getRoles().stream().map(Role::getName).toList()).setIssuedAt(now)
				.setExpiration(expiry).signWith(key, SignatureAlgorithm.HS256).compact();
	}

	public String getUsernameFromToken(String token) {
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
			return true;
		} catch (JwtException | IllegalArgumentException ex) {
			return false;
		}
	}
}
