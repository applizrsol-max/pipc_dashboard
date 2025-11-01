package com.pipc.dashboard.establishment.request;

import java.util.List;

import lombok.Data;

@Data
public class MedicalBillRequest {
	private String title;
	private String period;
	private String month;
	private String year;
	private String date;
	private List<MedicalBillData> data;
}
