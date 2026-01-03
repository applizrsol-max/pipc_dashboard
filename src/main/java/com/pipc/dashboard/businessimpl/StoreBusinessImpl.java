package com.pipc.dashboard.businessimpl;

import java.util.List;

import org.slf4j.MDC;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.pipc.dashboard.business.StoreBusiness;
import com.pipc.dashboard.service.StoreService;
import com.pipc.dashboard.store.request.StoreRequest;
import com.pipc.dashboard.store.response.StoreResponse;
import com.pipc.dashboard.utility.ApplicationError;

@Component
public class StoreBusinessImpl implements StoreBusiness {

	private final StoreService storeService;

	public StoreBusinessImpl(StoreService storeService) {
		this.storeService = storeService;
	}

	@Override
	public StoreResponse saveOrUpdateStore(StoreRequest storeRequest) {
		StoreResponse storeResponse = new StoreResponse();
		try {
			// ✅ Get username from MDC (already set in interceptor)
			String createdBy = MDC.get("user");

			// ✅ Delegate to service (which handles error & success messaging)
			storeResponse = storeService.saveOrUpdate(storeRequest, createdBy);

		} catch (Exception e) {
			// ✅ Fallback error in case service layer throws any unexpected exception
			ApplicationError error = new ApplicationError();
			error.setErrorCode("1");
			error.setErrorDescription(e.getMessage());

			storeResponse.setErrorDetails(error);
			storeResponse.setMessage("Error while saving or updating report");

		}
		return storeResponse;
	}

	@Override
	public StoreResponse getStores(String year) {
		return storeService.getStores(year);
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadStoreData(String year) throws Exception {
		return storeService.downloadStoreData(year);
	}
}
