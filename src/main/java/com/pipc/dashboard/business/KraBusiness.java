package com.pipc.dashboard.business;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.pipc.dashboard.pdn.request.KraRequest;
import com.pipc.dashboard.pdn.response.KraResponse;

public interface KraBusiness {

	KraResponse saveOrUpdateKra(KraRequest request);

	KraResponse getKraByPeriod(String kraPeriod);

	ByteArrayInputStream generateKraExcel(String kraPeriod) throws IOException;

}
