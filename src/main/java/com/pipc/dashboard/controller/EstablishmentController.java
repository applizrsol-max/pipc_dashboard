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

import com.pipc.dashboard.bhusmapadan.request.PraptraMasterDataRequest;
import com.pipc.dashboard.bhusmapadan.response.BhaniniResponse;
import com.pipc.dashboard.bhusmapadan.response.PraptraMasterDataResponse;
import com.pipc.dashboard.business.EstablishmentBusiness;
import com.pipc.dashboard.establishment.request.AgendaRequest;
import com.pipc.dashboard.establishment.request.AgendaSecRequest;
import com.pipc.dashboard.establishment.request.AppealWrapper;
import com.pipc.dashboard.establishment.request.AppealWrapper2;
import com.pipc.dashboard.establishment.request.BhaniniRequest;
import com.pipc.dashboard.establishment.request.EmployeePostingRequest;
import com.pipc.dashboard.establishment.request.IncomeTaxDeductionRequest;
import com.pipc.dashboard.establishment.request.MahaparRegisterRequest;
import com.pipc.dashboard.establishment.request.ThirteenRequest;
import com.pipc.dashboard.establishment.response.AgendaResponse;
import com.pipc.dashboard.establishment.response.AgendaSecResponse;
import com.pipc.dashboard.establishment.response.AppealResponse;
import com.pipc.dashboard.establishment.response.EmployeePostingResponse;
import com.pipc.dashboard.establishment.response.IncomeTaxDeductionResponse;
import com.pipc.dashboard.establishment.response.ThirteenResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/pipc/dashboard/establishment")
@RequiredArgsConstructor
public class EstablishmentController {

	private final EstablishmentBusiness establishmentBusiness;

	private String user() {
		return MDC.get("user") != null ? MDC.get("user") : "SYSTEM";
	}

	private String corrId() {
		return MDC.get("correlationId");
	}

	/* ========================= APPEAL – 1 ========================= */

	@PostMapping("/saveOrUpdateAppeal")
	public AppealResponse saveOrUpdateAppeal(@RequestBody AppealWrapper request) {
		log.info("START saveOrUpdateAppeal | user={} | corrId={}", user(), corrId());
		AppealResponse res = establishmentBusiness.saveOrUpdateAppeal(request);
		log.info("END saveOrUpdateAppeal | corrId={}", corrId());
		return res;
	}

	@GetMapping("/getAppealData")
	public ResponseEntity<AppealResponse> getAppealData(@RequestParam String year) {
		log.info("START getAppealData | year={} | user={}", year, user());
		return ResponseEntity.ok(establishmentBusiness.getAppealData(year));
	}

	@GetMapping("/downloadAppealArj")
	public ResponseEntity<InputStreamResource> downloadAppealArj(@RequestParam(required = false) String year)
			throws IOException {
		log.info("DOWNLOAD AppealArj | year={} | user={}", year, user());
		return establishmentBusiness.downloadAppealArj(year);
	}

	/* ========================= EMPLOYEE POSTING ========================= */

	@PostMapping("/saveOrUpdateEmployeePosting")
	public EmployeePostingResponse saveOrUpdateEmployeePosting(@RequestBody EmployeePostingRequest request) {
		log.info("START saveOrUpdateEmployeePosting | user={}", user());
		return establishmentBusiness.saveOrUpdateEmployeePosting(request);
	}

	@GetMapping("/getEmployeePostingData")
	public ResponseEntity<EmployeePostingResponse> getEmployeePostingData(
			@RequestParam(required = false) String adhikariKarmacharyacheNav, @RequestParam String year) {

		log.info("START getEmployeePostingData | name={} | year={}", adhikariKarmacharyacheNav, year);

		return ResponseEntity.ok(establishmentBusiness.getEmployeePostingData(adhikariKarmacharyacheNav, year));
	}

	/* ========================= INCOME TAX ========================= */

	@PostMapping("/saveOrUpdateIncomeTaxDeduc")
	public IncomeTaxDeductionResponse saveOrUpdateIncomeTaxDeduc(@RequestBody IncomeTaxDeductionRequest request) {

		log.info("START saveOrUpdateIncomeTaxDeduc | year={} | month={}", request.getYear(), request.getMonth());

		return establishmentBusiness.saveOrUpdateIncomeTaxDeduc(request);
	}

	@GetMapping("/getIncomeTaxDeductionData")
	public ResponseEntity<IncomeTaxDeductionResponse> getIncomeTaxDeductionData(@RequestParam String year,
			@RequestParam String month) {

		log.info("START getIncomeTaxDeductionData | year={} | month={}", year, month);
		return ResponseEntity.ok(establishmentBusiness.getIncomeTaxDeductionData(year, month));
	}

	/* ========================= AGENDA ========================= */

	@PostMapping("/saveOrUpdateAgenda")
	public AgendaResponse saveOrUpdateAgenda(@RequestBody AgendaRequest request) {
		log.info("START saveOrUpdateAgenda | year={}", request.getMeta().getYear());
		return establishmentBusiness.saveOrUpdateAgenda(request);
	}

