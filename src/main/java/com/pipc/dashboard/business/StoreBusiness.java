package com.pipc.dashboard.business;

import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;

import com.pipc.dashboard.store.request.StoreRequest;
import com.pipc.dashboard.store.response.StoreResponse;

public interface StoreBusiness {

	StoreResponse saveOrUpdateStore(StoreRequest storeRequest);

	ResponseEntity<InputStreamResource> downloadStoreData(String year) throws Exception;

	StoreResponse getStores(String year);

}
