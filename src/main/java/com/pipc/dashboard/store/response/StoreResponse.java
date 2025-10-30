package com.pipc.dashboard.store.response;

import com.pipc.dashboard.store.request.StoreRequest;
import com.pipc.dashboard.utility.BaseResponse;

import lombok.Data;

@Data
public class StoreResponse extends BaseResponse {
	private String message;
	private StoreRequest data;
}
