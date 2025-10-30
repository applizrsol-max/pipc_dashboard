package com.pipc.dashboard.business;

import com.pipc.dashboard.pdn.request.KraRequest;
import com.pipc.dashboard.pdn.response.KraResponse;

public interface KraBusiness {

	KraResponse saveOrUpdateKra(KraRequest request);

	KraResponse getKraByPeriod(String kraPeriod, int page, int size);

}
