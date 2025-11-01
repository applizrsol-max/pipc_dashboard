package com.pipc.dashboard.establishment.request;

import lombok.Data;

@Data
public class Certification {
	private String employmentContinuation;
	private Integer leaveBalanceBefore;
	private Integer leaveBalanceAfter;
	private String asOnDate;
}
