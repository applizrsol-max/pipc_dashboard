package com.pipc.dashboard.serviceimpl;

import java.util.Map;

import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pipc.dashboard.accounts.repository.AccountsEntity;
import com.pipc.dashboard.accounts.repository.AccountsRepository;
import com.pipc.dashboard.accounts.request.AccountsRequest;
import com.pipc.dashboard.accounts.response.AccountsResponse;
import com.pipc.dashboard.service.AcountService;
import com.pipc.dashboard.utility.ApplicationError;
import com.pipc.dashboard.utility.JsonUtils;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AccountServiceImpl implements AcountService {

	private final AccountsRepository accountRepo;

	public AccountServiceImpl(AccountsRepository accountRepo) {
		this.accountRepo = accountRepo;
	}

	@Override
	public AccountsResponse saveOrUpdateAccounts(AccountsRequest request) {

		AccountsResponse response = new AccountsResponse();
		ApplicationError error = new ApplicationError();

		try {
			String userFromMDC = MDC.get("user");
			final String currentUser = (userFromMDC != null) ? userFromMDC : "SYSTEM";

			String accountsYear = request.getAccountsYear();
			StringBuilder actionSummary = new StringBuilder();

			// Iterate over categories
			for (Map.Entry<String, JsonNode> categoryEntry : request.getReports().entrySet()) {
				String category = categoryEntry.getKey();
				JsonNode rowsArray = categoryEntry.getValue();

				if (rowsArray.isArray()) {
					for (JsonNode rowNode : rowsArray) {

						// Get rowId for uniqueness
						int rowId = rowNode.has("rowId") ? rowNode.get("rowId").asInt() : -1;
						if (rowId == -1) {
							actionSummary.append(category).append(": row without rowId skipped; ");
							continue;
						}

						// Fetch existing entity by category + rowId + accountsYear
						AccountsEntity entity = accountRepo
								.findByCategoryNameAndProjectYearAndRowId(category, accountsYear, rowId)
								.orElseGet(() -> AccountsEntity.builder().createdBy(currentUser).updatedBy(currentUser)
										.recordFlag("C").categoryName(category).projectYear(accountsYear).rowId(rowId)
										.build());

						ObjectNode existingNode = JsonUtils.ensureObjectNode(entity.getAccountsData());
						ObjectNode incomingNode = JsonUtils.ensureObjectNode(rowNode);

						boolean hasChanged = JsonUtils.mergeAndDetectChanges(existingNode, incomingNode);

						if (entity.getId() == null) {
							entity.setAccountsData(existingNode);
							entity.setUpdatedBy(currentUser);
							accountRepo.save(entity);
							actionSummary.append(category).append(" rowId ").append(rowId).append(": created; ");
						} else if (hasChanged) {
							entity.setAccountsData(existingNode);
							entity.setUpdatedBy(currentUser);
							entity.setRecordFlag("U");
							accountRepo.save(entity);
							actionSummary.append(category).append(" rowId ").append(rowId).append(": updated; ");
						} else {
							actionSummary.append(category).append(" rowId ").append(rowId).append(": no changes; ");
						}

						if (entity.getCreatedBy() == null) {
							entity.setCreatedBy(currentUser);
						}
					}
				}
			}

			error.setErrorCode("0");
			error.setErrorDescription(actionSummary.toString());

		} catch (Exception e) {
			error.setErrorCode("1");
			error.setErrorDescription("Error while saving data: " + e.getMessage());
		}

		response.setErrorDetails(error);
		return response;
	}

	public Page<AccountsEntity> getAllAccounts(int page, int size) {
		return accountRepo.findAll(PageRequest.of(page, size));
	}

}