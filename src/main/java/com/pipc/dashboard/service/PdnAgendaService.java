package com.pipc.dashboard.service;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import com.pipc.dashboard.pdn.repository.NrldEntity;
import com.pipc.dashboard.pdn.repository.PdnAgendaEntity;
import com.pipc.dashboard.pdn.request.IrrigationSaveRequest;
import com.pipc.dashboard.pdn.request.NrldRequest;
import com.pipc.dashboard.pdn.request.PdnAgendaRequest;
import com.pipc.dashboard.pdn.response.NrldResponse;
import com.pipc.dashboard.pdn.response.PdnAgendaResponse;

public interface PdnAgendaService {

	PdnAgendaResponse saveOrUpdatePdnAgenda(PdnAgendaRequest pdnAgendaRequest);

	NrldResponse saveOrUpdateNrld(NrldRequest nrldRequest);

	List<PdnAgendaEntity> getPDNAgenda(String projectYear);

	List<NrldEntity> getNrldByYear(String year);

	ResponseEntity<InputStreamResource> generateNrldExcel(String year) throws IOException;

	ResponseEntity<InputStreamResource> downloadPdnAgendaData(String year) throws IOException;

	NrldResponse saveOrUpdateIccCap(IrrigationSaveRequest req);

	NrldResponse getIccCapData(String year, String date);

	ResponseEntity<InputStreamResource> downloadIccData(String year, String date) throws Exception;

}
