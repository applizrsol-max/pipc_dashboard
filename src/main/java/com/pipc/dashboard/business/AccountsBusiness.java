package com.pipc.dashboard.business;

import org.springframework.data.domain.Page;

import com.pipc.dashboard.accounts.repository.AccountsEntity;
import com.pipc.dashboard.accounts.request.AccountsRequest;
import com.pipc.dashboard.accounts.response.AccountsResponse;

public interface AccountsBusiness {

	AccountsResponse saveAccounts(AccountsRequest request);

	Page<AccountsEntity> getAllAccounts(int page, int size);

}
