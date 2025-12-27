package com.pipc.dashboard.establishment.request;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
public class MahaparRegisterRowRequest {
	private Long rowId;
	private Long deleteId;
	private String flag; // "" or "D"
	private JsonNode data;
}
