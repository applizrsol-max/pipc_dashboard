package com.pipc.dashboard.business;

import com.pipc.dashboard.establishment.request.MedicalBillRequest;
import com.pipc.dashboard.establishment.response.MedicalBillResponse;

public interface EstablishmentBusiness {

	MedicalBillResponse saveOrUpdateMedicalBill(MedicalBillRequest request);

}
