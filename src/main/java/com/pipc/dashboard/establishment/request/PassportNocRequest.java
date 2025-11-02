package com.pipc.dashboard.establishment.request;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
public class PassportNocRequest {
	private Long rowId;
	private String year;
	private String month;
	private String date;
	private String flag; // Only used for DELETE; Create/Update auto handled internally
	private JsonNode noObjectionCertificate;
	private JsonNode identityConfirmation;
	private JsonNode dynamicColumns;
}