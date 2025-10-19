package com.pipc.dashboard.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pipc.dashboard.business.LoginBussiness;
import com.pipc.dashboard.login.entities.User;
import com.pipc.dashboard.login.request.LoginRequest;
import com.pipc.dashboard.login.request.RefreshTokenRequest;
import com.pipc.dashboard.login.request.RegisterRequest;
import com.pipc.dashboard.login.response.LoginResponse;
import com.pipc.dashboard.security.utility.JwtProvider;
import com.pipc.dashboard.token.entity.RefreshToken;
import com.pipc.dashboard.utility.BaseResponse;

@RequestMapping("/pipc/dashboard/onboarding")
@RestController
public class LoginController {

	private Logger log = LoggerFactory.getLogger(LoginController.class);

	private final LoginBussiness loginBusiness;

	@GetMapping("/hi")
	public String sayHi() {
		return "Hi";
	}

	public LoginController(LoginBussiness loginBusiness) {
		this.loginBusiness = loginBusiness;
	}

	@PostMapping("/register")
	public LoginResponse register(@RequestBody RegisterRequest registerRequest) {
		return loginBusiness.register(registerRequest);

	}

	@PostMapping("/login")
	public LoginResponse logIn(@RequestBody LoginRequest logInRequest) {
		return loginBusiness.login(logInRequest);

	}

	@DeleteMapping("/deleteUser/{username}")
	public BaseResponse deleteUser(@PathVariable String username) {
		return loginBusiness.deleteUser(username);
	}

	@PostMapping("/refresh-token")
	public LoginResponse refreshAccessToken(@RequestBody RefreshTokenRequest request) {

		return loginBusiness.refreshAccessToken(request);

	}
}
