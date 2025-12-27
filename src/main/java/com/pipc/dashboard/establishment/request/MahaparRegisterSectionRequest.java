package com.pipc.dashboard.establishment.request;

import java.util.List;

import lombok.Data;

@Data
public class MahaparRegisterSectionRequest {
	private Long sectionId;
	private String sectionName;
	private List<MahaparRegisterRowRequest> rows;
}
