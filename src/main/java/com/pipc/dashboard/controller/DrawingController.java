package com.pipc.dashboard.controller;

import java.io.IOException;

import org.slf4j.MDC;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pipc.dashboard.business.DrawingBusiness;
import com.pipc.dashboard.drawing.request.DamInspectionRequest;
import com.pipc.dashboard.drawing.request.DamNalikaRequest;
import com.pipc.dashboard.drawing.request.DamSafetyRequest;
import com.pipc.dashboard.drawing.request.PralambitBhusampadanRequest;
import com.pipc.dashboard.drawing.request.SinchanKshamataRequest;
import com.pipc.dashboard.drawing.request.TenderSaveRequest;
import com.pipc.dashboard.drawing.response.DamInspectionResponse;
import com.pipc.dashboard.drawing.response.DamNalikaResponse;
import com.pipc.dashboard.drawing.response.DamSafetyResponse;
import com.pipc.dashboard.drawing.response.PralambitBhusampadanResponse;
import com.pipc.dashboard.drawing.response.SinchanKshamataResponse;
import com.pipc.dashboard.drawing.response.TenderBhamaResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/pipc/dashboard/drawing")
@RequiredArgsConstructor
public class DrawingController {

	private final DrawingBusiness drawingBusiness;

	/*
	 * ========================= DAM SAFETY =========================
	 */

	@PostMapping("/saveOrUpdateDamSafety")
	public DamSafetyResponse saveOrUpdateDamSafety(@RequestBody DamSafetyRequest damSafetyRequest) {

		log.info("Save/Update DamSafety | corrId={}", MDC.get("correlationId"));
		return drawingBusiness.saveOrUpdateDamSafety(damSafetyRequest);
	}

	@GetMapping("/getDamSafetyData")
	public ResponseEntity<DamSafetyResponse> getDamSafetyData(@RequestParam String year) {

		log.debug("Get DamSafety | year={} | corrId={}", year, MDC.get("correlationId"));
		return ResponseEntity.ok(drawingBusiness.getDamSafetyData(year));
	}

	/*
	 * ========================= DAM INSPECTION =========================
	 */

	@PostMapping("/saveOrUpdateDamInspection")
	public DamInspectionResponse saveOrUpdateDamInspection(@RequestBody DamInspectionRequest damInspectionRequest) {

		log.info("Save/Update DamInspection | corrId={}", MDC.get("correlationId"));
		return drawingBusiness.saveOrUpdateDamInspection(damInspectionRequest);
	}

	@GetMapping("/getDamInspection")
	public ResponseEntity<DamInspectionResponse> getDamInspectionData(@RequestParam String year) {

		log.debug("Get DamInspection | year={} period={} | corrId={}", year, MDC.get("correlationId"));

		return ResponseEntity.ok(drawingBusiness.getDamInspectionData(year));
	}

	/*
	 * ========================= DAM NALIKA =========================
	 */

	@PostMapping("/saveOrUpdateNalika")
	public DamNalikaResponse saveOrUpdateNalika(@RequestBody DamNalikaRequest request) {

		log.info("Save/Update Nalika | corrId={}", MDC.get("correlationId"));
		return drawingBusiness.saveOrUpdateNalika(request);
	}

	@GetMapping("/getNalikaData")
	public ResponseEntity<DamNalikaResponse> getNalikaByPeriod(@RequestParam String period) {

		log.debug("Get Nalika | period={} | corrId={}", period, MDC.get("correlationId"));
		return ResponseEntity.ok(drawingBusiness.getNalikaByPeriod(period));
	}

	/*
	 * ========================= PRALAMBIT BHUSAMPADAN =========================
	 */

	@PostMapping("/saveOrUpdatePralambitBhusampadan")
	public PralambitBhusampadanResponse saveOrUpdatePralambitBhusampadan(@RequestBody PralambitBhusampadanRequest req) {

		log.info("Save/Update PralambitBhusampadan | corrId={}", MDC.get("correlationId"));
		return drawingBusiness.saveOrUpdatePralambitBhusampadan(req);
	}

	@GetMapping("/getPralambitBhusampadan")
	public ResponseEntity<PralambitBhusampadanResponse> getPralambitBhusampadan(@RequestParam String period) {

		log.debug("Get PralambitBhusampadan | period={} | corrId={}", period, MDC.get("correlationId"));

		return ResponseEntity.ok(drawingBusiness.getPralambitBhusampadan(period));
	}

	/*
	 * ========================= DOWNLOAD EXCELS =========================
	 */

	@GetMapping("/downloadDamSafetyExcel")
	public ResponseEntity<InputStreamResource> downloadDamSafetyExcel(@RequestParam String period) throws IOException {

		log.info("Download DamSafety Excel | corrId={}", MDC.get("correlationId"));
		return drawingBusiness.downloadDamSafetyExcel(period);
	}

	@GetMapping("/downloadNalikaExcel")
	public ResponseEntity<InputStreamResource> downloadNalikaExcel(@RequestParam String period) throws IOException {

		log.info("Download Nalika Excel | corrId={}", MDC.get("correlationId"));
		return drawingBusiness.downloadNalikaExcel(period);
	}

	@GetMapping("/downloadPralambitBhusampadanExcel")
	public ResponseEntity<InputStreamResource> downloadPralambitBhusampadanExcel(@RequestParam String period)
			throws IOException {

		log.info("Download PralambitBhusampadan Excel | corrId={}", MDC.get("correlationId"));
		return drawingBusiness.downloadPralambitBhusampadanExcel(period);
	}

