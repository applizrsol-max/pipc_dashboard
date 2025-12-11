package com.pipc.dashboard.establishment.response;

import java.util.List;

import com.pipc.dashboard.establishment.repository.AgendaSecEntityGATB;
import com.pipc.dashboard.establishment.repository.AgendaSecEntityGATD;
import com.pipc.dashboard.utility.BaseResponse;

import lombok.Data;

@Data
public class AgendaSecResponse extends BaseResponse{
	 private List<AgendaSecEntityGATB> dataGatB;
	 private List<AgendaSecEntityGATD> dataGatD;
	    private String message;
}
