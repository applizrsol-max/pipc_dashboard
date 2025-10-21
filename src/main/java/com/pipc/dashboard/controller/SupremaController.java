package com.pipc.dashboard.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pipc.dashboard.business.SupremaBusiness;
import com.pipc.dashboard.suprama.repository.SupremaEntity;
import com.pipc.dashboard.suprama.request.SupremaRequest;
import com.pipc.dashboard.suprama.response.SupremaResponse;

@RestController
@RequestMapping("/pipc/dashboard/suprema")

public class SupremaController {

	private Logger log = LoggerFactory.getLogger(LoginController.class);

	private final SupremaBusiness supremaBusiness;

	public SupremaController(SupremaBusiness supremaBusiness) {
		this.supremaBusiness = supremaBusiness;
	}

	@PostMapping("/saveOrUpdateSuprema")
	public SupremaResponse saveOrUpdateSuprema(@RequestBody SupremaRequest supremaRequest) {
		return supremaBusiness.saveOrUpdateSuprema(supremaRequest);
	}
	 @GetMapping("/getSuprema")
	    public Page<SupremaEntity> getSuprema(
	            @RequestParam String projectYear,
	            @RequestParam(defaultValue = "0") int page,
	            @RequestParam(defaultValue = "10") int size) {
	        return supremaBusiness.getSupremaByProjectYear(projectYear, page, size);
	    }
}