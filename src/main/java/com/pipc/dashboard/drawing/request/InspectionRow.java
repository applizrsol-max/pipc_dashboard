package com.pipc.dashboard.drawing.request;

import java.util.Map;

import lombok.Data;

@Data
public class InspectionRow {
	private Integer rowId;
	private Long deleteId;
	private String year;
	private String month;
	private String flag; // C, U, D
	private Map<String, Object> data; // dynamic data
}