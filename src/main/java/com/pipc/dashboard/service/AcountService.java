package com.pipc.dashboard.service;

import com.pipc.dashboard.accounts.request.AccountsRequest;
import com.pipc.dashboard.accounts.response.AccountsResponse;

public interface AcountService {

	AccountsResponse saveOrUpdateAccounts(AccountsRequest request);

}
