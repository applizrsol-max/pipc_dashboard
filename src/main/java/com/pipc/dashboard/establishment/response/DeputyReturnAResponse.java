package com.pipc.dashboard.establishment.response;

import com.pipc.dashboard.establishment.request.DeputyReturnARequest;
import com.pipc.dashboard.utility.BaseResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeputyReturnAResponse extends BaseResponse {

	private String year;
	private String upAdhikshakAbhiyanta;
	private DeputyReturnARequest data;
	private String message;

}
