package com.pipc.dashboard.service;

import com.pipc.dashboard.pdn.request.KraRequest;
import com.pipc.dashboard.pdn.response.KraResponse;

public interface KraService {

	KraResponse saveOrUpdateKra(KraRequest request);

	KraResponse getKraByPeriod(String kraPeriod, int page, int size);

}
