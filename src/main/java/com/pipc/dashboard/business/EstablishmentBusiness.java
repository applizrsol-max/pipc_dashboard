package com.pipc.dashboard.business;

import com.pipc.dashboard.establishment.request.AppealRequest;
import com.pipc.dashboard.establishment.request.EmployeePostingRequest;
import com.pipc.dashboard.establishment.request.LeaveRequest;
import com.pipc.dashboard.establishment.request.MedicalBillRequest;
import com.pipc.dashboard.establishment.response.AppealResponse;
import com.pipc.dashboard.establishment.response.EmployeePostingResponse;
import com.pipc.dashboard.establishment.response.LeaveResponse;
import com.pipc.dashboard.establishment.response.MedicalBillResponse;

public interface EstablishmentBusiness {

	MedicalBillResponse saveOrUpdateMedicalBill(MedicalBillRequest request);

	MedicalBillResponse getMedicalBills(String employeeName, String month, String year, String period, String date);

	LeaveResponse saveOrUpdateLeave(LeaveRequest request);

	LeaveResponse getLeaveDetails(String employeeName, String year, String month, String date);

	AppealResponse saveOrUpdateAppeal(AppealRequest request);

	AppealResponse getAppealData(String year, int page, int size);

	EmployeePostingResponse saveOrUpdateEmployeePosting(EmployeePostingRequest request);

	EmployeePostingResponse getEmployeePostingData(String adhikariKarmacharyacheNav, String year, int page, int size);

}
