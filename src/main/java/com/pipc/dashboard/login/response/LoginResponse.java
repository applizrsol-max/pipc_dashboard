package com.pipc.dashboard.login.response;

import java.util.Set;

import com.pipc.dashboard.login.entities.Role;
import com.pipc.dashboard.utility.BaseResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LoginResponse extends BaseResponse {
	private String accessToken;
	private String refreshToken;
	private String message;
	private Set<Role> grantedAuthorities;
	private String userName;
}
