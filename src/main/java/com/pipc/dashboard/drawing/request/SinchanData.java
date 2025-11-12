package com.pipc.dashboard.drawing.request;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
public class SinchanData {
	private String sectionTitle;
	private Integer rowId;
	private Long deleteId;
	private String flag; // S,U,D
	private JsonNode rows; // dynamic JSON structure per section
}