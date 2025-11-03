package com.pipc.dashboard.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import com.pipc.dashboard.accounts.repository.AccountsEntity;
import com.pipc.dashboard.accounts.request.AccountsRequest;
import com.pipc.dashboard.accounts.response.AccountsResponse;

public interface AcountService {

	AccountsResponse saveOrUpdateAccounts(AccountsRequest request);

	Page<AccountsEntity> getAllAccounts(int page, int size);

	List<AccountsEntity> getAllAccountsByYear(String year);

	ByteArrayInputStream generateMarathiExcelForYear(String year) throws IOException;

	ResponseEntity<InputStreamResource> downloadAccountsReport(String year) throws IOException;

}
