package com.pipc.dashboard.login.request;

import java.util.Set;

import lombok.Data;

@Data
public class RegisterRequest {
	private String username;
	private String password;
	private Set<String> roles;

	public RegisterRequest() {
	}

	public RegisterRequest(String username, String password, Set<String> roles) {
		this.username = username;
		this.password = password;
		this.roles = roles;
	}

}
