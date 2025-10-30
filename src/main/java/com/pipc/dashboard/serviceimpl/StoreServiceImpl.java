package com.pipc.dashboard.serviceimpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
	@Transactional
	public StoreResponse saveOrUpdate(StoreRequest storeRequest, String createdBy) {
		StoreResponse response = new StoreResponse();
		ApplicationError error = new ApplicationError();
		StringBuilder log = new StringBuilder();

		try {
			Integer newEkunEkandar = storeRequest.getEkunEkandar();

			// ---------- 1) Handle overall ekunEkandar change ----------
			// findExistingEkunEkandar() returns Optional<Integer> (distinct)
			Integer existingOverall = storeRepository.findExistingEkunEkandar().orElse(null);

			// If different (including existing null), update only the rows that actually
			// differ
			if ((existingOverall == null && newEkunEkandar != null)
					|| (existingOverall != null && !existingOverall.equals(newEkunEkandar))) {

				// Use repository bulk update that updates only rows where ekunEkandar <> :total
				// This prevents touching rows that already have the same ekunEkandar.
				storeRepository.updateEkunEkandarAndTimestamp(newEkunEkandar, createdBy);

				log.append("ekunEkandar changed -> updated ekunEkandar on affected rows. ");
			}

			// ---------- 2) For each department: handle dept ekun change and per-row
			// changes ----------
			for (DepartmentSection dept : storeRequest.getDepartments()) {
				String deptName = dept.getDepartmentName();
				Integer newDeptEkun = dept.getEkun();

				// Find current ekun value for department (distinct) if exists
				Optional<Integer> existingDeptEkunOpt = storeRepository.findExistingEkunForDept(deptName);
				Integer existingDeptEkun = existingDeptEkunOpt.orElse(null);

				// If dept ekun changed (including previously null -> now non-null), update only
				// those rows with different ekun
				if ((existingDeptEkun == null && newDeptEkun != null)
						|| (existingDeptEkun != null && !existingDeptEkun.equals(newDeptEkun))) {

					// fetch rows of this department and update only those that differ
					List<StoreEntity> deptRows = storeRepository.findAllByDepartmentName(deptName);
					boolean anyDeptRowUpdated = false;
					for (StoreEntity rowEntity : deptRows) {
						if (rowEntity.getEkun() == null || !rowEntity.getEkun().equals(newDeptEkun)) {
							rowEntity.setEkun(newDeptEkun);
							rowEntity.setUpdatedBy(createdBy);
							rowEntity.setUpdatedAt(LocalDateTime.now());
							rowEntity.setFlag("U");
							anyDeptRowUpdated = true;
						}
					}
					if (anyDeptRowUpdated) {
						storeRepository.saveAll(deptRows); // save only modified rows
						log.append("Department ekun changed for '").append(deptName)
								.append("' -> updated affected rows. ");
					}
				}

				// ---------- 3) Now handle each incoming row (create/update single row)
				// ----------
				if (dept.getRows() == null)
					continue;
				for (VibhagRow row : dept.getRows()) {
					Integer rowId = row.getRowId();
					if (rowId == null)
						continue;

					Optional<StoreEntity> existingEntityOpt = storeRepository.findByDepartmentNameAndRowId(deptName,
							rowId);

					// Convert to JsonNode safely and deterministically
					JsonNode incomingJson;
					try {
						// serialize -> parse to ensure a valid JsonNode (avoids null)
						String jsonStr = objectMapper.writeValueAsString(row);
						incomingJson = objectMapper.readTree(jsonStr);
					} catch (Exception ex) {
						// fallback to empty object — never store null
						incomingJson = objectMapper.createObjectNode();
					}

					if (existingEntityOpt.isPresent()) {
						StoreEntity entity = existingEntityOpt.get();

						// Compare existing JSON with incoming JSON safely (handle null)
						boolean rowJsonChanged = true;
						JsonNode existingJson = entity.getRowsData();
						if (existingJson != null) {
							rowJsonChanged = !existingJson.equals(incomingJson);
						} else {
							// existingJson is null -> treat as change (we'll overwrite)
							rowJsonChanged = true;
						}

						// Only update this row if
						// - its JSON changed OR
						// - dept ekun was changed earlier (we already updated those rows above), or
						// - overall ekunEkandar changed and this row still had different value (note:
						// already handled by bulk update)
						if (rowJsonChanged) {
							entity.setRowsData(incomingJson);
							entity.setEkun(newDeptEkun);
							entity.setEkunEkandar(newEkunEkandar);
							entity.setUpdatedBy(createdBy);
							entity.setUpdatedAt(LocalDateTime.now());
							entity.setFlag("U");
							storeRepository.save(entity);
							log.append("Updated rowId ").append(rowId).append(" in '").append(deptName).append("'. ");
						} else {
							// nothing changed for this row specifically (dept/overall updates already
							// applied above)
						}

					} else {
						// create new record (row-level)
						StoreEntity entity = new StoreEntity();
						entity.setDepartmentName(deptName);
						entity.setRowId(rowId);
						entity.setRowsData(incomingJson);
						entity.setEkun(newDeptEkun);
						entity.setEkunEkandar(newEkunEkandar);
						entity.setCreatedBy(createdBy);
						entity.setUpdatedBy(createdBy);
						entity.setCreatedAt(LocalDateTime.now());
						entity.setUpdatedAt(LocalDateTime.now());
						entity.setFlag("C");
						storeRepository.save(entity);
						log.append("Created rowId ").append(rowId).append(" in '").append(deptName).append("'. ");
					}
				} // end rows loop
			} // end departments loop

			error.setErrorCode("0");
			error.setErrorDescription("Success: " + log.toString());
			response.setErrorDetails(error);
			response.setMessage("Processed successfully.");
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
	public StoreResponse getStores(int page, int size) {
		StoreResponse response = new StoreResponse();
		ApplicationError error = new ApplicationError();

		try {
			// Step 1: get all distinct department names
			List<String> departments = storeRepository.findDistinctDepartmentNames();

			List<DepartmentSection> departmentSections = new ArrayList<>();

			// Step 2: For each department, apply pagination
			for (String dept : departments) {
				Page<StoreEntity> deptPage = storeRepository.findByDepartmentName(dept, PageRequest.of(page, size));

				if (deptPage.isEmpty())
					continue;

				// Map entity -> request-like response
				List<VibhagRow> rows = new ArrayList<>();
				for (StoreEntity entity : deptPage.getContent()) {
					VibhagRow row = new ObjectMapper().convertValue(entity.getRowsData(), VibhagRow.class);
					row.setRowId(entity.getRowId());
					rows.add(row);
				}

				DepartmentSection section = new DepartmentSection();
				section.setDepartmentName(dept);
				section.setEkun(deptPage.getContent().get(0).getEkun());
				section.setRows(rows);

				departmentSections.add(section);
			}

			// Step 3: build response similar to request
			StoreRequest storeData = new StoreRequest();
			storeData.setEkunEkandar(storeRepository.findExistingEkunEkandar().orElse(null));
			storeData.setDepartments(departmentSections);

			response.setData(storeData);
			error.setErrorCode("0");
			error.setErrorDescription("Fetched successfully with department-wise pagination.");
			response.setErrorDetails(error);
			response.setMessage("Success");
			return response;

		} catch (Exception e) {
			error.setErrorCode("1");
			error.setErrorDescription("Error fetching paginated data: " + e.getMessage());
			response.setErrorDetails(error);
			response.setMessage("Failed to fetch data.");
			return response;
		}
	}

}
