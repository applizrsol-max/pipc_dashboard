package com.pipc.dashboard.serviceimpl;

import java.time.LocalDateTime;
import java.util.*;

import org.slf4j.MDC;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.pipc.dashboard.service.SupremaService;
import com.pipc.dashboard.suprama.repository.SupremaEntity;
import com.pipc.dashboard.suprama.repository.SupremaRepository;
import com.pipc.dashboard.suprama.request.SupremaRequest;
import com.pipc.dashboard.suprama.response.SupremaResponse;
import com.pipc.dashboard.utility.ApplicationError;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class SupremeServiceImpl implements SupremaService {

    private final SupremaRepository supremaRepo;

    public SupremeServiceImpl(SupremaRepository supremaRepo) {
        this.supremaRepo = supremaRepo;
    }

    // ----------------------------------------------------
    // 🔹 Save or Update Suprema Data
    // ----------------------------------------------------
    @Override
    public SupremaResponse saveOrUpdateSuprema(SupremaRequest request) {
        SupremaResponse response = new SupremaResponse();
        ApplicationError error = new ApplicationError();

        try {
            String currentUser = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
            String projectYear = request.getProjectYear();

            List<String> createdProjects = new ArrayList<>();
            List<String> updatedProjects = new ArrayList<>();

            for (JsonNode row : request.getRows()) {

                // ✅ Dynamic field extraction for flexibility
                Integer rowId = row.has("rowId") ? row.get("rowId").asInt() : null;
                String projectName = extractFieldValue(row, "prakalchenav", "projectname", "project", "name");

                if (rowId == null || projectName == null || projectName.isEmpty())
                    continue; // skip invalid record

                // 🔍 Find existing entry
                Optional<SupremaEntity> optionalEntity =
                        supremaRepo.findByProjectYearAndRowIdAndProjectName(projectYear, rowId, projectName);

                SupremaEntity entity;

                if (optionalEntity.isPresent()) {
                    entity = optionalEntity.get();

                    // ✅ Update only if actual data changes
                    if (!entity.getSupremaData().equals(row)) {
                        entity.setSupremaData(row);
                        entity.setUpdatedBy(currentUser);
                        entity.setUpdatedDatetime(LocalDateTime.now());
                        entity.setRecordFlag("U");
                        supremaRepo.save(entity);
                        updatedProjects.add(projectName + " (RowId: " + rowId + ")");
                    }

                } else {
                    // ✅ Create new entry
                    entity = SupremaEntity.builder()
                            .projectName(projectName)
                            .projectYear(projectYear)
                            .rowId(rowId)
                            .supremaData(row)
                            .createdBy(currentUser)
                            .updatedBy(currentUser)
                            .createdDatetime(LocalDateTime.now())
                            .updatedDatetime(LocalDateTime.now())
                            .recordFlag("C")
                            .build();

                    supremaRepo.save(entity);
                    createdProjects.add(projectName + " (RowId: " + rowId + ")");
                }
            }

            // ✅ Build concise message
            StringBuilder desc = new StringBuilder();
            if (!createdProjects.isEmpty())
                desc.append("Created proposals: ").append(String.join(", ", createdProjects)).append(". ");
            if (!updatedProjects.isEmpty())
                desc.append("Updated proposals: ").append(String.join(", ", updatedProjects)).append(". ");
            if (createdProjects.isEmpty() && updatedProjects.isEmpty())
                desc.append("No changes detected. ");

            desc.append("Changes performed by ").append(currentUser).append(".");

            error.setErrorCode("0");
            error.setErrorDescription(desc.toString());

        } catch (Exception e) {
            error.setErrorCode("1");
            error.setErrorDescription("Error while saving Suprema data: " + e.getMessage());
        }

        response.setErrorDetails(error);
        return response;
    }

    // ----------------------------------------------------
    // 🔹 Paginated Get API
    // ----------------------------------------------------
    @Override
    public Page<SupremaEntity> getSupremaByProjectYear(String projectYear, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("rowId").ascending());
        return supremaRepo.findByProjectYear(projectYear, pageable);
    }

    // ----------------------------------------------------
    // 🔍 Helper Method: extract field safely by matching possible names
    // ----------------------------------------------------
    private String extractFieldValue(JsonNode node, String... possibleNames) {
        if (node == null || !node.isObject()) return null;

        for (String key : possibleNames) {
            for (Iterator<String> it = node.fieldNames(); it.hasNext();) {
                String field = it.next();
                if (field.equalsIgnoreCase(key) || field.toLowerCase().contains(key.toLowerCase())) {
                    JsonNode value = node.get(field);
                    if (value != null && !value.isNull())
                        return value.asText();
                }
            }
        }
        return null;
    }
}
