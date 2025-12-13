package com.pipc.dashboard.pdn.request;

import java.util.Map;

import lombok.Data;

@Data
public class IrrigationRowDTO {
	private Long rowId;
	private Long deleteId;
	private String flag; // C/U/D from UI (D only)
	private Map<String, Object> data; // dynamic columns
}
