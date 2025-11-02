package com.pipc.dashboard.establishment.request;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class IncomeTaxDeductionRequest {
	private String month;
	private String year;
	private List<Map<String, Object>> data;
}
