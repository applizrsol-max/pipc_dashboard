package com.pipc.dashboard.businessimpl;

import org.springframework.stereotype.Component;

import com.pipc.dashboard.bhusmapadan.request.PraptraMasterDataRequest;
import com.pipc.dashboard.bhusmapadan.response.PraptraMasterDataResponse;
import com.pipc.dashboard.business.BhusampadanBusiness;
import com.pipc.dashboard.establishment.response.MasterDataResponse;
import com.pipc.dashboard.service.BhusampadanService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class BhusampadanBusinessImpl implements BhusampadanBusiness {

	private final BhusampadanService bhusampadanService;

	@Override
	public PraptraMasterDataResponse processPraptraMasterData(PraptraMasterDataRequest request) {
		return this.bhusampadanService.processPraptraMasterData(request);
	}

	@Override
	public PraptraMasterDataResponse getPraptraMasterData(String year, String projectName) {
		return this.bhusampadanService.getPraptraMasterData(year, projectName);
	}

	@Override
	public PraptraMasterDataResponse savePraptra1MasterData(PraptraMasterDataRequest request) {
		return this.bhusampadanService.savePraptra1MasterData(request);
	}

	@Override
	public MasterDataResponse getPraptra1MasterData(String year, String projectName) {
		return this.bhusampadanService.getPraptra1MasterData(year, projectName);
	}

	@Override
	public PraptraMasterDataResponse savePraptra2MasterData(PraptraMasterDataRequest request) {
		return this.bhusampadanService.savePraptra2MasterData(request);
	}

	@Override
	public MasterDataResponse getPraptra2MasterData(String year, String projectName) {
		return this.bhusampadanService.getPraptra2MasterData(year, projectName);
	}

	@Override
	public PraptraMasterDataResponse savePraptra3MasterData(PraptraMasterDataRequest request) {
		return this.bhusampadanService.savePraptra3MasterData(request);
	}

	@Override
	public MasterDataResponse getPraptra3MasterData(String year, String projectName) {
		return this.bhusampadanService.getPraptra3MasterData(year, projectName);
	}
}
