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
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

				// ---------- 3) Now handle each incoming row (create/update/delete single row)
				// ----------
				if (dept.getRows() == null)
					continue;
				for (VibhagRow row : dept.getRows()) {
					Integer rowId = row.getRowId();
					if (rowId == null)
						continue;

					Optional<StoreEntity> existingEntityOpt = storeRepository.findByDepartmentNameAndRowId(deptName,
							rowId);

					// ---------- DELETE LOGIC ADDED HERE ----------
					if ("D".equalsIgnoreCase(row.getFlag())) {
						if (existingEntityOpt.isPresent()) {
							storeRepository.delete(existingEntityOpt.get());
							log.append("Deleted rowId ").append(rowId).append(" from '").append(deptName).append("'. ");
						} else {
							log.append("Delete requested for rowId ").append(rowId).append(" in '").append(deptName)
									.append("' but not found. ");
						}
						continue; // skip further processing for this row
					}
					// ---------- END DELETE LOGIC ----------

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

	@Transactional(readOnly = true)
	@Override
	public ResponseEntity<InputStreamResource> downloadStoreData() throws IOException {

		List<StoreEntity> records = storeRepository.findAll();

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
