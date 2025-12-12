package com.pipc.dashboard.pdn.response;

import java.util.List;
import java.util.Map;

import com.pipc.dashboard.utility.BaseResponse;

import lombok.Data;

@Data
public class NrldResponse extends BaseResponse {
	private List<Map<String, Object>> data;

}
