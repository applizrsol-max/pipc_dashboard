package com.pipc.dashboard.drawing.response;

import java.util.List;
import java.util.Map;

import com.pipc.dashboard.utility.BaseResponse;

import lombok.Data;

@Data
public class TenderBhamaResponse extends BaseResponse {
	private List<Map<String, Object>> rows;
}
