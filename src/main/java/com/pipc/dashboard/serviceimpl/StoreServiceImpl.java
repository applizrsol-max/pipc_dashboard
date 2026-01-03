package com.pipc.dashboard.serviceimpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.MDC;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

import lombok.extern.slf4j.Slf4j;

@Slf4j
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
		StringBuilder activityLog = new StringBuilder();

		final String year = storeRequest.getYear();
		final Integer newEkunEkandar = storeRequest.getEkunEkandar();
		final LocalDateTime now = LocalDateTime.now();

		log.info("START saveOrUpdate Store | year={} | user={}", year, createdBy);

		try {

			// =====================================================
			// 1️⃣ OVERALL EKUN EKANDAR (YEAR SCOPED)
			// =====================================================
			StoreEntity existingOverall = storeRepository.findExistingEkunEkandarByYear(year).orElse(null);

			if (!Objects.equals(existingOverall, newEkunEkandar)) {

				storeRepository.updateEkunEkandarAndTimestampByYear(newEkunEkandar, createdBy, year);

				activityLog.append("Overall ekunEkandar updated. ");
				log.debug("Updated ekunEkandar | old={} | new={} | year={}", existingOverall, newEkunEkandar, year);
			}

			// =====================================================
			// 2️⃣ DEPARTMENT LEVEL
			// =====================================================
			for (DepartmentSection dept : storeRequest.getDepartments()) {

				String deptName = dept.getDepartmentName();
				Integer newDeptEkun = dept.getEkun();

				log.debug("Processing department | name={} | year={}", deptName, year);

				StoreEntity existingDeptEkun = storeRepository.findExistingEkunForDeptAndYear(deptName, year)
						.orElse(null);

				if (!Objects.equals(existingDeptEkun, newDeptEkun)) {

					List<StoreEntity> deptRows = storeRepository.findAllByDepartmentNameAndYear(deptName, year);

					boolean deptUpdated = false;
					for (StoreEntity e : deptRows) {
						if (!Objects.equals(e.getEkun(), newDeptEkun)) {
							e.setEkun(newDeptEkun);
							e.setUpdatedBy(createdBy);
							e.setUpdatedAt(now);
							e.setFlag("U");
							deptUpdated = true;
						}
					}

					if (deptUpdated) {
						storeRepository.saveAll(deptRows);
						activityLog.append("Dept ekun updated for ").append(deptName).append(". ");
						log.debug("Department ekun updated | dept={} | year={}", deptName, year);
					}
				}

				// =====================================================
				// 3️⃣ ROW LEVEL
				// =====================================================
				if (dept.getRows() == null)
					continue;

				for (VibhagRow row : dept.getRows()) {

					Integer rowId = row.getRowId();
					Long deleteId = row.getDeleteId();

					if (rowId == null)
						continue;

					Optional<StoreEntity> existingOpt = storeRepository.findByDepartmentNameAndRowIdAndYear(deptName,
							rowId, year);

					Optional<StoreEntity> deleteOpt = storeRepository.findByDepartmentNameAndDeleteIdAndYear(deptName,
							deleteId, year);

					// ---------------- DELETE ----------------
					if ("D".equalsIgnoreCase(row.getFlag())) {

						if (deleteOpt.isPresent()) {
							storeRepository.delete(deleteOpt.get());
							activityLog.append("Deleted rowId ").append(rowId).append(" (").append(deptName)
									.append("). ");
							log.debug("Deleted row | dept={} | rowId={} | year={}", deptName, rowId, year);
						} else {
							log.warn("Delete requested but not found | dept={} | deleteId={} | year={}", deptName,
									deleteId, year);
						}
						continue;
					}

					// ---------------- JSON ----------------
					JsonNode incomingJson = objectMapper.valueToTree(row);

					// ---------------- UPDATE ----------------
					if (existingOpt.isPresent()) {

						StoreEntity entity = existingOpt.get();
						boolean jsonChanged = entity.getRowsData() == null
								|| !entity.getRowsData().equals(incomingJson);

						if (jsonChanged) {
							entity.setRowsData(incomingJson);
							entity.setEkun(newDeptEkun);
							entity.setEkunEkandar(newEkunEkandar);
							entity.setUpdatedBy(createdBy);
							entity.setUpdatedAt(now);
							entity.setFlag("U");

							storeRepository.save(entity);

							activityLog.append("Updated rowId ").append(rowId).append(" (").append(deptName)
									.append("). ");
							log.debug("Updated row | dept={} | rowId={} | year={}", deptName, rowId, year);
						}

					}
					// ---------------- CREATE ----------------
					else {

						StoreEntity entity = new StoreEntity();
						entity.setYear(year);
						entity.setDepartmentName(deptName);
						entity.setRowId(rowId);
						entity.setDeleteId(deleteId);
						entity.setRowsData(incomingJson);
						entity.setEkun(newDeptEkun);
						entity.setEkunEkandar(newEkunEkandar);
						entity.setCreatedBy(createdBy);
						entity.setUpdatedBy(createdBy);
						entity.setCreatedAt(now);
						entity.setUpdatedAt(now);
						entity.setFlag("C");

						storeRepository.save(entity);

						activityLog.append("Created rowId ").append(rowId).append(" (").append(deptName).append("). ");
						log.debug("Created row | dept={} | rowId={} | year={}", deptName, rowId, year);
					}
				}
			}

			error.setErrorCode("0");
			error.setErrorDescription(activityLog.toString());
			response.setErrorDetails(error);
			response.setMessage("Processed successfully");

			log.info("SUCCESS saveOrUpdate Store | year={} | user={}", year, createdBy);
			return response;

		} catch (Exception e) {

			log.error("ERROR saveOrUpdate Store | year={} | user={}", year, createdBy, e);

			error.setErrorCode("1");
			error.setErrorDescription("Error while saving data: " + e.getMessage());
			response.setErrorDetails(error);
			response.setMessage("Failed");
			return response;
		}
	}

	@Override
	public StoreResponse getStores(String year) {

		StoreResponse response = new StoreResponse();
		ApplicationError error = new ApplicationError();

		final String user = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
		final String corrId = MDC.get("correlationId");

		log.info("START getStores | year={} | user={} | corrId={}", year, user, corrId);

		try {
			// =====================================================
			// 1️⃣ Fetch distinct departments YEAR-wise
			// =====================================================
			List<String> departments = storeRepository.findDistinctDepartmentNamesByYear(year);

			if (departments == null || departments.isEmpty()) {
				log.warn("No store data found | year={} | corrId={}", year, corrId);

				error.setErrorCode("NO_DATA");
				error.setErrorDescription("No store data found for year: " + year);
				response.setErrorDetails(error);
				response.setMessage("No data");
				return response;
			}

			List<DepartmentSection> departmentSections = new ArrayList<>(departments.size());

			// =====================================================
			// 2️⃣ Fetch ALL rows department-wise (NO pagination)
			// =====================================================
			for (String dept : departments) {

				List<StoreEntity> entities = storeRepository.findByDepartmentNameAndYearOrderByRowIdAsc(dept, year);

				if (entities == null || entities.isEmpty())
					continue;

				List<VibhagRow> rows = new ArrayList<>(entities.size());

				for (StoreEntity entity : entities) {

					VibhagRow row = objectMapper.convertValue(entity.getRowsData(), VibhagRow.class);

					row.setRowId(entity.getRowId());
					row.setDeleteId(entity.getDeleteId());
					rows.add(row);
				}

				DepartmentSection section = new DepartmentSection();
				section.setDepartmentName(dept);
				section.setEkun(entities.get(0).getEkun());
				section.setRows(rows);

				departmentSections.add(section);

				log.debug("Loaded department={} | rows={} | year={}", dept, rows.size(), year);
			}

			// =====================================================
			// 3️⃣ Build final response (same structure as earlier)
			// =====================================================
			StoreRequest storeData = new StoreRequest();
			storeData.setYear(year);

			storeData.setDepartments(departmentSections);

			response.setData(storeData);

			error.setErrorCode("0");
			error.setErrorDescription("Fetched successfully");
			response.setErrorDetails(error);
			response.setMessage("Success");

			log.info("SUCCESS getStores | year={} | departments={} | corrId={}", year, departmentSections.size(),
					corrId);

			return response;

		} catch (Exception e) {

			log.error("ERROR getStores | year={} | corrId={}", year, corrId, e);

			error.setErrorCode("1");
			error.setErrorDescription("Error fetching data: " + e.getMessage());
			response.setErrorDetails(error);
			response.setMessage("Failed");

			return response;
		}
	}

	@Transactional(readOnly = true)
	@Override
	public ResponseEntity<InputStreamResource> downloadStoreData(String year) throws IOException {

		List<StoreEntity> records = storeRepository.findByYear(year);

		// Group by department name preserving order
		Map<String, List<StoreEntity>> grouped = records.stream().collect(
				Collectors.groupingBy(StoreEntity::getDepartmentName, LinkedHashMap::new, Collectors.toList()));

		try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

			Sheet sheet = wb.createSheet("StoreData");

			// === Fonts ===
			Font headerFont = wb.createFont();
			headerFont.setBold(true);
			headerFont.setFontHeightInPoints((short) 12);
			headerFont.setFontName("Mangal");

			Font boldFont = wb.createFont();
			boldFont.setBold(true);
			boldFont.setFontHeightInPoints((short) 10);
			boldFont.setFontName("Mangal");

			Font normalFont = wb.createFont();
			normalFont.setFontHeightInPoints((short) 10);
			normalFont.setFontName("Mangal");

			// === Styles ===
			CellStyle titleCenter = wb.createCellStyle();
			titleCenter.setFont(headerFont);
			titleCenter.setAlignment(HorizontalAlignment.CENTER);
			titleCenter.setVerticalAlignment(VerticalAlignment.CENTER);

			CellStyle tableHeader = wb.createCellStyle();
			tableHeader.setFont(boldFont);
			tableHeader.setAlignment(HorizontalAlignment.CENTER);
			tableHeader.setVerticalAlignment(VerticalAlignment.CENTER);
			tableHeader.setBorderTop(BorderStyle.THIN);
			tableHeader.setBorderBottom(BorderStyle.THIN);
			tableHeader.setBorderLeft(BorderStyle.THIN);
			tableHeader.setBorderRight(BorderStyle.THIN);
			tableHeader.setWrapText(true);

			CellStyle dataCell = wb.createCellStyle();
			dataCell.setFont(normalFont);
			dataCell.setVerticalAlignment(VerticalAlignment.CENTER);
			dataCell.setBorderTop(BorderStyle.THIN);
			dataCell.setBorderBottom(BorderStyle.THIN);
			dataCell.setBorderLeft(BorderStyle.THIN);
			dataCell.setBorderRight(BorderStyle.THIN);
			dataCell.setWrapText(true);

			CellStyle centerCell = wb.createCellStyle();
			centerCell.cloneStyleFrom(dataCell);
			centerCell.setAlignment(HorizontalAlignment.CENTER);

			CellStyle boldRight = wb.createCellStyle();
			boldRight.cloneStyleFrom(dataCell);
			boldRight.setAlignment(HorizontalAlignment.RIGHT);
			Font bf = wb.createFont();
			bf.setBold(true);
			bf.setFontName("Mangal");
			boldRight.setFont(bf);

			// === Title Row ===
			int rowNum = 0;
			Row titleRow = sheet.createRow(rowNum++);
			titleRow.setHeightInPoints(25);
			Cell tcell = titleRow.createCell(0);
			tcell.setCellValue("अधीक्षक अभियंता, पुणे पाटबंधारे प्रकल्प मंडळ, पुणे प्रलंबित भांडार पडताळणी परिच्छेद");
			tcell.setCellStyle(titleCenter);
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

			rowNum++; // Blank row for spacing

			// === Header Rows ===
			Row headerRow1 = sheet.createRow(rowNum++);
			Row headerRow2 = sheet.createRow(rowNum++);

			String[] topHeaders = { "अ. क्र", "विभागाचे नाव", "वर्ष", "प्रलंबित परिच्छेदांचा तपशिल",
					"प्रलंबित परिच्छेदांची संख्या", "सद्यस्थिती", "", "", "" };

			String[] bottomHeaders = { "", "", "", "", "", "विभाग", "मंडळ", "भांडार पडताळणी पथक",
					"सादर केल्याचा दिनांक" };

			for (int i = 0; i < topHeaders.length; i++) {
				Cell c1 = headerRow1.createCell(i);
				c1.setCellValue(topHeaders[i]);
				c1.setCellStyle(tableHeader);

				Cell c2 = headerRow2.createCell(i);
				c2.setCellValue(bottomHeaders[i]);
				c2.setCellStyle(tableHeader);
				sheet.setColumnWidth(i, 5000);
			}

			// Merge Header cells
			sheet.addMergedRegion(new CellRangeAddress(rowNum - 2, rowNum - 1, 0, 0)); // अ. क्र
			sheet.addMergedRegion(new CellRangeAddress(rowNum - 2, rowNum - 1, 1, 1)); // विभागाचे नाव
			sheet.addMergedRegion(new CellRangeAddress(rowNum - 2, rowNum - 1, 2, 2)); // वर्ष
			sheet.addMergedRegion(new CellRangeAddress(rowNum - 2, rowNum - 1, 3, 3)); // तपशील
			sheet.addMergedRegion(new CellRangeAddress(rowNum - 2, rowNum - 1, 4, 4)); // संख्या
			sheet.addMergedRegion(new CellRangeAddress(rowNum - 2, rowNum - 2, 5, 8)); // सद्यस्थिती colspan

			int globalSerial = 1;
			int overallSum = 0;

			// === Data Section ===
			for (Map.Entry<String, List<StoreEntity>> entry : grouped.entrySet()) {
				String dept = entry.getKey();
				List<StoreEntity> deptRecords = entry.getValue();

				List<JsonNode> rows = new ArrayList<>();
				for (StoreEntity se : deptRecords) {
					JsonNode rd = se.getRowsData();
					if (rd == null)
						continue;
					if (rd.isArray())
						rd.forEach(rows::add);
					else
						rows.add(rd);
				}

				rows.sort(Comparator.comparingInt(n -> n.path("a_kr").asInt(0)));

				int startRow = rowNum;
				int deptSum = 0;

				for (JsonNode node : rows) {
					if ("D".equalsIgnoreCase(node.path("flag").asText("")))
						continue;

					Row r = sheet.createRow(rowNum++);
					int c = 0;

					Cell c0 = r.createCell(c++);
					c0.setCellValue(node.path("a_kr").asInt(globalSerial++));
					c0.setCellStyle(centerCell);

					Cell c1 = r.createCell(c++);
					c1.setCellValue(dept);
					c1.setCellStyle(dataCell);

					Cell c2 = r.createCell(c++);
					c2.setCellValue(node.path("varsh").asText(""));
					c2.setCellStyle(centerCell);

					Cell c3 = r.createCell(c++);
					c3.setCellValue(node.path("pralambitParichhedTapsheel").asText(""));
					c3.setCellStyle(dataCell);

					int count = node.path("pralambitParichhedSankhya").asInt(0);
					deptSum += count;
					overallSum += count;

					Cell c4 = r.createCell(c++);
					c4.setCellValue(count);
					c4.setCellStyle(centerCell);

					JsonNode s = node.path("sadyasthiti");
					r.createCell(c++).setCellValue(s.path("vibhag").asText(""));
					r.createCell(c++).setCellValue(s.path("mandal").asText(""));
					r.createCell(c++).setCellValue(s.path("bhandarPadtalaniPathak").asText(""));
					r.createCell(c++).setCellValue(s.path("sadarKelyachaDinank").asText(""));
					for (int i = 5; i <= 8; i++)
						r.getCell(i).setCellStyle(dataCell);
				}

				// Merge विभागाचे नाव rowspan
				if (rows.size() > 1) {
					sheet.addMergedRegion(new CellRangeAddress(startRow, rowNum - 1, 1, 1));
				}

				// Add spacing row
				rowNum++;

				// === Department total ===
				Row sumRow = sheet.createRow(rowNum++);
				Cell lbl = sumRow.createCell(0);
				lbl.setCellValue("एकूण");
				lbl.setCellStyle(boldRight);
				sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 3));

				Cell val = sumRow.createCell(4);
				val.setCellValue(deptSum);
				val.setCellStyle(centerCell);

				rowNum++; // extra spacing after each dept
			}

			// === Overall total ===
			Row totalRow = sheet.createRow(rowNum++);
			Cell tlbl = totalRow.createCell(0);
			tlbl.setCellValue("एकूण एकंदर");
			tlbl.setCellStyle(boldRight);
			sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 3));

			Cell tval = totalRow.createCell(4);
			tval.setCellValue(overallSum);
			tval.setCellStyle(centerCell);

			// === Auto-size columns ===
			for (int i = 0; i <= 8; i++) {
				sheet.autoSizeColumn(i);
				int w = sheet.getColumnWidth(i);
				sheet.setColumnWidth(i, Math.min(w + 1000, 10000));
			}

			// Fix column width for "प्रलंबित परिच्छेदांची संख्या"
			sheet.setColumnWidth(4, 9000);

			wb.write(out);
			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

			return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=\"StoreData.xlsx\"")
					.contentType(MediaType
							.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
					.body(new InputStreamResource(in));
		}
	}

}
