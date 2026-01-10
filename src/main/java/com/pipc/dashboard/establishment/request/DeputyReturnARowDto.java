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
public class DeputyReturnARowDto {

	private Long rowId;
	private Long deleteId;
	private String flag;

	private Integer kramank;

	private JsonNode data;
}
