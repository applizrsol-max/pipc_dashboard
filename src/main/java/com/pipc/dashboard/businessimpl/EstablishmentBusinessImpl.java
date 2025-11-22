package com.pipc.dashboard.businessimpl;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.pipc.dashboard.business.EstablishmentBusiness;
import com.pipc.dashboard.establishment.request.AppealWrapper;
import com.pipc.dashboard.establishment.request.EmployeePostingRequest;
import com.pipc.dashboard.establishment.request.IncomeTaxDeductionRequest;
import com.pipc.dashboard.establishment.request.LeaveRequest;
import com.pipc.dashboard.establishment.request.MedicalBillRequest;
import com.pipc.dashboard.establishment.request.PassportNocRequest;
import com.pipc.dashboard.establishment.response.AppealResponse;
import com.pipc.dashboard.establishment.response.EmployeePostingResponse;
import com.pipc.dashboard.establishment.response.IncomeTaxDeductionResponse;
import com.pipc.dashboard.establishment.response.LeaveResponse;
import com.pipc.dashboard.establishment.response.MedicalBillResponse;
import com.pipc.dashboard.establishment.response.PassportNocResponse;
import com.pipc.dashboard.service.EstablishmentService;

@Component
public class EstablishmentBusinessImpl implements EstablishmentBusiness {

	@Autowired
	private EstablishmentService establishmentService;

	@Override
	public MedicalBillResponse saveOrUpdateMedicalBill(MedicalBillRequest request) {

		return establishmentService.saveOrUpdateMedicalBill(request);
	}

	@Override
	public MedicalBillResponse getMedicalBills(String employeeName, String month, String year, String period,
			String date) {
		return establishmentService.getMedicalBills(employeeName, month, year, period, date);
	}

	@Override
	public LeaveResponse saveOrUpdateLeave(LeaveRequest request) {
		return establishmentService.saveOrUpdateLeave(request);
	}

	@Override
	public LeaveResponse getLeaveDetails(String employeeName, String year, String month, String date) {

		return establishmentService.getLeaveDetails(employeeName, year, month, date);
	}

	@Override
	public AppealResponse saveOrUpdateAppeal(AppealWrapper request) {

		return establishmentService.saveOrUpdateAppeal(request);
	}

	@Override
	public AppealResponse getAppealData(String year, int page, int size) {
		return establishmentService.getAppealData(year, page, size);
	}

	@Override
	public EmployeePostingResponse saveOrUpdateEmployeePosting(EmployeePostingRequest request) {
		return establishmentService.saveOrUpdateEmployeePosting(request);
	}

	@Override
	public EmployeePostingResponse getEmployeePostingData(String adhikariKarmacharyacheNav, String year, int page,
			int size) {
		return establishmentService.getEmployeePostingData(adhikariKarmacharyacheNav, year, page, size);
	}

	@Override
	public IncomeTaxDeductionResponse saveOrUpdateIncomeTaxDeduc(IncomeTaxDeductionRequest request) {
		return establishmentService.saveOrUpdateIncomeTaxDeduc(request);
	}

	@Override
	public Page<IncomeTaxDeductionResponse> getIncomeTaxDeductionData(String year, String month, int page, int size) {
		return establishmentService.getIncomeTaxDeductionData(year, month, page, size);
	}

	@Override
	public PassportNocResponse saveOrUpdatePassportNoc(PassportNocRequest request) {
		return establishmentService.saveOrUpdatePassportNoc(request);
	}

	@Override
	public Page<PassportNocResponse> getPassportNocData(String year, String month, String employeeName, int page,
			int size) {
		return establishmentService.getPassportNocData(year, month, employeeName, page, size);
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadAppealArj(String year) throws IOException {
		return establishmentService.downloadAppealArj(year);
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadMedicalBill(String employeeName, String date) throws Exception {
		return establishmentService.downloadMedicalBill(employeeName, date);
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadLeaveDetails(String employeeName, String date)
			throws Exception {
		return establishmentService.downloadLeaveDetails(employeeName, date);
	}

}
