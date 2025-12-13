package com.pipc.dashboard.drawing.request;

import java.util.List;

import lombok.Data;

@Data
public class TenderSaveRequest {
	private String year;
	private String month;
	private String date;
	private List<TenderRowDTO> rows;
}
