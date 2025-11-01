package com.pipc.dashboard.drawing.request;

import java.util.Map;

import lombok.Data;

@Data
public class DamDynamicRow {
	private Integer rowId;
    private String year;
    private String month;
    private Map<String, Object> data;  // Holds dynamic key-value pairs (e.g., kharip, rabbi, etc.)
    private String flag;
}
