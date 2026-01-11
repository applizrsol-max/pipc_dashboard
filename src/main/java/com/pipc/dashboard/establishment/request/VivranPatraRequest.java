package com.pipc.dashboard.establishment.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VivranPatraRequest {

	private String year;
	private String upadhikshakAbhiyanta;
	private String manjurVarg;
	private String reportMonth;

	private List<VivranGroupDto> groups;
}
