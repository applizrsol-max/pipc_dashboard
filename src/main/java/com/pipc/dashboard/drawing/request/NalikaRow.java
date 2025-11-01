package com.pipc.dashboard.drawing.request;

import java.util.Map;

import lombok.Data;

@Data
public class NalikaRow {
	private Integer rowId;
    private String flag; // "D" or ""
    private Map<String, Object> data;
}
