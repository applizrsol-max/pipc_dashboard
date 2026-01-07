package com.pipc.dashboard.bhusmapadan.request;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
public class PraptraMasterDataRowRequest {

	private Long rowId; // long
	private Long deleteId; // long (only for delete)
	private String flag; // only "D" or blank

	private JsonNode data; 
}
