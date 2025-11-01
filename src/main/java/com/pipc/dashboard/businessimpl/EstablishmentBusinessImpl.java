package com.pipc.dashboard.businessimpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pipc.dashboard.business.EstablishmentBusiness;
import com.pipc.dashboard.establishment.request.MedicalBillRequest;
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

}
