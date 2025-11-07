package com.pipc.dashboard.serviceimpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipc.dashboard.drawing.repository.DamInspectionEntity;
import com.pipc.dashboard.drawing.repository.DamInspectionRepository;
import com.pipc.dashboard.drawing.repository.DamMetaEntity;
import com.pipc.dashboard.drawing.repository.DamMetaRepository;
import com.pipc.dashboard.drawing.repository.DamNalikaEntity;
import com.pipc.dashboard.drawing.repository.DamNalikaRepository;
import com.pipc.dashboard.drawing.repository.DamSafetyEntity;
import com.pipc.dashboard.drawing.repository.DamSafetyRepository;
import com.pipc.dashboard.drawing.repository.PralambitBhusampadanEntity;
import com.pipc.dashboard.drawing.repository.PralambitBhusampadanRepository;
import com.pipc.dashboard.drawing.request.DamDynamicRow;
import com.pipc.dashboard.drawing.request.DamInspectionRequest;
import com.pipc.dashboard.drawing.request.DamNalikaRequest;
import com.pipc.dashboard.drawing.request.DamSafetyRequest;
import com.pipc.dashboard.drawing.request.DepartmentData;
import com.pipc.dashboard.drawing.request.InspectionRow;
import com.pipc.dashboard.drawing.request.NalikaDepartmentData;
import com.pipc.dashboard.drawing.request.NalikaRow;
import com.pipc.dashboard.drawing.request.PralambitBhusampadanRequest;
import com.pipc.dashboard.drawing.request.PralambitBhusampadanRow;
import com.pipc.dashboard.drawing.request.PralambitVishay;
import com.pipc.dashboard.drawing.response.DamInspectionResponse;
import com.pipc.dashboard.drawing.response.DamNalikaResponse;
import com.pipc.dashboard.drawing.response.DamSafetyResponse;
import com.pipc.dashboard.drawing.response.PralambitBhusampadanResponse;
import com.pipc.dashboard.service.DrawingService;
import com.pipc.dashboard.utility.ApplicationError;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class DrawingServiceImpl implements DrawingService {
	@Autowired
	private DamSafetyRepository damRepository;

	@Autowired
	private DamMetaRepository damMetaRepository;
	@Autowired
	private DamInspectionRepository damInspectionRepository;

	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private DamNalikaRepository damNalikaRepository;

	@Autowired
	private PralambitBhusampadanRepository pralambitBhusampadanRepository;

	@Transactional
	@Override
	public DamSafetyResponse saveOrUpdateDamSafety(DamSafetyRequest damSafetyRequest) {

		DamSafetyResponse response = new DamSafetyResponse();
		ApplicationError error = new ApplicationError();

		StringBuilder log = new StringBuilder();
		int updated = 0, created = 0, deleted = 0;
		String userFromMDC = MDC.get("user");

		try {
			// ---------- META HANDLING ----------
			DamMetaEntity savedMeta;

			if (damSafetyRequest.getDamMetaData() != null) {
				String title = damSafetyRequest.getDamMetaData().getTitle();
				String period = damSafetyRequest.getDamMetaData().getPeriod();
				String unit = damSafetyRequest.getDamMetaData().getUnit();

				Optional<DamMetaEntity> existingMetaOpt = damMetaRepository.findByTitleAndPeriod(title, period);

				if (existingMetaOpt.isPresent()) {
					DamMetaEntity meta = existingMetaOpt.get();
					boolean changed = !Objects.equals(meta.getUnit(), unit);
					if (changed) {
						meta.setUnit(unit);
						meta.setUpdatedAt(LocalDateTime.now());
						meta.setUpdatedBy(userFromMDC);
						damMetaRepository.save(meta);
						log.append("Updated Meta: [Title=").append(title).append(", Period=").append(period)
								.append("]. ");
					}
					savedMeta = meta;
				} else {
					DamMetaEntity meta = new DamMetaEntity();
					meta.setTitle(title);
					meta.setPeriod(period);
					meta.setUnit(unit);
					meta.setCreatedAt(LocalDateTime.now());
					meta.setUpdatedAt(LocalDateTime.now());
					meta.setCreatedBy(userFromMDC);
					meta.setUpdatedBy(userFromMDC);
					damMetaRepository.save(meta);
					savedMeta = meta;
					log.append("Created new Meta: [Title=").append(title).append(", Period=").append(period)
							.append("]. ");
				}
			} else {
				response.setMessage("Meta information missing in request.");
				error.setErrorCode("META_MISSING");
				error.setErrorDescription("Meta section is required for Dam Safety request.");
				response.setErrorDetails(error);
				return response;
			}

			// ---------- ROW HANDLING ----------
			for (DamDynamicRow row : damSafetyRequest.getRows()) {

				if (row.getRowId() == null || row.getYear() == null || row.getMonth() == null)
					continue;

				String projectName = (row.getData() != null) ? (String) row.getData().get("projectName") : null;

				// ---------- DELETE ----------
				if ("D".equalsIgnoreCase(row.getFlag())) {
					damRepository.findByDeleteIdAndYearAndMonthAndProjectNameAndMetaId(row.getDeleteId(), row.getYear(),
							row.getMonth(), projectName, savedMeta.getId()).ifPresent(entity -> {
								damRepository.delete(entity);
							});
					deleted++;
					log.append("Deleted deleteId ").append(row.getDeleteId()).append(", project '").append(projectName)
							.append("' for metaId ").append(savedMeta.getId()).append(". ");
					continue;
				}

				// ---------- CREATE or UPDATE ----------
				Optional<DamSafetyEntity> existingOpt = damRepository.findByRowIdAndYearAndMonthAndProjectNameAndMetaId(
						row.getRowId(), row.getYear(), row.getMonth(), projectName, savedMeta.getId());

				JsonNode incomingJson;
				try {
					incomingJson = objectMapper.valueToTree(row.getData());
				} catch (Exception e) {
					incomingJson = objectMapper.createObjectNode();
				}

				if (existingOpt.isPresent()) {
					DamSafetyEntity entity = existingOpt.get();

					boolean changed = !Objects.equals(entity.getData(), incomingJson)
							|| !Objects.equals(entity.getYear(), row.getYear())
							|| !Objects.equals(entity.getMonth(), row.getMonth())
							|| !Objects.equals(entity.getProjectName(), projectName);

					if (changed) {
						entity.setData(incomingJson);
						entity.setYear(row.getYear());
						entity.setMonth(row.getMonth());
						entity.setProjectName(projectName);
						entity.setUpdatedBy(userFromMDC);
						entity.setUpdatedAt(LocalDateTime.now());
						entity.setFlag("U");
						damRepository.save(entity);
						updated++;
						log.append("Updated rowId ").append(row.getRowId()).append(", project '").append(projectName)
								.append("' for metaId ").append(savedMeta.getId()).append(". ");
					}

				} else {
					DamSafetyEntity entity = new DamSafetyEntity();
					entity.setDeleteId(row.getDeleteId());
					entity.setRowId(row.getRowId());
					entity.setYear(row.getYear());
					entity.setMonth(row.getMonth());
					entity.setProjectName(projectName);
					entity.setData(incomingJson);
					entity.setMeta(savedMeta);
					entity.setCreatedBy(userFromMDC);
					entity.setUpdatedBy(userFromMDC);
					entity.setCreatedAt(LocalDateTime.now());
					entity.setUpdatedAt(LocalDateTime.now());
					entity.setFlag("C");
					damRepository.save(entity);
					created++;
					log.append("Created rowId ").append(row.getRowId()).append(", project '").append(projectName)
							.append("' for metaId ").append(savedMeta.getId()).append(". ");
				}
			}

			// ---------- SUCCESS RESPONSE ----------
			response.setMessage(String.format("Processed: %d updated, %d created, %d deleted. %s", updated, created,
					deleted, log.toString()));

			error.setErrorCode("SUCCESS");
			error.setErrorDescription("Data saved or updated successfully.");
			response.setErrorDetails(error);

			return response;

		} catch (Exception e) {
			// ---------- ERROR HANDLING ----------
			ApplicationError err = new ApplicationError();
			err.setErrorCode("DAM_SAVE_ERROR");
			err.setErrorDescription(e.getMessage());
			response.setMessage("Error while saving Dam Safety Data.");
			response.setErrorDetails(err);
			return response;
		}
	}

	@Override
	public DamSafetyResponse getDamSafetyData(String year, int page, int size) {
		DamSafetyResponse response = new DamSafetyResponse();
		ApplicationError error = new ApplicationError();

		try {
			Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
			List<DamMetaEntity> metaList = damMetaRepository.findByPeriodContaining(year);

			if (metaList.isEmpty()) {
				response.setMessage("No records found for year: " + year);
				error.setErrorCode("NO_DATA_FOUND");
				error.setErrorDescription("No dam safety data found for the given year.");
				response.setErrorDetails(error);
				return response;
			}

			List<Object> metaResponses = new ArrayList<>();

			for (DamMetaEntity meta : metaList) {
				Page<DamSafetyEntity> rowsPage = damRepository.findByMetaId(meta.getId(), pageable);

				List<Map<String, Object>> rowList = rowsPage.getContent().stream().map(entity -> {
					Map<String, Object> rowMap = new LinkedHashMap<>();
					rowMap.put("rowId", entity.getRowId());
					rowMap.put("year", entity.getYear());
					rowMap.put("month", entity.getMonth());
					rowMap.put("data", entity.getData());
					rowMap.put("flag", entity.getFlag());
					return rowMap;
				}).collect(Collectors.toList());

				Map<String, Object> metaBlock = new LinkedHashMap<>();
				Map<String, Object> metaData = new LinkedHashMap<>();
				metaData.put("title", meta.getTitle());
				metaData.put("period", meta.getPeriod());
				metaData.put("unit", meta.getUnit());

				metaBlock.put("meta", metaData);
				metaBlock.put("rows", rowList);
				metaBlock.put("totalElements", rowsPage.getTotalElements());
				metaBlock.put("totalPages", rowsPage.getTotalPages());
				metaBlock.put("pageNumber", rowsPage.getNumber());

				metaResponses.add(metaBlock);
			}

			response.setData(metaResponses);
			response.setMessage("Data fetched successfully for year: " + year);
			error.setErrorCode("SUCCESS");
			error.setErrorDescription("Data fetched successfully.");
			response.setErrorDetails(error);

		} catch (Exception e) {
			error.setErrorCode("DAM_FETCH_ERROR");
			error.setErrorDescription(e.getMessage());
			response.setMessage("Error fetching Dam Safety Data.");
			response.setErrorDetails(error);
		}

		return response;
	}

	@Transactional
	@Override
	public DamInspectionResponse saveOrUpdateDamInspection(DamInspectionRequest request) {

		DamInspectionResponse response = new DamInspectionResponse();
		ApplicationError error = new ApplicationError();
		StringBuilder log = new StringBuilder();
		String user = MDC.get("user");
		int created = 0, updated = 0, deleted = 0;

		try {
			String title = request.getTitle();
			String period = request.getPeriod();

			for (Map.Entry<String, DepartmentData> deptEntry : request.getDepartments().entrySet()) {
				String deptKey = deptEntry.getKey();
				DepartmentData deptData = deptEntry.getValue();

				for (InspectionRow row : deptData.getRows()) {
					if (row.getRowId() == null || row.getYear() == null)
						continue;

					Optional<DamInspectionEntity> existingOpt = damInspectionRepository
							.findByTitleAndDepartmentKeyAndRowIdAndYearAndMonthAndPeriod(title, deptKey, row.getRowId(),
									row.getYear(), row.getMonth(), period);

					JsonNode jsonData = objectMapper.valueToTree(row.getData());
					String flag = row.getFlag() == null ? "" : row.getFlag().trim().toUpperCase();

					// --- DELETE ---
					if ("D".equals(flag)) {
						existingOpt.ifPresent(entity -> {
							damInspectionRepository.delete(entity);
							log.append("Deleted row ").append(row.getRowId()).append(" from dept ").append(deptKey)
									.append(". ");
						});
						deleted++;
						continue;
					}

					// --- UPDATE ---
					if (existingOpt.isPresent()) {
						DamInspectionEntity entity = existingOpt.get();
						entity.setData(jsonData);
						entity.setUpdatedBy(user);
						entity.setUpdatedAt(LocalDateTime.now());
						entity.setFlag("U");
						damInspectionRepository.save(entity);
						updated++;
						log.append("Updated row ").append(row.getRowId()).append(" from dept ").append(deptKey)
								.append(". ");
					}
					// --- CREATE ---
					else {
						DamInspectionEntity entity = new DamInspectionEntity();
						entity.setTitle(title);
						entity.setPeriod(period);
						entity.setDepartmentKey(deptKey);
						entity.setDepartmentName(deptData.getName());
						entity.setRowId(row.getRowId());
						entity.setYear(row.getYear());
						entity.setMonth(row.getMonth());
						entity.setData(jsonData);
						entity.setFlag("C");
						entity.setCreatedAt(LocalDateTime.now());
						entity.setUpdatedAt(LocalDateTime.now());
						entity.setCreatedBy(user);
						entity.setUpdatedBy(user);
						damInspectionRepository.save(entity);
						created++;
						log.append("Created row ").append(row.getRowId()).append(" in dept ").append(deptKey)
								.append(". ");
					}
				}
			}

			response.setMessage(String.format("Processed: %d created, %d updated, %d deleted. %s", created, updated,
					deleted, log.toString()));

			error.setErrorCode("INSPECTION_SAVE_SUCCESS");
			error.setErrorDescription("Data processed successfully.");
			response.setErrorDetails(error);

		} catch (Exception e) {
			error.setErrorCode("INSPECTION_SAVE_ERROR");
			error.setErrorDescription(e.getMessage());
			response.setMessage("Error while saving Dam Inspection data.");
			response.setErrorDetails(error);
		}

		return response;
	}

	@Override
	public DamInspectionResponse getDamInspectionData(String year, String period, String departmentKey, int page,
			int size) {
		DamInspectionResponse response = new DamInspectionResponse();
		ApplicationError error = new ApplicationError();

		try {
			List<Map<String, Object>> finalDataList = new ArrayList<>();

			// ✅ Case 1: Specific department
			if (departmentKey != null && !departmentKey.isEmpty()) {
				Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
				Page<DamInspectionEntity> pageResult = damInspectionRepository.findByYearAndPeriodAndDepartmentKey(year,
						period, departmentKey, pageable);

				for (DamInspectionEntity entity : pageResult.getContent()) {
					finalDataList.add(mapEntityToRecord(entity));
				}

			} else {
				// ✅ Case 2: No department filter → fetch all departments
				List<String> departments = damInspectionRepository.findDistinctDepartmentKeys(year, period);
				if (departments == null || departments.isEmpty()) {
					response.setMessage("No departments found for given year and period.");
					response.setData(Collections.emptyList());
					error.setErrorCode("NO_DEPARTMENT");
					error.setErrorDescription("No department data found.");
					response.setErrorDetails(error);
					return response;
				}

				int perDeptLimit = Math.max(1, size / departments.size()); // at least 1 record per department

				for (String dept : departments) {
					Pageable pageable = PageRequest.of(page, perDeptLimit, Sort.by("updatedAt").descending());
					Page<DamInspectionEntity> deptPage = damInspectionRepository
							.findByYearAndPeriodAndDepartmentKey(year, period, dept, pageable);

					for (DamInspectionEntity entity : deptPage.getContent()) {
						finalDataList.add(mapEntityToRecord(entity));
					}
				}
			}

			// ✅ Sort all fetched records by updatedAt (latest first)
			finalDataList.sort((a, b) -> {
				Object valA = a.get("updatedAt");
				Object valB = b.get("updatedAt");
				if (valA instanceof Comparable && valB instanceof Comparable) {
					return ((Comparable) valB).compareTo(valA);
				}
				return 0;
			});

			// ✅ Fill response
			response.setData(finalDataList);
			response.setMessage("Data fetched successfully for year " + year + " and period " + period);

			error.setErrorCode("INSPECTION_FETCH_SUCCESS");
			error.setErrorDescription("Data fetched successfully.");
			response.setErrorDetails(error);

		} catch (Exception e) {
			error.setErrorCode("INSPECTION_FETCH_ERROR");
			error.setErrorDescription(e.getMessage());
			response.setMessage("Error fetching Dam Inspection data.");
			response.setErrorDetails(error);
			response.setData(Collections.emptyList());
		}

		return response;
	}

	// ✅ Helper Method
	private Map<String, Object> mapEntityToRecord(DamInspectionEntity entity) {
		Map<String, Object> record = new LinkedHashMap<>();
		record.put("id", entity.getId());
		record.put("title", entity.getTitle());
		record.put("period", entity.getPeriod());
		record.put("departmentKey", entity.getDepartmentKey());
		record.put("departmentName", entity.getDepartmentName());
		record.put("rowId", entity.getRowId());
		record.put("year", entity.getYear());
		record.put("month", entity.getMonth());
		record.put("flag", entity.getFlag());
		record.put("data", entity.getData());
		record.put("createdBy", entity.getCreatedBy());
		record.put("createdAt", entity.getCreatedAt());
		record.put("updatedBy", entity.getUpdatedBy());
		record.put("updatedAt", entity.getUpdatedAt());
		return record;
	}

	@Override
	public DamNalikaResponse saveOrUpdateNalika(DamNalikaRequest request) {
		DamNalikaResponse response = new DamNalikaResponse();
		ApplicationError error = new ApplicationError();

		String user = MDC.get("user");
		int created = 0, updated = 0, deleted = 0;
		StringBuilder log = new StringBuilder();

		try {
			for (Map.Entry<String, NalikaDepartmentData> deptEntry : request.getDepartments().entrySet()) {
				String deptKey = deptEntry.getKey();
				NalikaDepartmentData deptData = deptEntry.getValue();

				for (NalikaRow row : deptData.getRows()) {
					if (row.getRowId() == null)
						continue;

					Optional<DamNalikaEntity> existingOpt = damNalikaRepository
							.findByDepartmentKeyAndRowIdAndYearAndMonthAndPeriod(deptKey, row.getRowId(),
									request.getYear(), request.getMonth(), request.getPeriod());

					String flag = row.getFlag() == null ? "" : row.getFlag().trim().toUpperCase();
					JsonNode jsonData = objectMapper.valueToTree(row); // ✅ Fixed

					// ---------- DELETE ----------
					if ("D".equals(flag)) {
						existingOpt.ifPresent(entity -> {
							damNalikaRepository.delete(entity); // ✅ Hard delete (as per previous logic)
						});
						deleted++;
						log.append("Deleted row ").append(row.getRowId()).append(" from dept ").append(deptKey)
								.append(". ");
						continue;
					}

					// ---------- UPDATE ----------
					if (existingOpt.isPresent()) {
						DamNalikaEntity entity = existingOpt.get();
						entity.setData(jsonData);
						entity.setUpdatedBy(user);
						entity.setUpdatedAt(LocalDateTime.now());
						entity.setFlag("U");
						damNalikaRepository.save(entity);
						updated++;
						continue;
					}

					// ---------- CREATE ----------
					DamNalikaEntity entity = new DamNalikaEntity();
					entity.setTitle(request.getTitle());
					entity.setPeriod(request.getPeriod());
					entity.setDepartmentKey(deptKey);
					entity.setDepartmentName(deptData.getDepartmentName());
					entity.setRowId(row.getRowId());
					entity.setYear(request.getYear());
					entity.setMonth(request.getMonth());
					entity.setData(jsonData);
					entity.setFlag("C");
					entity.setCreatedAt(LocalDateTime.now());
					entity.setUpdatedAt(LocalDateTime.now());
					entity.setCreatedBy(user);
					entity.setUpdatedBy(user);
					damNalikaRepository.save(entity);
					created++;
				}
			}

			response.setMessage(String.format("Processed: %d created, %d updated, %d deleted. %s", created, updated,
					deleted, log.toString()));
			error.setErrorCode("NALIKA_SAVE_SUCCESS");
			error.setErrorDescription("Data processed successfully.");
			response.setErrorDetails(error);
			return response;

		} catch (Exception e) {
			error.setErrorCode("NALIKA_SAVE_ERROR");
			error.setErrorDescription(e.getMessage());
			response.setMessage("Error while saving Nalika data.");
			response.setErrorDetails(error);
			return response;
		}
	}

	@Override
	public DamNalikaResponse getNalikaByPeriod(String period, String departmentKey, int page, int size) {
		DamNalikaResponse response = new DamNalikaResponse();
		ApplicationError error = new ApplicationError();

		try {
			Pageable pageable = PageRequest.of(page, size);
			Page<DamNalikaEntity> entityPage;

			if (departmentKey != null && !departmentKey.isEmpty()) {
				entityPage = damNalikaRepository.findByPeriodAndDepartment(period, departmentKey, pageable);
			} else {
				entityPage = damNalikaRepository.findByPeriod(period, pageable);
			}

			List<Map<String, Object>> resultList = new ArrayList<>();

			for (DamNalikaEntity entity : entityPage.getContent()) {
				Map<String, Object> map = new LinkedHashMap<>();
				map.put("id", entity.getId());
				map.put("title", entity.getTitle());
				map.put("period", entity.getPeriod());
				map.put("departmentKey", entity.getDepartmentKey());
				map.put("departmentName", entity.getDepartmentName());
				map.put("rowId", entity.getRowId());
				map.put("year", entity.getYear());
				map.put("month", entity.getMonth());
				map.put("data", entity.getData()); // full JSONB data
				map.put("flag", entity.getFlag());
				map.put("createdAt", entity.getCreatedAt());
				map.put("updatedAt", entity.getUpdatedAt());
				resultList.add(map);
			}

			response.setMessage("Nalika data fetched successfully");
			response.setData(resultList);

			error.setErrorCode("NALIKA_GET_SUCCESS");
			error.setErrorDescription("Data fetched successfully");
			response.setErrorDetails(error);

			return response;

		} catch (Exception e) {
			error.setErrorCode("NALIKA_GET_ERROR");
			error.setErrorDescription(e.getMessage());
			response.setMessage("Error fetching Nalika data");
			response.setErrorDetails(error);
			return response;
		}
	}

	@Override
	public PralambitBhusampadanResponse saveOrUpdatePralambitBhusampadan(PralambitBhusampadanRequest req) {
		PralambitBhusampadanResponse resp = new PralambitBhusampadanResponse();
		ApplicationError err = new ApplicationError();

		String user = MDC.get("user");
		int created = 0, updated = 0, deleted = 0;

		try {
			for (PralambitBhusampadanRow row : req.getData()) {
				for (PralambitVishay v : row.getPralambitVishay()) {
					String flag = v.getFlag() == null ? "" : v.getFlag().trim().toUpperCase();

					Optional<PralambitBhusampadanEntity> existingOpt = pralambitBhusampadanRepository
							.findByPeriodAndKramankAndSubIdAndStar(req.getPeriod(), row.getKramank(), v.getSubId(),
									row.getStar());

					// delete
					if ("D".equals(flag)) {
						existingOpt.ifPresent(pralambitBhusampadanRepository::delete);
						deleted++;
						continue;
					}

					JsonNode json = objectMapper.valueToTree(v);
					if (existingOpt.isPresent()) {
						PralambitBhusampadanEntity e = existingOpt.get();
						if (!Objects.equals(e.getData(), json)) {
							e.setData(json);
							e.setUpdatedBy(user);
							e.setUpdatedAt(LocalDateTime.now());
							e.setFlag("U");
							pralambitBhusampadanRepository.save(e);
							updated++;
						}
					} else {
						PralambitBhusampadanEntity e = new PralambitBhusampadanEntity();
						e.setTitle(req.getTitle());
						e.setPeriod(req.getPeriod());
						e.setKramank(row.getKramank());
						e.setSubId(v.getSubId());
						e.setStar(row.getStar());
						e.setData(json);
						e.setFlag("C");
						e.setCreatedBy(user);
						e.setUpdatedBy(user);
						e.setCreatedAt(LocalDateTime.now());
						e.setUpdatedAt(LocalDateTime.now());
						pralambitBhusampadanRepository.save(e);
						created++;
					}
				}
			}

			resp.setMessage(String.format("Created: %d | Updated: %d | Deleted: %d", created, updated, deleted));
			err.setErrorCode("BHUSAMPADAN_SAVE_OK");
			err.setErrorDescription("Save or update successful");
			resp.setErrorDetails(err);
			return resp;
		} catch (Exception e) {
			err.setErrorCode("BHUSAMPADAN_SAVE_ERR");
			err.setErrorDescription(e.getMessage());
			resp.setErrorDetails(err);
			resp.setMessage("Error saving Pralambit Bhusampadan");
			return resp;
		}
	}

	@Override
	public PralambitBhusampadanResponse getPralambitBhusampadan(String period, String star, int page, int size) {
		PralambitBhusampadanResponse resp = new PralambitBhusampadanResponse();
		ApplicationError err = new ApplicationError();

		try {
			Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
			Page<PralambitBhusampadanEntity> result = (star == null || star.isBlank())
					? pralambitBhusampadanRepository.findByPeriod(period, pageable)
					: pralambitBhusampadanRepository.findByPeriodAndStar(period, star, pageable);

			List<Map<String, Object>> out = new ArrayList<>();
			for (PralambitBhusampadanEntity e : result.getContent()) {
				Map<String, Object> m = new LinkedHashMap<>();
				m.put("id", e.getId());
				m.put("title", e.getTitle());
				m.put("period", e.getPeriod());
				m.put("kramank", e.getKramank());
				m.put("subId", e.getSubId());
				m.put("star", e.getStar());
				m.put("data", objectMapper.convertValue(e.getData(), new TypeReference<Map<String, Object>>() {
				}));
				m.put("flag", e.getFlag());
				m.put("createdAt", e.getCreatedAt());
				out.add(m);
			}

			resp.setData(out);
			resp.setMessage("Data fetched successfully");
			err.setErrorCode("BHUSAMPADAN_GET_OK");
			err.setErrorDescription("Records retrieved");
			resp.setErrorDetails(err);
			return resp;
		} catch (Exception e) {
			err.setErrorCode("BHUSAMPADAN_GET_ERR");
			err.setErrorDescription(e.getMessage());
			resp.setErrorDetails(err);
			resp.setMessage("Error fetching data");
			return resp;
		}
	}
}
