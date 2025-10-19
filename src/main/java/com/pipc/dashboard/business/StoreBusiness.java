package com.pipc.dashboard.business;

import com.pipc.dashboard.store.request.StoreRequest;
import com.pipc.dashboard.store.response.StoreResponse;

public interface StoreBusiness {

	StoreResponse saveOrUpdateStore(StoreRequest storeRequest);

}
