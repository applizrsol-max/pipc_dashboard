package com.pipc.dashboard.business;

import com.pipc.dashboard.bhusmapadan.request.PraptraMasterDataRequest;
import com.pipc.dashboard.bhusmapadan.response.PraptraMasterDataResponse;

public interface BhusampadanBusiness {

	PraptraMasterDataResponse processPraptraMasterData(PraptraMasterDataRequest request);

	PraptraMasterDataResponse getPraptraMasterData(String year);

	PraptraMasterDataResponse savePraptra1MasterData(PraptraMasterDataRequest request);

	PraptraMasterDataResponse getPraptra1MasterData(String year);

	PraptraMasterDataResponse savePraptra2MasterData(PraptraMasterDataRequest request);

	PraptraMasterDataResponse getPraptra2MasterData(String year);

	PraptraMasterDataResponse savePraptra3MasterData(PraptraMasterDataRequest request);

	PraptraMasterDataResponse getPraptra3MasterData(String year);

}
