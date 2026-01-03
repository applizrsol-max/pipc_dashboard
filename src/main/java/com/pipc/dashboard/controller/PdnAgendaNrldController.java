package com.pipc.dashboard.controller;

import java.util.List;

import org.slf4j.MDC;
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
import com.pipc.dashboard.pdn.request.IrrigationSaveRequest;
import com.pipc.dashboard.pdn.request.NrldRequest;
import com.pipc.dashboard.pdn.request.PdnAgendaRequest;
import com.pipc.dashboard.pdn.response.NrldResponse;
import com.pipc.dashboard.pdn.response.PdnAgendaResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/pipc/dashboard/pdn")
@RequiredArgsConstructor
public class PdnAgendaNrldController {

	private final PdnAgendaBusiness pdnAgendaBusiness;

	/*
	 * ========================= PDN AGENDA =========================
	 */

	@PostMapping("/saveOrUpdatePdnAgenda")
	public PdnAgendaResponse saveOrUpdatePdnAgenda(@RequestBody PdnAgendaRequest pdnAgendaRequest) {

		log.info("Save/Update PDN Agenda | corrId={}", MDC.get("correlationId"));
		return pdnAgendaBusiness.saveOrUpdatePdnAgenda(pdnAgendaRequest);
	}

	@GetMapping("/getPDNAgendaByProjectYear")
	public List<PdnAgendaEntity> getPDNAgendaByProjectYear(@RequestParam String projectYear) {

		log.debug("Get PDN Agenda | year={} | corrId={}", projectYear, MDC.get("correlationId"));

		return pdnAgendaBusiness.getPDNAgenda(projectYear);
	}

	@GetMapping("/downloadPdnAgendaData")
	public ResponseEntity<InputStreamResource> downloadPdnAgendaData(@RequestParam String year) throws Exception {

		log.info("Download PDN Agenda Excel | corrId={}", MDC.get("correlationId"));
		return pdnAgendaBusiness.downloadPdnAgendaData(year);
	}

	/*
	 * ========================= NRLD =========================
	 */

	@PostMapping("/saveOrUpdateNrld")
	public NrldResponse saveOrUpdateNrld(@RequestBody NrldRequest nrldRequest) {

		log.info("Save/Update NRLD | corrId={}", MDC.get("correlationId"));
		return pdnAgendaBusiness.saveOrUpdateNrld(nrldRequest);
	}

	@GetMapping("/getNrldByYearAndDam")
	public List<NrldEntity> getNrldByYearAndDam(@RequestParam String year) {

		log.debug("Get NRLD | year={} | corrId={}", year, MDC.get("correlationId"));

		return pdnAgendaBusiness.getNrldByYear(year);
	}

	@GetMapping("/downloadNrldData")
	public ResponseEntity<InputStreamResource> downloadNrldExcel(@RequestParam String year) throws Exception {

		log.info("Download NRLD Excel | corrId={}", MDC.get("correlationId"));
		return pdnAgendaBusiness.generateNrldExcel(year);
	}

	/*
	 * ========================= ICC CAPACITY =========================
	 */

	@PostMapping("/saveOrUpdateIccCap")
	public NrldResponse saveOrUpdateIccCap(@RequestBody IrrigationSaveRequest req) {

		log.info("Save/Update ICC Cap | corrId={}", MDC.get("correlationId"));
		return pdnAgendaBusiness.saveOrUpdateIccCap(req);
	}

	@GetMapping("/getIccCapData")
	public NrldResponse getIccCapData(@RequestParam String year, @RequestParam String date) {

		log.debug("Get ICC Cap | year={} | corrId={}", year, MDC.get("correlationId"));
		return pdnAgendaBusiness.getIccCapData(year, date);
	}

	@GetMapping("/downloadIccData")
	public ResponseEntity<InputStreamResource> downloadIccData(@RequestParam String year, @RequestParam String date)
			throws Exception {

		log.info("Download ICC Excel | corrId={}", MDC.get("correlationId"));
		return pdnAgendaBusiness.downloadIccData(year, date);
	}
}
