package com.pipc.dashboard.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

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
	public Page<AccountsEntity> getAccounts(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return accountsBusiness.getAllAccounts(page, size);
	}

	@GetMapping("/download/{year}")

	public ResponseEntity<InputStreamResource> downloadMarathiExcel(@PathVariable String year) throws IOException {
		ByteArrayInputStream in = accountsBusiness.generateMarathiExcelForYear(year);
		InputStreamResource file = new InputStreamResource(in);

		String fileName = "PIPC_" + year + "_मराठी.xlsx";
		String encodedFileName = UriUtils.encode(fileName, StandardCharsets.UTF_8);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
				.contentType(
						MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(file);
	}

}
