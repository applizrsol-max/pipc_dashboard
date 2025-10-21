package com.pipc.dashboard.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pipc.dashboard.accounts.repository.AccountsEntity;
import com.pipc.dashboard.accounts.request.AccountsRequest;
import com.pipc.dashboard.accounts.response.AccountsResponse;
import com.pipc.dashboard.business.AccountsBusiness;

@RestController
@RequestMapping("/pipc/dashboard/accounts")

public class AccountsController {

	private final AccountsBusiness accountsBusiness;

	public AccountsController(AccountsBusiness accountsBusiness) {
		this.accountsBusiness = accountsBusiness;
	}

	@PostMapping("/saveOrUpdateAccounts")
	public AccountsResponse saveOrUpdateReport(@RequestBody AccountsRequest request) {
		return accountsBusiness.saveAccounts(request);
	}
	

    @GetMapping("/getAccounts")
    public Page<AccountsEntity> getAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return accountsBusiness.getAllAccounts(page, size);
    }
}
