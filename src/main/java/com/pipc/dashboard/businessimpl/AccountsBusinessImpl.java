package com.pipc.dashboard.businessimpl;

import org.springframework.data.domain.Page;
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
	public Page<AccountsEntity> getAllAccounts(int page, int size) {
		return accountService.getAllAccounts(page,size);
	}

}
