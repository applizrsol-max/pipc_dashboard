package com.pipc.dashboard.security.utility;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.pipc.dashboard.login.entities.User;
import com.pipc.dashboard.login.repository.RefreshTokenRepository;
import com.pipc.dashboard.login.repository.UserRepository;
import com.pipc.dashboard.token.entity.RefreshToken;

@Service
public class RefreshTokenService {
	@Value("${app.jwt.refresh-token-expiration-ms}")
	private long refreshTokenDurationMs;

	private final RefreshTokenRepository refreshRepo;
	private final UserRepository userRepo;

	public RefreshTokenService(RefreshTokenRepository refreshRepo, UserRepository userRepo) {
		this.refreshRepo = refreshRepo;
		this.userRepo = userRepo;
	}

	public RefreshToken createRefreshToken(User user) {
		return refreshRepo.findByUser(user).map(existingToken -> {
			existingToken.setToken(UUID.randomUUID().toString());
			existingToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
			existingToken.setRevoked(false);
			return refreshRepo.save(existingToken);
		}).orElseGet(() -> {
			RefreshToken rt = new RefreshToken();
			rt.setUser(user);
			rt.setToken(UUID.randomUUID().toString());
			rt.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
			rt.setRevoked(false);
			return refreshRepo.save(rt);
		});
	}

	public Optional<RefreshToken> findByToken(String token) {
		return refreshRepo.findByToken(token);
	}

	public void verifyExpiration(RefreshToken token) {
		if (token.getExpiryDate().isBefore(Instant.now()) || token.isRevoked()) {
			refreshRepo.delete(token);
			throw new RuntimeException("Refresh token expired or revoked. Please login again.");
		}
	}

	public void deleteByUser(User user) {
		refreshRepo.deleteByUser(user);
	}
}
