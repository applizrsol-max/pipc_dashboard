package com.pipc.dashboard.login.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pipc.dashboard.login.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByUsername(String username);

	boolean existsByUsername(String username);
}