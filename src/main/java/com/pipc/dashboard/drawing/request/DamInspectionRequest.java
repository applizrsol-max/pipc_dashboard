package com.pipc.dashboard.drawing.request;

import java.util.Map;

import lombok.Data;

@Data
public class DamInspectionRequest {
	private String title;
	private String period;
	private Map<String, DepartmentData> departments;
}