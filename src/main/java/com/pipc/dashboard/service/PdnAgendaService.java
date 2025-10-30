package com.pipc.dashboard.service;

import org.springframework.data.domain.Page;

import com.pipc.dashboard.pdn.repository.NrldEntity;
import com.pipc.dashboard.pdn.repository.PdnAgendaEntity;
import com.pipc.dashboard.pdn.request.NrldRequest;
import com.pipc.dashboard.pdn.request.PdnAgendaRequest;
import com.pipc.dashboard.pdn.response.NrldResponse;
import com.pipc.dashboard.pdn.response.PdnAgendaResponse;

public interface PdnAgendaService {

	PdnAgendaResponse saveOrUpdatePdnAgenda(PdnAgendaRequest pdnAgendaRequest);

	NrldResponse saveOrUpdateNrld(NrldRequest nrldRequest);

	Page<PdnAgendaEntity> getPDNAgenda(String projectYear, String projectName, int page, int size);

	Page<NrldEntity> getNrldByYear(String year, String damName, int page, int size);

}
