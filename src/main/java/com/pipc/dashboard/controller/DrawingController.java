package com.pipc.dashboard.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping("/pipc/dashboard/drawing")
public class DrawingController {

	@Autowired
	private DrawingBusiness drawingBusiness;

	@PostMapping("/saveOrUpdateDamSafety")
	public DamSafetyResponse saveOrUpdateDamSafety(@RequestBody DamSafetyRequest damSafetyRequest) {
		return drawingBusiness.saveOrUpdateDamSafety(damSafetyRequest);
	}

	@GetMapping("/getDamSafetyData")
	public ResponseEntity<DamSafetyResponse> getDamSafetyData(@RequestParam String year,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(drawingBusiness.getDamSafetyData(year, page, size));
	}

	@PostMapping("/saveOrUpdateDamInspection")
	public DamInspectionResponse saveOrUpdateDamInspection(@RequestBody DamInspectionRequest damInspectionRequest) {
		return drawingBusiness.saveOrUpdateDamInspection(damInspectionRequest);
	}

	@GetMapping("/getDamInspection")
	public ResponseEntity<DamInspectionResponse> getDamInspectionData(@RequestParam String year,
			@RequestParam String period, @RequestParam(required = false) String departmentKey,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

		DamInspectionResponse response = drawingBusiness.getDamInspectionData(year, period, departmentKey, page, size);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/saveOrUpdateNalika")
	public DamNalikaResponse saveOrUpdateNalika(@RequestBody DamNalikaRequest request) {
		return drawingBusiness.saveOrUpdateNalika(request);
	}

	@GetMapping("/getNalikaData")
	public ResponseEntity<DamNalikaResponse> getNalikaByPeriod(@RequestParam String period,
			@RequestParam(required = false) String departmentKey, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {

		DamNalikaResponse response = drawingBusiness.getNalikaByPeriod(period, departmentKey, page, size);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/saveOrUpdatePralambitBhusampadan")
	public PralambitBhusampadanResponse saveOrUpdatePralambitBhusampadan(@RequestBody PralambitBhusampadanRequest req) {
		return drawingBusiness.saveOrUpdatePralambitBhusampadan(req);
	}

	@GetMapping("/getPralambitBhusampadan")
	public ResponseEntity<PralambitBhusampadanResponse> getPralambitBhusampadan(@RequestParam String period,
			@RequestParam(required = false) String star, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(drawingBusiness.getPralambitBhusampadan(period, star, page, size));
	}

	@GetMapping("/downloadDamSafetyExcel")
	public ResponseEntity<InputStreamResource> downloadDamSafetyExcel(@RequestParam String period) throws IOException {
		return drawingBusiness.downloadDamSafetyExcel(period);
	}

	@GetMapping("/downloadNalikaExcel")
	public ResponseEntity<InputStreamResource> downloadNalikaExcel(@RequestParam String period) throws IOException {
		return drawingBusiness.downloadNalikaExcel(period);
	}

	@GetMapping("/downloadPralambitBhusampadanExcel")
	public ResponseEntity<InputStreamResource> downloadPralambitBhusampadanExcel(@RequestParam String period)
			throws IOException {
		return drawingBusiness.downloadPralambitBhusampadanExcel(period);
	}

	@GetMapping("/downloadDamInspectionExcel")
	public ResponseEntity<InputStreamResource> downloadDamInspectionExcel(@RequestParam String period)
			throws IOException {
		return drawingBusiness.downloadDamInspectionExcel(period);
	}

	@PostMapping("/saveOrUpdateIrrigationCapacity")
	public SinchanKshamataResponse saveOrUpdateIrrigationCapacity(@RequestBody SinchanKshamataRequest request) {
		return drawingBusiness.saveOrUpdateIrrigationCapacity(request);

	}

	@GetMapping("/getIrrigationCapacityData")
	public ResponseEntity<SinchanKshamataResponse> getIrrigationCapacityData(@RequestParam String period,
			@RequestParam(required = false) String date) {

		SinchanKshamataResponse response = drawingBusiness.getSinchanKshamataData(period, date);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/downloadIrrigationCapacityData")
	public ResponseEntity<InputStreamResource> downloadSinchanKshamataExcel(@RequestParam String period,
			@RequestParam String date) throws IOException {
		return drawingBusiness.downloadSinchanKshamataExcel(period, date);
	}

	@PostMapping("/saveOrUpdateTenderBhama")
	public TenderBhamaResponse saveOrUpdateTenderBhama(@RequestBody TenderSaveRequest req) {
		return drawingBusiness.saveOrUpdateTenderBhama(req);
	}

	@GetMapping("/getTenderBhamaDetails")
	public TenderBhamaResponse get(@RequestParam String year, @RequestParam String month, @RequestParam String date) {
		return drawingBusiness.getTenderBhamaDetails(year, month, date);
	}

	@GetMapping("/downloadTenderBhama")
	public ResponseEntity<InputStreamResource> downloadTenderBhama(@RequestParam String year, String month,
			@RequestParam String date) throws IOException {
		return drawingBusiness.downloadTenderBhama(year, month, date);
	}

	@PostMapping("/saveOrUpdateTenderTarget")
	public TenderBhamaResponse saveOrUpdateTenderTarget(@RequestBody TenderSaveRequest req) {
		return drawingBusiness.saveOrUpdateTenderTarget(req);
	}

	@GetMapping("/getTenderTarget")
	public TenderBhamaResponse getTenderTarget(@RequestParam String year, @RequestParam String month) {

		return drawingBusiness.getTenderTarget(year, month);
	}

	@GetMapping("/downloadTenderTarget")
	public ResponseEntity<InputStreamResource> downloadTenderTarget(@RequestParam String year, String month)
			throws IOException {
		return drawingBusiness.downloadTenderTarget(year, month);
	}

	@PostMapping("/saveOrUpdateTenderPlan")
	public TenderBhamaResponse saveOrUpdateTenderPlan(@RequestBody TenderSaveRequest req) {
		return drawingBusiness.saveOrUpdateTenderPlan(req);
	}

	@GetMapping("/getTenderPlan")
	public TenderBhamaResponse getTenderPlan(@RequestParam String year) {
		return drawingBusiness.getTenderPlan(year);
	}

	@GetMapping("/downloadTenderPlan")
	public ResponseEntity<InputStreamResource> downloadTenderPlan(@RequestParam String year) throws IOException {
		return drawingBusiness.downloadTenderPlan(year);
	}
}