	@GetMapping("/getAgendaByYearAndTargetDate")
	public AgendaResponse getAgendaByYearAndTargetDate(@RequestParam String year, @RequestParam String targetDate) {

		log.info("FETCH Agenda | year={} | targetDate={}", year, targetDate);
		return establishmentBusiness.getAgendaByYearAndTargetDate(year, targetDate);
	}

	@GetMapping("/downloadAgendaExcel")
	public ResponseEntity<InputStreamResource> downloadAgendaExcel(@RequestParam String year,
			@RequestParam String targetDate) throws Exception {

		log.info("DOWNLOAD AgendaExcel | year={} | targetDate={}", year, targetDate);
		return establishmentBusiness.downloadAgendaExcel(year, targetDate);
	}

	/* ========================= AGENDA SECTION ========================= */

	@PostMapping("/saveOrUpdateAgendaSec")
	public AgendaSecResponse saveAgendaSec(@RequestBody AgendaSecRequest request) {
		log.info("START saveOrUpdateAgendaSec | section={}", request.getRows());
		return establishmentBusiness.saveOrUpdateAgendaSec(request);
	}

	@GetMapping("/getAgendaSec")
	public AgendaSecResponse getAgendaSec(@RequestParam String year, @RequestParam String targetDate,
			@RequestParam String section) {

		log.info("FETCH AgendaSec | year={} | targetDate={} | section={}", year, targetDate, section);

		return establishmentBusiness.getAgendaSecByYearAndTargetDate(year, targetDate, section);
	}

	@GetMapping("/downloadAgendaSecExcel")
	public ResponseEntity<InputStreamResource> downloadAgendaSecExcel(@RequestParam String year,
			@RequestParam String targetDate, @RequestParam String section) throws Exception {

		log.info("DOWNLOAD AgendaSecExcel | year={} | targetDate={} | section={}", year, targetDate, section);

		return establishmentBusiness.downloadAgendaSecExcel(year, targetDate, section);
	}

	/* ========================= ANUKAMPA ========================= */

	@PostMapping("/saveOrUpdateAnukampa")
	public ThirteenResponse saveOrUpdateAnukampa(@RequestBody ThirteenRequest req) {
		log.info("START saveOrUpdateAnukampa | year={}", req.getMeta().getYear());
		return establishmentBusiness.saveOrUpdateAnukampa(req);
	}

	@GetMapping("/getAnukampaData")
	public ThirteenResponse getAnukampaData(@RequestParam String year, @RequestParam String targetDate) {

		log.info("FETCH Anukampa | year={} | targetDate={}", year, targetDate);
		return establishmentBusiness.getAnukampaData(year, targetDate);
	}

	@GetMapping("/downloadAnukampaExcel")
	public ResponseEntity<InputStreamResource> downloadAnukampaExcel(@RequestParam String year,
			@RequestParam String targetDate) throws Exception {

		log.info("DOWNLOAD AnukampaExcel | year={} | targetDate={}", year, targetDate);
		return establishmentBusiness.downloadAnukampaExcel(year, targetDate);
	}

	/* ========================= APPEAL – 2 ========================= */

	@PostMapping("/saveOrUpdateAppeal2")
	public AppealResponse saveOrUpdateAppeal2(@RequestBody AppealWrapper2 request) {
		log.info("START saveOrUpdateAppeal2 | user={}", user());
		return establishmentBusiness.saveOrUpdateAppeal2(request);
	}

	@GetMapping("/getAppealData2")
	public ResponseEntity<AppealResponse> getAppealData2(@RequestParam(required = false) String year) {

		log.info("FETCH Appeal2 | year={}", year);
		return ResponseEntity.ok(establishmentBusiness.getAppealData2(year));
	}

	@GetMapping("/downloadAppealArj2")
	public ResponseEntity<InputStreamResource> downloadAppealArj2(@RequestParam(required = false) String year)
			throws IOException {

		log.info("DOWNLOAD AppealArj2 | year={}", year);
		return establishmentBusiness.downloadAppealArj2(year);
	}

	/*
	 * ========================= MASTER / CR / MAHAPAR / GOPNIYA
	 * =========================
	 */

	@PostMapping("/saveMasterData")
	public PraptraMasterDataResponse saveMasterData(@RequestBody PraptraMasterDataRequest request) {

		log.info("START saveMasterData | year={}", request.getYear());
		return establishmentBusiness.saveMasterData(request);
	}

	@GetMapping("/getMasterData")
	public PraptraMasterDataResponse getMasterData(@RequestParam String year) {
		log.info("FETCH MasterData | year={}", year);
		return establishmentBusiness.getMasterData(year);
	}

	@GetMapping("/downloadMasterData")
	public ResponseEntity<InputStreamResource> downloadMasterData(@RequestParam(required = false) String year)
			throws IOException {

		log.info("DOWNLOAD MasterData | year={}", year);
		return establishmentBusiness.downloadMasterData(year);
	}

