package com.pipc.dashboard.service;

import java.io.IOException;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;

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

public interface DrawingService {

	DamSafetyResponse saveOrUpdateDamSafety(DamSafetyRequest damSafetyRequest);

	DamSafetyResponse getDamSafetyData(String year, int page, int size);

	DamInspectionResponse saveOrUpdateDamInspection(DamInspectionRequest damInspectionRequest);

	DamInspectionResponse getDamInspectionData(String year, String period, String departmentKey, int page, int size);

	DamNalikaResponse saveOrUpdateNalika(DamNalikaRequest request);

	DamNalikaResponse getNalikaByPeriod(String period, String departmentKey, int page, int size);

	PralambitBhusampadanResponse saveOrUpdatePralambitBhusampadan(PralambitBhusampadanRequest req);

	PralambitBhusampadanResponse getPralambitBhusampadan(String period, String star, int page, int size);

	ResponseEntity<InputStreamResource> downloadDamSafetyExcel(String period) throws IOException;

	ResponseEntity<InputStreamResource> downloadNalikaExcel(String period) throws IOException;

	ResponseEntity<InputStreamResource> downloadPralambitBhusampadanExcel(String period) throws IOException;

	ResponseEntity<InputStreamResource> downloadDamInspectionExcel(String period) throws IOException;

	SinchanKshamataResponse saveOrUpdateIrrigationCapacity(SinchanKshamataRequest request);

	SinchanKshamataResponse getSinchanKshamataData(String period, String date);

	ResponseEntity<InputStreamResource> downloadSinchanKshamataExcel(String period, String year) throws IOException;

	TenderBhamaResponse saveOrUpdateTenderBhama(TenderSaveRequest req);

	TenderBhamaResponse getTenderBhamaDetails(String year, String month, String date);

	ResponseEntity<InputStreamResource> downloadTenderBhama(String year, String month, String date) throws IOException;

	TenderBhamaResponse saveOrUpdateTenderTarget(TenderSaveRequest req);

	TenderBhamaResponse getTenderTarget(String year, String month);

	ResponseEntity<InputStreamResource> downloadTenderTarget(String year, String month) throws IOException;

	TenderBhamaResponse saveOrUpdateTenderPlan(TenderSaveRequest req);

	TenderBhamaResponse getTenderPlan(String year);

	ResponseEntity<InputStreamResource> downloadTenderPlan(String year) throws IOException;

	TenderBhamaResponse saveOrUpdateTenderSummary(TenderSaveRequest req);

	TenderBhamaResponse getTenderSummary(String year);

	ResponseEntity<InputStreamResource> downloadTenderSummary(String year) throws IOException;

}
