package com.pipc.dashboard.establishment.response;

import java.util.List;

import com.pipc.dashboard.establishment.repository.AgendaOfficerEntity;
import com.pipc.dashboard.utility.BaseResponse;

import lombok.Data;

@Data
public class AgendaResponse extends BaseResponse {
	private String message;
	private List<AgendaOfficerEntity> data;

}