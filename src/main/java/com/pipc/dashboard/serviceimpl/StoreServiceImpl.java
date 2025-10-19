package com.pipc.dashboard.serviceimpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
	        boolean ekunEkandarChanged = false;
	        Integer newEkunEkandar = storeRequest.getEkunEkandar();

	        // ✅ Fetch existing departments from DB
	        List<StoreEntity> allEntities = storeRepository.findAll();
	        Map<String, StoreEntity> existingMap = allEntities.stream()
	                .collect(Collectors.toMap(StoreEntity::getDeptName, e -> e));

	        for (DepartmentSection dept : storeRequest.getDepartments()) {
	            StoreEntity entity = existingMap.get(dept.getDepartmentName());
	            JsonNode newData = objectMapper.valueToTree(dept);
	            boolean deptChanged = false;
	            List<String> changedFields = new ArrayList<>();

	            if (entity != null) {
	                // Compare existing and new data
	                JsonNode oldData = entity.getData();
	                if (!oldData.equals(newData)) {
	                    deptChanged = true;
	                    changedFields.add("data");
	                }

	                if (!Objects.equals(entity.getEkun(), dept.getEkun())) {
	                    deptChanged = true;
	                    changedFields.add("ekun");
	                }

	                if (!Objects.equals(entity.getEkunEkandar(), newEkunEkandar)) {
	                    ekunEkandarChanged = true;
	                    changedFields.add("ekunEkandar");
	                }

	                // ✅ Update only if necessary
	                if (deptChanged) {
	                    entity.setData(newData);
	                    entity.setEkun(dept.getEkun());
	                    entity.setEkunEkandar(newEkunEkandar);
	                    entity.setFlag("U");
	                    entity.setUpdatedBy(createdBy != null ? createdBy : entity.getCreatedBy());
	                    entity.setUpdatedAt(LocalDateTime.now());
	                    storeRepository.save(entity);

	                    statusMessage.append("Updated: ")
	                            .append(dept.getDepartmentName())
	                            .append(" → Fields changed: ")
	                            .append(String.join(", ", changedFields))
	                            .append(" | ");
	                }

	            } else {
	                // ✅ New department entry
	                entity = new StoreEntity();
	                entity.setDeptName(dept.getDepartmentName());
	                entity.setData(newData);
	                entity.setEkun(dept.getEkun());
	                entity.setEkunEkandar(newEkunEkandar);
	                entity.setCreatedBy(createdBy);
	                entity.setUpdatedBy(createdBy);
	                entity.setCreatedAt(LocalDateTime.now());
	                entity.setUpdatedAt(LocalDateTime.now());
	                entity.setFlag("C");
	                storeRepository.save(entity);

	                statusMessage.append("Created: ")
	                        .append(dept.getDepartmentName())
	                        .append(" | ");
	            }
	        }

	        // ✅ If ekunEkandar changed, update all departments' totals
	        if (ekunEkandarChanged) {
	            for (StoreEntity entity : allEntities) {
	                if (!Objects.equals(entity.getEkunEkandar(), newEkunEkandar)) {
	                    entity.setEkunEkandar(newEkunEkandar);
	                    entity.setFlag("U");
	                    entity.setUpdatedBy(createdBy != null ? createdBy : entity.getCreatedBy());
	                    entity.setUpdatedAt(LocalDateTime.now());
	                    storeRepository.save(entity);

	                    statusMessage.append("EkunEkandar updated for: ")
	                            .append(entity.getDeptName())
	                            .append(" | ");
	                }
	            }
	        }

	        // ✅ Prepare final response
	        error.setErrorCode("0");
	        error.setErrorDescription("Success");
	        response.setErrorDetails(error);
	        response.setMessage("Reports updated successfully. " + statusMessage.toString());
	        return response;

	    } catch (Exception e) {
	        error.setErrorCode("1");
	        error.setErrorDescription("Unexpected error: " + e.getMessage());
	        response.setErrorDetails(error);
	        response.setMessage("Failed to save or update reports.");
	        return response;
	    }
	}

	/**
	 * Utility to find changed fields between two JsonNodes
	 */
	private List<String> getChangedFields(JsonNode oldData, JsonNode newData) {
		List<String> changed = new ArrayList<>();
		Iterator<String> fieldNames = newData.fieldNames();
		while (fieldNames.hasNext()) {
			String field = fieldNames.next();
			JsonNode oldVal = oldData.get(field);
			JsonNode newVal = newData.get(field);
			if (oldVal == null || !oldVal.equals(newVal)) {
				changed.add(field);
			}
		}
		return changed;
	}

}
