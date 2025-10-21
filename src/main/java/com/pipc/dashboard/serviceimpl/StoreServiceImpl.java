package com.pipc.dashboard.serviceimpl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipc.dashboard.service.StoreService;
import com.pipc.dashboard.store.repository.StoreEntity;
import com.pipc.dashboard.store.repository.StoreRepository;
import com.pipc.dashboard.store.request.DepartmentSection;
import com.pipc.dashboard.store.request.StoreRequest;
import com.pipc.dashboard.store.response.StoreResponse;
import com.pipc.dashboard.utility.ApplicationError;

@Service
public class StoreServiceImpl implements StoreService {

	private final StoreRepository storeRepository;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public StoreServiceImpl(StoreRepository storeRepository) {
		this.storeRepository = storeRepository;
	}

	@Override
	public StoreResponse saveOrUpdate(StoreRequest storeRequest, String createdBy) {
		StoreResponse response = new StoreResponse();
		ApplicationError error = new ApplicationError();
		StringBuilder statusMessage = new StringBuilder();

		try {
			Integer ekunEkandar = storeRequest.getEkunEkandar();

			for (DepartmentSection dept : storeRequest.getDepartments()) {

				// ✅ Always create new entry (no update, no skip)
				StoreEntity entity = new StoreEntity();
				entity.setDeptName(dept.getDepartmentName());

				JsonNode data = objectMapper.valueToTree(dept);
				entity.setData(data);

				entity.setEkun(dept.getEkun());
				entity.setEkunEkandar(ekunEkandar);
				entity.setCreatedBy(createdBy);
				entity.setUpdatedBy(createdBy); // never null
				entity.setCreatedAt(LocalDateTime.now());
				entity.setUpdatedAt(LocalDateTime.now());
				entity.setFlag("C");

				storeRepository.save(entity);

				statusMessage.append("Created: ").append(dept.getDepartmentName()).append(" | ");
			}

			// ✅ Prepare final response
			error.setErrorCode("0");
			error.setErrorDescription("Departments created successfully. " + statusMessage.toString());
			response.setErrorDetails(error);
			response.setMessage("All records created successfully.");
			return response;

		} catch (Exception e) {
			error.setErrorCode("1");
			error.setErrorDescription("Error while saving data: " + e.getMessage());
			response.setErrorDetails(error);
			response.setMessage("Failed to save reports.");
			return response;
		}
	}
}
