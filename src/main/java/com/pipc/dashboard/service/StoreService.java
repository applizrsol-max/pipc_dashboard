package com.pipc.dashboard.service;

import org.springframework.data.domain.Page;

import com.pipc.dashboard.store.repository.StoreEntity;
import com.pipc.dashboard.store.request.StoreRequest;
import com.pipc.dashboard.store.response.StoreResponse;

public interface StoreService {

	StoreResponse saveOrUpdate(StoreRequest storeRequest, String createdBy);

	StoreResponse getStores(int page, int size);

}
