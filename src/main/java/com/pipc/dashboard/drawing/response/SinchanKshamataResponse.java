package com.pipc.dashboard.drawing.response;

import java.util.Map;

import com.pipc.dashboard.utility.BaseResponse;

import lombok.Data;

@Data
public class SinchanKshamataResponse extends BaseResponse {
	private String message;

	private String period;
	private String title;
	private String date;
	private Map<String, Object> data;
}
