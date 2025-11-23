package com.pipc.dashboard.business;

import java.util.List;

import com.pipc.dashboard.login.entities.Role;
import com.pipc.dashboard.login.entities.UserResponse;
import com.pipc.dashboard.login.request.LoginRequest;
import com.pipc.dashboard.login.request.RefreshTokenRequest;
import com.pipc.dashboard.login.request.RegisterRequest;
import com.pipc.dashboard.login.request.UpdateUserRolesRequest;
import com.pipc.dashboard.login.response.LoginResponse;
import com.pipc.dashboard.utility.BaseResponse;

public interface LoginBussiness {

	public LoginResponse register(RegisterRequest registerRequest);

	public LoginResponse login(LoginRequest loginRequest);

	public BaseResponse deleteUser(String username);

	public LoginResponse refreshAccessToken(RefreshTokenRequest request);

	public List<Role> getAllRole();

	public List<UserResponse> getAllUser();

	public BaseResponse updateUserRoles(UpdateUserRolesRequest request);

	public boolean otpPwdReset(String emailId, String userName);

	public boolean verifyOtp(String emailId, String userName, String otp);

	public boolean resetPassword(String userName, String newPwd);
}
