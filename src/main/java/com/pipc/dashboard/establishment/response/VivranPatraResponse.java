package com.pipc.dashboard.establishment.response;

import com.pipc.dashboard.establishment.request.VivranPatraRequest;
import com.pipc.dashboard.utility.BaseResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VivranPatraResponse extends BaseResponse {

	private String year;
	private String upadhikshakAbhiyanta;
	private VivranPatraRequest data;
	private String message;
}
