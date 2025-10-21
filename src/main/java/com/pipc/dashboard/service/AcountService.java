package com.pipc.dashboard.service;

import org.springframework.data.domain.Page;

import com.pipc.dashboard.accounts.repository.AccountsEntity;
import com.pipc.dashboard.accounts.request.AccountsRequest;
import com.pipc.dashboard.accounts.response.AccountsResponse;

public interface AcountService {

	AccountsResponse saveOrUpdateAccounts(AccountsRequest request);

	Page<AccountsEntity> getAllAccounts(int page, int size);

}
