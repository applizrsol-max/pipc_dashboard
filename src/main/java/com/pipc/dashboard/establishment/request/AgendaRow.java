package com.pipc.dashboard.establishment.request;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
public class AgendaRow {
	private long rowId; // For create/update
	private String deleteFlag; // If D -> delete mode
	private long deleteId; // Which rowId to delete
	private JsonNode columnData; // Full dynamic JSON
	private String upAdhikshakAbhiyantaName;
}