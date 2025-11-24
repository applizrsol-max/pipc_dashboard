package com.pipc.dashboard.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pipc.dashboard.business.LoginBussiness;
import com.pipc.dashboard.login.entities.Role;
import com.pipc.dashboard.login.entities.UserResponse;
import com.pipc.dashboard.login.request.LoginRequest;
import com.pipc.dashboard.login.request.OtpRequest;
import com.pipc.dashboard.login.request.RefreshTokenRequest;
import com.pipc.dashboard.login.request.RegisterRequest;
import com.pipc.dashboard.login.request.ResetPasswordRequest;
import com.pipc.dashboard.login.request.UpdateUserRolesRequest;
import com.pipc.dashboard.login.request.VerifyOtpRequest;
import com.pipc.dashboard.login.response.LoginResponse;
import com.pipc.dashboard.utility.ApplicationError;
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

	@PostMapping("/getAllRole")
	public List<Role> getAllRole() {
		return loginBusiness.getAllRole();

	}

	@PostMapping("/getAllUser")
	public List<UserResponse> getAllUser() {
		return loginBusiness.getAllUser();

	}

	@PutMapping("/updateRole")
	public BaseResponse updateUserRoles(@RequestBody UpdateUserRolesRequest request) {
		return loginBusiness.updateUserRoles(request);
	}

	@PostMapping("/otpPwdReset")
	public ApplicationError otpPwdReset(@RequestBody OtpRequest request) {
		ApplicationError error = new ApplicationError();
		boolean sent = loginBusiness.otpPwdReset(request.getEmailId(), request.getUserName());

		if (sent) {
			error.setErrorCode("0");
			error.setErrorDescription("OTP sent successfully");
		}

		else {
			error.setErrorCode("1");
			error.setErrorDescription("Email not found!");
		}
		return error;
	}

	@PostMapping("/verifyOtpReset")
	public ApplicationError verifyOtp(@RequestBody VerifyOtpRequest req) {
		ApplicationError error = new ApplicationError();

		boolean isValid = loginBusiness.verifyOtp(req.getEmailId(), req.getUserName(), req.getOtp());

		if (isValid) {
			error.setErrorCode("0");
			error.setErrorDescription("Otp verified");
		} else {
			error.setErrorCode("1");
			error.setErrorDescription("Invalid or expired OTP");
		}

		return error;
	}

	@PostMapping("/resetPassword")
	public ApplicationError resetPassword(@RequestBody ResetPasswordRequest req) {
		ApplicationError error = new ApplicationError();

		boolean updated = loginBusiness.resetPassword(req.getUserName(), req.getNewPwd());

		if (updated) {
			error.setErrorCode("0");
			error.setErrorDescription("Password updated successfully");
		}

		else {
			error.setErrorCode("1");
			error.setErrorDescription("Password Updation Failed");
		}
		return error;

	}

}
