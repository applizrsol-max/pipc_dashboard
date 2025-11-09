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
import com.pipc.dashboard.drawing.response.DamInspectionResponse;
import com.pipc.dashboard.drawing.response.DamNalikaResponse;
import com.pipc.dashboard.drawing.response.DamSafetyResponse;
import com.pipc.dashboard.drawing.response.PralambitBhusampadanResponse;

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

}