	@GetMapping("/downloadDamInspectionExcel")
	public ResponseEntity<InputStreamResource> downloadDamInspectionExcel(@RequestParam String period)
			throws IOException {

		log.info("Download DamInspection Excel | corrId={}", MDC.get("correlationId"));
		return drawingBusiness.downloadDamInspectionExcel(period);
	}

	/*
	 * ========================= IRRIGATION CAPACITY =========================
	 */

	@PostMapping("/saveOrUpdateIrrigationCapacity")
	public SinchanKshamataResponse saveOrUpdateIrrigationCapacity(@RequestBody SinchanKshamataRequest request) {

		log.info("Save/Update IrrigationCapacity | corrId={}", MDC.get("correlationId"));
		return drawingBusiness.saveOrUpdateIrrigationCapacity(request);
	}

	@GetMapping("/getIrrigationCapacityData")
	public ResponseEntity<SinchanKshamataResponse> getIrrigationCapacityData(@RequestParam String period,
			@RequestParam(required = false) String date) {

		log.debug("Get IrrigationCapacity | period={} | corrId={}", period, MDC.get("correlationId"));

		return ResponseEntity.ok(drawingBusiness.getSinchanKshamataData(period, date));
	}

	@GetMapping("/downloadIrrigationCapacityData")
	public ResponseEntity<InputStreamResource> downloadSinchanKshamataExcel(@RequestParam String period,
			@RequestParam String date) throws IOException {

		log.info("Download IrrigationCapacity Excel | corrId={}", MDC.get("correlationId"));
		return drawingBusiness.downloadSinchanKshamataExcel(period, date);
	}

	/*
	 * ========================= TENDER – BHAMA / TARGET / PLAN / SUMMARY
	 * =========================
	 */

	@PostMapping("/saveOrUpdateTenderBhama")
	public TenderBhamaResponse saveOrUpdateTenderBhama(@RequestBody TenderSaveRequest req) {

		log.info("Save/Update TenderBhama | corrId={}", MDC.get("correlationId"));
		return drawingBusiness.saveOrUpdateTenderBhama(req);
	}

	@GetMapping("/getTenderBhamaDetails")
	public TenderBhamaResponse get(@RequestParam String year, @RequestParam String month, @RequestParam String date) {

		log.debug("Get TenderBhama | year={} | corrId={}", year, MDC.get("correlationId"));
		return drawingBusiness.getTenderBhamaDetails(year, month, date);
	}

	@GetMapping("/downloadTenderBhama")
	public ResponseEntity<InputStreamResource> downloadTenderBhama(@RequestParam String year,
			@RequestParam String month, @RequestParam String date) throws IOException {

		log.info("Download TenderBhama | corrId={}", MDC.get("correlationId"));
		return drawingBusiness.downloadTenderBhama(year, month, date);
	}

	@PostMapping("/saveOrUpdateTenderTarget")
	public TenderBhamaResponse saveOrUpdateTenderTarget(@RequestBody TenderSaveRequest req) {

		log.info("Save/Update TenderTarget | corrId={}", MDC.get("correlationId"));
		return drawingBusiness.saveOrUpdateTenderTarget(req);
	}

	@GetMapping("/getTenderTarget")
	public TenderBhamaResponse getTenderTarget(@RequestParam String year, @RequestParam String month) {

		log.debug("Get TenderTarget | year={} | corrId={}", year, MDC.get("correlationId"));
		return drawingBusiness.getTenderTarget(year, month);
	}

	@GetMapping("/downloadTenderTarget")
	public ResponseEntity<InputStreamResource> downloadTenderTarget(@RequestParam String year,
			@RequestParam String month) throws IOException {

		log.info("Download TenderTarget | corrId={}", MDC.get("correlationId"));
		return drawingBusiness.downloadTenderTarget(year, month);
	}

	@PostMapping("/saveOrUpdateTenderPlan")
	public TenderBhamaResponse saveOrUpdateTenderPlan(@RequestBody TenderSaveRequest req) {

		log.info("Save/Update TenderPlan | corrId={}", MDC.get("correlationId"));
		return drawingBusiness.saveOrUpdateTenderPlan(req);
	}

	@GetMapping("/getTenderPlan")
	public TenderBhamaResponse getTenderPlan(@RequestParam String year) {

		log.debug("Get TenderPlan | year={} | corrId={}", year, MDC.get("correlationId"));
		return drawingBusiness.getTenderPlan(year);
	}

	@GetMapping("/downloadTenderPlan")
	public ResponseEntity<InputStreamResource> downloadTenderPlan(@RequestParam String year) throws IOException {

		log.info("Download TenderPlan | corrId={}", MDC.get("correlationId"));
		return drawingBusiness.downloadTenderPlan(year);
	}

	@PostMapping("/saveOrUpdateTenderSummary")
	public TenderBhamaResponse saveOrUpdateTenderSummary(@RequestBody TenderSaveRequest req) {

		log.info("Save/Update TenderSummary | corrId={}", MDC.get("correlationId"));
		return drawingBusiness.saveOrUpdateTenderSummary(req);
	}

	@GetMapping("/getTenderSummary")
	public TenderBhamaResponse getTenderSummary(@RequestParam String year) {

		log.debug("Get TenderSummary | year={} | corrId={}", year, MDC.get("correlationId"));
		return drawingBusiness.getTenderSummary(year);
	}

	@GetMapping("/downloadTenderSummary")
	public ResponseEntity<InputStreamResource> downloadTenderSummary(@RequestParam String year) throws IOException {

		log.info("Download TenderSummary | corrId={}", MDC.get("correlationId"));
		return drawingBusiness.downloadTenderSummary(year);
	}
}
