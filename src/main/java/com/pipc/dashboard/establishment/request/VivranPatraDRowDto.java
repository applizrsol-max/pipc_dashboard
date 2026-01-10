package com.pipc.dashboard.establishment.request;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VivranPatraDRowDto {

	private Long rowId;
	private String flag; // C / U / D
	private Long deleteId;
	private JsonNode data;
}
