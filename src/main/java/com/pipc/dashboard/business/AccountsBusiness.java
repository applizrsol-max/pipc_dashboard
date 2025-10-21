package com.pipc.dashboard.business;

import com.pipc.dashboard.accounts.request.AccountsRequest;
import com.pipc.dashboard.accounts.response.AccountsResponse;

public interface AccountsBusiness {

	AccountsResponse saveAccounts(AccountsRequest request);

}
