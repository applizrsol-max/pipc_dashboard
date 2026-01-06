package com.pipc.dashboard.business;

import java.io.IOException;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;

import com.pipc.dashboard.bhusmapadan.request.PraptraMasterDataRequest;
import com.pipc.dashboard.bhusmapadan.response.BhaniniResponse;
import com.pipc.dashboard.bhusmapadan.response.PraptraMasterDataResponse;
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

public interface EstablishmentBusiness {

	AppealResponse saveOrUpdateAppeal(AppealWrapper request);

	AppealResponse getAppealData(String year);

	EmployeePostingResponse saveOrUpdateEmployeePosting(EmployeePostingRequest request);

	EmployeePostingResponse getEmployeePostingData(String adhikariKarmacharyacheNav, String year);

	IncomeTaxDeductionResponse saveOrUpdateIncomeTaxDeduc(IncomeTaxDeductionRequest request);

	IncomeTaxDeductionResponse getIncomeTaxDeductionData(String year, String month);

	ResponseEntity<InputStreamResource> downloadAppealArj(String year) throws IOException;

	AgendaResponse saveOrUpdateAgenda(AgendaRequest request);

	AgendaResponse getAgendaByYearAndTargetDate(String year, String targetDate);

	ResponseEntity<InputStreamResource> downloadAgendaExcel(String year, String targetDate) throws Exception;

	AgendaSecResponse saveOrUpdateAgendaSec(AgendaSecRequest request);

	AgendaSecResponse getAgendaSecByYearAndTargetDate(String year, String targetDate, String section);

	ResponseEntity<InputStreamResource> downloadAgendaSecExcel(String year, String targetDate, String section)
			throws Exception;

	ThirteenResponse saveOrUpdateAnukampa(ThirteenRequest req);

	ThirteenResponse getAnukampaData(String year, String targetDate);

	ResponseEntity<InputStreamResource> downloadAnukampaExcel(String year, String targetDate) throws Exception;

	AppealResponse saveOrUpdateAppeal2(AppealWrapper2 request);

	AppealResponse getAppealData2(String year);

	ResponseEntity<InputStreamResource> downloadAppealArj2(String year) throws IOException;

	PraptraMasterDataResponse saveMasterData(PraptraMasterDataRequest request);

	PraptraMasterDataResponse getMasterData(String year);

	ResponseEntity<InputStreamResource> downloadMasterData(String year) throws IOException;

	PraptraMasterDataResponse saveCrFileList(PraptraMasterDataRequest request);

	PraptraMasterDataResponse getCrFileList(String year);

	ResponseEntity<InputStreamResource> downloadCrFileList(String year) throws IOException;

	PraptraMasterDataResponse saveCrFileRtrList(PraptraMasterDataRequest request);

	PraptraMasterDataResponse getCrFileRtrList(String year);

	ResponseEntity<InputStreamResource> downloadCrFileRtrList(String year) throws IOException;

	PraptraMasterDataResponse getMahaparRegister(String year);

	PraptraMasterDataResponse saveMahaparRegister(MahaparRegisterRequest request);

	ResponseEntity<InputStreamResource> downloadMahaparRegister(String year) throws IOException;

	PraptraMasterDataResponse saveKaryaratGopniyaAhwal(MahaparRegisterRequest request);

	PraptraMasterDataResponse getKaryaratGopniyaAhwal(String year, String type);

	ResponseEntity<InputStreamResource> downloadKaryaratGopniyaAhwal(String year, String type) throws IOException;

	BhaniniResponse saveOrUpdateBhaniniData(BhaniniRequest request);

	BhaniniResponse getBhaniniData(String employeeName, String year);

	ResponseEntity<InputStreamResource> downloadBhaniniData(String year, String employeeName) throws IOException ;

}
