package com.pipc.dashboard.pdn.request;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class KraRequest {
	private String title;
	private String kraPeriod;
	private String reference;
	private List<Map<String, Object>> kraData; // ✅ dynamic keys
	private Integer rowId; // for update

}