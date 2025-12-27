package com.pipc.dashboard.establishment.request;

import java.util.List;

import lombok.Data;

@Data
public class MahaparRegisterRequest {
	private String year;

	private List<MahaparRegisterSectionRequest> sections;
}
