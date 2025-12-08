package com.pipc.dashboard.accounts.response;

import java.util.List;

import com.pipc.dashboard.accounts.repository.PendingParaEntity;
import com.pipc.dashboard.utility.BaseResponse;

import lombok.Data;

@Data
public class PendingParaResponse extends BaseResponse {
	private List<PendingParaEntity> data;

}
