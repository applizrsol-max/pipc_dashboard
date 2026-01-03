package com.pipc.dashboard.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.MDC;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.pipc.dashboard.accounts.request.AccountsRequest;
import com.pipc.dashboard.accounts.request.PendingParaRequest;
import com.pipc.dashboard.accounts.response.AccountsResponse;
import com.pipc.dashboard.accounts.response.PendingParaResponse;
import com.pipc.dashboard.business.AccountsBusiness;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/pipc/dashboard/accounts")
@RequiredArgsConstructor
public class AccountsController {

	private final AccountsBusiness accountsBusiness;

	/*
	 * ========================= SAVE / UPDATE ACCOUNTS =========================
	 */
	@PostMapping("/saveOrUpdateAccounts")
	public AccountsResponse saveOrUpdateReport(@RequestBody AccountsRequest request) {

		log.info("Save/Update Accounts | corrId={}", MDC.get("correlationId"));
		return accountsBusiness.saveAccounts(request);
	}

	/*
	 * ========================= GET ACCOUNTS BY YEAR =========================
	 */
	@GetMapping("/getAllAccountsByYear")
	public Map<String, List<JsonNode>> getAllAccountsByYear(@RequestParam String year) {

		log.debug("Get Accounts By Year={} | corrId={}", year, MDC.get("correlationId"));

		return accountsBusiness.getAllAccountsByYear(year);
	}

	/*
	 * ========================= DOWNLOAD ACCOUNT REPORT =========================
	 */
	@GetMapping("/downloadAccountReport")
	public ResponseEntity<InputStreamResource> downloadAccountsReport(@RequestParam String year) throws IOException {

		log.info("Download Account Report | year={} | corrId={}", year, MDC.get("correlationId"));

		return accountsBusiness.downloadAccountsReport(year);
	}

	/*
	 * ========================= SAVE / UPDATE PENDING PARA
	 * =========================
	 */
	@PostMapping("/saveOrUpdatePendingPara")
	public PendingParaResponse savePendingPara(@RequestBody PendingParaRequest request) {

		log.info("Save/Update PendingPara | corrId={}", MDC.get("correlationId"));

		return accountsBusiness.savePendingPara(request);
	}

	/*
	 * ========================= GET ALL PENDING PARA =========================
	 */
	@GetMapping("/getAllPendingPara")
	public PendingParaResponse getAllPendingPara(@RequestParam Integer year) {

		log.debug("Get PendingPara | year={} | corrId={}", year, MDC.get("correlationId"));

		return accountsBusiness.getAllPendingPara(year);
	}

	/*
	 * ========================= DOWNLOAD PENDING PARA =========================
	 */
	@GetMapping("/downloadPendingPara")
	public ResponseEntity<InputStreamResource> downloadPendingPara(@RequestParam Integer year) throws Exception {

		log.info("Download PendingPara | year={} | corrId={}", year, MDC.get("correlationId"));

		return accountsBusiness.downloadPendingPara(year);
	}
}
