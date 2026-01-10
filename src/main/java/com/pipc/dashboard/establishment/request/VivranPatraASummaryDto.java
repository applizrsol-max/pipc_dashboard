package com.pipc.dashboard.establishment.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VivranPatraASummaryDto {

	private String district;
	private Integer sanctionPost;
	private Integer workingPost;
	private Integer vacantPost;
	private Integer futureVacancy;
}
