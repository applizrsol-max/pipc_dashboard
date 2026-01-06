package com.pipc.dashboard.bhusmapadan.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.pipc.dashboard.utility.BaseResponse;

import lombok.Data;

@Data
public class BhaniniResponse extends BaseResponse {
	
	private String message;
	private JsonNode data;

}
