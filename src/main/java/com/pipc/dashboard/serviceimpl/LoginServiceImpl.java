package com.pipc.dashboard.serviceimpl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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
import com.pipc.dashboard.utility.EmailService;

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
	private final RefreshTokenRepository refreshTokenRepository;
	private final EmailService emailService;

	@Autowired
	public LoginServiceImpl(UserRepository userRepo, RoleRepository roleRepo, PasswordEncoder passwordEncoder,
			AuthenticationManager authManager, JwtProvider jwtProvider, RefreshTokenService refreshTokenService,
			RefreshTokenRepository refreshTokenRepository, EmailService emailService) {
		this.userRepo = userRepo;
		this.roleRepo = roleRepo;
		this.passwordEncoder = passwordEncoder;
		this.authManager = authManager;
		this.jwtProvider = jwtProvider;
		this.refreshTokenService = refreshTokenService;
		this.refreshTokenRepository = refreshTokenRepository;
		this.emailService = emailService;

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

		// 🔥 Store SHA-512 hashed password (matching Angular)
		String hashedPassword = EncryptionUtils.sha512(registerRequest.getPassword());
		user.setPassword(hashedPassword);

		// 🔥 Assign roles
		Set<Role> assignedRoles = new HashSet<>();

		if (registerRequest.getRoles() == null || registerRequest.getRoles().isEmpty()) {
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

				// 🔥 Generate tokens for newly registered user
				String accessToken = jwtProvider.generateAccessToken(savedUser);
				RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);

				error.setErrorCode("0");
				error.setErrorDescription("User registered successfully!");

				loginResponse.setUserName(registerRequest.getUsername());
				loginResponse.setAccessToken(accessToken);
				loginResponse.setRefreshToken(refreshToken.getToken());
				loginResponse.setGrantedAuthorities(assignedRoles);

			} else {
				error.setErrorCode("1");
				error.setErrorDescription("User registration failed!");
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
			// 1. Fetch user from DB
			User user = userRepo.findByUsername(loginRequest.getUserName())
					.orElseThrow(() -> new IllegalStateException("User not found"));

			// 2. Client sent hash
			String clientHash = loginRequest.getPassword();

			if (clientHash == null || clientHash.isEmpty()) {
				error.setErrorCode("2");
				error.setErrorDescription("Missing hash in request");
				loginResponse.setErrorDetails(error);
				return loginResponse;
			}

			// 3. Fetch stored actual password (from DB)
			String storedPassword = user.getPassword(); // DB actual password

			// 5. Compare SHA-512 hashes
			if (!loginRequest.getPassword().equalsIgnoreCase(storedPassword)) {
				error.setErrorCode("2");
				error.setErrorDescription("Invalid username or password");
				loginResponse.setErrorDetails(error);
				return loginResponse;
			}

			// 6. If match → generate tokens
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
				loginResponse.setUserName(user.getUsername());
			}

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

		// 1️⃣ Get currently authenticated user
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String currentUsername = authentication.getName();

		// 2️⃣ Load current user from DB
		Optional<User> currentUserOpt = userRepo.findByUsername(currentUsername);
		if (currentUserOpt.isEmpty()) {
			error.setErrorCode("1");
			error.setErrorDescription("Authenticated user not found in the system");
			response.setErrorDetails(error);
			return response;
		}

		User currentUser = currentUserOpt.get();

		// 3️⃣ Check if current user has ADMIN role
		boolean isAdmin = currentUser.getRoles().stream()
				.anyMatch(role -> role.getName().equalsIgnoreCase("ROLE_ADMIN"));

		if (!isAdmin) {
			error.setErrorCode("2");
			error.setErrorDescription("You are not authorized to delete the user");
			response.setErrorDetails(error);
			return response;
		}

		// 4️⃣ Proceed with delete logic
		Optional<User> userOpt = userRepo.findByUsername(username);
		if (userOpt.isEmpty()) {
			error.setErrorCode("3");
			error.setErrorDescription("User not found");
		} else {

			if (userOpt.get().getUsername().equalsIgnoreCase(currentUsername)) {
				error.setErrorCode("4");
				error.setErrorDescription("You cannot delete your own account");
			} else {
				refreshTokenService.deleteByUser(userOpt.get());
				userRepo.delete(userOpt.get());
				error.setErrorCode("0");
				error.setErrorDescription("User deleted successfully");
			}
		}

		response.setErrorDetails(error);
		return response;
	}

	@Override
	public LoginResponse refreshAccessToken(RefreshTokenRequest request) {
		ApplicationError error = new ApplicationError();
		LoginResponse response = new LoginResponse();

		Optional<RefreshToken> optionalToken = refreshTokenRepository.findByToken(request.getRefreshToken());

		if (optionalToken.isEmpty()) {
			error.setErrorCode("1");
			error.setErrorDescription("Invalid refresh token");
			response.setErrorDetails(error);
			return response;
		}

		RefreshToken refreshToken = optionalToken.get();

		if (refreshToken.isRevoked() || refreshToken.getExpiryDate().isBefore(Instant.now())) {
			error.setErrorCode("1");
			error.setErrorDescription("Refresh token expired or revoked");
			response.setErrorDetails(error);
			return response;
		}

		User user = refreshToken.getUser();
		String newAccessToken = jwtProvider.generateAccessToken(user);

		error.setErrorCode("0");
		error.setErrorDescription("Access token refreshed successfully");

		response.setAccessToken(newAccessToken);
		response.setRefreshToken(request.getRefreshToken());
		response.setErrorDetails(error);

		return response;
	}

	@Override
	public List<Role> getAllRole() {
		// TODO Auto-generated method stub
		return roleRepo.findAll();
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
				error.setErrorDescription("User not found: " + request.getUsername());
				response.setErrorDetails(error);
				return response;
			}

			User user = userOpt.get();
			List<String> requestedRoles = request.getRoles();

			if (requestedRoles == null || requestedRoles.isEmpty()) {
				error.setErrorCode("1");
				error.setErrorDescription("Role list cannot be empty");
				response.setErrorDetails(error);
				return response;
			}

			Set<Role> validRoles = new HashSet<>();
			for (String roleName : requestedRoles) {
				Optional<Role> roleOpt = roleRepo.findByName(roleName);
				if (roleOpt.isEmpty()) {
					error.setErrorCode("1");
					error.setErrorDescription("Role not found: " + roleName);
					response.setErrorDetails(error);
					return response;
				}
				validRoles.add(roleOpt.get());
			}

			// Replace all roles with new ones
			user.setRoles(validRoles);
			userRepo.save(user);

			error.setErrorCode("0");
			error.setErrorDescription("Roles updated successfully for user: " + user.getUsername());
		} catch (Exception e) {
			error.setErrorCode("1");
			error.setErrorDescription("Error updating roles: " + e.getMessage());
		}
		response.setErrorDetails(error);

		return response;
	}

	@Override
	public boolean otpPwdReset(String emailId, String userName) {

		// Validate user by username
		Optional<User> userOpt = userRepo.findByUsername(userName);
		if (!userOpt.isPresent()) {
			System.out.println("User not found for username: " + userName);
			return false;
		}

		User user = userOpt.get();

		// Generate 6-digit OTP
		String otp = String.valueOf((int) (Math.random() * 900000) + 100000);

		// Store OTP in DB
		user.setOtp(otp);
		user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
		userRepo.save(user);

		// Email subject
		String subject = "Password Reset OTP - PIPC (Pune Irrigation Project Circle)";

		// Email body (Professional English)
		String body = "Hello " + user.getUsername() + ",\n\n"
				+ "You have requested to reset your account password. Please use the below One-Time Password (OTP) to proceed:\n\n"
				+ "Your OTP: " + otp + "\n\n"
				+ "This OTP is valid for 10 minutes only. Do not share it with anyone.\n\n"
				+ "If you did not initiate this request, please ignore this email or contact support immediately.\n\n"
				+ "Regards,\n" + "PIPC — Pune Irrigation Project Circle\n" + "Support: support@pipc.example.com";

		// Send Email
		emailService.sendEmail(emailId, subject, body);

		return true;
	}

	@Override
	public boolean verifyOtp(String emailId, String userName, String otp) {

		Optional<User> userOpt = userRepo.findByUsername(userName);
		if (!userOpt.isPresent())
			return false;

		User user = userOpt.get();

		if (user.getOtp() == null || user.getOtp().isEmpty())
			return false;
		if (!otp.equals(user.getOtp()))
			return false;
		if (user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(LocalDateTime.now()))
			return false;

		// OTP verified → mark verified
		user.setOtpVerified(true);

		// Clear OTP values
		user.setOtp(null);
		user.setOtpExpiry(null);

		userRepo.save(user);
		return true;
	}

	@Override
	public boolean resetPassword(String userName, String newPwd) {

		Optional<User> userOpt = userRepo.findByUsername(userName);
		if (!userOpt.isPresent()) {
			return false; // user not found
		}

		User user = userOpt.get();

		// ❗ IMPORTANT check — OTP must be verified before resetting password
		if (user.getOtpVerified() == null || !user.getOtpVerified()) {
			System.out.println("Password reset blocked — OTP not verified.");
			return false;
		}

		user.setPassword(newPwd);

		// Reset OTP verification flag
		user.setOtpVerified(false);

		userRepo.save(user);
		return true;
	}

}
