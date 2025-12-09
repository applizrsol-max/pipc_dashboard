package com.pipc.dashboard.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pipc.dashboard.business.EstablishmentBusiness;
import com.pipc.dashboard.establishment.request.AgendaRequest;
import com.pipc.dashboard.establishment.request.AppealWrapper;
import com.pipc.dashboard.establishment.request.EmployeePostingRequest;
import com.pipc.dashboard.establishment.request.IncomeTaxDeductionRequest;
import com.pipc.dashboard.establishment.request.LeaveRequest;
import com.pipc.dashboard.establishment.request.MedicalBillRequest;
import com.pipc.dashboard.establishment.request.PassportNocRequest;
import com.pipc.dashboard.establishment.response.AgendaResponse;
import com.pipc.dashboard.establishment.response.AppealResponse;
import com.pipc.dashboard.establishment.response.EmployeePostingResponse;
import com.pipc.dashboard.establishment.response.IncomeTaxDeductionResponse;
import com.pipc.dashboard.establishment.response.LeaveResponse;
import com.pipc.dashboard.establishment.response.MedicalBillResponse;
import com.pipc.dashboard.establishment.response.PassportNocResponse;

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

	@PostMapping("/saveOrUpdateAppeal")
	public AppealResponse saveOrUpdateAppeal(@RequestBody AppealWrapper request) {
		return establishmentBusiness.saveOrUpdateAppeal(request);
	}

	@GetMapping("/getAppealData")
	public ResponseEntity<AppealResponse> getAppealData(@RequestParam(required = false) String year,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

		AppealResponse response = establishmentBusiness.getAppealData(year, page, size);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/saveOrUpdateEmployeePosting")
	public EmployeePostingResponse saveOrUpdateEmployeePosting(@RequestBody EmployeePostingRequest request) {
		return establishmentBusiness.saveOrUpdateEmployeePosting(request);
	}

	@GetMapping("/getEmployeePostingData")
	public ResponseEntity<EmployeePostingResponse> getEmployeePostingData(
			@RequestParam(required = false) String adhikariKarmacharyacheNav,
			@RequestParam(required = false) String year, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {

		EmployeePostingResponse response = establishmentBusiness.getEmployeePostingData(adhikariKarmacharyacheNav, year,
				page, size);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/saveOrUpdateIncomeTaxDeduc")
	public IncomeTaxDeductionResponse saveOrUpdateIncomeTaxDeduc(@RequestBody IncomeTaxDeductionRequest request) {
		return establishmentBusiness.saveOrUpdateIncomeTaxDeduc(request);
	}

	@GetMapping("/getIncomeTaxDeductionData")
	public ResponseEntity<Page<IncomeTaxDeductionResponse>> getIncomeTaxDeductionData(@RequestParam String year,
			@RequestParam(required = false, defaultValue = "") String month, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {

		Page<IncomeTaxDeductionResponse> data = establishmentBusiness.getIncomeTaxDeductionData(year, month, page,
				size);
		return ResponseEntity.ok(data);
	}

	@PostMapping("/saveOrUpdatePassportNoc")
	public PassportNocResponse saveOrUpdatePassportNoc(@RequestBody PassportNocRequest request) {
		return establishmentBusiness.saveOrUpdatePassportNoc(request);
	}

	@GetMapping("/getPassportNocData")
	public Page<PassportNocResponse> getPassportNocData(@RequestParam(required = false) String year,
			@RequestParam(required = false) String month, @RequestParam(required = false) String employeeName,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		return establishmentBusiness.getPassportNocData(year, month, employeeName, page, size);
	}

	@GetMapping("/downloadMedicalBill")
	public ResponseEntity<InputStreamResource> downloadMedicalBill(@RequestParam String employeeName,
			@RequestParam String date) throws Exception {

		return establishmentBusiness.downloadMedicalBill(employeeName, date);
	}

	@GetMapping("/downloadAppealArj")
	public ResponseEntity<InputStreamResource> downloadAppealArj(@RequestParam(required = false) String year)
			throws IOException {

		return establishmentBusiness.downloadAppealArj(year);
	}

	@GetMapping("/downloadLeaveDetails")
	public ResponseEntity<InputStreamResource> downloadLeaveDetails(@RequestParam String employeeName,
			@RequestParam String date) throws Exception {

		return establishmentBusiness.downloadLeaveDetails(employeeName, date);
	}

	@PostMapping("/saveOrUpdateAgenda")
	public AgendaResponse saveOrUpdateAgenda(@RequestBody AgendaRequest request) {
		return establishmentBusiness.saveOrUpdateAgenda(request);
	}

	@GetMapping("/getAgendaByYearAndTargetDate")
	public AgendaResponse getAgendaByYearAndTargetDate(@RequestParam String year, @RequestParam String targetDate) {

		return establishmentBusiness.getAgendaByYearAndTargetDate(year, targetDate);
	}

	@GetMapping("/downloadAgendaExcel")
	public ResponseEntity<InputStreamResource> downloadAgendaExcel(@RequestParam String year,
			@RequestParam String targetDate) throws Exception {

		return establishmentBusiness.downloadAgendaExcel(year, targetDate);
	}
	
//	 @PostMapping("/saveOrUpdateAgendaSec")
//	    public AgendaSecResponse save(@RequestBody AgendaSecRequest request) {
//	        return service.saveOrUpdateAgendaSec(request);
//	    }

}
