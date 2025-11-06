package com.pipc.dashboard.pdn.request;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
public class AgendaDetail {
	private Integer rowId;
	private JsonNode columnData;
	private Long deleteId;
	private String flag;
}