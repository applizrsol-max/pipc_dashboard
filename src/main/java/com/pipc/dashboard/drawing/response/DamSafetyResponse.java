package com.pipc.dashboard.drawing.response;

import com.pipc.dashboard.utility.BaseResponse;

import lombok.Data;

@Data
public class DamSafetyResponse extends BaseResponse {
	private String message;
	private Object data;
}
