package com.pipc.dashboard.store.request;

import java.util.List;

import lombok.Data;

@Data
public class DepartmentSection {
	private String departmentName; // e.g. "Karyakari Abhiyanta, Bhama Asakhed Dharn Vibhag, Pune"
	private List<VibhagRow> rows; // all rows for that department
	private Integer ekun; // total for that department
}
