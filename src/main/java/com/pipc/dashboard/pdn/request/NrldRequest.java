package com.pipc.dashboard.pdn.request;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
public class NrldRequest {
	private String submissionYear;
	private List<JsonNode> records;
}
