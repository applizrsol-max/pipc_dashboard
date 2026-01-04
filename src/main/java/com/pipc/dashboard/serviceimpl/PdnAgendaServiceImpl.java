package com.pipc.dashboard.serviceimpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.MDC;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipc.dashboard.pdn.repository.IrrigationCapacityEntityRevised;
import com.pipc.dashboard.pdn.repository.IrrigationRepositoryRevised;
import com.pipc.dashboard.pdn.repository.NrldEntity;
import com.pipc.dashboard.pdn.repository.NrldRepository;
import com.pipc.dashboard.pdn.repository.PdnAgendaEntity;
import com.pipc.dashboard.pdn.repository.PdnAgendaRepository;
import com.pipc.dashboard.pdn.request.AgendaDetail;
import com.pipc.dashboard.pdn.request.AgendaPoint;
import com.pipc.dashboard.pdn.request.IrrigationRowDTO;
import com.pipc.dashboard.pdn.request.IrrigationSaveRequest;
import com.pipc.dashboard.pdn.request.NrldRequest;
import com.pipc.dashboard.pdn.request.PdnAgendaRequest;
import com.pipc.dashboard.pdn.response.NrldResponse;
import com.pipc.dashboard.pdn.response.PdnAgendaResponse;
import com.pipc.dashboard.service.PdnAgendaService;
import com.pipc.dashboard.utility.ApplicationError;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PdnAgendaServiceImpl implements PdnAgendaService {

	private final PdnAgendaRepository pdnAgnedaRepo;
	private final NrldRepository nrldRepo;
	private final IrrigationRepositoryRevised irrigationRepositoryRevised;
	private final ObjectMapper objectMapper;

	// ----------------------------------------------------
	// 🔹 Save or Update PDN Agenda
	// ----------------------------------------------------
	@Override
	@Transactional
	public PdnAgendaResponse saveOrUpdatePdnAgenda(PdnAgendaRequest pdnAgendaRequest) {

		PdnAgendaResponse response = new PdnAgendaResponse();
		ApplicationError error = new ApplicationError();

		final String corrId = MDC.get("correlationId");
		final String currentUser = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");

		log.info("START saveOrUpdatePdnAgenda | year={} | user={} | corrId={}", pdnAgendaRequest.getSubmissionYear(),
				currentUser, corrId);

		StringBuilder statusMsg = new StringBuilder();

		try {

			for (AgendaPoint point : pdnAgendaRequest.getAgendaPoints()) {

				for (AgendaDetail detail : point.getDetails()) {

					JsonNode incomingData = detail.getColumnData();

					// 🔹 Extract dam name
					String damName = extractFieldValue(incomingData, "NameOfDam");
					if (damName == null || damName.isBlank()) {
						statusMsg.append("[Skipped: Missing Dam Name | rowId=").append(detail.getRowId()).append("], ");
						continue;
					}

					String flag = detail.getFlag();
					Long deleteId = detail.getDeleteId();

					// 🔍 Fetch existing records
					Optional<PdnAgendaEntity> existingOpt = pdnAgnedaRepo
							.findBySubmissionYearAndPointOfAgendaAndRecordIdAndNameOfDam(
									pdnAgendaRequest.getSubmissionYear(), point.getPointOfAgenda(), detail.getRowId(),
									damName);

					Optional<PdnAgendaEntity> existingByDeleteId = Optional.empty();
					if (deleteId != null) {
						existingByDeleteId = pdnAgnedaRepo.findBySubmissionYearAndPointOfAgendaAndDeleteIdAndNameOfDam(
								pdnAgendaRequest.getSubmissionYear(), point.getPointOfAgenda(), deleteId, damName);
					}

					// ================= DELETE =================
					if ("D".equalsIgnoreCase(flag)) {

						if (existingByDeleteId.isPresent()) {
							pdnAgnedaRepo.delete(existingByDeleteId.get());
							statusMsg.append("Deleted(deleteId): ").append(damName).append(", ");
						} else if (existingOpt.isPresent()) {
							pdnAgnedaRepo.delete(existingOpt.get());
							statusMsg.append("Deleted(rowId): ").append(damName).append(", ");
						} else {
							statusMsg.append("Delete requested but not found: ").append(damName).append(", ");
						}
						continue;
					}

					// ================= UPDATE =================
					if (existingOpt.isPresent()) {

						PdnAgendaEntity entity = existingOpt.get();

						if (!entity.getColumnData().equals(incomingData)) {
							entity.setColumnData(incomingData);
							entity.setUpdatedBy(currentUser);
							entity.setUpdatedAt(LocalDateTime.now());
							entity.setRecordFlag("U");

							pdnAgnedaRepo.save(entity);
							statusMsg.append("Updated: ").append(damName).append(", ");
						} else {
							statusMsg.append("No change: ").append(damName).append(", ");
						}

					}
					// ================= CREATE =================
					else {

						PdnAgendaEntity entity = new PdnAgendaEntity();
						entity.setSubmissionTitle(pdnAgendaRequest.getSubmissionTitle());
						entity.setSubmissionYear(pdnAgendaRequest.getSubmissionYear());
						entity.setSrNo(point.getSrNo());
						entity.setPointOfAgenda(point.getPointOfAgenda());
						entity.setRecordId(detail.getRowId());
						entity.setNameOfDam(damName);
						entity.setColumnData(incomingData);
						entity.setRecordFlag("C");
						entity.setDeleteId(deleteId);

						entity.setCreatedBy(currentUser);
						entity.setUpdatedBy(currentUser);
						entity.setCreatedAt(LocalDateTime.now());
						entity.setUpdatedAt(LocalDateTime.now());

						pdnAgnedaRepo.save(entity);
						statusMsg.append("Created: ").append(damName).append(", ");
					}
				}
			}

			error.setErrorCode("0");
			error.setErrorDescription("Agenda processed successfully");
			response.setMessage(statusMsg.toString());
			response.setErrorDetails(error);

			log.info("SUCCESS saveOrUpdatePdnAgenda | year={} | corrId={}", pdnAgendaRequest.getSubmissionYear(),
					corrId);

		} catch (Exception e) {

			log.error("ERROR saveOrUpdatePdnAgenda | year={} | corrId={}", pdnAgendaRequest.getSubmissionYear(), corrId,
					e);

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
	@Transactional
	public NrldResponse saveOrUpdateNrld(NrldRequest nrldRequest) {

		NrldResponse response = new NrldResponse();
		ApplicationError error = new ApplicationError();

		final String corrId = MDC.get("correlationId");
		final String currentUser = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");

		log.info("START saveOrUpdateNrld | year={} | user={} | corrId={}", nrldRequest.getSubmissionYear(), currentUser,
				corrId);

		List<String> createdRecords = new ArrayList<>();
		List<String> updatedRecords = new ArrayList<>();
		List<String> deletedRecords = new ArrayList<>();

		try {

			final String year = nrldRequest.getSubmissionYear();
			final LocalDateTime now = LocalDateTime.now();

			for (JsonNode record : nrldRequest.getRecords()) {

				JsonNode recordData = record.get("data");
				if (recordData == null || recordData.isNull()) {
					continue;
				}

				String damName = recordData.has("NameOfDam") ? recordData.get("NameOfDam").asText() : null;

				Integer rowId = recordData.has("rowId") ? recordData.get("rowId").asInt() : null;

				Long deleteId = recordData.has("deleteId") ? recordData.get("deleteId").asLong() : null;

				String flag = recordData.has("flag") ? recordData.get("flag").asText().trim() : null;

				if (damName == null || damName.isBlank() || rowId == null) {
					continue;
				}

				// ================= DELETE =================
				if ("D".equalsIgnoreCase(flag) && deleteId != null) {

					Optional<NrldEntity> toDelete = nrldRepo.findByYearAndDamNameAndDeleteId(year, damName, deleteId);

					if (toDelete.isPresent()) {
						nrldRepo.delete(toDelete.get());
						deletedRecords.add("Deleted: " + damName + " (DeleteId: " + deleteId + ")");
					} else {
						deletedRecords.add("Delete requested but record not found for " + damName + " (DeleteId: "
								+ deleteId + ")");
					}
					continue;
				}

				// ================= UPDATE / CREATE =================
				Optional<NrldEntity> existingOpt = nrldRepo.findByRowIdAndDamNameAndYear(rowId, damName, year);

				if (existingOpt.isPresent()) {

					NrldEntity entity = existingOpt.get();

					if (!entity.getData().equals(recordData)) {
						entity.setData(recordData);
						entity.setUpdatedBy(currentUser);
						entity.setUpdatedAt(now);
						entity.setRecordFlag("U");

						nrldRepo.save(entity);
						updatedRecords.add("Updated: " + damName);
					}

				} else {

					NrldEntity entity = NrldEntity.builder().rowId(rowId).deleteId(deleteId).damName(damName).year(year)
							.data(recordData).createdBy(currentUser).updatedBy(currentUser).createdAt(now)
							.updatedAt(now).recordFlag("C").build();

					nrldRepo.save(entity);
					createdRecords.add("Created: " + damName);
				}
			}

			// ================= RESPONSE BUILD =================
			StringBuilder summary = new StringBuilder();

			if (!createdRecords.isEmpty()) {
				summary.append(String.join(", ", createdRecords)).append(". ");
			}
			if (!updatedRecords.isEmpty()) {
				summary.append(String.join(", ", updatedRecords)).append(". ");
			}
			if (!deletedRecords.isEmpty()) {
				summary.append(String.join(", ", deletedRecords)).append(". ");
			}
			if (summary.length() == 0) {
				summary.append("No changes detected.");
			}

			error.setErrorCode("0");
			error.setErrorDescription(summary.toString());
			response.setErrorDetails(error);

			log.info("SUCCESS saveOrUpdateNrld | year={} | created={} | updated={} | deleted={} | corrId={}", year,
					createdRecords.size(), updatedRecords.size(), deletedRecords.size(), corrId);

		} catch (Exception e) {

			log.error("ERROR saveOrUpdateNrld | year={} | corrId={}", nrldRequest.getSubmissionYear(), corrId, e);

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
	public List<PdnAgendaEntity> getPDNAgenda(String projectYear) {

		final String corrId = MDC.get("correlationId");
		final String user = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");

		log.info("START getPDNAgenda | projectYear={} | user={} | corrId={}", projectYear, user, corrId);

		try {

			// 🔹 Single DB call with sorting
			List<PdnAgendaEntity> list = pdnAgnedaRepo.findBySubmissionYearOrderByRecordIdAsc(projectYear);

			if (list == null || list.isEmpty()) {
				log.warn("No PDN Agenda found | projectYear={} | corrId={}", projectYear, corrId);
				return Collections.emptyList();
			}

			log.info("SUCCESS getPDNAgenda | projectYear={} | records={} | corrId={}", projectYear, list.size(),
					corrId);

			return list;

		} catch (Exception e) {

			log.error("ERROR getPDNAgenda | projectYear={} | corrId={}", projectYear, corrId, e);
			return Collections.emptyList();
		}
	}

	@Override
	public List<NrldEntity> getNrldByYear(String year) {

		final String corrId = MDC.get("correlationId");
		final String user = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");

		log.info("START getNrldByYear | year={} | user={} | corrId={}", year, user, corrId);

		try {

			// 🔹 Single DB call with DB-level sorting
			List<NrldEntity> list = nrldRepo.findByYearOrderByRowIdAsc(year);

			if (list == null || list.isEmpty()) {
				log.warn("No NRLD data found | year={} | corrId={}", year, corrId);
				return Collections.emptyList();
			}

			log.info("SUCCESS getNrldByYear | year={} | records={} | corrId={}", year, list.size(), corrId);

			return list;

		} catch (Exception e) {

			log.error("ERROR getNrldByYear | year={} | corrId={}", year, corrId, e);
			return Collections.emptyList();
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

	@Override
	@Transactional
	public NrldResponse saveOrUpdateIccCap(IrrigationSaveRequest req) {

		NrldResponse response = new NrldResponse();
		ApplicationError error = new ApplicationError();

		final String corrId = MDC.get("correlationId");
		final String currentUser = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");

		log.info("START saveOrUpdateIccCap | year={} | date={} | user={} | corrId={}", req.getYear(), req.getDate(),
				currentUser, corrId);

		try {

			final LocalDateTime now = LocalDateTime.now();
			final ObjectMapper mapper = this.objectMapper; // reuse injected mapper

			for (IrrigationRowDTO row : req.getRows()) {

				// ================= DELETE =================
				if ("D".equalsIgnoreCase(row.getFlag())) {

					Optional<IrrigationCapacityEntityRevised> toDelete = irrigationRepositoryRevised
							.findByYearAndDateAndDeleteId(req.getYear(), req.getDate(), row.getDeleteId());

					toDelete.ifPresent(entity -> {
						irrigationRepositoryRevised.delete(entity);
						log.debug("Deleted ICC CAP | deleteId={} | corrId={}", row.getDeleteId(), corrId);
					});

					continue;
				}

				// ================= CREATE / UPDATE =================
				Optional<IrrigationCapacityEntityRevised> existing = irrigationRepositoryRevised
						.findByYearAndDateAndRowId(req.getYear(), req.getDate(), row.getRowId());

				IrrigationCapacityEntityRevised entity = existing.orElseGet(IrrigationCapacityEntityRevised::new);

				entity.setYear(req.getYear());
				entity.setDate(req.getDate());
				entity.setRowId(row.getRowId());
				entity.setDeleteId(row.getDeleteId());
				entity.setData(mapper.valueToTree(row.getData()));
				entity.setFlag(existing.isPresent() ? "U" : "C");

				if (existing.isEmpty()) {
					entity.setCreatedAt(now);
					entity.setCreatedBy(currentUser);
				}

				entity.setUpdatedAt(now);
				entity.setUpdatedBy(currentUser);

				irrigationRepositoryRevised.save(entity);
			}

			error.setErrorCode("0");
			error.setErrorDescription("Saved Successfully");
			response.setErrorDetails(error);

			log.info("SUCCESS saveOrUpdateIccCap | year={} | date={} | corrId={}", req.getYear(), req.getDate(),
					corrId);

			return response;

		} catch (Exception ex) {

			log.error("ERROR saveOrUpdateIccCap | year={} | date={} | corrId={}", req.getYear(), req.getDate(), corrId,
					ex);

			error.setErrorCode("500");
			error.setErrorDescription("Error occurred while saving: " + ex.getMessage());
			response.setErrorDetails(error);
			return response;
		}
	}

	@Override
	public NrldResponse getIccCapData(String year, String date) {

		NrldResponse response = new NrldResponse();
		ApplicationError error = new ApplicationError();

		final String corrId = MDC.get("correlationId");
		final String user = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");

		log.info("START getIccCapData | year={} | date={} | user={} | corrId={}", year, date, user, corrId);

		try {

			List<IrrigationCapacityEntityRevised> entities = irrigationRepositoryRevised.findByYearAndDate(year, date);

			if (entities == null || entities.isEmpty()) {

				log.warn("No ICC CAP data found | year={} | date={} | corrId={}", year, date, corrId);

				response.setData(List.of());
				error.setErrorCode("0");
				error.setErrorDescription("No data found");
				response.setErrorDetails(error);
				return response;
			}

			// Sort by rowId ASC
			entities.sort(Comparator.comparing(IrrigationCapacityEntityRevised::getRowId));

			List<Map<String, Object>> finalList = new ArrayList<>(entities.size());
			final ObjectMapper mapper = this.objectMapper;

			for (IrrigationCapacityEntityRevised e : entities) {

				Map<String, Object> row = mapper.convertValue(e.getData(), Map.class);
				row.put("rowId", e.getRowId());
				row.put("deleteId", e.getDeleteId());
				row.put("flag", e.getFlag());

				finalList.add(row);
			}

			response.setData(finalList);
			error.setErrorCode("0");
			error.setErrorDescription("Success");
			response.setErrorDetails(error);

			log.info("SUCCESS getIccCapData | year={} | date={} | records={} | corrId={}", year, date, finalList.size(),
					corrId);

			return response;

		} catch (Exception ex) {

			log.error("ERROR getIccCapData | year={} | date={} | corrId={}", year, date, corrId, ex);

			error.setErrorCode("500");
			error.setErrorDescription("Error fetching data: " + ex.getMessage());
			response.setErrorDetails(error);
			return response;
		}
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadIccData(String year, String date) throws IOException {

		List<IrrigationCapacityEntityRevised> list = irrigationRepositoryRevised.findByYearAndDate(year, date);
		list.sort(Comparator.comparing(IrrigationCapacityEntityRevised::getRowId));

		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = wb.createSheet("Irrigation Capacity");

		// ================== FONTS ==================
		XSSFFont titleFont = wb.createFont();
		titleFont.setBold(true);
		titleFont.setFontHeightInPoints((short) 13);

		XSSFFont headerFont = wb.createFont();
		headerFont.setBold(true);
		headerFont.setFontHeightInPoints((short) 10);

		XSSFFont sectionFont = wb.createFont();
		sectionFont.setBold(true);
		sectionFont.setColor(IndexedColors.RED.getIndex());
		sectionFont.setFontHeightInPoints((short) 10);

		XSSFFont dataFont = wb.createFont();
		dataFont.setFontHeightInPoints((short) 10);

		// ================== STYLES ==================
		XSSFCellStyle titleStyle = wb.createCellStyle();
		titleStyle.setAlignment(HorizontalAlignment.CENTER);
		titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		titleStyle.setFont(titleFont);

		// UPDATED → LEFT aligned + NO background
		XSSFCellStyle subHeaderStyle = wb.createCellStyle();
		subHeaderStyle.setAlignment(HorizontalAlignment.LEFT); // UPDATED
		subHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		subHeaderStyle.setFont(headerFont);
		subHeaderStyle.setFillPattern(FillPatternType.NO_FILL); // UPDATED

		// UPDATED → No grey background
		XSSFCellStyle sectionStyle = wb.createCellStyle();
		sectionStyle.setAlignment(HorizontalAlignment.LEFT);
		sectionStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		sectionStyle.setFont(sectionFont);
		sectionStyle.setFillPattern(FillPatternType.NO_FILL); // UPDATED
		sectionStyle.setBorderBottom(BorderStyle.THIN);
		sectionStyle.setBorderTop(BorderStyle.THIN);
		sectionStyle.setBorderLeft(BorderStyle.THIN);
		sectionStyle.setBorderRight(BorderStyle.THIN);

		XSSFCellStyle headerStyle = wb.createCellStyle();
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);
		headerStyle.setFont(headerFont);

		XSSFCellStyle dataStyle = wb.createCellStyle();
		dataStyle.setAlignment(HorizontalAlignment.CENTER);
		dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		dataStyle.setBorderBottom(BorderStyle.THIN);
		dataStyle.setBorderTop(BorderStyle.THIN);
		dataStyle.setBorderLeft(BorderStyle.THIN);
		dataStyle.setBorderRight(BorderStyle.THIN);
		dataStyle.setFont(dataFont);

		XSSFCellStyle leftStyle = wb.createCellStyle();
		leftStyle.cloneStyleFrom(dataStyle);
		leftStyle.setAlignment(HorizontalAlignment.LEFT);

		// Utility
		BiFunction<Row, Integer, Cell> cell = (r, c) -> {
			Cell cc = r.getCell(c);
			if (cc == null)
				cc = r.createCell(c);
			return cc;
		};

		int rowIndex = 0;

		// ================== TITLE (Dynamic Date) ==================
		Row titleRow = sheet.createRow(rowIndex++);
		titleRow.setHeightInPoints(22); // UPDATED (bigger spacing)

		String marathiDate = formatMarathiDate(date);
		cell.apply(titleRow, 0).setCellValue(marathiDate + " अखेर निर्मित सिंचन क्षमता");
		cell.apply(titleRow, 0).setCellStyle(titleStyle);
		sheet.addMergedRegion(new CellRangeAddress(titleRow.getRowNum(), titleRow.getRowNum(), 0, 7));

		// ================== SUB HEADER ==================
		Row deptRow = sheet.createRow(rowIndex++);
		deptRow.setHeightInPoints(18); // UPDATED spacing

		cell.apply(deptRow, 0).setCellValue("पुणे पाटबंधारे प्रकल्प मंडळ, पुणे");
		cell.apply(deptRow, 0).setCellStyle(subHeaderStyle); // UPDATED left align, no bg
		sheet.addMergedRegion(new CellRangeAddress(deptRow.getRowNum(), deptRow.getRowNum(), 0, 7));

		// ================== HEADER TABLE ==================
		// ================== HEADER ROW 1 ==================
		Row h1 = sheet.createRow(rowIndex++);
		h1.setHeightInPoints(24);
		for (int i = 0; i <= 7; i++)
			cell.apply(h1, i).setCellStyle(headerStyle);

		cell.apply(h1, 0).setCellValue("अ. क्र.");
		cell.apply(h1, 1).setCellValue("प्रकल्पाचे नांव");
		cell.apply(h1, 2).setCellValue("अंतिम सिंचन क्षमता");
		cell.apply(h1, 4).setCellValue("निमित्त सिंचन क्षमता");
		cell.apply(h1, 6).setCellValue("उर्वरीत सिंचन क्षमता");

		// Merge spanning 2 rows for first two cols
		sheet.addMergedRegion(new CellRangeAddress(h1.getRowNum(), h1.getRowNum() + 1, 0, 0));
		sheet.addMergedRegion(new CellRangeAddress(h1.getRowNum(), h1.getRowNum() + 1, 1, 1));
		sheet.addMergedRegion(new CellRangeAddress(h1.getRowNum(), h1.getRowNum(), 2, 3));
		sheet.addMergedRegion(new CellRangeAddress(h1.getRowNum(), h1.getRowNum(), 4, 5));
		sheet.addMergedRegion(new CellRangeAddress(h1.getRowNum(), h1.getRowNum(), 6, 7));

		// ================== HEADER ROW 2 ==================
		Row h2 = sheet.createRow(rowIndex++);
		h2.setHeightInPoints(22);

		// Ensure merged region cells have borders
		cell.apply(h2, 0).setCellStyle(headerStyle); // FIX
		cell.apply(h2, 1).setCellStyle(headerStyle); // FIX

		String[] labels = { "ICA", "IP", "ICA", "IP", "ICA", "IP" };
		int col = 2;
		for (String lbl : labels) {
			Cell c = cell.apply(h2, col);
			c.setCellValue(lbl);
			c.setCellStyle(headerStyle);
			col++;
		}

		// ================== SECTION ROW ==================
		Row sec = sheet.createRow(rowIndex++);
		sec.setHeightInPoints(18); // UPDATED

		cell.apply(sec, 0).setCellValue("मोठे प्रकल्प (प्रगत)");
		cell.apply(sec, 0).setCellStyle(sectionStyle); // UPDATED no bg

		sheet.addMergedRegion(new CellRangeAddress(sec.getRowNum(), sec.getRowNum(), 0, 7));

		// ================== DATA ROWS ==================
		long totalAntICA = 0, totalAntIP = 0;
		long totalNimitICA = 0, totalNimitIP = 0;
		long totalUpareetICA = 0, totalUpareetIP = 0;

		int sr = 1;

		for (IrrigationCapacityEntityRevised e : list) {

			JsonNode d = e.getData();

			Row r = sheet.createRow(rowIndex++);
			r.setHeightInPoints(20);

			cell.apply(r, 0).setCellValue(sr++);
			cell.apply(r, 1).setCellValue(d.path("projectName").asText(""));
			cell.apply(r, 2).setCellValue(d.path("antishechanICA").asLong(0));
			cell.apply(r, 3).setCellValue(d.path("antishechanIP").asLong(0));
			cell.apply(r, 4).setCellValue(d.path("nimitICA").asLong(0));
			cell.apply(r, 5).setCellValue(d.path("nimitIP").asLong(0));
			cell.apply(r, 6).setCellValue(d.path("upareetICA").asLong(0));
			cell.apply(r, 7).setCellValue(d.path("upareetIP").asLong(0));

			// styles
			cell.apply(r, 0).setCellStyle(dataStyle);
			cell.apply(r, 1).setCellStyle(leftStyle);
			for (int i = 2; i <= 7; i++)
				cell.apply(r, i).setCellStyle(dataStyle);

			// totals
			totalAntICA += d.path("antishechanICA").asLong(0);
			totalAntIP += d.path("antishechanIP").asLong(0);
			totalNimitICA += d.path("nimitICA").asLong(0);
			totalNimitIP += d.path("nimitIP").asLong(0);
			totalUpareetICA += d.path("upareetICA").asLong(0);
			totalUpareetIP += d.path("upareetIP").asLong(0);
		}

		// ================== TOTAL ROW ==================
		Row total = sheet.createRow(rowIndex++);
		total.setHeightInPoints(24);

		for (int i = 0; i <= 7; i++)
			cell.apply(total, i).setCellStyle(headerStyle);

		cell.apply(total, 0).setCellValue("पुपाप्रमं, पुणे");
		sheet.addMergedRegion(new CellRangeAddress(total.getRowNum(), total.getRowNum(), 0, 1));

		cell.apply(total, 2).setCellValue(totalAntICA);
		cell.apply(total, 3).setCellValue(totalAntIP);
		cell.apply(total, 4).setCellValue(totalNimitICA);
		cell.apply(total, 5).setCellValue(totalNimitIP);
		cell.apply(total, 6).setCellValue(totalUpareetICA);
		cell.apply(total, 7).setCellValue(totalUpareetIP);

		// Adjust widths
		for (int i = 0; i <= 7; i++) {
			sheet.autoSizeColumn(i);
			if (sheet.getColumnWidth(i) < 4300)
				sheet.setColumnWidth(i, 4300);
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		wb.write(out);
		wb.close();

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Irrigation_Capacity.xlsx")
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())));
	}

	private String formatMarathiDate(String yyyyMmDd) {
		LocalDate dt = LocalDate.parse(yyyyMmDd);
		String[] months = { "", "जानेवारी", "फेब्रुवारी", "मार्च", "एप्रिल", "मे", "जून", "जुलै", "ऑगस्ट", "सप्टेंबर",
				"ऑक्टोबर", "नोव्हेंबर", "डिसेंबर" };

		return dt.getDayOfMonth() + " " + months[dt.getMonthValue()] + " " + dt.getYear();
	}

}
