package com.pipc.dashboard.establishment.response;

import java.util.List;

import com.pipc.dashboard.establishment.request.DeputyReturnADivisionDto;
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

	private List<DeputyReturnADivisionDto> division;
}
