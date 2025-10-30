package com.pipc.dashboard.businessimpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pipc.dashboard.business.LoginBussiness;
import com.pipc.dashboard.login.entities.Role;
import com.pipc.dashboard.login.entities.UserResponse;
import com.pipc.dashboard.login.request.LoginRequest;
import com.pipc.dashboard.login.request.RefreshTokenRequest;
import com.pipc.dashboard.login.request.RegisterRequest;
import com.pipc.dashboard.login.response.LoginResponse;
import com.pipc.dashboard.service.LoginService;
import com.pipc.dashboard.utility.BaseResponse;

@Service
public class LoginBussinessImpl implements LoginBussiness {

	private final LoginService loginService;

	@Autowired
	public LoginBussinessImpl(LoginService loginService) {

		this.loginService = loginService;
	}

	public LoginResponse register(RegisterRequest registerRequest) {

		return loginService.register(registerRequest);

	}

	public LoginResponse login(LoginRequest loginRequest) {
		return loginService.login(loginRequest);
	}

	@Override
	public BaseResponse deleteUser(String username) {
		return loginService.deleteUser(username);
	}

	@Override
	public LoginResponse refreshAccessToken(RefreshTokenRequest request) {

		return loginService.refreshAccessToken(request);

	}

	@Override
	public List<Role> getAllRole() {
		return loginService.getAllRole();
	}

	@Override
	public List<UserResponse> getAllUser() {
		// TODO Auto-generated method stub
		return loginService.getAllUser().stream()
				.map(user -> new UserResponse(user.getUsername(),
						user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet())))
				.collect(Collectors.toList());
	}
}
