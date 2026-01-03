package com.pipc.dashboard.serviceimpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.pipc.dashboard.service.SupremaService;
import com.pipc.dashboard.suprama.repository.SupremaEntity;
import com.pipc.dashboard.suprama.repository.SupremaRepository;
import com.pipc.dashboard.suprama.request.SupremaRequest;
import com.pipc.dashboard.suprama.response.SupremaResponse;
import com.pipc.dashboard.utility.ApplicationError;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SupremeServiceImpl implements SupremaService {

	private final SupremaRepository supremaRepo;

	// ----------------------------------------------------
	// 🔹 Save or Update Suprema Data
	// ----------------------------------------------------
	@Override
	@Transactional
	public SupremaResponse saveOrUpdateSuprema(SupremaRequest request) {

		SupremaResponse response = new SupremaResponse();
		ApplicationError error = new ApplicationError();

		final String corrId = MDC.get("correlationId");
		final String currentUser = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
		final String projectYear = request.getProjectYear();
		final LocalDateTime now = LocalDateTime.now();

		log.info("START saveOrUpdateSuprema | projectYear={} | rows={} | user={} | corrId={}", projectYear,
				request.getRows() == null ? 0 : request.getRows().size(), currentUser, corrId);

		try {

			List<String> createdProjects = new ArrayList<>();
			List<String> updatedProjects = new ArrayList<>();
			List<String> deletedProjects = new ArrayList<>();

			for (JsonNode row : request.getRows()) {

				Integer rowId = row.has("rowId") ? row.get("rowId").asInt() : null;
				Long deleteId = row.has("deleteId") ? row.get("deleteId").asLong() : null;
				String projectName = extractFieldValue(row, "prakalchenav", "projectname", "project", "name");
				String flag = row.has("flag") ? row.get("flag").asText().trim() : null;

				if (rowId == null || projectName == null || projectName.isBlank()) {
					log.debug("Skipping invalid row | rowId={} | projectName={}", rowId, projectName);
					continue;
				}

				/* ---------------- DELETE ---------------- */
				if ("D".equalsIgnoreCase(flag)) {

					Optional<SupremaEntity> existingOpt = supremaRepo
							.findByProjectYearAndDeleteIdAndProjectName(projectYear, deleteId, projectName);

					if (existingOpt.isPresent()) {
						supremaRepo.delete(existingOpt.get());
						deletedProjects.add(projectName + " (deleteId: " + deleteId + ")");
						log.info("Deleted Suprema | project={} | deleteId={} | corrId={}", projectName, deleteId,
								corrId);
					} else {
						deletedProjects.add(projectName + " (deleteId: " + deleteId + ") - not found");
						log.warn("Delete requested but not found | project={} | deleteId={} | corrId={}", projectName,
								deleteId, corrId);
					}
					continue;
				}

				/* ---------------- UPDATE / CREATE ---------------- */
				Optional<SupremaEntity> existingOpt = supremaRepo.findByProjectYearAndRowIdAndProjectName(projectYear,
						rowId, projectName);

				if (existingOpt.isPresent()) {

					SupremaEntity entity = existingOpt.get();

					if (!Objects.equals(entity.getSupremaData(), row)) {

						entity.setSupremaData(row);
						entity.setUpdatedBy(currentUser);
						entity.setUpdatedDatetime(now);
						entity.setRecordFlag("U");

						supremaRepo.save(entity);
						updatedProjects.add(projectName + " (RowId: " + rowId + ")");

						log.info("Updated Suprema | project={} | rowId={} | corrId={}", projectName, rowId, corrId);
					} else {
						log.debug("No change detected | project={} | rowId={} | corrId={}", projectName, rowId, corrId);
					}

				} else {

					SupremaEntity entity = SupremaEntity.builder().projectName(projectName).projectYear(projectYear)
							.rowId(rowId)
							.deleteId(
									deleteId != null ? deleteId : ThreadLocalRandom.current().nextLong(100000, 999999))
							.supremaData(row).createdBy(currentUser).updatedBy(currentUser).createdDatetime(now)
							.updatedDatetime(now).recordFlag("C").build();

					supremaRepo.save(entity);
					createdProjects.add(projectName + " (RowId: " + rowId + ")");

					log.info("Created Suprema | project={} | rowId={} | corrId={}", projectName, rowId, corrId);
				}
			}

			/* ---------------- RESPONSE SUMMARY ---------------- */
			StringBuilder desc = new StringBuilder();

			if (!createdProjects.isEmpty())
				desc.append("Created: ").append(String.join(", ", createdProjects)).append(". ");

			if (!updatedProjects.isEmpty())
				desc.append("Updated: ").append(String.join(", ", updatedProjects)).append(". ");

			if (!deletedProjects.isEmpty())
				desc.append("Deleted: ").append(String.join(", ", deletedProjects)).append(". ");

			if (createdProjects.isEmpty() && updatedProjects.isEmpty() && deletedProjects.isEmpty())
				desc.append("No changes detected. ");

			desc.append("By ").append(currentUser).append(".");

			error.setErrorCode("0");
			error.setErrorDescription(desc.toString());

			log.info("SUCCESS saveOrUpdateSuprema | created={} | updated={} | deleted={} | corrId={}",
					createdProjects.size(), updatedProjects.size(), deletedProjects.size(), corrId);

		} catch (Exception e) {

			log.error("ERROR saveOrUpdateSuprema | projectYear={} | corrId={}", projectYear, corrId, e);

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
	public List<SupremaEntity> getSupremaByProjectYear(String projectYear) {

		final String corrId = MDC.get("correlationId");
		final String user = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");

		log.info("START getSupremaByProjectYear | projectYear={} | user={} | corrId={}", projectYear, user, corrId);

		try {

			List<SupremaEntity> list = supremaRepo.findByProjectYearOrderByRowIdAsc(projectYear);

			if (list == null || list.isEmpty()) {
				log.warn("No Suprema data found | projectYear={} | corrId={}", projectYear, corrId);
				return Collections.emptyList();
			}

			log.info("SUCCESS getSupremaByProjectYear | projectYear={} | records={} | corrId={}", projectYear,
					list.size(), corrId);

			return list;

		} catch (Exception e) {

			log.error("ERROR getSupremaByProjectYear | projectYear={} | corrId={}", projectYear, corrId, e);

			return Collections.emptyList();
		}
	}

	// ----------------------------------------------------
	// 🔍 Helper Method: extract field safely by matching possible names
	// ----------------------------------------------------
	private String extractFieldValue(JsonNode node, String... possibleNames) {
		if (node == null || !node.isObject())
			return null;

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

	public ResponseEntity<InputStreamResource> downloadSupremaExcel(String projectYear) throws IOException {

		List<SupremaEntity> allRecords = supremaRepo.findByProjectYearOrderByRowIdAsc(projectYear);

		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("सुप्रमा प्रस्ताव सध्यस्थिती");

		// ✅ Column widths
		sheet.setColumnWidth(0, 1500); // क्रमांक
		sheet.setColumnWidth(1, 7000); // प्रकल्पाचे नाव
		sheet.setColumnWidth(2, 3000); // मंजूर प्रमा/सुप्रमा
		sheet.setColumnWidth(3, 4000); // किंमत (₹ कोटी)
		sheet.setColumnWidth(4, 4000); // मंजुरीचा दिनांक
		sheet.setColumnWidth(5, 3000); // प्रस्तावित सुप्रमा
		sheet.setColumnWidth(6, 4000); // किंमत (₹ कोटी)
		sheet.setColumnWidth(7, 18000); // सुप्रमा प्रस्ताव सध्यस्थिती

		// ✅ Styles
		CellStyle titleStyle = createTitleStyle(workbook);
		CellStyle headerStyle = createHeaderStyle(workbook);
		CellStyle subHeaderStyle = createSubHeaderStyle(workbook);
		CellStyle textStyle = createTextStyle(workbook);

		int rowIdx = 0;

		// 🔹 Title Row
		Row titleRow = sheet.createRow(rowIdx++);
		Cell titleCell = titleRow.createCell(0);
		titleCell.setCellValue("सुप्रमा प्रस्ताव सध्यस्थिती");
		titleCell.setCellStyle(titleStyle);
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));
		titleRow.setHeightInPoints(30);

		// 🔹 Empty spacer row
		rowIdx++;

		// 🔹 Department Row
		Row deptRow = sheet.createRow(rowIdx++);
		Cell deptCell = deptRow.createCell(0);
		deptCell.setCellValue("पुणे पाटबंधारे प्रकल्प मंडळ, पुणे");

		// ✅ Bold + centered style for dept name
		CellStyle deptStyle = workbook.createCellStyle();
		Font deptFont = workbook.createFont();
		deptFont.setBold(true);
		deptFont.setFontHeightInPoints((short) 10);
		deptStyle.setFont(deptFont);
//		deptStyle.setAlignment(HorizontalAlignment.CENTER);
//		deptStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		deptCell.setCellStyle(deptStyle);

		// ✅ Merge department row across all columns
		sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 7));

		// 🔹 Empty spacer after department
		rowIdx++;

		// ✅ Header rows start at rowIdx = 4 (Excel row 5)
		// --------------------------------------------------

		// 🔹 Header Row 1 (Top-level)
		Row header1 = sheet.createRow(rowIdx++);
		String[] headers1 = { "अ. क्र.", "प्रकल्पाचे नाव", "मंजुर प्रमा / सुप्रमा किंमत (रु.कोटी) ", "",
				"मंजुरीचा दिनांक", "प्रस्तावित सुप्रमा", "", "सुप्रमा प्रस्ताव सध्यस्थिती" };
		for (int i = 0; i < headers1.length; i++) {
			Cell cell = header1.createCell(i);
			cell.setCellValue(headers1[i]);
			cell.setCellStyle(headerStyle);
		}

		// ✅ Correct Merge Structure (row index offset fixed)
		sheet.addMergedRegion(new CellRangeAddress(4, 6, 0, 0)); // अ. क्र. rowspan=3
		sheet.addMergedRegion(new CellRangeAddress(4, 6, 1, 1)); // प्रकल्पाचे नाव rowspan=3
		sheet.addMergedRegion(new CellRangeAddress(4, 4, 2, 3)); // मंजूर प्रमा / सुप्रमा colspan=2
		sheet.addMergedRegion(new CellRangeAddress(4, 6, 4, 4)); // मंजुरीचा दिनांक rowspan=3
		sheet.addMergedRegion(new CellRangeAddress(4, 4, 5, 6)); // प्रस्तावित सुप्रमा colspan=2 (parent)
		sheet.addMergedRegion(new CellRangeAddress(4, 6, 7, 7)); // सुप्रमा प्रस्ताव सध्यस्थिती rowspan=3

		// 🔹 Header Row 2 (Second Level)
		Row header2 = sheet.createRow(rowIdx++);
		String[] headers2 = { "", "", "प्रमा / सुप्रमा", "किंमत (₹ कोटी)", "", "किंमत (रु. कोटी)", "", "" };
		for (int i = 0; i < headers2.length; i++) {
			Cell cell = header2.createCell(i);
			cell.setCellValue(headers2[i]);
			cell.setCellStyle(subHeaderStyle);
		}

		// ✅ Merge “किंमत (रु. कोटी)” under प्रस्तावित सुप्रमा (colspan=2)
		sheet.addMergedRegion(new CellRangeAddress(5, 5, 5, 6));

		// 🔹 Header Row 3 (Third Level)
		Row header3 = sheet.createRow(rowIdx++);
		String[] headers3 = { "", "", "", "", "", "प्रमा / सुप्रमा", "किंमत (₹ कोटी)", "" };
		for (int i = 0; i < headers3.length; i++) {
			Cell cell = header3.createCell(i);
			cell.setCellValue(headers3[i]);
			cell.setCellStyle(subHeaderStyle);
		}

		// --------------------------------------------------
		// 🔹 Data Rows
		int serial = 1;
		for (SupremaEntity entity : allRecords) {
			JsonNode data = entity.getSupremaData();
			Row row = sheet.createRow(rowIdx++);
			int col = 0;

			createCell(row, col++, serial++, textStyle); // अ. क्र.
			createCell(row, col++, data.path("prakalchenav").asText(""), textStyle);
			createCell(row, col++, data.path("manjurPramaSuprama").asText(""), textStyle);
			createCell(row, col++, data.path("manjurKimta").asText(""), textStyle);
			createCell(row, col++, formatDate(data.path("manjuriDate").asText("")), textStyle);
			createCell(row, col++, data.path("prastavitSuprama").asText(""), textStyle);
			createCell(row, col++, data.path("prastavitKimta").asText(""), textStyle);
			createCell(row, col++, data.path("supramaProposalStatus").asText(""), textStyle);
		}

		// ✅ Write to output
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		workbook.write(out);
		workbook.close();

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=Suprema_Report_" + projectYear + ".xlsx");

		return ResponseEntity.ok().headers(headers)
				.contentType(
						MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(new InputStreamResource(in));
	}

	// ----------------- Helper Methods -----------------

	private String formatDate(String dateStr) {
		try {
			return DateTimeFormatter.ofPattern("yyyy-MM-dd").parse(dateStr)
					.query(java.time.temporal.TemporalQueries.localDate())
					.format(DateTimeFormatter.ofPattern("d/M/yyyy"));
		} catch (Exception e) {
			return dateStr;
		}
	}

	private void createCell(Row row, int col, Object value, CellStyle style) {
		Cell cell = row.createCell(col);
		if (value instanceof String)
			cell.setCellValue((String) value);
		else if (value instanceof Number)
			cell.setCellValue(((Number) value).doubleValue());
		else if (value != null)
			cell.setCellValue(value.toString());
		cell.setCellStyle(style);
	}

	private CellStyle createTitleStyle(Workbook wb) {
		Font font = wb.createFont();
		font.setBold(true);
		font.setFontHeightInPoints((short) 14);
		CellStyle style = wb.createCellStyle();
		style.setFont(font);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		return style;
	}

	private CellStyle createHeaderStyle(Workbook wb) {
		Font font = wb.createFont();
		font.setBold(true);
		font.setFontHeightInPoints((short) 10);
		CellStyle style = wb.createCellStyle();
		style.setFont(font);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setWrapText(true);
		return style;
	}

	private CellStyle createSubHeaderStyle(Workbook wb) {
		CellStyle style = createHeaderStyle(wb);
		Font font = wb.createFont();
		font.setBold(true);
		font.setFontHeightInPoints((short) 10);
		style.setFont(font);
		return style;
	}

	private CellStyle createTextStyle(Workbook wb) {
		Font font = wb.createFont();
		font.setFontHeightInPoints((short) 10);
		CellStyle style = wb.createCellStyle();
		style.setFont(font);
		style.setVerticalAlignment(VerticalAlignment.TOP);
		style.setWrapText(true);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		return style;
	}

}
