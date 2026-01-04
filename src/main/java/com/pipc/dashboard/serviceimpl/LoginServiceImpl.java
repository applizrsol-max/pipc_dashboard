package com.pipc.dashboard.serviceimpl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.pipc.dashboard.login.entities.Role;
import com.pipc.dashboard.login.entities.User;
import com.pipc.dashboard.login.repository.RefreshTokenRepository;
import com.pipc.dashboard.login.repository.RoleRepository;
import com.pipc.dashboard.login.repository.UserRepository;
import com.pipc.dashboard.login.request.LoginRequest;
import com.pipc.dashboard.login.request.RefreshTokenRequest;
import com.pipc.dashboard.login.request.RegisterRequest;
import com.pipc.dashboard.login.request.UpdateUserRolesRequest;
import com.pipc.dashboard.login.response.LoginResponse;
import com.pipc.dashboard.security.utility.EncryptionUtils;
import com.pipc.dashboard.security.utility.JwtProvider;
import com.pipc.dashboard.security.utility.RefreshTokenService;
import com.pipc.dashboard.service.LoginService;
import com.pipc.dashboard.token.entity.RefreshToken;
import com.pipc.dashboard.utility.ApplicationError;
import com.pipc.dashboard.utility.BaseResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

	private final UserRepository userRepo;
	private final RoleRepository roleRepo;

	private final JwtProvider jwtProvider;
	private final RefreshTokenService refreshTokenService;
	private final RefreshTokenRepository refreshTokenRepository;

	/* ========================== REGISTER ========================== */

	@Override
	public LoginResponse register(RegisterRequest registerRequest) {

		LoginResponse response = new LoginResponse();
		ApplicationError error = new ApplicationError();
		String corrId = MDC.get("correlationId");

		log.info("START register | username={} | corrId={}", registerRequest.getUsername(), corrId);

		try {

			// ---------------- VALIDATION ----------------
			if (userRepo.existsByUsername(registerRequest.getUsername())) {
				error.setErrorCode("1");
				error.setErrorDescription("Username already taken");
				response.setErrorDetails(error);
				return response;
			}

			// ---------------- USER ----------------
			User user = new User();
			user.setUsername(registerRequest.getUsername());
			user.setPassword(EncryptionUtils.sha512(registerRequest.getPassword()));

			Set<Role> finalRoles = new HashSet<>();

			// ---------------- ROLE LOGIC ----------------
			if (registerRequest.getRoles() == null || registerRequest.getRoles().isEmpty()) {

				// DEFAULT ROLE
				Role defaultRole = roleRepo.findByName("ROLE_USER")
						.orElseThrow(() -> new RuntimeException("ROLE_USER not configured"));
				finalRoles.add(defaultRole);

			} else {

				for (String roleName : registerRequest.getRoles()) {

					Role role = roleRepo.findByName(roleName)
							.orElseThrow(() -> new RuntimeException("Invalid role: " + roleName));

					finalRoles.add(role);

					// ---------------- CASE 1: SUB ROLE ----------------
					if (role.getParentCard() != null) {

						Role parent = roleRepo.findByName(role.getParentCard())
								.orElseThrow(() -> new RuntimeException("Parent role missing for " + roleName));

						finalRoles.add(parent);
					}

					// ---------------- CASE 2: MAIN ROLE ----------------
					if (role.getParentCard() == null) {

						List<Role> subRoles = roleRepo.findByParentCard(role.getName());
						finalRoles.addAll(subRoles);
					}
				}
			}

			user.setRoles(finalRoles);

			// ---------------- SAVE USER ----------------
			User savedUser = userRepo.save(user);

			// ---------------- TOKENS ----------------
			String accessToken = jwtProvider.generateAccessToken(savedUser);
			RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);

			// ---------------- RESPONSE ----------------
			response.setUserName(savedUser.getUsername());
			response.setAccessToken(accessToken);
			response.setRefreshToken(refreshToken.getToken());
			response.setGrantedAuthorities(finalRoles);

			error.setErrorCode("0");
			error.setErrorDescription("User registered successfully");
			response.setErrorDetails(error);

			log.info("SUCCESS register | username={} | roles={} | corrId={}", savedUser.getUsername(),
					finalRoles.stream().map(Role::getName).toList(), corrId);

			return response;

		} catch (Exception e) {

			log.error("ERROR register | username={} | corrId={}", registerRequest.getUsername(), corrId, e);

			error.setErrorCode("1");
			error.setErrorDescription(e.getMessage());
			response.setErrorDetails(error);
			return response;
		}
	}

	/* ========================== LOGIN ========================== */

	@Override
	public LoginResponse login(LoginRequest request) {

		LoginResponse response = new LoginResponse();
		ApplicationError error = new ApplicationError();

		log.info("START login | username={}", request.getUserName());

		try {

			Optional<User> userOpt = userRepo.findByUsername(request.getUserName());
			if (userOpt.isEmpty()) {
				error.setErrorCode("2");
				error.setErrorDescription("Invalid username or password");
				response.setErrorDetails(error);
				return response;
			}

			User user = userOpt.get();

			if (!request.getPassword().equalsIgnoreCase(user.getPassword())) {
				error.setErrorCode("2");
				error.setErrorDescription("Invalid username or password");
				response.setErrorDetails(error);
				return response;
			}

			String accessToken = jwtProvider.generateAccessToken(user);
			RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

			response.setUserName(user.getUsername());
			response.setAccessToken(accessToken);
			response.setRefreshToken(refreshToken.getToken());
			response.setGrantedAuthorities(user.getRoles());

			error.setErrorCode("0");
			error.setErrorDescription("Success");

			log.info("SUCCESS login | username={}", user.getUsername());

		} catch (Exception e) {
			log.error("ERROR login | username={}", request.getUserName(), e);
			error.setErrorCode("3");
			error.setErrorDescription("Authentication failed");
		}

		response.setErrorDetails(error);
		return response;
	}

	/* ========================== DELETE USER ========================== */

	@Override
	public BaseResponse deleteUser(String username) {

		BaseResponse response = new BaseResponse();
		ApplicationError error = new ApplicationError();

		log.warn("Delete user requested | username={}", username);

		try {

			String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
			Optional<User> currentUserOpt = userRepo.findByUsername(currentUsername);

			if (currentUserOpt.isEmpty()) {
				error.setErrorCode("1");
				error.setErrorDescription("Authenticated user not found");
				response.setErrorDetails(error);
				return response;
			}

			boolean isAdmin = currentUserOpt.get().getRoles().stream()
					.anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getName()));

			if (!isAdmin) {
				error.setErrorCode("2");
				error.setErrorDescription("Not authorized");
				response.setErrorDetails(error);
				return response;
			}

			Optional<User> userOpt = userRepo.findByUsername(username);
			if (userOpt.isEmpty()) {
				error.setErrorCode("3");
				error.setErrorDescription("User not found");
			} else if (username.equalsIgnoreCase(currentUsername)) {
				error.setErrorCode("4");
				error.setErrorDescription("Cannot delete own account");
			} else {
				refreshTokenService.deleteByUser(userOpt.get());
				userRepo.delete(userOpt.get());
				error.setErrorCode("0");
				error.setErrorDescription("User deleted successfully");
			}

		} catch (Exception e) {
			log.error("ERROR deleteUser | username={}", username, e);
			error.setErrorCode("5");
			error.setErrorDescription("Delete failed");
		}

		response.setErrorDetails(error);
		return response;
	}

	/* ========================== REFRESH TOKEN ========================== */

	@Override
	public LoginResponse refreshAccessToken(RefreshTokenRequest request) {

		LoginResponse response = new LoginResponse();
		ApplicationError error = new ApplicationError();

		Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(request.getRefreshToken());

		if (tokenOpt.isEmpty() || tokenOpt.get().isRevoked()
				|| tokenOpt.get().getExpiryDate().isBefore(Instant.now())) {

			error.setErrorCode("1");
			error.setErrorDescription("Invalid or expired refresh token");
			response.setErrorDetails(error);
			return response;
		}

		User user = tokenOpt.get().getUser();
		response.setAccessToken(jwtProvider.generateAccessToken(user));
		response.setRefreshToken(request.getRefreshToken());

		error.setErrorCode("0");
		error.setErrorDescription("Token refreshed");

		response.setErrorDetails(error);
		return response;
	}

	/* ========================== ROLES & USERS ========================== */

	@Override
	public Map<String, List<Role>> getAllRole() {

		List<Role> roles = roleRepo.findAll();

		// Result: MainCard -> List of SubCards
		Map<String, List<Role>> result = new LinkedHashMap<>();

		// 1️⃣ Identify MAIN cards (parentCard == null)
		List<Role> mainCards = roles.stream().filter(r -> r.getParentCard() == null || r.getParentCard().isBlank())
				.toList();

		// 2️⃣ For each MAIN card, attach its SUB cards
		for (Role main : mainCards) {

			List<Role> subCards = roles.stream()
					.filter(r -> r.getParentCard() != null && r.getParentCard().equalsIgnoreCase(main.getName()))
					.toList();

			result.put(main.getName(), subCards);
		}

		return result;
	}

	@Override
	public List<User> getAllUser() {
		return userRepo.findAll();
	}

	@Override
	public BaseResponse updateUserRoles(UpdateUserRolesRequest request) {

		BaseResponse response = new BaseResponse();
		ApplicationError error = new ApplicationError();

		try {

			Optional<User> userOpt = userRepo.findByUsername(request.getUsername());
			if (userOpt.isEmpty()) {
				error.setErrorCode("1");
				error.setErrorDescription("User not found");
				response.setErrorDetails(error);
				return response;
			}

			Set<Role> roles = new HashSet<>();
			for (String roleName : request.getRoles()) {
				Optional<Role> roleOpt = roleRepo.findByName(roleName);
				if (roleOpt.isEmpty()) {
					error.setErrorCode("1");
					error.setErrorDescription("Role not found: " + roleName);
					response.setErrorDetails(error);
					return response;
				}
				roles.add(roleOpt.get());
			}

			User user = userOpt.get();
			user.setRoles(roles);
			userRepo.save(user);

			error.setErrorCode("0");
			error.setErrorDescription("Roles updated successfully");

		} catch (Exception e) {
			log.error("ERROR updateUserRoles | user={}", request.getUsername(), e);
			error.setErrorCode("2");
			error.setErrorDescription("Update failed");
		}

		response.setErrorDetails(error);
		return response;
	}

	/* ========================== OTP / PASSWORD ========================== */

	@Override
	public boolean otpPwdReset(String emailId, String userName) {

		Optional<User> userOpt = userRepo.findByUsername(userName);
		if (userOpt.isEmpty())
			return false;

		User user = userOpt.get();
		String otp = String.valueOf(100000 + new Random().nextInt(900000));

		user.setOtp(otp);
		user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
		userRepo.save(user);

		// emailService.sendEmail(emailId, "OTP", otp);

		log.info("OTP generated | username={}", userName);
		return true;
	}

	@Override
	public boolean verifyOtp(String emailId, String userName, String otp) {

		Optional<User> userOpt = userRepo.findByUsername(userName);
		if (userOpt.isEmpty())
			return false;

		User user = userOpt.get();

		if (user.getOtp() == null || !otp.equals(user.getOtp()))
			return false;

		if (user.getOtpExpiry().isBefore(LocalDateTime.now()))
			return false;

		user.setOtpVerified(true);
		user.setOtp(null);
		user.setOtpExpiry(null);
		userRepo.save(user);

		log.info("OTP verified | username={}", userName);
		return true;
	}

	@Override
	public boolean resetPassword(String userName, String newPwd) {

		Optional<User> userOpt = userRepo.findByUsername(userName);
		if (userOpt.isEmpty())
			return false;

		User user = userOpt.get();

		if (user.getOtpVerified() == null || !user.getOtpVerified())
			return false;

		user.setPassword(newPwd);
		user.setOtpVerified(false);
		userRepo.save(user);

		log.info("Password reset | username={}", userName);
		return true;
	}
}
