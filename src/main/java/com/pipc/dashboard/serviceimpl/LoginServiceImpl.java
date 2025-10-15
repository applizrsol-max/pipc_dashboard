package com.pipc.dashboard.serviceimpl;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.pipc.dashboard.login.entities.Role;
import com.pipc.dashboard.login.entities.User;
import com.pipc.dashboard.login.repository.RoleRepository;
import com.pipc.dashboard.login.repository.UserRepository;
import com.pipc.dashboard.login.request.LoginRequest;
import com.pipc.dashboard.login.request.RegisterRequest;
import com.pipc.dashboard.login.response.LoginResponse;
import com.pipc.dashboard.security.utility.JwtProvider;
import com.pipc.dashboard.security.utility.RefreshTokenService;
import com.pipc.dashboard.service.LoginService;
import com.pipc.dashboard.token.entity.RefreshToken;
import com.pipc.dashboard.utility.ApplicationError;
import com.pipc.dashboard.utility.BaseResponse;

import jakarta.transaction.Transactional;

@Transactional
@Component
public class LoginServiceImpl implements LoginService {
	private final UserRepository userRepo;
	private final RoleRepository roleRepo;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authManager;
	private final JwtProvider jwtProvider;
	private final RefreshTokenService refreshTokenService;

	@Autowired
	public LoginServiceImpl(UserRepository userRepo, RoleRepository roleRepo, PasswordEncoder passwordEncoder,
			AuthenticationManager authManager, JwtProvider jwtProvider, RefreshTokenService refreshTokenService) {
		this.userRepo = userRepo;
		this.roleRepo = roleRepo;
		this.passwordEncoder = passwordEncoder;
		this.authManager = authManager;
		this.jwtProvider = jwtProvider;
		this.refreshTokenService = refreshTokenService;

	}

	@Override
	public LoginResponse register(RegisterRequest registerRequest) {
		LoginResponse loginResponse = new LoginResponse();
		ApplicationError error = new ApplicationError();

		if (userRepo.existsByUsername(registerRequest.getUsername())) {
			error.setErrorCode("1");
			error.setErrorDescription("Username already taken");
			loginResponse.setErrorDetails(error);
			return loginResponse;
		}

		User user = new User();
		user.setUsername(registerRequest.getUsername());
		user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

		// ✅ Fetch roles dynamically from request
		Set<Role> assignedRoles = new HashSet<>();

		if (registerRequest.getRoles() == null || registerRequest.getRoles().isEmpty()) {
			// Default role if none provided
			Role userRole = roleRepo.findByName("ROLE_USER")
					.orElseGet(() -> roleRepo.save(new Role(null, "ROLE_USER")));
			assignedRoles.add(userRole);
		} else {
			for (String roleName : registerRequest.getRoles()) {
				Role role = roleRepo.findByName(roleName).orElseGet(() -> roleRepo.save(new Role(null, roleName)));
				assignedRoles.add(role);
			}
		}

		user.setRoles(assignedRoles);

		try {
			User savedUser = userRepo.save(user);

			if (savedUser.getId() != null) {
				// ✅ Generate tokens on successful registration
				String accessToken = jwtProvider.generateAccessToken(savedUser);
				RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);

				error.setErrorCode("0");
				error.setErrorDescription("User registered successfully!!");

				loginResponse.setAccessToken(accessToken);
				loginResponse.setRefreshToken(refreshToken.getToken());
				loginResponse.setGrantedAuthorities(assignedRoles);
			} else {
				error.setErrorCode("1");
				error.setErrorDescription("User registration failed!!");
			}
		} catch (Exception e) {
			error.setErrorCode("1");
			error.setErrorDescription("Error during registration: " + e.getMessage());
		}

		loginResponse.setErrorDetails(error);
		return loginResponse;
	}

	@Override
	public LoginResponse login(LoginRequest loginRequest) {
		LoginResponse loginResponse = new LoginResponse();
		ApplicationError error = new ApplicationError();

		try {
			// Authenticate user
			Authentication authentication = authManager.authenticate(
					new UsernamePasswordAuthenticationToken(loginRequest.getUserName(), loginRequest.getPassword()));

			User user = userRepo.findByUsername(loginRequest.getUserName())
					.orElseThrow(() -> new IllegalStateException("User not found"));

			String accessToken = jwtProvider.generateAccessToken(user);
			RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

			if (refreshToken == null || refreshToken.getToken() == null) {
				error.setErrorCode("1");
				error.setErrorDescription("Failed to generate token");
			} else {
				error.setErrorCode("0");
				error.setErrorDescription("Success");
				loginResponse.setAccessToken(accessToken);
				loginResponse.setRefreshToken(refreshToken.getToken());
				loginResponse.setGrantedAuthorities(user.getRoles());
			}

		} catch (org.springframework.security.authentication.BadCredentialsException ex) {
			error.setErrorCode("2");
			error.setErrorDescription("Invalid username or password");
		} catch (Exception e) {
			error.setErrorCode("3");
			error.setErrorDescription("Authentication failed: " + e.getMessage());
		}

		loginResponse.setErrorDetails(error);
		return loginResponse;
	}

	@Transactional
	@Override
	public BaseResponse deleteUser(String username) {
		BaseResponse response = new BaseResponse();
		ApplicationError error = new ApplicationError();

		Optional<User> userOpt = userRepo.findByUsername(username);
		if (userOpt.isEmpty()) {
			error.setErrorCode("1");
			error.setErrorDescription("User not found");
		} else {
			refreshTokenService.deleteByUser(userOpt.get());
			userRepo.delete(userOpt.get());
			error.setErrorCode("0");
			error.setErrorDescription("User deleted successfully");
		}

		response.setErrorDetails(error);
		return response;
	}

}
