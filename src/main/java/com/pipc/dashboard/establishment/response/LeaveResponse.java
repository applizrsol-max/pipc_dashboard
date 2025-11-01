package com.pipc.dashboard.establishment.response;

import java.util.List;
import java.util.Map;

import com.pipc.dashboard.utility.BaseResponse;

import lombok.Data;

@Data
public class LeaveResponse extends BaseResponse {

	private String message;
	private List<Map<String, Object>> data;
}
