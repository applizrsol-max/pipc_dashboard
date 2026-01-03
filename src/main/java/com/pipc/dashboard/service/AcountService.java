package com.pipc.dashboard.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.pipc.dashboard.accounts.request.AccountsRequest;
import com.pipc.dashboard.accounts.request.PendingParaRequest;
import com.pipc.dashboard.accounts.response.AccountsResponse;
import com.pipc.dashboard.accounts.response.PendingParaResponse;

public interface AcountService {

	AccountsResponse saveOrUpdateAccounts(AccountsRequest request);

	Map<String, List<JsonNode>> getAllAccountsByYear(String year);

	ResponseEntity<InputStreamResource> downloadAccountsReport(String year) throws IOException;

	PendingParaResponse savePendingPara(PendingParaRequest request);

	PendingParaResponse getAllPendingPara(Integer year);

	ResponseEntity<InputStreamResource> downloadPendingPara(Integer year) throws IOException;

}
