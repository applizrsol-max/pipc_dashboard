package com.pipc.dashboard.establishment.request;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
public class AgendaSecRow {
	private long rowId;
	private String deleteFlag; // "", D
	private long deleteId;
	private JsonNode columnData; // JSON data block
	
}
