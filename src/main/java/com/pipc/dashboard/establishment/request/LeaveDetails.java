package com.pipc.dashboard.establishment.request;

import lombok.Data;

@Data
public class LeaveDetails {
	private String ruleReference;
	private String reason;
	private String fromDate;
	private String toDate;
	private Integer totalDays;
	private String leaveType;
}
