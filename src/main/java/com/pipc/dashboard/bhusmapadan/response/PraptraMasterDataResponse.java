package com.pipc.dashboard.bhusmapadan.response;

import java.util.Map;

import com.pipc.dashboard.utility.BaseResponse;

import lombok.Data;

@Data
public class PraptraMasterDataResponse extends BaseResponse {

	private String message;
	private Map<String, Object> data;
}
