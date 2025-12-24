package com.pipc.dashboard.businessimpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pipc.dashboard.bhusmapadan.request.PraptraMasterDataRequest;
import com.pipc.dashboard.bhusmapadan.response.PraptraMasterDataResponse;
import com.pipc.dashboard.business.BhusampadanBusiness;
import com.pipc.dashboard.service.BhusampadanService;

@Component
public class BhusampadanBusinessImpl implements BhusampadanBusiness {

	private BhusampadanService bhusampadanService;

	@Autowired
	public BhusampadanBusinessImpl(BhusampadanService bhusampadanService) {
		this.bhusampadanService = bhusampadanService;
	}

	@Override
	public PraptraMasterDataResponse processPraptraMasterData(PraptraMasterDataRequest request) {
		return this.bhusampadanService.processPraptraMasterData(request);
	}

	@Override
	public PraptraMasterDataResponse getPraptraMasterData(String year) {
		return this.bhusampadanService.getPraptraMasterData(year);
	}

	@Override
	public PraptraMasterDataResponse savePraptra1MasterData(PraptraMasterDataRequest request) {
		return this.bhusampadanService.savePraptra1MasterData(request);
	}

	@Override
	public PraptraMasterDataResponse getPraptra1MasterData(String year) {
		return this.bhusampadanService.getPraptra1MasterData(year);
	}

	@Override
	public PraptraMasterDataResponse savePraptra2MasterData(PraptraMasterDataRequest request) {
		return this.bhusampadanService.savePraptra2MasterData(request);
	}

	@Override
	public PraptraMasterDataResponse getPraptra2MasterData(String year) {
		return this.bhusampadanService.getPraptra2MasterData(year);
	}

	@Override
	public PraptraMasterDataResponse savePraptra3MasterData(PraptraMasterDataRequest request) {
		return this.bhusampadanService.savePraptra3MasterData(request);
	}

	@Override
	public PraptraMasterDataResponse getPraptra3MasterData(String year) {
		return this.bhusampadanService.getPraptra3MasterData(year);
	}
}
