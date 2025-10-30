package com.pipc.dashboard.pdn.response;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pipc.dashboard.utility.BaseResponse;

import lombok.Data;

@Data
public class KraResponse extends BaseResponse {
	private String message;
	private ObjectNode responseData;
}
