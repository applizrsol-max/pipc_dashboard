package com.pipc.dashboard.business;

import org.springframework.http.ResponseEntity;

import com.pipc.dashboard.login.request.LoginRequest;
import com.pipc.dashboard.login.request.RefreshTokenRequest;
import com.pipc.dashboard.login.request.RegisterRequest;
import com.pipc.dashboard.login.response.LoginResponse;
import com.pipc.dashboard.utility.BaseResponse;

public interface LoginBussiness {

	public LoginResponse register(RegisterRequest registerRequest);

	public LoginResponse login(LoginRequest loginRequest);

	public BaseResponse deleteUser(String username);

	public LoginResponse refreshAccessToken(RefreshTokenRequest request);
}
