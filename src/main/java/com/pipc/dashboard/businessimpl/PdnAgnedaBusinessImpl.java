package com.pipc.dashboard.businessimpl;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.pipc.dashboard.business.PdnAgendaBusiness;
import com.pipc.dashboard.pdn.repository.NrldEntity;
import com.pipc.dashboard.pdn.repository.PdnAgendaEntity;
import com.pipc.dashboard.pdn.request.IrrigationSaveRequest;
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
	public List<PdnAgendaEntity> getPDNAgenda(String projectYear) {

		return pdnAgendaService.getPDNAgenda(projectYear);
	}

	@Override
	public List<NrldEntity> getNrldByYear(String year) {
		return pdnAgendaService.getNrldByYear(year);
	}

	@Override
	public ResponseEntity<InputStreamResource> generateNrldExcel(String year) throws IOException {
		return pdnAgendaService.generateNrldExcel(year);
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadPdnAgendaData(String year) throws IOException {
		return pdnAgendaService.downloadPdnAgendaData(year);
	}

	@Override
	public NrldResponse saveOrUpdateIccCap(IrrigationSaveRequest req) {
		return pdnAgendaService.saveOrUpdateIccCap(req);
	}

	@Override
	public NrldResponse getIccCapData(String year, String date) {
		return pdnAgendaService.getIccCapData(year, date);
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadIccData(String year, String date) throws Exception {
		return pdnAgendaService.downloadIccData(year, date);
	}

}
