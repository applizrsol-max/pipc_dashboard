package com.pipc.dashboard.business;

import com.pipc.dashboard.bhusmapadan.request.PraptraMasterDataRequest;
import com.pipc.dashboard.bhusmapadan.response.PraptraMasterDataResponse;
import com.pipc.dashboard.establishment.response.MasterDataResponse;

public interface BhusampadanBusiness {

	PraptraMasterDataResponse processPraptraMasterData(PraptraMasterDataRequest request);

	PraptraMasterDataResponse getPraptraMasterData(String year);

	PraptraMasterDataResponse savePraptra1MasterData(PraptraMasterDataRequest request);

	MasterDataResponse getPraptra1MasterData(String year, String projectName);

	PraptraMasterDataResponse savePraptra2MasterData(PraptraMasterDataRequest request);

	MasterDataResponse getPraptra2MasterData(String year, String projectName);

	PraptraMasterDataResponse savePraptra3MasterData(PraptraMasterDataRequest request);

	MasterDataResponse getPraptra3MasterData(String year, String projectName);

}
