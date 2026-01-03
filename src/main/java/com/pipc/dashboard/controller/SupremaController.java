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

import com.pipc.dashboard.business.SupremaBusiness;
import com.pipc.dashboard.suprama.repository.SupremaEntity;
import com.pipc.dashboard.suprama.request.SupremaRequest;
import com.pipc.dashboard.suprama.response.SupremaResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/pipc/dashboard/suprema")
@RequiredArgsConstructor
public class SupremaController {

	private final SupremaBusiness supremaBusiness;

	/*
	 * ========================= SAVE / UPDATE SUPREMA =========================
	 */
	@PostMapping("/saveOrUpdateSuprema")
	public SupremaResponse saveOrUpdateSuprema(@RequestBody SupremaRequest supremaRequest) {

		log.info("Save/Update Suprema | corrId={}", MDC.get("correlationId"));
		return supremaBusiness.saveOrUpdateSuprema(supremaRequest);
	}

	/*
	 * ========================= GET SUPREMA (PAGINATED) =========================
	 */
	@GetMapping("/getSuprema")
	public List<SupremaEntity> getSuprema(@RequestParam String projectYear) {

		log.debug("Get Suprema | projectYear={} | corrId={}", projectYear, MDC.get("correlationId"));

		return supremaBusiness.getSupremaByProjectYear(projectYear);
	}

	/*
	 * ========================= DOWNLOAD SUPREMA EXCEL =========================
	 */
	@GetMapping("/downloadSupremaExcel")
	public ResponseEntity<InputStreamResource> downloadSupremaExcel(@RequestParam("year") String year)
			throws Exception {

		log.info("Download Suprema Excel | year={} | corrId={}", year, MDC.get("correlationId"));

		return supremaBusiness.downloadSupremaExcel(year);
	}
}
