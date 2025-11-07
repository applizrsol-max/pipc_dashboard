package com.pipc.dashboard.businessimpl;

import java.io.IOException;

import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.pipc.dashboard.business.SupremaBusiness;
import com.pipc.dashboard.service.SupremaService;
import com.pipc.dashboard.suprama.repository.SupremaEntity;
import com.pipc.dashboard.suprama.request.SupremaRequest;
import com.pipc.dashboard.suprama.response.SupremaResponse;

@Component
public class SupremaBusinessImpl implements SupremaBusiness {

	private final SupremaService supremaService;

	public SupremaBusinessImpl(SupremaService supremaService) {
		this.supremaService = supremaService;
	}

	@Override
	public SupremaResponse saveOrUpdateSuprema(SupremaRequest supremaRequest) {
		return supremaService.saveOrUpdateSuprema(supremaRequest);
	}

	@Override
	public Page<SupremaEntity> getSupremaByProjectYear(String projectYear, int page, int size) {
		return supremaService.getSupremaByProjectYear(projectYear, page, size);
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadSupremaExcel(String year) throws IOException {
		return supremaService.downloadSupremaExcel(year);
	}

}
