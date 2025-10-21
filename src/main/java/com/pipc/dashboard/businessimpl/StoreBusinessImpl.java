package com.pipc.dashboard.businessimpl;

import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.pipc.dashboard.business.StoreBusiness;
import com.pipc.dashboard.service.StoreService;
import com.pipc.dashboard.store.repository.StoreEntity;
import com.pipc.dashboard.store.request.StoreRequest;
import com.pipc.dashboard.store.response.StoreResponse;
import com.pipc.dashboard.utility.ApplicationError;

@Service
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
	public Page<StoreEntity> getStores(int page, int size) {
		return storeService.getStores(page, size);
	}
}
