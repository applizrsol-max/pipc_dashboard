package com.pipc.dashboard.business;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.pipc.dashboard.store.repository.StoreEntity;
import com.pipc.dashboard.store.request.StoreRequest;
import com.pipc.dashboard.store.response.StoreResponse;

public interface StoreBusiness {

	StoreResponse saveOrUpdateStore(StoreRequest storeRequest);

	Page<StoreEntity> getStores(int page, int size);

	

}
