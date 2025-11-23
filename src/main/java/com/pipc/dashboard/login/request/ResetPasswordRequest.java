package com.pipc.dashboard.login.request;

import lombok.Data;

@Data
public class ResetPasswordRequest {
	private String userName;
	private String newPwd;
}
