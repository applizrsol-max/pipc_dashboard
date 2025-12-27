package com.pipc.dashboard.service;

import java.io.IOException;

import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import com.pipc.dashboard.bhusmapadan.request.PraptraMasterDataRequest;
import com.pipc.dashboard.bhusmapadan.response.PraptraMasterDataResponse;
import com.pipc.dashboard.establishment.request.AgendaRequest;
import com.pipc.dashboard.establishment.request.AgendaSecRequest;
import com.pipc.dashboard.establishment.request.AppealWrapper;
import com.pipc.dashboard.establishment.request.AppealWrapper2;
import com.pipc.dashboard.establishment.request.EmployeePostingRequest;
import com.pipc.dashboard.establishment.request.IncomeTaxDeductionRequest;
import com.pipc.dashboard.establishment.request.LeaveRequest;
import com.pipc.dashboard.establishment.request.MahaparRegisterRequest;
import com.pipc.dashboard.establishment.request.MedicalBillRequest;
import com.pipc.dashboard.establishment.request.PassportNocRequest;
import com.pipc.dashboard.establishment.request.ThirteenRequest;
import com.pipc.dashboard.establishment.response.AgendaResponse;
import com.pipc.dashboard.establishment.response.AgendaSecResponse;
import com.pipc.dashboard.establishment.response.AppealResponse;
import com.pipc.dashboard.establishment.response.EmployeePostingResponse;
import com.pipc.dashboard.establishment.response.IncomeTaxDeductionResponse;
import com.pipc.dashboard.establishment.response.LeaveResponse;
import com.pipc.dashboard.establishment.response.MedicalBillResponse;
import com.pipc.dashboard.establishment.response.PassportNocResponse;
import com.pipc.dashboard.establishment.response.ThirteenResponse;

public interface EstablishmentService {

	MedicalBillResponse saveOrUpdateMedicalBill(MedicalBillRequest request);

	MedicalBillResponse getMedicalBills(String employeeName, String month, String year, String period, String date);

	LeaveResponse saveOrUpdateLeave(LeaveRequest request);

	LeaveResponse getLeaveDetails(String employeeName, String year, String month, String date);

	AppealResponse saveOrUpdateAppeal(AppealWrapper request);

	AppealResponse getAppealData(String year, int page, int size);

	EmployeePostingResponse saveOrUpdateEmployeePosting(EmployeePostingRequest request);

	EmployeePostingResponse getEmployeePostingData(String adhikariKarmacharyacheNav, String year, int page, int size);

	IncomeTaxDeductionResponse saveOrUpdateIncomeTaxDeduc(IncomeTaxDeductionRequest request);

	Page<IncomeTaxDeductionResponse> getIncomeTaxDeductionData(String year, String month, int page, int size);

	PassportNocResponse saveOrUpdatePassportNoc(PassportNocRequest request);

	Page<PassportNocResponse> getPassportNocData(String year, String month, String employeeName, int page, int size);

	ResponseEntity<InputStreamResource> downloadAppealArj(String year) throws IOException;

	ResponseEntity<InputStreamResource> downloadMedicalBill(String employeeName, String date)
			throws IOException, Exception;

	ResponseEntity<InputStreamResource> downloadLeaveDetails(String employeeName, String date) throws Exception;

	AgendaResponse saveOrUpdateAgenda(AgendaRequest request);

	AgendaResponse getAgendaByYearAndTargetDate(String year, String targetDate);

	ResponseEntity<InputStreamResource> downloadAgendaExcel(String year, String targetDate) throws Exception;

	AgendaSecResponse saveOrUpdateAgendaSec(AgendaSecRequest dto);

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

}
