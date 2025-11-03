package com.pipc.dashboard.business;

import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import com.pipc.dashboard.store.repository.StoreEntity;
import com.pipc.dashboard.store.request.StoreRequest;
import com.pipc.dashboard.store.response.StoreResponse;

public interface StoreBusiness {

	StoreResponse saveOrUpdateStore(StoreRequest storeRequest);

	StoreResponse getStores(int page, int size);

	ResponseEntity<InputStreamResource> downloadStoreData() throws Exception;

}
