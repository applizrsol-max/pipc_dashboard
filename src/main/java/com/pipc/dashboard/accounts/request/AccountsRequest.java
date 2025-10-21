package com.pipc.dashboard.accounts.request;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
public class AccountsRequest {

	private Map<String, JsonNode> reports;
	private String accountsYear;
}
