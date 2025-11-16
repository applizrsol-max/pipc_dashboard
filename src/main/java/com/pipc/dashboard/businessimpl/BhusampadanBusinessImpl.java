package com.pipc.dashboard.businessimpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pipc.dashboard.business.BhusampadanBusiness;
import com.pipc.dashboard.service.BhusampadanService;

@Component
public class BhusampadanBusinessImpl implements BhusampadanBusiness {

	private BhusampadanService bhusampadanService;

	@Autowired 
	public BhusampadanBusinessImpl(BhusampadanService bhusampadanService) {
		this.bhusampadanService = bhusampadanService;
	}
}
