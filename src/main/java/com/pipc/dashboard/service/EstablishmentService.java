package com.pipc.dashboard.service;

import com.pipc.dashboard.establishment.request.MedicalBillRequest;
import com.pipc.dashboard.establishment.response.MedicalBillResponse;

public interface EstablishmentService {

	MedicalBillResponse saveOrUpdateMedicalBill(MedicalBillRequest request);

}
