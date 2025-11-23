package com.pipc.dashboard.login.request;

import lombok.Data;

@Data
public class VerifyOtpRequest {
	private String emailId;
	private String userName;
	private String otp;
}
