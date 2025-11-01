package com.pipc.dashboard.business;

import com.pipc.dashboard.establishment.request.LeaveRequest;
import com.pipc.dashboard.establishment.request.MedicalBillRequest;
import com.pipc.dashboard.establishment.response.LeaveResponse;
import com.pipc.dashboard.establishment.response.MedicalBillResponse;

public interface EstablishmentBusiness {

	MedicalBillResponse saveOrUpdateMedicalBill(MedicalBillRequest request);

	MedicalBillResponse getMedicalBills(String employeeName, String month, String year, String period,String date);

	LeaveResponse saveOrUpdateLeave(LeaveRequest request);

	LeaveResponse getLeaveDetails(String employeeName, String year, String month, String date);

}
