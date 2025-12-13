package com.pipc.dashboard.drawing.request;

import java.util.Map;

import lombok.Data;

@Data
public class TenderRowDTO {
	private Long kramank;
	private Long rowId;
	private Long deleteId;
	private String flag;
	private Map<String, Object> data;
}
