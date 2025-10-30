package com.pipc.dashboard.businessimpl;

import org.springframework.stereotype.Component;

import com.pipc.dashboard.business.KraBusiness;
import com.pipc.dashboard.pdn.request.KraRequest;
import com.pipc.dashboard.pdn.response.KraResponse;
import com.pipc.dashboard.service.KraService;

@Component
public class KraBusinessImpl implements KraBusiness {

	private final KraService kraService;

	public KraBusinessImpl(KraService kraService) {
		this.kraService = kraService;
	}

	@Override
	public KraResponse saveOrUpdateKra(KraRequest request) {
		return kraService.saveOrUpdateKra(request);
	}

	@Override
	public KraResponse getKraByPeriod(String kraPeriod, int page, int size) {
		return kraService.getKraByPeriod(kraPeriod, page, size);
	}

}
