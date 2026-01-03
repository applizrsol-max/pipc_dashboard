package com.pipc.dashboard.businessimpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.pipc.dashboard.accounts.request.AccountsRequest;
import com.pipc.dashboard.accounts.request.PendingParaRequest;
import com.pipc.dashboard.accounts.response.AccountsResponse;
import com.pipc.dashboard.accounts.response.PendingParaResponse;
import com.pipc.dashboard.business.AccountsBusiness;
import com.pipc.dashboard.service.AcountService;

@Component
public class AccountsBusinessImpl implements AccountsBusiness {

	private final AcountService accountService;

	public AccountsBusinessImpl(AcountService accountService) {
		this.accountService = accountService;
	}

	@Override
	public AccountsResponse saveAccounts(AccountsRequest request) {

		return accountService.saveOrUpdateAccounts(request);
	}

	@Override
	public Map<String, Object> getAllAccountsByYear(String year) {
		return accountService.getAllAccountsByYear(year);
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadAccountsReport(String year) throws IOException {
		return accountService.downloadAccountsReport(year);
	}

	@Override
	public PendingParaResponse savePendingPara(PendingParaRequest request) {
		return accountService.savePendingPara(request);
	}

	@Override
	public PendingParaResponse getAllPendingPara(Integer year) {
		return accountService.getAllPendingPara(year);
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadPendingPara(Integer year) throws IOException {
		return accountService.downloadPendingPara(year);
	}

}
