package com.pipc.dashboard.businessimpl;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.pipc.dashboard.bhusmapadan.request.PraptraMasterDataRequest;
import com.pipc.dashboard.bhusmapadan.response.PraptraMasterDataResponse;
import com.pipc.dashboard.business.EstablishmentBusiness;
import com.pipc.dashboard.establishment.request.AgendaRequest;
import com.pipc.dashboard.establishment.request.AgendaSecRequest;
import com.pipc.dashboard.establishment.request.AppealWrapper;
import com.pipc.dashboard.establishment.request.AppealWrapper2;
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
import com.pipc.dashboard.service.EstablishmentService;

@Component
public class EstablishmentBusinessImpl implements EstablishmentBusiness {

	@Autowired
	private EstablishmentService establishmentService;

	@Override
	public AppealResponse saveOrUpdateAppeal(AppealWrapper request) {

		return establishmentService.saveOrUpdateAppeal(request);
	}

	@Override
	public AppealResponse getAppealData(String year) {
		return establishmentService.getAppealData(year);
	}

	@Override
	public EmployeePostingResponse saveOrUpdateEmployeePosting(EmployeePostingRequest request) {
		return establishmentService.saveOrUpdateEmployeePosting(request);
	}

	@Override
	public EmployeePostingResponse getEmployeePostingData(String adhikariKarmacharyacheNav, String year) {
		return establishmentService.getEmployeePostingData(adhikariKarmacharyacheNav, year);
	}

	@Override
	public IncomeTaxDeductionResponse saveOrUpdateIncomeTaxDeduc(IncomeTaxDeductionRequest request) {
		return establishmentService.saveOrUpdateIncomeTaxDeduc(request);
	}

	@Override
	public IncomeTaxDeductionResponse getIncomeTaxDeductionData(String year, String month) {
		return establishmentService.getIncomeTaxDeductionData(year, month);
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadAppealArj(String year) throws IOException {
		return establishmentService.downloadAppealArj(year);
	}

	@Override
	public AgendaResponse saveOrUpdateAgenda(AgendaRequest request) {
		return establishmentService.saveOrUpdateAgenda(request);
	}

	@Override
	public AgendaResponse getAgendaByYearAndTargetDate(String year, String targetDate) {
		return establishmentService.getAgendaByYearAndTargetDate(year, targetDate);
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadAgendaExcel(String year, String targetDate) throws Exception {
		return establishmentService.downloadAgendaExcel(year, targetDate);

	}

	@Override
	public AgendaSecResponse saveOrUpdateAgendaSec(AgendaSecRequest dto) {
		return establishmentService.saveOrUpdateAgendaSec(dto);
	}

	@Override
	public AgendaSecResponse getAgendaSecByYearAndTargetDate(String year, String targetDate, String section) {
		return establishmentService.getAgendaSecByYearAndTargetDate(year, targetDate, section);
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadAgendaSecExcel(String year, String targetDate, String section)
			throws Exception {
		return establishmentService.downloadAgendaSecExcel(year, targetDate, section);
	}

	@Override
	public ThirteenResponse saveOrUpdateAnukampa(ThirteenRequest req) {
		return establishmentService.saveOrUpdateAnukampa(req);
	}

	@Override
	public ThirteenResponse getAnukampaData(String year, String targetDate) {
		return establishmentService.getAnukampaData(year, targetDate);
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadAnukampaExcel(String year, String targetDate) throws Exception {
		return establishmentService.downloadAnukampaExcel(year, targetDate);
	}

	@Override
	public AppealResponse saveOrUpdateAppeal2(AppealWrapper2 request) {
		return establishmentService.saveOrUpdateAppeal2(request);
	}

	@Override
	public AppealResponse getAppealData2(String year) {
		return establishmentService.getAppealData2(year);
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadAppealArj2(String year) throws IOException {
		return establishmentService.downloadAppealArj2(year);
	}

	@Override
	public PraptraMasterDataResponse saveMasterData(PraptraMasterDataRequest request) {
		return establishmentService.saveMasterData(request);
	}

	@Override
	public PraptraMasterDataResponse getMasterData(String year) {
		return establishmentService.getMasterData(year);
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadMasterData(String year) throws IOException {
		return establishmentService.downloadMasterData(year);
	}

	@Override
	public PraptraMasterDataResponse saveCrFileList(PraptraMasterDataRequest request) {
		return establishmentService.saveCrFileList(request);
	}

	@Override
	public PraptraMasterDataResponse getCrFileList(String year) {
		return establishmentService.getCrFileList(year);
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadCrFileList(String year) throws IOException {
		return establishmentService.downloadCrFileList(year);
	}

	@Override
	public PraptraMasterDataResponse saveCrFileRtrList(PraptraMasterDataRequest request) {
		return establishmentService.saveCrFileRtrList(request);
	}

	@Override
	public PraptraMasterDataResponse getCrFileRtrList(String year) {
		return establishmentService.getCrFileRtrList(year);
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadCrFileRtrList(String year) throws IOException {
		return establishmentService.downloadCrFileRtrList(year);
	}

	@Override
	public PraptraMasterDataResponse saveMahaparRegister(MahaparRegisterRequest request) {
		return establishmentService.saveMahaparRegister(request);
	}

	@Override
	public PraptraMasterDataResponse getMahaparRegister(String year) {
		return establishmentService.getMahaparRegister(year);
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadMahaparRegister(String year) throws IOException {
		return establishmentService.downloadMahaparRegister(year);
	}

	@Override
	public PraptraMasterDataResponse saveKaryaratGopniyaAhwal(MahaparRegisterRequest request) {
		return establishmentService.saveKaryaratGopniyaAhwal(request);
	}

	@Override
	public PraptraMasterDataResponse getKaryaratGopniyaAhwal(String year, String type) {
		return establishmentService.getKaryaratGopniyaAhwal(year, type);
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadKaryaratGopniyaAhwal(String year, String type)
			throws IOException {
		return establishmentService.downloadKaryaratGopniyaAhwal(year, type);
	}

}
