package com.pipc.dashboard.pdn.request;

import java.util.List;

import lombok.Data;

@Data
public class IrrigationSaveRequest {
	private String year;
	private String date;
	private List<IrrigationRowDTO> rows;
}