package com.pipc.dashboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pipc.dashboard.bhusmapadan.request.PraptraMasterDataRequest;
import com.pipc.dashboard.bhusmapadan.response.PraptraMasterDataResponse;
import com.pipc.dashboard.business.BhusampadanBusiness;

@RestController
@RequestMapping("/pipc/dashboard/bhusampadan")
public class BhusampadanController {

	private BhusampadanBusiness bhusampadanBusiness;

	@Autowired
	public BhusampadanController(BhusampadanBusiness bhusampadanBusiness) {
		this.bhusampadanBusiness = bhusampadanBusiness;
	}

	@PostMapping("/savePrapatraMasterData")
	public PraptraMasterDataResponse savePraptraMasterData(@RequestBody PraptraMasterDataRequest request) {

		return bhusampadanBusiness.processPraptraMasterData(request);
	}

	@GetMapping("/getPrapatraMasterData")
	public PraptraMasterDataResponse getPraptraMasterData(@RequestParam String year) {

		return bhusampadanBusiness.getPraptraMasterData(year);
	}

	@PostMapping("/savePraptra1MasterData")
	public PraptraMasterDataResponse savePraptra1MasterData(@RequestBody PraptraMasterDataRequest request) {

		return bhusampadanBusiness.savePraptra1MasterData(request);
	}

	@GetMapping("/getPraptra1MasterData")
	public PraptraMasterDataResponse getPraptra1MasterData(@RequestParam String year) {

		return bhusampadanBusiness.getPraptra1MasterData(year);
	}

	@PostMapping("/savePraptra2MasterData")
	public PraptraMasterDataResponse savePraptra2MasterData(@RequestBody PraptraMasterDataRequest request) {

		return bhusampadanBusiness.savePraptra2MasterData(request);
	}

	@GetMapping("/getPraptra2MasterData")
	public PraptraMasterDataResponse getPraptra2MasterData(@RequestParam String year) {

		return bhusampadanBusiness.getPraptra2MasterData(year);
	}

	@PostMapping("/savePraptra3MasterData")
	public PraptraMasterDataResponse savePraptra3MasterData(@RequestBody PraptraMasterDataRequest request) {

		return bhusampadanBusiness.savePraptra3MasterData(request);
	}

	// 🔹 GET (YEAR ONLY)
	@GetMapping("/getPraptra3MasterData")
	public PraptraMasterDataResponse getPraptra3MasterData(@RequestParam String year) {

		return bhusampadanBusiness.getPraptra3MasterData(year);
	}
}
