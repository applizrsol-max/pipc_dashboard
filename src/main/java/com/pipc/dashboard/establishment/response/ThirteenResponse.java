package com.pipc.dashboard.establishment.response;

import java.util.List;

import com.pipc.dashboard.establishment.repository.AgendaThirteenEntity;
import com.pipc.dashboard.utility.BaseResponse;

import lombok.Data;

@Data
public class ThirteenResponse extends BaseResponse {
	private String message;
	private List<AgendaThirteenEntity> data;
}
