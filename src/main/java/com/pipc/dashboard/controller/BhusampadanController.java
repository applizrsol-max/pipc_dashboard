package com.pipc.dashboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pipc.dashboard.business.BhusampadanBusiness;

@RestController
@RequestMapping("/pipc/dashboard/bhusampadan")
public class BhusampadanController {

	private BhusampadanBusiness bhusampadanBusiness;

	@Autowired
	public BhusampadanController(BhusampadanBusiness bhusampadanBusiness) {
		this.bhusampadanBusiness = bhusampadanBusiness;
	}

}
