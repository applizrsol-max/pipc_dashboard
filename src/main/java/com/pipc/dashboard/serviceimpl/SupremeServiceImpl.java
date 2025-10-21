package com.pipc.dashboard.serviceimpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

	@Override
	public SupremaResponse saveOrUpdateSuprema(SupremaRequest request) {
		SupremaResponse response = new SupremaResponse();
		ApplicationError error = new ApplicationError();

		try {
			String user = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
			final String currentUser = user;

			String projectYear = request.getProjectYear();
			List<String> createdProjects = new ArrayList<>();
			List<String> updatedProjects = new ArrayList<>();

			for (JsonNode row : request.getRows()) {
				String projectName = row.get("prakalchenav").asText();
				Integer rowId = row.has("rowId") ? row.get("rowId").asInt() : null;

				if (rowId == null)
					continue; // skip invalid row

				// Fetch row by projectYear + rowId + projectName
				Optional<SupremaEntity> optionalEntity = supremaRepo
						.findByProjectYearAndRowIdAndProjectName(projectYear, rowId, projectName);

				SupremaEntity entity;

				if (optionalEntity.isPresent()) {
					entity = optionalEntity.get();

					// Only update if there is a change
					JsonNode existingData = entity.getSupremaData();
					if (!existingData.equals(row)) {
						entity.setSupremaData(row);
						entity.setUpdatedBy(currentUser);
						entity.setUpdatedDatetime(LocalDateTime.now());
						entity.setRecordFlag("U");
						updatedProjects.add(projectName);
						supremaRepo.save(entity);
					}
				} else {
					// Create new row
					entity = SupremaEntity.builder().projectName(projectName).projectYear(projectYear).rowId(rowId)
							.supremaData(row).createdBy(currentUser).updatedBy(currentUser)
							.createdDatetime(LocalDateTime.now()).updatedDatetime(LocalDateTime.now()).recordFlag("C")
							.build();

					supremaRepo.save(entity);
					createdProjects.add(projectName);
				}
			}

			// Build concise response message
			StringBuilder desc = new StringBuilder();
			if (!createdProjects.isEmpty()) {
				desc.append("Created proposals: ").append(String.join(", ", createdProjects)).append(". ");
			}
			if (!updatedProjects.isEmpty()) {
				desc.append("Updated proposals: ").append(String.join(", ", updatedProjects)).append(". ");
			}
			if (createdProjects.isEmpty() && updatedProjects.isEmpty()) {
				desc.append("No changes detected.");
			}
			desc.append(" Changes performed by ").append(currentUser).append(".");

			error.setErrorCode("0");
			error.setErrorDescription(desc.toString());

		} catch (Exception e) {
			error.setErrorCode("1");
			error.setErrorDescription("Error while saving suprema data: " + e.getMessage());
		}

		response.setErrorDetails(error);
		return response;

	}

	@Override
	public Page<SupremaEntity> getSupremaByProjectYear(String projectYear, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("rowId").ascending());
		return supremaRepo.findByProjectYear(projectYear, pageable);
	}
}
