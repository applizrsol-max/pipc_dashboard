package com.pipc.dashboard.businessimpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.pipc.dashboard.accounts.repository.AccountsEntity;
import com.pipc.dashboard.accounts.request.AccountsRequest;
import com.pipc.dashboard.accounts.response.AccountsResponse;
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
	public Map<String, Object> getAllAccounts(int page, int size) {
		return accountService.getAllAccounts(page, size);
	}

	@Override
	public Map<String, Object> getAllAccountsByYear(String year) {
		return accountService.getAllAccountsByYear(year);
	}

	@Override
	public ByteArrayInputStream generateMarathiExcelForYear(String year) throws IOException {
		return accountService.generateMarathiExcelForYear(year);
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadAccountsReport(String year) throws IOException {
		return accountService.downloadAccountsReport(year);
	}

	@Override
	public Map<String, Object> getAllAccountsByYear(int page, int size, String year) {
		return accountService.getAllAccountsByYear(page, size, year);
	}

}
