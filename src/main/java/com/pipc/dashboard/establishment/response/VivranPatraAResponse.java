package com.pipc.dashboard.establishment.response;

import com.pipc.dashboard.utility.BaseResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VivranPatraAResponse extends BaseResponse {

	private String year;
	private String upAdhikshakAbhiyanta;
	private Object data;
	private String message;
}
