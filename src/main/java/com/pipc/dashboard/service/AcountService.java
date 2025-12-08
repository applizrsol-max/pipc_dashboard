package com.pipc.dashboard.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import com.pipc.dashboard.accounts.repository.AccountsEntity;
import com.pipc.dashboard.accounts.request.AccountsRequest;
import com.pipc.dashboard.accounts.request.PendingParaRequest;
import com.pipc.dashboard.accounts.response.AccountsResponse;
import com.pipc.dashboard.accounts.response.PendingParaResponse;

public interface AcountService {

	AccountsResponse saveOrUpdateAccounts(AccountsRequest request);

	Map<String, Object> getAllAccounts(int page, int size);

	Map<String, Object> getAllAccountsByYear(String year);

	ByteArrayInputStream generateMarathiExcelForYear(String year) throws IOException;

	ResponseEntity<InputStreamResource> downloadAccountsReport(String year) throws IOException;

	Map<String, Object> getAllAccountsByYear(int page, int size, String year);

	PendingParaResponse savePendingPara(PendingParaRequest request);

	PendingParaResponse getAllPendingPara(Integer year);

	ResponseEntity<InputStreamResource> downloadPendingPara(Integer year) throws IOException;

}
