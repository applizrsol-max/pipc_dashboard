package com.pipc.dashboard.service;

import com.pipc.dashboard.store.request.StoreRequest;
import com.pipc.dashboard.store.response.StoreResponse;

public interface StoreService {

	StoreResponse saveOrUpdate(StoreRequest storeRequest, String createdBy);

}
