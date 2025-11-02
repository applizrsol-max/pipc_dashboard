package com.pipc.dashboard.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.pipc.dashboard.pdn.request.KraRequest;
import com.pipc.dashboard.pdn.response.KraResponse;

public interface KraService {

	KraResponse saveOrUpdateKra(KraRequest request);

	KraResponse getKraByPeriod(String kraPeriod, int page, int size);

	ByteArrayInputStream generateKraExcel(String kraPeriod) throws IOException;

}
