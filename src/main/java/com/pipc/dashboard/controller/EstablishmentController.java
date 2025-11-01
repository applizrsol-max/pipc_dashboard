package com.pipc.dashboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pipc.dashboard.business.EstablishmentBusiness;
import com.pipc.dashboard.establishment.request.LeaveRequest;
import com.pipc.dashboard.establishment.request.MedicalBillRequest;
import com.pipc.dashboard.establishment.response.LeaveResponse;
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

	@GetMapping("/getMedicalBills")
	public ResponseEntity<MedicalBillResponse> getMedicalBills(@RequestParam(required = false) String employeeName,
			@RequestParam(required = false) String month, @RequestParam(required = false) String year,
			@RequestParam(required = false) String period, String date) {

		return ResponseEntity.ok(establishmentBusiness.getMedicalBills(employeeName, month, year, period, date));
	}

	@PostMapping("/saveOrUpdateLeave")
	public LeaveResponse saveOrUpdateLeave(@RequestBody LeaveRequest request) {
		return establishmentBusiness.saveOrUpdateLeave(request);
	}

	@GetMapping("/getLeaveDetails")
	public ResponseEntity<LeaveResponse> getLeaveDetails(@RequestParam(required = false) String employeeName,
			@RequestParam(required = false) String year, @RequestParam(required = false) String month,
			@RequestParam(required = false) String date) {

		LeaveResponse response = establishmentBusiness.getLeaveDetails(employeeName, year, month, date);
		return ResponseEntity.ok(response);
	}

}
