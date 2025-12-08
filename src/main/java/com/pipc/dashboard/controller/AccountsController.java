package com.pipc.dashboard.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pipc.dashboard.accounts.request.AccountsRequest;
import com.pipc.dashboard.accounts.request.PendingParaRequest;
import com.pipc.dashboard.accounts.response.AccountsResponse;
import com.pipc.dashboard.accounts.response.PendingParaResponse;
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
	public Map<String, Object> getAccounts(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return accountsBusiness.getAllAccounts(page, size);
	}

	@GetMapping("/getAllAccountsByYear")
	public Map<String, Object> getAllAccountsByYear(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, String year) {
		return accountsBusiness.getAllAccountsByYear(page, size, year);
	}

	@GetMapping("/downloadAccountReport")
	public ResponseEntity<InputStreamResource> downloadAccountsReport(@RequestParam String year) throws IOException {
		return accountsBusiness.downloadAccountsReport(year);
	}

	@PostMapping("/saveOrUpdatePendingPara")
	public PendingParaResponse savePendingPara(@RequestBody PendingParaRequest request) {
		return accountsBusiness.savePendingPara(request);
	}

	@GetMapping("/getAllPendingPara")
	public PendingParaResponse getAllPendingPara(@RequestParam Integer year) {
		return accountsBusiness.getAllPendingPara(year);
	}

	@GetMapping("/downloadPendingPara")
	public ResponseEntity<InputStreamResource> downloadPendingPara(@RequestParam Integer year) throws Exception {
		return accountsBusiness.downloadPendingPara(year);
	}

}
