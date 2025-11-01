package com.pipc.dashboard.establishment.request;

import lombok.Data;

@Data
public class HolidayJoinApproval {
	private Boolean permission;
	private String holidayDates;
	private String remark;
}
