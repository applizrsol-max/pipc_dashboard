package com.pipc.dashboard.controller;

import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pipc.dashboard.bhusmapadan.request.PraptraMasterDataRequest;
import com.pipc.dashboard.bhusmapadan.response.PraptraMasterDataResponse;
import com.pipc.dashboard.business.BhusampadanBusiness;
import com.pipc.dashboard.establishment.response.MasterDataResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/pipc/dashboard/bhusampadan")
@RequiredArgsConstructor
public class BhusampadanController {

	private final BhusampadanBusiness bhusampadanBusiness;

	/*
	 * ========================= PRAPATRA MASTER DATA =========================
	 */

	@PostMapping("/savePrapatraMasterData")
	public PraptraMasterDataResponse savePraptraMasterData(@RequestBody PraptraMasterDataRequest request) {

		log.info("Save Prapatra Master Data | corrId={}", MDC.get("correlationId"));
		return bhusampadanBusiness.processPraptraMasterData(request);
	}

	@GetMapping("/getPrapatraMasterData")
	public PraptraMasterDataResponse getPraptraMasterData(@RequestParam String year, @RequestParam String projectName) {

		log.debug("Get Prapatra Master Data | year={} | corrId={}", year, MDC.get("correlationId"));

		return bhusampadanBusiness.getPraptraMasterData(year, projectName);
	}

	/*
	 * ========================= PRAPATRA – 1 =========================
	 */

	@PostMapping("/savePraptra1MasterData")
	public PraptraMasterDataResponse savePraptra1MasterData(@RequestBody PraptraMasterDataRequest request) {

		log.info("Save Prapatra1 Master Data | corrId={}", MDC.get("correlationId"));
		return bhusampadanBusiness.savePraptra1MasterData(request);
	}

	@GetMapping("/getPraptra1MasterData")
	public MasterDataResponse getPraptra1MasterData(@RequestParam String year,
			@RequestParam String projectName) {

		log.debug("Get Prapatra1 Master Data | year={} | corrId={}", year, MDC.get("correlationId"));

		return bhusampadanBusiness.getPraptra1MasterData(year, projectName);
	}

	/*
	 * ========================= PRAPATRA – 2 =========================
	 */

	@PostMapping("/savePraptra2MasterData")
	public PraptraMasterDataResponse savePraptra2MasterData(@RequestBody PraptraMasterDataRequest request) {

		log.info("Save Prapatra2 Master Data | corrId={}", MDC.get("correlationId"));
		return bhusampadanBusiness.savePraptra2MasterData(request);
	}

	@GetMapping("/getPraptra2MasterData")
	public MasterDataResponse getPraptra2MasterData(@RequestParam String year,
			@RequestParam String projectName) {

		log.debug("Get Prapatra2 Master Data | year={} | corrId={}", year, MDC.get("correlationId"));

		return bhusampadanBusiness.getPraptra2MasterData(year, projectName);
	}

	/*
	 * ========================= PRAPATRA – 3 =========================
	 */

	@PostMapping("/savePraptra3MasterData")
	public PraptraMasterDataResponse savePraptra3MasterData(@RequestBody PraptraMasterDataRequest request) {

		log.info("Save Prapatra3 Master Data | corrId={}", MDC.get("correlationId"));
		return bhusampadanBusiness.savePraptra3MasterData(request);
	}

	@GetMapping("/getPraptra3MasterData")
	public MasterDataResponse getPraptra3MasterData(@RequestParam String year,
			@RequestParam String projectName) {

		log.debug("Get Prapatra3 Master Data | year={} | corrId={}", year, MDC.get("correlationId"));

		return bhusampadanBusiness.getPraptra3MasterData(year, projectName);
	}
}
