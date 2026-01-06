package com.pipc.dashboard.establishment.request;

import java.util.List;

import lombok.Data;

@Data
public class BhaniniRequest {
	private String flag; // "D" only, else null / empty

	private String year;

	private EmployeeInfo employee;

	private List<MonthlyDetailBhanini> monthlyDetails;

	private PreviousYearPendingBhanini previousYearPending;

	private KhatyavarJamaRakamBhanini khatyavarJamaRakam;
}
