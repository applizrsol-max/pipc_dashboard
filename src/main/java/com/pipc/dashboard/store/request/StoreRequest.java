package com.pipc.dashboard.store.request;

import java.util.List;

import lombok.Data;

@Data
public class StoreRequest {
	private List<DepartmentSection> departments;
	private Integer ekunEkandar;
}
