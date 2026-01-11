package com.pipc.dashboard.establishment.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VivranRowDto {

	private Long rowId;
	private Long deleteId;
	private String flag; // C / U / D

	private String postName;
	private Integer sanction;
	private Integer working;
	private Integer vacant;
}
