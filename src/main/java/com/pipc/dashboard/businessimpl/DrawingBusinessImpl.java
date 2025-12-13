package com.pipc.dashboard.businessimpl;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

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
import com.pipc.dashboard.service.DrawingService;

@Component
public class DrawingBusinessImpl implements DrawingBusiness {

	@Autowired
	private DrawingService drawingService;

	@Override
	public DamSafetyResponse saveOrUpdateDamSafety(DamSafetyRequest damSafetyRequest) {
		return drawingService.saveOrUpdateDamSafety(damSafetyRequest);
	}

	@Override
	public DamSafetyResponse getDamSafetyData(String year, int page, int size) {
		return drawingService.getDamSafetyData(year, page, size);
	}

	@Override
	public DamInspectionResponse saveOrUpdateDamInspection(DamInspectionRequest damInspectionRequest) {
		return drawingService.saveOrUpdateDamInspection(damInspectionRequest);
	}

	@Override
	public DamInspectionResponse getDamInspectionData(String year, String period, String departmentKey, int page,
			int size) {
		return drawingService.getDamInspectionData(year, period, departmentKey, page, size);
	}

	@Override
	public DamNalikaResponse saveOrUpdateNalika(DamNalikaRequest request) {

		return drawingService.saveOrUpdateNalika(request);
	}

	@Override
	public DamNalikaResponse getNalikaByPeriod(String period, String departmentKey, int page, int size) {
		return drawingService.getNalikaByPeriod(period, departmentKey, page, size);
	}

	@Override
	public PralambitBhusampadanResponse saveOrUpdatePralambitBhusampadan(PralambitBhusampadanRequest req) {
		return drawingService.saveOrUpdatePralambitBhusampadan(req);
	}

	@Override
	public PralambitBhusampadanResponse getPralambitBhusampadan(String period, String star, int page, int size) {
		return drawingService.getPralambitBhusampadan(period, star, page, size);
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadDamSafetyExcel(String period) throws IOException {
		return drawingService.downloadDamSafetyExcel(period);
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadNalikaExcel(String period) throws IOException {
		return drawingService.downloadNalikaExcel(period);
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadPralambitBhusampadanExcel(String period) throws IOException {
		return drawingService.downloadPralambitBhusampadanExcel(period);
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadDamInspectionExcel(String period) throws IOException {
		return drawingService.downloadDamInspectionExcel(period);
	}

	@Override
	public SinchanKshamataResponse saveOrUpdateIrrigationCapacity(SinchanKshamataRequest request) {
		return drawingService.saveOrUpdateIrrigationCapacity(request);
	}

	@Override
	public SinchanKshamataResponse getSinchanKshamataData(String period, String date) {
		return drawingService.getSinchanKshamataData(period, date);
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadSinchanKshamataExcel(String period, String year)
			throws IOException {
		return drawingService.downloadSinchanKshamataExcel(period, year);
	}

	@Override
	public TenderBhamaResponse saveOrUpdateTenderBhama(TenderSaveRequest req) {
		return drawingService.saveOrUpdateTenderBhama(req);
	}

	@Override
	public TenderBhamaResponse getTenderBhamaDetails(String year, String month, String date) {
		return drawingService.getTenderBhamaDetails(year, month, date);
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadTenderBhama(String year, String month, String date)
			throws IOException {
		return drawingService.downloadTenderBhama(year, month, date);
	}

	@Override
	public TenderBhamaResponse saveOrUpdateTenderTarget(TenderSaveRequest req) {
		return drawingService.saveOrUpdateTenderTarget(req);
	}

	@Override
	public TenderBhamaResponse getTenderTarget(String year, String month) {
		return drawingService.getTenderTarget(year, month);
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadTenderTarget(String year, String month) throws IOException {
		return drawingService.downloadTenderTarget(year, month);
	}

	@Override
	public TenderBhamaResponse saveOrUpdateTenderPlan(TenderSaveRequest req) {
		return drawingService.saveOrUpdateTenderPlan(req);
	}

	@Override
	public TenderBhamaResponse getTenderPlan(String year) {
		return drawingService.getTenderPlan(year);
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadTenderPlan(String year) throws IOException {
		return drawingService.downloadTenderPlan(year);
	}

	@Override
	public TenderBhamaResponse saveOrUpdateTenderSummary(TenderSaveRequest req) {
		return drawingService.saveOrUpdateTenderSummary(req);
	}

	@Override
	public TenderBhamaResponse getTenderSummary(String year) {
		return drawingService.getTenderSummary(year);
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadTenderSummary(String year) throws IOException {
		return drawingService.downloadTenderSummary(year);
	}

}
