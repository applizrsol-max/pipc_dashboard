package com.pipc.dashboard.controller;

import java.io.ByteArrayInputStream;

import org.slf4j.MDC;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pipc.dashboard.business.KraBusiness;
import com.pipc.dashboard.pdn.request.KraRequest;
import com.pipc.dashboard.pdn.response.KraResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/pipc/dashboard/kra")
@RequiredArgsConstructor
public class KraController {

	private final KraBusiness kraBusiness;

	/*
	 * ========================= SAVE / UPDATE KRA =========================
	 */
	@PostMapping("/saveOrUpdateKra")
	public KraResponse saveOrUpdateKra(@RequestBody KraRequest request) {

		log.info("Save/Update KRA | corrId={}", MDC.get("correlationId"));
		return kraBusiness.saveOrUpdateKra(request);
	}

	/*
	 * ========================= GET KRA BY PERIOD =========================
	 */
	@GetMapping("/getKraByYear")
	public KraResponse getKraByYear(@RequestParam String kraPeriod) {

		log.debug("Get KRA | period={} | corrId={}", kraPeriod, MDC.get("correlationId"));
		return kraBusiness.getKraByPeriod(kraPeriod);
	}

	/*
	 * ========================= DOWNLOAD KRA EXCEL =========================
	 */
	@GetMapping("/downloadKraData")
	public ResponseEntity<InputStreamResource> downloadKraExcel(@RequestParam String kraPeriod) throws Exception {

		log.info("Download KRA Excel | period={} | corrId={}", kraPeriod, MDC.get("correlationId"));

		ByteArrayInputStream in = kraBusiness.generateKraExcel(kraPeriod);

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=KRA_Report_" + kraPeriod + ".xlsx");

		return ResponseEntity.ok().headers(headers)
				.contentType(
						MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(new InputStreamResource(in));
	}
}