	@PostMapping("/saveCrFileList")
	public PraptraMasterDataResponse saveCrFileList(@RequestBody PraptraMasterDataRequest request) {

		log.info("START saveCrFileList | year={}", request.getYear());
		return establishmentBusiness.saveCrFileList(request);
	}

	@GetMapping("/getCrFileList")
	public PraptraMasterDataResponse getCrFileList(@RequestParam String year) {
		log.info("FETCH CrFileList | year={}", year);
		return establishmentBusiness.getCrFileList(year);
	}

	@GetMapping("/downloadCrFileList")
	public ResponseEntity<InputStreamResource> downloadCrFileList(@RequestParam(required = false) String year)
			throws IOException {

		log.info("DOWNLOAD CrFileList | year={}", year);
		return establishmentBusiness.downloadCrFileList(year);
	}

	@PostMapping("/saveCrFileRtrList")
	public PraptraMasterDataResponse saveCrFileRtrList(@RequestBody PraptraMasterDataRequest request) {

		log.info("START saveCrFileRtrList | year={}", request.getYear());
		return establishmentBusiness.saveCrFileRtrList(request);
	}

	@GetMapping("/getCrFileRtrList")
	public PraptraMasterDataResponse getCrFileRtrList(@RequestParam String year) {
		log.info("FETCH CrFileRtrList | year={}", year);
		return establishmentBusiness.getCrFileRtrList(year);
	}

	@GetMapping("/downloadCrFileRtrList")
	public ResponseEntity<InputStreamResource> downloadCrFileRtrList(@RequestParam(required = false) String year)
			throws IOException {

		log.info("DOWNLOAD CrFileRtrList | year={}", year);
		return establishmentBusiness.downloadCrFileRtrList(year);
	}

	@PostMapping("/saveMahaparRegister")
	public PraptraMasterDataResponse saveMahaparRegister(@RequestBody MahaparRegisterRequest request) {

		log.info("START saveMahaparRegister | year={}", request.getYear());
		return establishmentBusiness.saveMahaparRegister(request);
	}

	@GetMapping("/getMahaparRegister")
	public PraptraMasterDataResponse getMahaparRegister(@RequestParam String year) {
		log.info("FETCH MahaparRegister | year={}", year);
		return establishmentBusiness.getMahaparRegister(year);
	}

	@GetMapping("/downloadMahaparRegister")
	public ResponseEntity<InputStreamResource> downloadMahaparRegister(@RequestParam String year) throws IOException {

		log.info("DOWNLOAD MahaparRegister | year={}", year);
		return establishmentBusiness.downloadMahaparRegister(year);
	}

	@PostMapping("/saveKaryaratGopniyaAhwal")
	public PraptraMasterDataResponse saveKaryaratGopniyaAhwal(@RequestBody MahaparRegisterRequest request) {

		log.info("START saveKaryaratGopniyaAhwal | year={}", request.getYear());
		return establishmentBusiness.saveKaryaratGopniyaAhwal(request);
	}

	@GetMapping("/getKaryaratGopniyaAhwal")
	public PraptraMasterDataResponse getKaryaratGopniyaAhwal(@RequestParam String year, @RequestParam String type) {

		log.info("FETCH KaryaratGopniyaAhwal | year={} | type={}", year, type);
		return establishmentBusiness.getKaryaratGopniyaAhwal(year, type);
	}

	@GetMapping("/downloadKaryaratGopniyaAhwal")
	public ResponseEntity<InputStreamResource> downloadKaryaratGopniyaAhwal(@RequestParam String year,
			@RequestParam String type) throws IOException {

		log.info("DOWNLOAD KaryaratGopniyaAhwal | year={} | type={}", year, type);
		return establishmentBusiness.downloadKaryaratGopniyaAhwal(year, type);
	}

	@PostMapping("/saveOrUpdateBhaniniData")
	public BhaniniResponse saveOrUpdateBhaniniData(@RequestBody BhaniniRequest request) {

		log.info("START saveOrUpdateBhaniniData | year={} | name={}", request.getYear(),
				request.getEmployee().getName());
		return establishmentBusiness.saveOrUpdateBhaniniData(request);
	}

	@GetMapping("/getBhaniniData")
	public BhaniniResponse getBhaniniData(@RequestParam String employeeName, @RequestParam String year) {

		log.info("FETCH getBhaniniData | year={} | employeeName={}", year, employeeName);
		return establishmentBusiness.getBhaniniData(employeeName, year);
	}
	
	@GetMapping("/downloadBhaniniData")
	public ResponseEntity<InputStreamResource> downloadBhaniniData(@RequestParam String year,
			@RequestParam String employeeName) throws IOException {

		log.info("DOWNLOAD downloadBhaniniData | year={} | employeeName={}", year, employeeName);
		return establishmentBusiness.downloadBhaniniData(year, employeeName);
	}
}
