package com.pipc.dashboard.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pipc.dashboard.business.PdnAgendaBusiness;
import com.pipc.dashboard.pdn.repository.NrldEntity;
import com.pipc.dashboard.pdn.repository.PdnAgendaEntity;
import com.pipc.dashboard.pdn.request.NrldRequest;
import com.pipc.dashboard.pdn.request.PdnAgendaRequest;
import com.pipc.dashboard.pdn.response.NrldResponse;
import com.pipc.dashboard.pdn.response.PdnAgendaResponse;

@RestController
@RequestMapping("/pipc/dashboard/pdn")
public class PdnAgendaNrldController {
	private Logger log = LoggerFactory.getLogger(LoginController.class);

	private final PdnAgendaBusiness pdnAgendaBusiness;

	public PdnAgendaNrldController(PdnAgendaBusiness pdnAgendaBusiness) {
		this.pdnAgendaBusiness = pdnAgendaBusiness;
	}

	@PostMapping("/saveOrUpdatePdnAgenda")
	public PdnAgendaResponse saveOrUpdatePdnAgenda(@RequestBody PdnAgendaRequest pdnAgendaRequest) {
		return pdnAgendaBusiness.saveOrUpdatePdnAgenda(pdnAgendaRequest);
	}

	@PostMapping("/saveOrUpdateNrld")
	public NrldResponse saveOrUpdateNrld(@RequestBody NrldRequest nrldRequest) {
		return pdnAgendaBusiness.saveOrUpdateNrld(nrldRequest);
	}

	@GetMapping("/getPDNAgendaByProjectYear")
	public Page<PdnAgendaEntity> getPDNAgendaByProjectYear(@RequestParam String projectYear,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) String projectName) {

		return pdnAgendaBusiness.getPDNAgenda(projectYear, projectName, page, size);
	}

	@GetMapping("/getNrldByYearAndDam")
	public Page<NrldEntity> getNrldByYearAndDam(@RequestParam String year, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(required = false) String damName) {

		return pdnAgendaBusiness.getNrldByYear(year, damName, page, size);
	}

	@GetMapping("/downloadNrldData")
	public ResponseEntity<InputStreamResource> downloadNrldExcel(@RequestParam String year) throws Exception {
		return pdnAgendaBusiness.generateNrldExcel(year);
	}
}
