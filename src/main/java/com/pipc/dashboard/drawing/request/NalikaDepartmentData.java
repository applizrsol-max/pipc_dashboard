package com.pipc.dashboard.drawing.request;

import java.util.List;

import lombok.Data;

@Data
public class NalikaDepartmentData {
	private String departmentKey;
    private String departmentName;
    private List<NalikaRow> rows;
}
