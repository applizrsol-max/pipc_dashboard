package com.pipc.dashboard.serviceimpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

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
					if (row.getDeleteId() == null)
						continue;

					Optional<DamNalikaEntity> existingOpt = damNalikaRepository
							.findByDepartmentKeyAndRowIdAndYearAndMonthAndPeriod(deptKey, row.getRowId(),
									request.getYear(), request.getMonth(), request.getPeriod());

					Optional<DamNalikaEntity> existingOptDel = damNalikaRepository
							.findByDepartmentKeyAndDeleteIdAndYearAndMonthAndPeriod(deptKey, row.getDeleteId(),
									request.getYear(), request.getMonth(), request.getPeriod());

					String flag = row.getFlag() == null ? "" : row.getFlag().trim().toUpperCase();
					JsonNode jsonData = objectMapper.valueToTree(row); // ✅ Fixed

					// ---------- DELETE ----------
					if ("D".equals(flag)) {
						existingOptDel.ifPresent(entity -> {
							damNalikaRepository.delete(entity); // ✅ Hard delete (as per previous logic)
						});
						deleted++;
						log.append("Deleted row ").append(row.getDeleteId()).append(" from dept ").append(deptKey)
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
					entity.setDeleteId(row.getDeleteId());
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
	@Transactional
	public PralambitBhusampadanResponse saveOrUpdatePralambitBhusampadan(PralambitBhusampadanRequest req) {
		PralambitBhusampadanResponse resp = new PralambitBhusampadanResponse();
		ApplicationError err = new ApplicationError();

		String user = MDC.get("user");
		int created = 0, updated = 0, deleted = 0;

		try {
			for (PralambitBhusampadanRow row : req.getData()) {

				String overallFlag = row.getOverallflag() == null ? "" : row.getOverallflag().trim().toUpperCase();

				// 🔹 CASE 1: Delete all records if overallflag = "D"
				if ("D".equals(overallFlag) && row.getOveralldeleteId() != 0) {
					pralambitBhusampadanRepository.deleteByCustomOverAllDeleteId(row.getOveralldeleteId());
					deleted++;
					continue;
				}

				// 🔹 CASE 2: Process individual subjects
				for (PralambitVishay v : row.getPralambitVishay()) {
					String flag = v.getFlag() == null ? "" : v.getFlag().trim().toUpperCase();

					// 🧹 Delete a specific item by its deleteId
					if ("D".equals(flag) && v.getDeleteId() != 0) {
						pralambitBhusampadanRepository.deleteByCustomDeleteId(v.getDeleteId());
						deleted++;
						continue;
					}

					JsonNode json = objectMapper.valueToTree(v);

					// 🔍 Check if record already exists
					Optional<PralambitBhusampadanEntity> existingOpt = pralambitBhusampadanRepository
							.findByPeriodAndKramankAndSubIdAndStar(req.getPeriod(), row.getKramank(), v.getSubId(),
									row.getStar());

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
						// 🆕 Create new record
						PralambitBhusampadanEntity e = new PralambitBhusampadanEntity();
						e.setTitle(req.getTitle());
						e.setPeriod(req.getPeriod());
						e.setKramank(row.getKramank());
						e.setStar(row.getStar());
						e.setSubId(v.getSubId());
						e.setData(json);
						e.setFlag("C");
						e.setCreatedBy(user);
						e.setUpdatedBy(user);
						e.setCreatedAt(LocalDateTime.now());
						e.setUpdatedAt(LocalDateTime.now());

						// ✅ Set custom delete tracking IDs
						e.setOverAllDeleteId(row.getOveralldeleteId());
						e.setDeleteId(v.getDeleteId());

						pralambitBhusampadanRepository.save(e);
						created++;
					}
				}
			}

			// ✅ Build success response
			resp.setMessage(String.format("Created: %d | Updated: %d | Deleted: %d", created, updated, deleted));
			err.setErrorCode("BHUSAMPADAN_SAVE_OK");
			err.setErrorDescription("Save or update successful");
			resp.setErrorDetails(err);

		} catch (Exception e) {
			err.setErrorCode("BHUSAMPADAN_SAVE_ERR");
			err.setErrorDescription(e.getMessage());
			resp.setErrorDetails(err);
			resp.setMessage("Error saving Pralambit Bhusampadan");
		}

		return resp;
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

	@Override
	public ResponseEntity<InputStreamResource> downloadDamSafetyExcel(String period) throws IOException {
		DamMetaEntity meta = damMetaRepository.findFirstByPeriod(period)
				.orElseThrow(() -> new IllegalArgumentException("No meta found for period: " + period));

		List<DamSafetyEntity> rows = damRepository.findByMetaIdOrderByRowIdAsc(meta.getId());

		Workbook wb = new XSSFWorkbook();
		Sheet sh = wb.createSheet("प्रपत्र-1");

		// ---------- column widths ----------
		sh.setColumnWidth(0, 1600);
		sh.setColumnWidth(1, 7800);
		sh.setColumnWidth(2, 5200);
		for (int c = 3; c <= 10; c++)
			sh.setColumnWidth(c, 3800);
		sh.setColumnWidth(11, 4200);

		// ---------- styles ----------
		CellStyle title = titleStyle(wb);
		CellStyle subTitle = subTitleStyle(wb);
		CellStyle header = headerStyle(wb);
		CellStyle headerCenter = headerCenterStyle(wb);
		CellStyle cellTxt = cellTextStyle(wb);
		CellStyle cellNum = cellNumberStyle(wb);
		CellStyle totalStyle = totalRowStyle(wb);

		int r = 0;

		// ---------- title ----------
		Row t1 = sh.createRow(r++);
		create(sh, t1, 0, "महाराष्ट्र कृष्णा खोरे विकास महामंडळ, पुणे", title);
		sh.addMergedRegion(new CellRangeAddress(0, 0, 0, 11));

		Row t2 = sh.createRow(r++);
		create(sh, t2, 0, meta.getTitle(), subTitle);
		sh.addMergedRegion(new CellRangeAddress(1, 1, 0, 11));

		r++;

		// ---------- period & unit ----------
		Row p = sh.createRow(r++);
		CellRangeAddress unitMerge = new CellRangeAddress(p.getRowNum(), p.getRowNum(), 0, 10);
		sh.addMergedRegion(unitMerge);
		applyBorderToMergedRegion(sh, unitMerge, wb);

		Cell unitCell = p.createCell(0);
		unitCell.setCellValue("क्षेत्र : " + meta.getUnit());
		CellStyle rightAlign = wb.createCellStyle();
		rightAlign.cloneStyleFrom(cellTxt);
		rightAlign.setAlignment(HorizontalAlignment.RIGHT);
		rightAlign.setVerticalAlignment(VerticalAlignment.CENTER);
		unitCell.setCellStyle(rightAlign);

		// ---------- header main ----------
		Row g = sh.createRow(r++);
		headerCenter.setWrapText(true);
		create(sh, g, 0, "अ. क्र.", headerCenter);
		create(sh, g, 1, "प्रकल्पाचे नाव", headerCenter);
		create(sh, g, 2, "लाभार्थी जिल्हे", headerCenter);

		String headerText = "एका वर्षातील निश्चित सिंचन क्षमता\n(सन " + meta.getPeriod() + ")";
		create(sh, g, 3, headerText, headerCenter);
		CellRangeAddress irrigationMerge = new CellRangeAddress(g.getRowNum(), g.getRowNum(), 3, 9);
		sh.addMergedRegion(irrigationMerge);
		applyBorderToMergedRegion(sh, irrigationMerge, wb);
		g.setHeightInPoints(45);

		create(sh, g, 10, "रब्बी\nसमतुल्य\nक्षेत्र", headerCenter);
		sh.addMergedRegion(new CellRangeAddress(g.getRowNum(), g.getRowNum() + 1, 10, 10));
		sh.addMergedRegion(new CellRangeAddress(g.getRowNum(), g.getRowNum() + 1, 0, 0));
		sh.addMergedRegion(new CellRangeAddress(g.getRowNum(), g.getRowNum() + 1, 1, 1));
		sh.addMergedRegion(new CellRangeAddress(g.getRowNum(), g.getRowNum() + 1, 2, 2));

		// ---------- subheaders ----------
		Row h = sh.createRow(r++);
		String[] subHeaders = { "", "", "", "खरीप", "रब्बी", "उन्हाळी", "दुहंगामी", "बारमाही", "एकूण (IP)",
				"एकूण (ICA)", "" };
		for (int i = 0; i < subHeaders.length; i++)
			create(sh, h, i, subHeaders[i], header);

		// ---------- NEW: column numbering row ----------
		Row numRow = sh.createRow(r++);
		for (int i = 0; i <= 10; i++) {
			Cell c = numRow.createCell(i);
			c.setCellValue(i + 1);
			c.setCellStyle(headerCenter);
		}

		// ---------- data ----------
		double sumKharip = 0, sumRabbi = 0, sumSummer = 0, sumTwo = 0, sumPerennial = 0, sumIP = 0, sumICA = 0,
				sumTotal = 0;
		int sr = 1;
		Map<String, List<DamSafetyEntity>> groupedByProject = rows.stream()
				.filter(e -> !"D".equalsIgnoreCase(e.getFlag()))
				.collect(Collectors.groupingBy(e -> e.getData().path("projectName").asText("")));

		for (Map.Entry<String, List<DamSafetyEntity>> entry : groupedByProject.entrySet()) {
			String projectName = entry.getKey();
			List<DamSafetyEntity> projectRows = entry.getValue();
			int mergeStartRow = r;
			boolean first = true;

			for (DamSafetyEntity e : projectRows) {
				JsonNode d = e.getData();
				Row dr = sh.createRow(r++);
				int c = 0;

				if (first) {
					create(sh, dr, c++, String.valueOf(sr), cellTxt);
					create(sh, dr, c++, projectName, cellTxt);
					first = false;
				} else {
					create(sh, dr, c++, "", cellTxt);
					create(sh, dr, c++, "", cellTxt);
				}

				create(sh, dr, c++, d.path("beneficiaryDistrict").asText(""), cellTxt);

				double kh = d.path("kharip").asDouble(0);
				double rb = d.path("rabbi").asDouble(0);
				double su = d.path("summer").asDouble(0);
				double tw = d.path("twoCrop").asDouble(0);
				double pe = d.path("perennial").asDouble(0);
				double ip = d.path("ip").asDouble(0);
				double ica = d.path("ica").asDouble(0);
				double tot = d.path("totalArea").asDouble(0);

				createNum(sh, dr, c++, kh, cellNum);
				createNum(sh, dr, c++, rb, cellNum);
				createNum(sh, dr, c++, su, cellNum);
				createNum(sh, dr, c++, tw, cellNum);
				createNum(sh, dr, c++, pe, cellNum);
				createNum(sh, dr, c++, ip, cellNum);
				createNum(sh, dr, c++, ica, cellNum);
				createNum(sh, dr, c++, tot, cellNum);

				sumKharip += kh;
				sumRabbi += rb;
				sumSummer += su;
				sumTwo += tw;
				sumPerennial += pe;
				sumIP += ip;
				sumICA += ica;
				sumTotal += tot;
			}

			if (projectRows.size() > 1) {
				int mergeEndRow = r - 1;
				CellRangeAddress mergeSrNo = new CellRangeAddress(mergeStartRow, mergeEndRow, 0, 0);
				sh.addMergedRegion(mergeSrNo);
				applyBorderToMergedRegion(sh, mergeSrNo, wb);
				CellRangeAddress mergeProjectName = new CellRangeAddress(mergeStartRow, mergeEndRow, 1, 1);
				sh.addMergedRegion(mergeProjectName);
				applyBorderToMergedRegion(sh, mergeProjectName, wb);
			}

			sr++;
		}

		// ---------- FINAL TOTAL ROW ----------
		Row tr = sh.createRow(r++);
		Cell puneCell = tr.createCell(0);
		puneCell.setCellValue("पुणे पाटबंधारे प्रकल्प मंडळ, पुणे");

		CellStyle centerTxt = wb.createCellStyle();
		centerTxt.cloneStyleFrom(cellTxt);
		centerTxt.setAlignment(HorizontalAlignment.CENTER);
		centerTxt.setVerticalAlignment(VerticalAlignment.CENTER);
		puneCell.setCellStyle(centerTxt);
		sh.addMergedRegion(new CellRangeAddress(tr.getRowNum(), tr.getRowNum(), 0, 1));
		applyBorderToMergedRegion(sh, new CellRangeAddress(tr.getRowNum(), tr.getRowNum(), 0, 1), wb);

		create(sh, tr, 2, "एकूण", totalStyle);
		createNum(sh, tr, 3, sumKharip, totalStyle);
		createNum(sh, tr, 4, sumRabbi, totalStyle);
		createNum(sh, tr, 5, sumSummer, totalStyle);
		createNum(sh, tr, 6, sumTwo, totalStyle);
		createNum(sh, tr, 7, sumPerennial, totalStyle);
		createNum(sh, tr, 8, sumIP, totalStyle);
		createNum(sh, tr, 9, sumICA, totalStyle);
		createNum(sh, tr, 10, sumTotal, totalStyle);

		// ---------- FOOTER REMAINS SAME ----------

		// Create a version of cellTxt without borders
		CellStyle cellTxtNoBorder = wb.createCellStyle();
		cellTxtNoBorder.cloneStyleFrom(cellTxt);
		cellTxtNoBorder.setBorderTop(BorderStyle.NONE);
		cellTxtNoBorder.setBorderBottom(BorderStyle.NONE);
		cellTxtNoBorder.setBorderLeft(BorderStyle.NONE);
		cellTxtNoBorder.setBorderRight(BorderStyle.NONE);

		// 🔹 जनाई शिरसाई उ.सिं. योजनेचे ... (Partial bold)
		Row footer1 = sh.createRow(r++);
		String line1 = "जनाई शिरसाई उ.सिं. योजनेचे सिंचन व्यवस्थापन हे पुणे पाटबंधारे मंडळांतर्गत कार्यकारी अभियंता, उपसा सिंचन व्यवस्थापन विभाग , पुणे यांचेमार्फत करण्यात येते.";
		Cell f1Cell = footer1.createCell(0);

		XSSFRichTextString rich1 = new XSSFRichTextString(line1);
		int boldEnd1 = "जनाई शिरसाई उ.सिं. योजनेचे".length();

		// Bold only prefix
		Font boldFont1 = wb.createFont();
		boldFont1.setBold(true);
		Font normalFont1 = wb.createFont();
		normalFont1.setBold(false);
		rich1.applyFont(0, boldEnd1, boldFont1);
		rich1.applyFont(boldEnd1, line1.length(), normalFont1);

		f1Cell.setCellValue(rich1);
		f1Cell.setCellStyle(cellTxtNoBorder);
		sh.addMergedRegion(new CellRangeAddress(footer1.getRowNum(), footer1.getRowNum(), 0, 11));

		// 🔹 बोपगाव ल.पा.प्रकल्प ... (Partial bold)
		Row footer2 = sh.createRow(r++);
		String line2 = "बोपगाव ल.पा.प्रकल्प - शासन पत्र संकिर्ण-2023/(114/2022)/लपा,दि. 08/08/2023 अन्वये ल.पा. प्रकल्प रद्द करणेस शासन मान्यता प्राप्त आहे.";
		Cell f2Cell = footer2.createCell(0);

		XSSFRichTextString rich2 = new XSSFRichTextString(line2);
		int boldEnd2 = "बोपगाव ल.पा.प्रकल्प".length();

		// Bold only the project name part
		Font boldFont2 = wb.createFont();
		boldFont2.setBold(true);
		Font normalFont2 = wb.createFont();
		normalFont2.setBold(false);
		rich2.applyFont(0, boldEnd2, boldFont2);
		rich2.applyFont(boldEnd2, line2.length(), normalFont2);

		f2Cell.setCellValue(rich2);
		f2Cell.setCellStyle(cellTxtNoBorder);
		sh.addMergedRegion(new CellRangeAddress(footer2.getRowNum(), footer2.getRowNum(), 0, 11));

		// ---------- Signature section ----------

		// “स्थळ प्रतीवर...” – bold, italic, underline, starting from ‘रब्बी’ column (4)
		r++;
		Row sigRow1 = sh.createRow(r++);
		Cell sig1Cell = sigRow1.createCell(4);
		sig1Cell.setCellValue("स्थळ प्रतीवर मा. अ.अ. यांची सही असे.");

		// Create style from scratch (not cloned!) to avoid inheriting borders
		CellStyle italicBoldUnderline = wb.createCellStyle();

		// 🖋 Font setup
		Font italicBoldFont = wb.createFont();
		italicBoldFont.setBold(true);
		italicBoldFont.setItalic(true);
		italicBoldFont.setUnderline(Font.U_SINGLE);
		italicBoldFont.setFontHeightInPoints((short) 11);

		// 🔧 Assign font & alignment
		italicBoldUnderline.setFont(italicBoldFont);
		italicBoldUnderline.setAlignment(HorizontalAlignment.LEFT);
		italicBoldUnderline.setVerticalAlignment(VerticalAlignment.CENTER);

		// 🧹 Ensure no borders
		italicBoldUnderline.setBorderTop(BorderStyle.NONE);
		italicBoldUnderline.setBorderBottom(BorderStyle.NONE);
		italicBoldUnderline.setBorderLeft(BorderStyle.NONE);
		italicBoldUnderline.setBorderRight(BorderStyle.NONE);

		sig1Cell.setCellStyle(italicBoldUnderline);

		// Merge same as before
		sh.addMergedRegion(new CellRangeAddress(sigRow1.getRowNum(), sigRow1.getRowNum(), 4, 7));

		// ---------- Signature Name & Designation ----------

		Row sigRow2 = sh.createRow(r++);
		create(sh, sigRow2, 8, "(नि‍किता लि. हेमने)", cellTxtNoBorder);
		sh.addMergedRegion(new CellRangeAddress(sigRow2.getRowNum(), sigRow2.getRowNum(), 8, 10));

		Row sigRow3 = sh.createRow(r++);
		create(sh, sigRow3, 8, "उपअधीक्षक अभियंता,", cellTxtNoBorder);
		sh.addMergedRegion(new CellRangeAddress(sigRow3.getRowNum(), sigRow3.getRowNum(), 8, 10));

		Row sigRow4 = sh.createRow(r++);
		create(sh, sigRow4, 8, "पुणे पाटबंधारे प्रकल्प मंडळ,", cellTxtNoBorder);
		sh.addMergedRegion(new CellRangeAddress(sigRow4.getRowNum(), sigRow4.getRowNum(), 8, 10));

		Row sigRow5 = sh.createRow(r++);
		create(sh, sigRow5, 8, "पुणे-01.", cellTxtNoBorder);
		sh.addMergedRegion(new CellRangeAddress(sigRow5.getRowNum(), sigRow5.getRowNum(), 8, 10));

		// ---------- output ----------
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		wb.write(out);
		wb.close();

		// ✅ Ensure UTF-8 encoding for Marathi text
		byte[] excelBytes = out.toByteArray();

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''"
				+ UriUtils.encode("Dam_Safety_" + meta.getPeriod() + ".xlsx", StandardCharsets.UTF_8));

		return ResponseEntity.ok().headers(headers)
				.contentType(
						MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(new InputStreamResource(new ByteArrayInputStream(excelBytes)));
	}

	private static void create(Sheet sh, Row r, int c, String v, CellStyle st) {
		Cell cell = r.createCell(c);
		cell.setCellValue(v);
		cell.setCellStyle(st);
	}

	private static void createNum(Sheet sh, Row r, int c, double v, CellStyle st) {
		Cell cell = r.createCell(c);
		cell.setCellValue(v);
		cell.setCellStyle(st);
	}

	private static CellStyle titleStyle(Workbook wb) {
		CellStyle cs = wb.createCellStyle();
		Font f = wb.createFont();
		f.setBold(true);
		f.setFontHeightInPoints((short) 14);
		cs.setFont(f);
		cs.setAlignment(HorizontalAlignment.CENTER);
		cs.setVerticalAlignment(VerticalAlignment.CENTER);
		return cs;
	}

	private static CellStyle subTitleStyle(Workbook wb) {
		CellStyle cs = wb.createCellStyle();
		Font f = wb.createFont();
		f.setBold(true);
		f.setFontHeightInPoints((short) 12);
		cs.setFont(f);
		cs.setAlignment(HorizontalAlignment.CENTER);
		cs.setVerticalAlignment(VerticalAlignment.CENTER);
		return cs;
	}

	private static CellStyle headerStyle(Workbook wb) {
		CellStyle cs = headerCenterStyle(wb);
		cs.setWrapText(true);
		return cs;
	}

	private static CellStyle headerCenterStyle(Workbook wb) {
		CellStyle cs = wb.createCellStyle();
		Font f = wb.createFont();
		f.setBold(true);
		cs.setFont(f);
		cs.setAlignment(HorizontalAlignment.CENTER);
		cs.setVerticalAlignment(VerticalAlignment.CENTER);
		setBorder(cs);
		return cs;
	}

	private static CellStyle cellTextStyle(Workbook wb) {
		CellStyle cs = wb.createCellStyle();
		cs.setAlignment(HorizontalAlignment.LEFT);
		cs.setVerticalAlignment(VerticalAlignment.CENTER);
		setBorder(cs);
		return cs;
	}

	private static CellStyle cellNumberStyle(Workbook wb) {
		CellStyle cs = wb.createCellStyle();
		cs.setAlignment(HorizontalAlignment.RIGHT);
		cs.setVerticalAlignment(VerticalAlignment.CENTER);
		DataFormat df = wb.createDataFormat();
		cs.setDataFormat(df.getFormat("0.000"));
		setBorder(cs);
		return cs;
	}

	private static CellStyle totalRowStyle(Workbook wb) {
		CellStyle cs = wb.createCellStyle();
		Font f = wb.createFont();
		f.setBold(true);
		cs.setFont(f);
		cs.setAlignment(HorizontalAlignment.RIGHT);
		cs.setVerticalAlignment(VerticalAlignment.CENTER);
		DataFormat df = wb.createDataFormat();
		cs.setDataFormat(df.getFormat("0.000"));
		setBorder(cs);
		return cs;
	}

	private static void setBorder(CellStyle cs) {
		cs.setBorderTop(BorderStyle.THIN);
		cs.setBorderBottom(BorderStyle.THIN);
		cs.setBorderLeft(BorderStyle.THIN);
		cs.setBorderRight(BorderStyle.THIN);
	}

	private void applyBorderToMergedRegion(Sheet sheet, CellRangeAddress region, Workbook wb) {
		RegionUtil.setBorderTop(BorderStyle.THIN, region, sheet);
		RegionUtil.setBorderBottom(BorderStyle.THIN, region, sheet);
		RegionUtil.setBorderLeft(BorderStyle.THIN, region, sheet);
		RegionUtil.setBorderRight(BorderStyle.THIN, region, sheet);
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadNalikaExcel(String period) throws IOException {

		List<DamNalikaEntity> rows = damNalikaRepository.findByPeriodOrderByDepartmentKeyAscRowIdAsc(period);

		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sh = wb.createSheet("नळिका पाणी वितरण");

		// ---------- column widths ----------
		int[] widths = { 1600, 7200, 5200, 4200, 4200, 4200, 4200, 4200, 4200, 7200 };
		for (int i = 0; i < widths.length; i++)
			sh.setColumnWidth(i, widths[i]);

		// ---------- styles ----------
		DataFormat fmt = wb.createDataFormat();

		Font titleFont = wb.createFont();
		titleFont.setBold(true);
		titleFont.setFontHeightInPoints((short) 14);

		Font subTitleFont = wb.createFont();
		subTitleFont.setBold(true);
		subTitleFont.setFontHeightInPoints((short) 12);

		Font smallFont = wb.createFont();
		smallFont.setBold(true);
		smallFont.setFontHeightInPoints((short) 10);

		Font normalFont = wb.createFont();
		normalFont.setFontHeightInPoints((short) 10);

		Font boldFont = wb.createFont();
		boldFont.setBold(true);

		CellStyle title = wb.createCellStyle();
		title.setFont(titleFont);
		title.setAlignment(HorizontalAlignment.CENTER);
		title.setVerticalAlignment(VerticalAlignment.CENTER);

		CellStyle subTitle = wb.createCellStyle();
		subTitle.setFont(subTitleFont);
		subTitle.setAlignment(HorizontalAlignment.CENTER);
		subTitle.setVerticalAlignment(VerticalAlignment.CENTER);

		CellStyle header = wb.createCellStyle();
		header.setFont(smallFont);
		header.setAlignment(HorizontalAlignment.CENTER);
		header.setVerticalAlignment(VerticalAlignment.CENTER);
		header.setWrapText(true);
		header.setBorderTop(BorderStyle.MEDIUM);
		header.setBorderBottom(BorderStyle.MEDIUM);
		header.setBorderLeft(BorderStyle.MEDIUM);
		header.setBorderRight(BorderStyle.MEDIUM);

		CellStyle cellTxt = wb.createCellStyle();
		cellTxt.setFont(normalFont);
		cellTxt.setAlignment(HorizontalAlignment.LEFT);
		cellTxt.setVerticalAlignment(VerticalAlignment.CENTER);
		cellTxt.setWrapText(true);
		cellTxt.setBorderTop(BorderStyle.THIN);
		cellTxt.setBorderBottom(BorderStyle.THIN);
		cellTxt.setBorderLeft(BorderStyle.THIN);
		cellTxt.setBorderRight(BorderStyle.THIN);

		CellStyle cellNum = wb.createCellStyle();
		cellNum.cloneStyleFrom(cellTxt);
		cellNum.setAlignment(HorizontalAlignment.RIGHT);
		cellNum.setDataFormat(fmt.getFormat("0.00"));

		CellStyle totalStyle = wb.createCellStyle();
		totalStyle.cloneStyleFrom(cellNum);
		totalStyle.setFont(boldFont);
		totalStyle.setBorderTop(BorderStyle.MEDIUM);

		int r = 0;

		// ---------- titles ----------
		createMergedText(sh, sh.createRow(r++), 0, 9, "महाराष्ट्र कृष्णा खोरे विकास महामंडळ, पुणे", title);
		createMergedText(sh, sh.createRow(r++), 0, 9, "पुणे पाटबंधारे प्रकल्प मंडळ, पुणे", subTitle);
		createMergedText(sh, sh.createRow(r++), 0, 9,
				"नलिका द्वारे पाणी वितरण व्यवस्था - धोरण अंमलबजावणी (दि. 30/09/2025 अखेर)", subTitle);
		r++;

		// ---------- header ----------
		Row h1 = sh.createRow(r++);
		Row h2 = sh.createRow(r++);
		Row h3 = sh.createRow(r++);

		// Col 0–2
		applyHeaderMerge(sh, wb, h1, h3, 0, 0, "अ. क्र.", header);
		applyHeaderMerge(sh, wb, h1, h3, 1, 1, "महामंडळ / प्रादेशिक कार्यालयाचे नांव", header);
		applyHeaderMerge(sh, wb, h1, h3, 2, 2,
				"प्रकल्पांतर्गत नलिकाव्दारे सिंचन वितरणाचे प्रस्तावित संपूर्ण क्षेत्र (हे.)", header);

		// भौतिक प्रगती साध्य (cols 3–7)
		CellRangeAddress bhoutikMerge = new CellRangeAddress(h1.getRowNum(), h1.getRowNum(), 3, 7);
		createHeader(h1, 3, "भौतिक प्रगती साध्य", header);
		sh.addMergedRegion(bhoutikMerge);
		applyBorderToMergedRegionForNalika(sh, bhoutikMerge, wb);

		// Row 2 (subheaders)
		String[] subs = { "नलिकाव्दारे सिंचन वितरणाची कामे पूर्ण झालेले क्षेत्र (हे)", "प्रगतीपथावरील क्षेत्र (हे)",
				"कामाचे आदेश दिलेले क्षेत्र (हे)", "निविदा स्तरावरील क्षेत्र (हे)",
				"सर्वेक्षण स्तरावर प्रलंबित क्षेत्र (हे)" };
		for (int i = 0; i < subs.length; i++)
			createHeader(h2, 3 + i, subs[i], header);

		// Row 3 (IPs)
		for (int i = 3; i <= 7; i++)
			createHeader(h3, i, "IP", header);

		// प्रत्यक्ष सिंचन क्षेत्र (हे) IP (col 8)
		applyHeaderMerge(sh, wb, h1, h3, 8, 8, "प्रत्यक्ष सिंचन क्षेत्र (हे) IP", header);

		// शेरा कॉलम (col 9) rowspan 3
		applyHeaderMerge(sh, wb, h1, h3, 9, 9,
				"शेरा / नलिकाद्वारे पाणी वितरण व्यवस्था कामे करण्यास येणार्या अडचणीबाबत तपशील", header);

		// ---------- numbering ----------
		Row numRow = sh.createRow(r++);
		for (int i = 0; i < 10; i++) {
			Cell c = numRow.createCell(i);
			c.setCellValue(i + 1);
			c.setCellStyle(header);
		}

		// ---------- data ----------
		Map<String, List<DamNalikaEntity>> deptGroup = rows.stream().collect(
				Collectors.groupingBy(DamNalikaEntity::getDepartmentKey, LinkedHashMap::new, Collectors.toList()));

		double[] totals = new double[7];
		for (Map.Entry<String, List<DamNalikaEntity>> dept : deptGroup.entrySet()) {
			Row deptRow = sh.createRow(r++);
			// 🔹 Department name row (smaller font, left aligned)
			CellStyle deptStyle = wb.createCellStyle();
			deptStyle.cloneStyleFrom(subTitle);
			Font deptFont = wb.createFont();
			deptFont.setBold(true);
			deptFont.setFontHeightInPoints((short) 10); // 👈 same as column name size
			deptStyle.setFont(deptFont);
			deptStyle.setAlignment(HorizontalAlignment.LEFT);
			deptStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			createMergedTextLeft(sh, deptRow, 1, 9, dept.getValue().get(0).getDepartmentName(), deptStyle);

			int sr = 1;
			for (DamNalikaEntity e : dept.getValue()) {
				JsonNode d = e.getData();
				Row dr = sh.createRow(r++);
				int c = 0;

				createCenter(dr, c++, String.valueOf(sr++), cellTxt);
				createText(dr, c++, d.path("projectName").asText(""), cellTxt);

				double[] vals = { d.path("prakalpantargatNalikaSinchanSampurnKshetra").asDouble(0),
						d.path("nalikaSinchanPurnKshetra").asDouble(0), d.path("pragatipathavarilKshetra").asDouble(0),
						d.path("kamacheAadeshDilaleleKshetra").asDouble(0),
						d.path("nividaStaravarilKshetra").asDouble(0),
						d.path("sarvekshanStaravarPralambitKshetra").asDouble(0),
						d.path("pratyakshSinchanKshetraIp").asDouble(0) };
				String v9 = d.path("remarks").asText("");

				for (double val : vals)
					createNum(dr, c++, val, cellNum);
				createText(dr, c++, v9, cellTxt);

				for (int i = 0; i < totals.length; i++)
					totals[i] += vals[i];
			}
		}

		// ---------- total ----------
		Row totalRow = sh.createRow(r++);
		createText(totalRow, 1, "एकूण", totalStyle);
		for (int i = 0; i < totals.length; i++)
			createNum(totalRow, i + 2, totals[i], totalStyle);

		// ---------- signatures ----------
		r += 3;
		Row sigRow = sh.createRow(r);
		sigRow.setHeightInPoints(100);

		CellStyle sigStyle = wb.createCellStyle();
		Font sigFont = wb.createFont();
		sigFont.setBold(true);
		sigFont.setFontHeightInPoints((short) 11);
		sigStyle.setFont(sigFont);
		sigStyle.setAlignment(HorizontalAlignment.CENTER);
		sigStyle.setVerticalAlignment(VerticalAlignment.TOP);
		sigStyle.setWrapText(true);

		int leftCol = 6, rightCol = 8;

		Cell left = sigRow.createCell(leftCol);
		left.setCellValue("(कु. ह. पाटील)\nअधीक्षक अभियंता,\nपुणे पाटबंधारे प्रकल्प मंडळ,\nपुणे-01.");
		left.setCellStyle(sigStyle);
		sh.addMergedRegion(new CellRangeAddress(r, r, leftCol, leftCol + 1));

		Cell right = sigRow.createCell(rightCol);
		right.setCellValue("(निकिता लि. हेमने)\nउपअधीक्षक अभियंता,\nपुणे पाटबंधारे प्रकल्प मंडळ,\nपुणे.");
		right.setCellStyle(sigStyle);
		sh.addMergedRegion(new CellRangeAddress(r, r, rightCol, rightCol + 1));

		// ---------- output ----------
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		wb.write(out);
		wb.close();

		String safeFileName = URLEncoder.encode("Nalika_Pani_Vitaran_" + period + ".xlsx", StandardCharsets.UTF_8);
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + safeFileName);

		return ResponseEntity.ok().headers(headers)
				.contentType(
						MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())));
	}

	// --------------------------- 🔹 Helper Methods 🔹 ---------------------------
	// //

	/** 🔸 Creates merged centered text (for title rows etc.) */
	private void createMergedText(XSSFSheet sh, Row row, int from, int to, String text, CellStyle style) {
		Cell cell = row.createCell(from);
		cell.setCellValue(text);
		cell.setCellStyle(style);
		sh.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), from, to));
	}

	/** 🔸 Creates merged left-aligned text (for department name rows) */
	private void createMergedTextLeft(XSSFSheet sh, Row row, int from, int to, String text, CellStyle baseStyle) {
		Cell cell = row.createCell(from);
		CellStyle left = sh.getWorkbook().createCellStyle();
		left.cloneStyleFrom(baseStyle);
		left.setAlignment(HorizontalAlignment.LEFT);
		left.setVerticalAlignment(VerticalAlignment.CENTER);
		cell.setCellValue(text);
		cell.setCellStyle(left);
		sh.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), from, to));
	}

	/** 🔸 Simple header cell creator */
	private void createHeader(Row row, int col, String text, CellStyle style) {
		Cell cell = row.createCell(col);
		cell.setCellValue(text);
		cell.setCellStyle(style);
	}

	/** 🔸 Applies merge + borders for headers (multi-row merge) */
	private void applyHeaderMerge(XSSFSheet sh, XSSFWorkbook wb, Row h1, Row h2, int from, int to, String text,
			CellStyle style) {
		createHeader(h1, from, text, style);
		CellRangeAddress region = new CellRangeAddress(h1.getRowNum(), h2.getRowNum(), from, to);
		sh.addMergedRegion(region);
		applyBorderToMergedRegionForNalika(sh, region, wb);
	}

	/** 🔸 Applies MEDIUM borders to merged header region */
	private void applyBorderToMergedRegionForNalika(Sheet sheet, CellRangeAddress region, Workbook wb) {
		RegionUtil.setBorderTop(BorderStyle.MEDIUM, region, sheet);
		RegionUtil.setBorderBottom(BorderStyle.MEDIUM, region, sheet);
		RegionUtil.setBorderLeft(BorderStyle.MEDIUM, region, sheet);
		RegionUtil.setBorderRight(BorderStyle.MEDIUM, region, sheet);
	}

	/** 🔸 Generic text cell creator */
	private void createText(Row row, int col, String text, CellStyle style) {
		Cell cell = row.createCell(col);
		cell.setCellValue(text);
		cell.setCellStyle(style);
	}

	/** 🔸 Center-aligned text cell */
	private void createCenter(Row row, int col, String text, CellStyle baseStyle) {
		CellStyle style = row.getSheet().getWorkbook().createCellStyle();
		style.cloneStyleFrom(baseStyle);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		Cell cell = row.createCell(col);
		cell.setCellValue(text);
		cell.setCellStyle(style);
	}

	/** 🔸 Numeric cell (right aligned with borders) */
	private void createNum(Row row, int col, double val, CellStyle style) {
		Cell cell = row.createCell(col);
		cell.setCellValue(val);
		cell.setCellStyle(style);
	}

	/** 🔸 Right aligned multi-cell text (for footer/signatures) */
	private void createRight(XSSFSheet sh, Row row, int start, String text, CellStyle style, int end) {
		Cell cell = row.createCell(start);
		cell.setCellValue(text);
		CellStyle right = sh.getWorkbook().createCellStyle();
		right.cloneStyleFrom(style);
		right.setWrapText(true);
		right.setAlignment(HorizontalAlignment.RIGHT);
		right.setVerticalAlignment(VerticalAlignment.TOP);
		cell.setCellStyle(right);
		sh.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), start, end));
	}

	/**
	 * 🔸 Creates centered merged text (e.g., for header labels like "भौतिक प्रगती
	 * साध्य")
	 */
	private void createCenteredText(XSSFSheet sh, Row row, int from, int to, String text, CellStyle baseStyle) {
		Cell cell = row.createCell(from);
		CellStyle centered = sh.getWorkbook().createCellStyle();
		centered.cloneStyleFrom(baseStyle);
		centered.setAlignment(HorizontalAlignment.CENTER);
		centered.setVerticalAlignment(VerticalAlignment.CENTER);
		cell.setCellValue(text);
		cell.setCellStyle(centered);
		sh.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), from, to));
		applyBorderToMergedRegionForNalika(sh, new CellRangeAddress(row.getRowNum(), row.getRowNum(), from, to),
				sh.getWorkbook());
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadPralambitBhusampadanExcel(String period) throws IOException {

		List<PralambitBhusampadanEntity> rows = pralambitBhusampadanRepository
				.findByPeriodOrderByKramankAscSubIdAsc(period);

		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sh = wb.createSheet("प्रलंबित प्रकरणे");

		// ---------- Column widths ----------
		int[] widths = { 1600, 5200, 1200, 12000 };
		for (int i = 0; i < widths.length; i++)
			sh.setColumnWidth(i, widths[i]);

		// ---------- Fonts ----------
		Font titleFont = wb.createFont();
		titleFont.setBold(true);
		titleFont.setFontHeightInPoints((short) 13);

		Font headerFont = wb.createFont();
		headerFont.setBold(true);
		headerFont.setFontHeightInPoints((short) 11);

		Font normalFont = wb.createFont();
		normalFont.setFontHeightInPoints((short) 10);

		// ---------- Styles ----------
		CellStyle titleStyle = wb.createCellStyle();
		titleStyle.setFont(titleFont);
		titleStyle.setAlignment(HorizontalAlignment.CENTER);
		titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		titleStyle.setWrapText(true);
		// ❌ no border for title

		CellStyle headerStyle = wb.createCellStyle();
		headerStyle.setFont(headerFont);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerStyle.setWrapText(true);
		setBoldBorders(headerStyle);

		CellStyle textStyle = wb.createCellStyle();
		textStyle.setFont(normalFont);
		textStyle.setAlignment(HorizontalAlignment.LEFT);
		textStyle.setVerticalAlignment(VerticalAlignment.TOP);
		textStyle.setWrapText(true);
		setBoldBorders(textStyle);

		CellStyle centerStyle = wb.createCellStyle();
		centerStyle.cloneStyleFrom(textStyle);
		centerStyle.setAlignment(HorizontalAlignment.CENTER);
		centerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		setBoldBorders(centerStyle);

		int r = 0;

		// ---------- Leave two blank rows ----------
		sh.createRow(r++);
		sh.createRow(r++);

		// ---------- Title ----------
		Row titleRow = sh.createRow(r++);
		titleRow.setHeightInPoints(28);
		Cell titleCell = titleRow.createCell(0);
		titleCell.setCellValue("प्रलंबित प्रकरणे यादी (भूमिपादन)");
		titleCell.setCellStyle(titleStyle);
		sh.addMergedRegion(new CellRangeAddress(titleRow.getRowNum(), titleRow.getRowNum(), 0, 3));

		r++; // gap

		// ---------- Header ----------
		Row h1 = sh.createRow(r++);
		Row h2 = sh.createRow(r++);
		Row h3 = sh.createRow(r++);

		createHeaderForBhu(h1, 0, "अ. क्र.", headerStyle);
		sh.addMergedRegion(new CellRangeAddress(h1.getRowNum(), h3.getRowNum(), 0, 0));

		createHeaderForBhu(h1, 1, "स्तर", headerStyle);
		sh.addMergedRegion(new CellRangeAddress(h1.getRowNum(), h3.getRowNum(), 1, 1));

		createHeaderForBhu(h1, 2, "प्रलंबित विषय", headerStyle);
		sh.addMergedRegion(new CellRangeAddress(h1.getRowNum(), h3.getRowNum(), 2, 3));

		// ---------- Group data ----------
		Map<Integer, List<PralambitBhusampadanEntity>> grouped = rows.stream().collect(
				Collectors.groupingBy(PralambitBhusampadanEntity::getKramank, LinkedHashMap::new, Collectors.toList()));

		int srNo = 1;

		for (Map.Entry<Integer, List<PralambitBhusampadanEntity>> entry : grouped.entrySet()) {
			List<PralambitBhusampadanEntity> groupRows = entry.getValue();
			String star = groupRows.get(0).getStar();

			int startRow = r;
			int totalSub = groupRows.size();
			int innerSr = 1;

			for (PralambitBhusampadanEntity e : groupRows) {
				JsonNode d = e.getData();
				String vishay = d.path("vishay").asText("");

				Row row = sh.createRow(r++);
				row.setHeightInPoints(45); // Increase vertical space

				// Show numbering only if multiple प्रलंबित विषय exist
				if (totalSub > 1) {
					createCenterForBhu(row, 2, String.valueOf(innerSr++), centerStyle);
				} else {
					createCenterForBhu(row, 2, "", centerStyle);
				}

				createTextForBhu(row, 3, vishay, textStyle);
			}

			// ✅ Merge only if more than one row
			if (r - 1 > startRow) {
				sh.addMergedRegion(new CellRangeAddress(startRow, r - 1, 0, 0));
				sh.addMergedRegion(new CellRangeAddress(startRow, r - 1, 1, 1));
			}

			Row firstRow = sh.getRow(startRow);
			createCenterForBhu(firstRow, 0, String.valueOf(srNo++), centerStyle);
			createCenterForBhu(firstRow, 1, star, centerStyle);
		}

		// ---------- Apply bold borders (skip top rows) ----------
		for (int i = 3; i < r; i++) {
			Row row = sh.getRow(i);
			if (row == null)
				continue;
			for (int j = 0; j < 4; j++) {
				Cell cell = row.getCell(j);
				if (cell == null)
					cell = row.createCell(j);
				CellStyle s = wb.createCellStyle();
				s.cloneStyleFrom(cell.getCellStyle());
				setBoldBorders(s);
				cell.setCellStyle(s);
			}
		}

		// ---------- Output ----------
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		wb.write(out);
		wb.close();

		String safeFileName = URLEncoder.encode("Pralambit_Bhusampadan_" + period + ".xlsx", StandardCharsets.UTF_8);
		HttpHeaders headersHttp = new HttpHeaders();
		headersHttp.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + safeFileName);

		return ResponseEntity.ok().headers(headersHttp)
				.contentType(
						MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())));
	}

	// ---------- helper methods ----------
	private void createHeaderForBhu(Row row, int col, String text, CellStyle style) {
		Cell c = row.createCell(col);
		c.setCellValue(text);
		c.setCellStyle(style);
	}

	private void createTextForBhu(Row row, int col, String text, CellStyle style) {
		Cell c = row.createCell(col);
		c.setCellValue(text);
		c.setCellStyle(style);
	}

	private void createCenterForBhu(Row row, int col, String text, CellStyle style) {
		CellStyle s = row.getSheet().getWorkbook().createCellStyle();
		s.cloneStyleFrom(style);
		s.setAlignment(HorizontalAlignment.CENTER);
		s.setVerticalAlignment(VerticalAlignment.CENTER);
		setBoldBorders(s);
		Cell c = row.createCell(col);
		c.setCellValue(text);
		c.setCellStyle(s);
	}

	private void setBoldBorders(CellStyle style) {
		style.setBorderTop(BorderStyle.MEDIUM);
		style.setBorderBottom(BorderStyle.MEDIUM);
		style.setBorderLeft(BorderStyle.MEDIUM);
		style.setBorderRight(BorderStyle.MEDIUM);
	}

	private void applyBorderToMergedRegion(Sheet sheet, CellRangeAddress region) {
		RegionUtil.setBorderTop(BorderStyle.MEDIUM, region, sheet);
		RegionUtil.setBorderBottom(BorderStyle.MEDIUM, region, sheet);
		RegionUtil.setBorderLeft(BorderStyle.MEDIUM, region, sheet);
		RegionUtil.setBorderRight(BorderStyle.MEDIUM, region, sheet);
	}

}