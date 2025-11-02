package com.pipc.dashboard.businessimpl;

import java.io.IOException;

import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.pipc.dashboard.business.PdnAgendaBusiness;
import com.pipc.dashboard.pdn.repository.NrldEntity;
import com.pipc.dashboard.pdn.repository.PdnAgendaEntity;
import com.pipc.dashboard.pdn.request.NrldRequest;
import com.pipc.dashboard.pdn.request.PdnAgendaRequest;
import com.pipc.dashboard.pdn.response.NrldResponse;
import com.pipc.dashboard.pdn.response.PdnAgendaResponse;
import com.pipc.dashboard.service.PdnAgendaService;

@Component
public class PdnAgnedaBusinessImpl implements PdnAgendaBusiness {

	private final PdnAgendaService pdnAgendaService;

	public PdnAgnedaBusinessImpl(PdnAgendaService pdnAgendaService) {
		this.pdnAgendaService = pdnAgendaService;
	}

	@Override
	public PdnAgendaResponse saveOrUpdatePdnAgenda(PdnAgendaRequest pdnAgendaRequest) {

		return pdnAgendaService.saveOrUpdatePdnAgenda(pdnAgendaRequest);
	}

	@Override
	public NrldResponse saveOrUpdateNrld(NrldRequest nrldRequest) {
		return pdnAgendaService.saveOrUpdateNrld(nrldRequest);
	}

	@Override
	public Page<PdnAgendaEntity> getPDNAgenda(String projectYear, String projectName, int page, int size) {

		return pdnAgendaService.getPDNAgenda(projectYear, projectName, page, size);
	}

	@Override
	public Page<NrldEntity> getNrldByYear(String year, String damName, int page, int size) {
		return pdnAgendaService.getNrldByYear(year, damName, page, size);
	}

	@Override
	public ResponseEntity<InputStreamResource> generateNrldExcel(String year) throws IOException {
		return pdnAgendaService.generateNrldExcel(year);
	}

}
