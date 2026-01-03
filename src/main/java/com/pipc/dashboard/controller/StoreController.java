package com.pipc.dashboard.controller;

import org.slf4j.MDC;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pipc.dashboard.business.StoreBusiness;
import com.pipc.dashboard.store.request.StoreRequest;
import com.pipc.dashboard.store.response.StoreResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/pipc/dashboard/store")
@RequiredArgsConstructor
public class StoreController {

	private final StoreBusiness storeBusiness;

	/*
	 * ========================= SAVE / UPDATE STORE =========================
	 */
	@PostMapping("/saveOrUpdateStore")
	public StoreResponse saveOrUpdateStore(@RequestBody StoreRequest storeRequest) {

		log.info("Save/Update Store | corrId={}", MDC.get("correlationId"));
		return storeBusiness.saveOrUpdateStore(storeRequest);
	}

	/*
	 * ========================= GET STORE DATA =========================
	 */
	@GetMapping("/getStoreData")
	public StoreResponse getAllStores(@RequestParam String year) {

		log.debug("Get Store Data | page={} size={} | corrId={}", year, MDC.get("correlationId"));

		return storeBusiness.getStores(year);
	}

	/*
	 * ========================= DOWNLOAD STORE DATA =========================
	 */
	@GetMapping("/downloadStoreData")
	public ResponseEntity<InputStreamResource> downloadStoreData(@RequestParam String year) throws Exception {

		log.info("Download Store Data | corrId={}", MDC.get("correlationId"));
		return storeBusiness.downloadStoreData(year);
	}
}
