package com.pipc.dashboard.establishment.request;

import lombok.Data;

@Data
public class EmployeeInfo {
	private String name; // mandatory
	private String designation;
	private String payScaleAsOn;
}
