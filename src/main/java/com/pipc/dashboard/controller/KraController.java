package com.pipc.dashboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pipc.dashboard.business.KraBusiness;
import com.pipc.dashboard.pdn.request.KraRequest;
import com.pipc.dashboard.pdn.response.KraResponse;

@RestController
@RequestMapping("/pipc/dashboard/kra")
public class KraController {

	@Autowired
	private KraBusiness kraBusiness;

	@PostMapping("/saveOrUpdateKra")
	public KraResponse saveOrUpdateKra(@RequestBody KraRequest request) {
		return kraBusiness.saveOrUpdateKra(request);
	}

	@GetMapping("/getKraByYear")
	public KraResponse getKraByYear(@RequestParam String kraPeriod, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return kraBusiness.getKraByPeriod(kraPeriod, page, size);
	}
}