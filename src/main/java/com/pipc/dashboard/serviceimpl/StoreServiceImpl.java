package com.pipc.dashboard.serviceimpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipc.dashboard.service.StoreService;
import com.pipc.dashboard.store.repository.StoreEntity;
import com.pipc.dashboard.store.repository.StoreRepository;
import com.pipc.dashboard.store.request.DepartmentSection;
import com.pipc.dashboard.store.request.StoreRequest;
import com.pipc.dashboard.store.request.VibhagRow;
import com.pipc.dashboard.store.response.StoreResponse;
import com.pipc.dashboard.utility.ApplicationError;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class StoreServiceImpl implements StoreService {

	private final StoreRepository storeRepository;
	private final ObjectMapper objectMapper;

	public StoreServiceImpl(StoreRepository storeRepository, ObjectMapper objectMapper) {
		this.storeRepository = storeRepository;
		this.objectMapper = objectMapper;
	}

	@Override
	public StoreResponse saveOrUpdate(StoreRequest storeRequest, String createdBy) {
		StoreResponse response = new StoreResponse();
		ApplicationError error = new ApplicationError();
		StringBuilder statusMessage = new StringBuilder();

		try {
			Integer newEkunEkandar = storeRequest.getEkunEkandar();

			// 1️⃣ Check if overall total (ekunEkandar) has changed
			boolean overallChanged = false;
			Optional<Integer> existingOverallOpt = storeRepository.findExistingEkunEkandar();
			if (existingOverallOpt.isPresent()) {
				Integer existingOverall = existingOverallOpt.get();
				if (!existingOverall.equals(newEkunEkandar)) {
					overallChanged = true;
					// Update all rows for overall change
					List<StoreEntity> allRows = storeRepository.findAll();
					for (StoreEntity row : allRows) {
						row.setUpdatedBy(createdBy);
						row.setUpdatedAt(LocalDateTime.now());
						row.setEkunEkandar(newEkunEkandar);
						row.setFlag("U");
						storeRepository.save(row);
					}
					statusMessage.append("Overall total changed → all rows updated | ");
				}
			}

			// 2️⃣ Process each department
			for (DepartmentSection dept : storeRequest.getDepartments()) {
				Integer deptEkun = dept.getEkun();

				// Check if department total changed
				boolean deptChanged = false;
				Optional<Integer> existingDeptEkunOpt = storeRepository
						.findExistingEkunForDept(dept.getDepartmentName());
				if (existingDeptEkunOpt.isPresent()) {
					Integer existingDeptEkun = existingDeptEkunOpt.get();
					if (!existingDeptEkun.equals(deptEkun)) {
						deptChanged = true;
						// Update all rows of this department
						List<StoreEntity> deptRows = storeRepository.findAllByDeptName(dept.getDepartmentName());
						for (StoreEntity row : deptRows) {
							row.setUpdatedBy(createdBy);
							row.setUpdatedAt(LocalDateTime.now());
							row.setEkun(deptEkun);
							row.setFlag("U");
							storeRepository.save(row);
						}
						statusMessage.append("Department total changed → rows of ").append(dept.getDepartmentName())
								.append(" updated | ");
					}
				}

				// 3️⃣ Process individual rows
				for (VibhagRow row : dept.getRows()) {
					Integer rowId = row.getRowId();
					if (rowId == null)
						continue;

					Optional<StoreEntity> optionalEntity = storeRepository
							.findByDeptNameAndRowId(dept.getDepartmentName(), rowId);

					StoreEntity entity;
					JsonNode incomingData = objectMapper.valueToTree(row);

					if (optionalEntity.isPresent()) {
						entity = optionalEntity.get();

						// Only update if department/overall changed OR row data changed
						boolean hasChanged = overallChanged || deptChanged || !entity.getData().equals(incomingData);

						if (hasChanged) {
							entity.setData(incomingData);
							entity.setEkun(deptEkun);
							entity.setEkunEkandar(newEkunEkandar);
							entity.setUpdatedBy(createdBy);
							entity.setUpdatedAt(LocalDateTime.now());
							entity.setFlag("U");
							storeRepository.save(entity);
							statusMessage.append("Updated rowId ").append(rowId).append(" in department: ")
									.append(dept.getDepartmentName()).append(" | ");
						} else {
							statusMessage.append("No change for rowId ").append(rowId).append(" in department: ")
									.append(dept.getDepartmentName()).append(" | ");
						}
					} else {
						// Row does not exist → create
						entity = new StoreEntity();
						entity.setDeptName(dept.getDepartmentName());
						entity.setRowId(rowId);
						entity.setEkun(deptEkun);
						entity.setEkunEkandar(newEkunEkandar);
						entity.setData(incomingData);
						entity.setCreatedBy(createdBy);
						entity.setUpdatedBy(createdBy);
						entity.setCreatedAt(LocalDateTime.now());
						entity.setUpdatedAt(LocalDateTime.now());
						entity.setFlag("C");

						storeRepository.save(entity);
						statusMessage.append("Created rowId ").append(rowId).append(" in department: ")
								.append(dept.getDepartmentName()).append(" | ");
					}
				}
			}

			// ✅ Prepare response
			error.setErrorCode("0");
			error.setErrorDescription("Departments processed successfully. " + statusMessage.toString());
			response.setErrorDetails(error);
			response.setMessage("All rows processed successfully.");
			return response;

		} catch (Exception e) {
			error.setErrorCode("1");
			error.setErrorDescription("Error while saving data: " + e.getMessage());
			response.setErrorDetails(error);
			response.setMessage("Failed to save rows.");
			return response;
		}
	}

	@Override
	public Page<StoreEntity> getStores(int page, int size) {
		return storeRepository.findAll(PageRequest.of(page, size));
	}
}
