package com.pipc.dashboard.businessimpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pipc.dashboard.business.EstablishmentBusiness;
import com.pipc.dashboard.establishment.request.AppealRequest;
import com.pipc.dashboard.establishment.request.EmployeePostingRequest;
import com.pipc.dashboard.establishment.request.LeaveRequest;
import com.pipc.dashboard.establishment.request.MedicalBillRequest;
import com.pipc.dashboard.establishment.response.AppealResponse;
import com.pipc.dashboard.establishment.response.EmployeePostingResponse;
import com.pipc.dashboard.establishment.response.LeaveResponse;
import com.pipc.dashboard.establishment.response.MedicalBillResponse;
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
	public AppealResponse saveOrUpdateAppeal(AppealRequest request) {

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

}
