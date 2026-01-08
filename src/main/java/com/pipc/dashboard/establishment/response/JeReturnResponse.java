package com.pipc.dashboard.establishment.response;

import com.pipc.dashboard.utility.BaseResponse;

import lombok.Data;

@Data
public class JeReturnResponse extends BaseResponse {
	private Object data;
	private String message;
}
