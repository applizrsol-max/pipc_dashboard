package com.pipc.dashboard.service;

import com.pipc.dashboard.login.request.LoginRequest;
import com.pipc.dashboard.login.request.RegisterRequest;
import com.pipc.dashboard.login.response.LoginResponse;
import com.pipc.dashboard.utility.BaseResponse;

public interface LoginService {
	public LoginResponse register(RegisterRequest registerRequest);

	public LoginResponse login(LoginRequest loginRequest);

	public BaseResponse deleteUser(String username);
}
