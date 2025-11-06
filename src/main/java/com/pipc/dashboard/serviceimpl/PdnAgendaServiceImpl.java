package com.pipc.dashboard.serviceimpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
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
import org.slf4j.MDC;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipc.dashboard.pdn.repository.NrldEntity;
import com.pipc.dashboard.pdn.repository.NrldRepository;
import com.pipc.dashboard.pdn.repository.PdnAgendaEntity;
import com.pipc.dashboard.pdn.repository.PdnAgendaRepository;
import com.pipc.dashboard.pdn.request.AgendaDetail;
import com.pipc.dashboard.pdn.request.AgendaPoint;
import com.pipc.dashboard.pdn.request.NrldRequest;
import com.pipc.dashboard.pdn.request.PdnAgendaRequest;
import com.pipc.dashboard.pdn.response.NrldResponse;
import com.pipc.dashboard.pdn.response.PdnAgendaResponse;
import com.pipc.dashboard.service.PdnAgendaService;
import com.pipc.dashboard.utility.ApplicationError;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class PdnAgendaServiceImpl implements PdnAgendaService {

	private final PdnAgendaRepository pdnAgnedaRepo;
	private final ObjectMapper objectMapper;
	private final NrldRepository nrldRepo;

	public PdnAgendaServiceImpl(PdnAgendaRepository pdnAgnedaRepo, ObjectMapper objectMapper, NrldRepository nrldRepo) {
		this.pdnAgnedaRepo = pdnAgnedaRepo;
		this.objectMapper = objectMapper;
		this.nrldRepo = nrldRepo;
	}

	// ----------------------------------------------------
	// 🔹 Save or Update PDN Agenda
	// ----------------------------------------------------
	@Override
	@Transactional
	public PdnAgendaResponse saveOrUpdatePdnAgenda(PdnAgendaRequest pdnAgendaRequest) {

		PdnAgendaResponse response = new PdnAgendaResponse();
		ApplicationError error = new ApplicationError();
		StringBuilder statusMsg = new StringBuilder();

		try {
			String currentUser = Optional.ofNullable(MDC.get("userId")).orElse("SYSTEM");

			for (AgendaPoint point : pdnAgendaRequest.getAgendaPoints()) {
				for (AgendaDetail detail : point.getDetails()) {

					JsonNode incomingData = detail.getColumnData();

					// 🔹 Extract dam name dynamically
					String damName = extractFieldValue(incomingData, "NameOfDam");
					if (damName == null || damName.isEmpty()) {
						statusMsg.append("[Skipped: Missing Dam Name for RowId ").append(detail.getRowId())
								.append("], ");
						continue;
					}

					// 🔹 Read flag & deleteId from detail (top-level)
					String flag = detail.getFlag();
					Long deleteId = detail.getDeleteId();

					// 🔹 Find matching records (for both update & delete)
					Optional<PdnAgendaEntity> existingOpt = pdnAgnedaRepo
							.findBySubmissionYearAndPointOfAgendaAndRecordIdAndNameOfDam(
									pdnAgendaRequest.getSubmissionYear(), point.getPointOfAgenda(), detail.getRowId(),
									damName);

					Optional<PdnAgendaEntity> existingOptByDeleteId = Optional.empty();
					if (deleteId != null) {
						existingOptByDeleteId = pdnAgnedaRepo
								.findBySubmissionYearAndPointOfAgendaAndDeleteIdAndNameOfDam(
										pdnAgendaRequest.getSubmissionYear(), point.getPointOfAgenda(), deleteId,
										damName);
					}

					// ✅ DELETE LOGIC (based on flag 'D')
					if ("D".equalsIgnoreCase(flag)) {
						if (existingOptByDeleteId.isPresent()) {
							pdnAgnedaRepo.delete(existingOptByDeleteId.get());
							statusMsg.append("Deleted: ").append(damName).append(" (DeleteId: ").append(deleteId)
									.append("), ");
						} else if (existingOpt.isPresent()) {
							pdnAgnedaRepo.delete(existingOpt.get());
							statusMsg.append("Deleted by RowId: ").append(detail.getRowId()).append(" (")
									.append(damName).append("), ");
						} else {
							statusMsg.append("Delete requested but record not found for: ").append(damName)
									.append(", ");
						}
						continue; // skip rest (no update/create)
					}

					// 🔹 Otherwise handle Create or Update
					PdnAgendaEntity entity;
					if (existingOpt.isPresent()) {
						entity = existingOpt.get();
						JsonNode existingData = entity.getColumnData();

						if (!existingData.equals(incomingData)) {
							entity.setColumnData(incomingData);
							entity.setUpdatedBy(currentUser);
							entity.setUpdatedAt(LocalDateTime.now());
							entity.setRecordFlag("U");
							pdnAgnedaRepo.save(entity);
							statusMsg.append("Updated: ").append(damName).append(", ");
						} else {
							statusMsg.append("No change for: ").append(damName).append(", ");
						}

					} else {
						entity = new PdnAgendaEntity();
						entity.setSubmissionTitle(pdnAgendaRequest.getSubmissionTitle());
						entity.setSubmissionYear(pdnAgendaRequest.getSubmissionYear());
						entity.setSrNo(point.getSrNo());
						entity.setPointOfAgenda(point.getPointOfAgenda());
						entity.setRecordId(detail.getRowId());
						entity.setNameOfDam(damName);
						entity.setColumnData(incomingData);
						entity.setRecordFlag("C");
						entity.setCreatedBy(currentUser);
						entity.setUpdatedBy(currentUser);
						entity.setCreatedAt(LocalDateTime.now());
						entity.setUpdatedAt(LocalDateTime.now());
						entity.setDeleteId(deleteId); // ✅ Save deleteId if given

						pdnAgnedaRepo.save(entity);
						statusMsg.append("Created: ").append(damName).append(", ");
					}
				}
			}

			error.setErrorCode("0");
			error.setErrorDescription("Agenda processed: " + statusMsg);
			response.setMessage("Success");
			response.setErrorDetails(error);

		} catch (Exception e) {
			error.setErrorCode("1");
			error.setErrorDescription("Error while saving agenda: " + e.getMessage());
			response.setMessage("Failed");
			response.setErrorDetails(error);
		}

		return response;
	}

	// ----------------------------------------------------
	// 🔹 Save or Update NRLD (Dam Records)
	// ----------------------------------------------------
	@Override
	public NrldResponse saveOrUpdateNrld(NrldRequest nrldRequest) {
		NrldResponse response = new NrldResponse();
		ApplicationError error = new ApplicationError();

		List<String> createdRecords = new ArrayList<>();
		List<String> updatedRecords = new ArrayList<>();
		List<String> deletedRecords = new ArrayList<>();

		String currentUser = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");

		try {
			for (JsonNode record : nrldRequest.getRecords()) {

				JsonNode recordData = record.get("data");
				if (recordData == null || recordData.isNull())
					continue;

				String year = nrldRequest.getSubmissionYear();
				String damName = recordData.has("NameOfDam") ? recordData.get("NameOfDam").asText() : null;
				Integer rowId = recordData.has("rowId") ? recordData.get("rowId").asInt() : null;
				Long deleteId = recordData.has("deleteId") ? recordData.get("deleteId").asLong() : null;
				String flag = recordData.has("flag") ? recordData.get("flag").asText().trim() : null;

				if (damName == null || damName.isEmpty())
					continue;

				// ✅ DELETE logic
				if ("D".equalsIgnoreCase(flag) && deleteId != null) {
					Optional<NrldEntity> existingToDelete = nrldRepo.findByYearAndDamNameAndDeleteId(year, damName,
							deleteId);

					if (existingToDelete.isPresent()) {
						nrldRepo.delete(existingToDelete.get());
						deletedRecords.add("Deleted: " + damName + " (DeleteId: " + deleteId + ")");
					} else {
						deletedRecords.add("Delete requested but record not found for " + damName + " (DeleteId: "
								+ deleteId + ")");
					}
					continue; // Skip further processing for deleted record
				}

				// 🔍 Check for existing record
				Optional<NrldEntity> existingOpt = nrldRepo.findByRowIdAndDamNameAndYear(rowId, damName, year);

				NrldEntity entity;

				if (existingOpt.isPresent()) {
					entity = existingOpt.get();
					if (!entity.getData().equals(recordData)) {
						entity.setData(recordData);
						entity.setUpdatedBy(currentUser);
						entity.setUpdatedAt(LocalDateTime.now());
						entity.setRecordFlag("U");
						nrldRepo.save(entity);
						updatedRecords.add("Updated: " + damName);
					}
				} else {
					entity = NrldEntity.builder().rowId(rowId).deleteId(deleteId).damName(damName).year(year)
							.data(recordData).createdBy(currentUser).updatedBy(currentUser)
							.createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).recordFlag("C").build();

					nrldRepo.save(entity);
					createdRecords.add("Created: " + damName);
				}
			}

			// ✅ Combine summary message
			StringBuilder desc = new StringBuilder();
			if (!createdRecords.isEmpty())
				desc.append(String.join(", ", createdRecords)).append(". ");
			if (!updatedRecords.isEmpty())
				desc.append(String.join(", ", updatedRecords)).append(". ");
			if (!deletedRecords.isEmpty())
				desc.append(String.join(", ", deletedRecords)).append(". ");
			if (createdRecords.isEmpty() && updatedRecords.isEmpty() && deletedRecords.isEmpty())
				desc.append("No changes detected.");

			error.setErrorCode("0");
			error.setErrorDescription(desc.toString());
			response.setErrorDetails(error);

		} catch (Exception e) {
			error.setErrorCode("1");
			error.setErrorDescription("Error while saving NRLD data: " + e.getMessage());
			response.setErrorDetails(error);
		}

		return response;
	}

	// ----------------------------------------------------
	// 🔍 Helper for dynamic key extraction (case-insensitive)
	// ----------------------------------------------------
	private String extractFieldValue(JsonNode data, String keyword) {
		if (data == null || !data.isObject())
			return null;

		for (Iterator<String> it = data.fieldNames(); it.hasNext();) {
			String field = it.next();
			if (field.toLowerCase().contains(keyword.toLowerCase())) {
				JsonNode value = data.get(field);
				if (value != null && !value.isNull())
					return value.asText();
			}
		}
		return null;
	}

	@Override
	public Page<PdnAgendaEntity> getPDNAgenda(String projectYear, String projectName, int page, int size) {

		Pageable pageable = PageRequest.of(page, size, Sort.by("recordId").ascending());

		return pdnAgnedaRepo.findBySubmissionYear(projectYear, pageable);

	}

	@Override
	public Page<NrldEntity> getNrldByYear(String year, String damName, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("rowId").ascending());

		if (damName != null && !damName.isEmpty()) {
			return nrldRepo.findByYearAndDamNameContainingIgnoreCase(year, damName, pageable);
		} else {
			return nrldRepo.findByYear(year, pageable);
		}
	}

	@Override
	public ResponseEntity<InputStreamResource> generateNrldExcel(String year) throws IOException {
		List<NrldEntity> records = nrldRepo.findByYear(year);

		if (records == null || records.isEmpty()) {
			throw new IllegalArgumentException("No data found for NRLD year: " + year);
		}

		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("NRLD-" + year);

			// === FONT & STYLE SETUP ===
			Font boldFont = workbook.createFont();
			boldFont.setBold(true);
			boldFont.setFontHeightInPoints((short) 12);

			CellStyle boldCenter = workbook.createCellStyle();
			boldCenter.setFont(boldFont);
			boldCenter.setAlignment(HorizontalAlignment.CENTER);
			boldCenter.setVerticalAlignment(VerticalAlignment.CENTER);

			CellStyle normalCenter = workbook.createCellStyle();
			normalCenter.setAlignment(HorizontalAlignment.CENTER);
			normalCenter.setVerticalAlignment(VerticalAlignment.CENTER);
			normalCenter.setWrapText(true);
			normalCenter.setBorderBottom(BorderStyle.THIN);
			normalCenter.setBorderTop(BorderStyle.THIN);
			normalCenter.setBorderLeft(BorderStyle.THIN);
			normalCenter.setBorderRight(BorderStyle.THIN);

			CellStyle headerStyle = workbook.createCellStyle();
			headerStyle.cloneStyleFrom(boldCenter);
			headerStyle.setWrapText(true);
			headerStyle.setBorderBottom(BorderStyle.THIN);
			headerStyle.setBorderTop(BorderStyle.THIN);
			headerStyle.setBorderLeft(BorderStyle.THIN);
			headerStyle.setBorderRight(BorderStyle.THIN);

			// === HEADER SECTION ===
			int rowIndex = 0;

			Row titleRow = sheet.createRow(rowIndex++);
			sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 16));
			Cell titleCell = titleRow.createCell(0);
			titleCell.setCellValue("National Register of Large Dams-" + year);
			titleCell.setCellStyle(boldCenter);

			Row subTitleRow = sheet.createRow(rowIndex++);
			sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 16));
			Cell subTitleCell = subTitleRow.createCell(0);
			subTitleCell.setCellValue("Circle :- Superintending Engineer, Pune Irrigation Project Circle, Pune");
			subTitleCell.setCellStyle(normalCenter);

			rowIndex++; // blank line

			// === COLUMN HEADERS ===
			String[] headers = { "Sr.No.", "PIC", "Name of Dam", "SDSO Name", "Dam Owner", "Latitude/longitude",
					"Year of Completion", "River Basin", "River", "District", "Dam Type",
					"Height above Lowest Foundation Level (M)", "Dam Length (M)", "Gross Storage Capacity (MM3)",
					"Live Storage Capacity (MM3)", "Designed Spillway Capacity (M3/Sec)", "Purpose" };

			Row headerRow = sheet.createRow(rowIndex++);
			for (int i = 0; i < headers.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(headers[i]);
				cell.setCellStyle(headerStyle);
				sheet.setColumnWidth(i, 4500);
			}

			// === DATA ROWS ===
			for (NrldEntity entity : records) {
				JsonNode json = entity.getData();
				Row row = sheet.createRow(rowIndex++);
				int col = 0;

				String[] keys = { "Sr.No.", "PIC", "NameOfDam", "SDSO Name", "Dam Owner", "Latitude-Longitude",
						"YearOfCompletion", "RiverBasin", "River", "District", "DamType",
						"HeightAboveLowestFoundationLevel(M)", "DamLength(M)", "GrossStorageCapacity(MM3)",
						"LiveStorageCapacity(MM3)", "DesignedSpillwayCapacity(M3/Sec)", "Purpose" };

				for (String key : keys) {
					Cell cell = row.createCell(col++);
					JsonNode valueNode = json.get(key);
					String value = valueNode != null ? valueNode.asText() : "";
					cell.setCellValue(value);
					cell.setCellStyle(normalCenter);
				}
			}

			// === FOOTER ===
			// === FOOTER (Shifted 2 rows down, right-aligned) ===
			rowIndex += 2; // move footer 2 rows below last data

			Row footerRow = sheet.createRow(rowIndex);

			// Merge cells from Live Storage Capacity (col 14) to Purpose (col 16)
			sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIndex, rowIndex, 14, 16));

			Cell footerCell = footerRow.createCell(14);
			footerCell.setCellValue("Pune Irrigation Project Circle, Pune");

			// Footer style: italic + right aligned
			CellStyle footerStyle = workbook.createCellStyle();
			Font italicFont = workbook.createFont();
			italicFont.setItalic(true);
			footerStyle.setFont(italicFont);
			footerStyle.setAlignment(HorizontalAlignment.RIGHT);
			footerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

			footerCell.setCellStyle(footerStyle);

			// === WRITE TO STREAM ===
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			workbook.write(out);
			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

			String filename = "NRLD-" + year + ".xlsx";

			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
					.contentType(MediaType
							.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
					.body(new InputStreamResource(in));
		}
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadPdnAgendaData(String year) throws IOException {
		List<PdnAgendaEntity> records = pdnAgnedaRepo.findBySubmissionYear(year);

		if (records == null || records.isEmpty()) {
			throw new IllegalArgumentException("No agenda records found for year: " + year);
		}

		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("PDN Agenda " + year);

			// ===== Styles =====
			Font boldFont = workbook.createFont();
			boldFont.setBold(true);
			boldFont.setFontHeightInPoints((short) 12);

			CellStyle boldCenter = workbook.createCellStyle();
			boldCenter.setFont(boldFont);
			boldCenter.setAlignment(HorizontalAlignment.CENTER);
			boldCenter.setVerticalAlignment(VerticalAlignment.CENTER);
			boldCenter.setWrapText(true);
			boldCenter.setBorderBottom(BorderStyle.THIN);
			boldCenter.setBorderTop(BorderStyle.THIN);
			boldCenter.setBorderLeft(BorderStyle.THIN);
			boldCenter.setBorderRight(BorderStyle.THIN);

			CellStyle normalCell = workbook.createCellStyle();
			normalCell.setAlignment(HorizontalAlignment.LEFT);
			normalCell.setVerticalAlignment(VerticalAlignment.CENTER);
			normalCell.setWrapText(true);
			normalCell.setBorderBottom(BorderStyle.THIN);
			normalCell.setBorderTop(BorderStyle.THIN);
			normalCell.setBorderLeft(BorderStyle.THIN);
			normalCell.setBorderRight(BorderStyle.THIN);

			// ===== Title Row =====
			int rowIndex = 0;
			Row titleRow = sheet.createRow(rowIndex++);
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
			Cell titleCell = titleRow.createCell(0);
			titleCell.setCellValue("Agenda points for the meeting with Dam Owners.");
			titleCell.setCellStyle(boldCenter);

			// ===== Header Row =====
			String[] headers = { "Sr No", "Points", "Name Of Dams", "Remarks" };
			Row headerRow = sheet.createRow(rowIndex++);
			for (int i = 0; i < headers.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(headers[i]);
				cell.setCellStyle(boldCenter);
				sheet.setColumnWidth(i, 8000);
			}

			// ===== Group Data by SrNo =====
			Map<Integer, List<PdnAgendaEntity>> grouped = records.stream()
					.collect(Collectors.groupingBy(PdnAgendaEntity::getSrNo, LinkedHashMap::new, Collectors.toList()));

			// ===== Data Rows =====
			for (Map.Entry<Integer, List<PdnAgendaEntity>> entry : grouped.entrySet()) {
				int srNo = entry.getKey();
				List<PdnAgendaEntity> groupList = entry.getValue();

				int startRow = rowIndex;
				for (PdnAgendaEntity entity : groupList) {
					Row row = sheet.createRow(rowIndex++);
					JsonNode data = entity.getColumnData();

					// Sr.No.
					Cell srCell = row.createCell(0);
					srCell.setCellValue(srNo);
					srCell.setCellStyle(normalCell);

					// Points
					Cell pointCell = row.createCell(1);
					pointCell.setCellValue(entity.getPointOfAgenda() != null ? entity.getPointOfAgenda() : "");
					pointCell.setCellStyle(normalCell);

					// Name Of Dam
					Cell nameCell = row.createCell(2);
					String damName = data.has("NameOfDam") ? data.get("NameOfDam").asText() : "";
					nameCell.setCellValue(damName);
					nameCell.setCellStyle(normalCell);

					// Remarks
					Cell remarkCell = row.createCell(3);
					String remarks = data.has("Remarks") ? data.get("Remarks").asText() : "";
					remarkCell.setCellValue(remarks);
					remarkCell.setCellStyle(normalCell);
				}

				// Merge Sr.No. and Points columns vertically
				if (groupList.size() > 1) {
					int endRow = startRow + groupList.size() - 1;
					sheet.addMergedRegion(new CellRangeAddress(startRow, endRow, 0, 0)); // Sr.No.
					sheet.addMergedRegion(new CellRangeAddress(startRow, endRow, 1, 1)); // Points
				}
			}

			// ===== Final Formatting =====
			sheet.setZoom(100); // same look as 637x956 example
			sheet.setDefaultRowHeightInPoints(22);

			// ===== Write to stream =====
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			workbook.write(out);
			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

			String filename = "PDN-Agenda-" + year + ".xlsx";
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
					.contentType(MediaType
							.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
					.body(new InputStreamResource(in));
		}
	}

}
