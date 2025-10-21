//package com.pipc.dashboard.controller;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.pipc.dashboard.business.StoreBusiness;
//import com.pipc.dashboard.business.SupremaBusiness;
//import com.pipc.dashboard.store.response.StoreResponse;
//import com.pipc.dashboard.suprama.request.SupremaRequest;
//
//@RestController
//@RequestMapping("/pipc/dashboard/suprema")
//
//public class SupremaController {
//
//	private Logger log = LoggerFactory.getLogger(LoginController.class);
//
//	private final SupremaBusiness supremaBusiness;
//
//	public SupremaController(StoreBusiness supremaBusiness) {
//		this.supremaBusiness = supremaBusiness;
//	}
//
//	@PostMapping("/saveOrUpdateSuprema")
//	public StoreResponse saveOrUpdateSuprema(@RequestBody SupremaRequest supremaRequest) {
//		return supremaBusiness.saveOrUpdateStore(supremaRequest);
//	}
//
//}