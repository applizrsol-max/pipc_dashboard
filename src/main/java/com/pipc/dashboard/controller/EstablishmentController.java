package com.pipc.dashboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pipc.dashboard.business.EstablishmentBusiness;
import com.pipc.dashboard.establishment.request.MedicalBillRequest;
import com.pipc.dashboard.establishment.response.MedicalBillResponse;

@RestController
@RequestMapping("/pipc/dashboard/establishment")
public class EstablishmentController {

	@Autowired
	private EstablishmentBusiness establishmentBusiness;

	@PostMapping("/saveOrUpdateMedicalBill")
	public MedicalBillResponse saveOrUpdateMedicalBill(@RequestBody MedicalBillRequest request) {
		return establishmentBusiness.saveOrUpdateMedicalBill(request);
	}
}
