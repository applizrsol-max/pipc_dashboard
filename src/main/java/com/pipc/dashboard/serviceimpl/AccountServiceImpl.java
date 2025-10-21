package com.pipc.dashboard.serviceimpl;

import java.util.Map;

import org.slf4j.MDC;
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
            // --- Get current user safely and make final for lambda ---
            String userFromMDC = MDC.get("user");
            final String currentUser = (userFromMDC != null) ? userFromMDC : "SYSTEM";

            String accountsYear = request.getAccountsYear();
            StringBuilder actionSummary = new StringBuilder();

            for (Map.Entry<String, JsonNode> entry : request.getReports().entrySet()) {
                String category = entry.getKey();
                JsonNode incomingData = entry.getValue();

                // --- Fetch existing entity or create new ---
                AccountsEntity entity = accountRepo.findByCategoryNameAndProjectYear(category, accountsYear)
                        .orElseGet(() -> AccountsEntity.builder()
                                .createdBy(currentUser)
                                .updatedBy(currentUser) // initially same as createdBy
                                .recordFlag("C")
                                .build());

                entity.setCategoryName(category);
                entity.setProjectYear(accountsYear);

                ObjectNode existingNode = JsonUtils.ensureObjectNode(entity.getAccountsData());
                ObjectNode incomingNode = JsonUtils.ensureObjectNode(incomingData);

                // --- Detect changes recursively ---
                boolean hasChanged = JsonUtils.mergeAndDetectChanges(existingNode, incomingNode);

                if (entity.getId() == null) {
                    // New record
                    entity.setAccountsData(existingNode);
                    entity.setUpdatedBy(currentUser);
                    accountRepo.save(entity);
                    actionSummary.append(category)
                                 .append(": created successfully by ")
                                 .append(currentUser)
                                 .append("; ");
                } else if (hasChanged) {
                    // Existing record updated
                    entity.setAccountsData(existingNode);
                    entity.setUpdatedBy(currentUser); // updated by user performing update
                    entity.setRecordFlag("U");
                    accountRepo.save(entity);
                    actionSummary.append(category)
                                 .append(": updated successfully by ")
                                 .append(currentUser)
                                 .append("; ");
                } else {
                    // No changes
                    actionSummary.append(category)
                                 .append(": no changes; ");
                }

                // Ensure createdBy never null
                if (entity.getCreatedBy() == null) {
                    entity.setCreatedBy(currentUser);
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
}
