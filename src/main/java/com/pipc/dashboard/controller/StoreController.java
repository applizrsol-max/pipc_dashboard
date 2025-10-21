package com.pipc.dashboard.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pipc.dashboard.business.StoreBusiness;
import com.pipc.dashboard.store.repository.StoreEntity;
import com.pipc.dashboard.store.request.StoreRequest;
import com.pipc.dashboard.store.response.StoreResponse;

@RestController
@RequestMapping("/pipc/dashboard/store")

public class StoreController {

	private Logger log = LoggerFactory.getLogger(LoginController.class);

	private final StoreBusiness storeBusiness;

	public StoreController(StoreBusiness storeBusiness) {
		this.storeBusiness = storeBusiness;
	}

	@PostMapping("/saveOrUpdateStore")
	public StoreResponse saveOrUpdateStore(@RequestBody StoreRequest storeRequest) {
		return storeBusiness.saveOrUpdateStore(storeRequest);
	}

	@GetMapping("/getStoreData")
	public Page<StoreEntity> getAllStores(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {

		return storeBusiness.getStores(page,size);
	}

}
