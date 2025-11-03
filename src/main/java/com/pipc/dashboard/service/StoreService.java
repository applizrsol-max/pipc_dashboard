package com.pipc.dashboard.service;

import java.io.IOException;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;

import com.pipc.dashboard.store.request.StoreRequest;
import com.pipc.dashboard.store.response.StoreResponse;

public interface StoreService {

	StoreResponse saveOrUpdate(StoreRequest storeRequest, String createdBy);

	StoreResponse getStores(int page, int size);

	ResponseEntity<InputStreamResource> downloadStoreData() throws IOException;

}
