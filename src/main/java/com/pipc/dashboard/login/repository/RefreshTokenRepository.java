package com.pipc.dashboard.login.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pipc.dashboard.login.entities.User;
import com.pipc.dashboard.token.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
	Optional<RefreshToken> findByToken(String token);

	Optional<RefreshToken> findByUser(User user);

	void deleteByUser(User user);
}