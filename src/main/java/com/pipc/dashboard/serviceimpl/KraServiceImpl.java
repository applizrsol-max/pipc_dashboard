package com.pipc.dashboard.serviceimpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pipc.dashboard.pdn.repository.KraEntity;
import com.pipc.dashboard.pdn.repository.KraRepository;
import com.pipc.dashboard.pdn.request.KraRequest;
import com.pipc.dashboard.pdn.response.KraResponse;
import com.pipc.dashboard.service.KraService;
import com.pipc.dashboard.utility.ApplicationError;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Service
public class KraServiceImpl implements KraService {

	@Autowired
	private KraRepository kraRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private EntityManager entityManager; // Necessary for detach()

	@Override
	@Transactional
	public KraResponse saveOrUpdateKra(KraRequest request) {
		KraResponse kraResponse = new KraResponse();
		ApplicationError error = new ApplicationError();
		StringBuilder msg = new StringBuilder();

		try {
			String userFromMDC = MDC.get("user");
			if (userFromMDC == null)
				userFromMDC = "SYSTEM";
			final String currentUser = userFromMDC;
			ObjectMapper mapper = this.objectMapper;
			// Use a single timestamp for consistency within the transaction
			LocalDateTime now = LocalDateTime.now();

			for (Map<String, Object> rowData : request.getKraData()) {
				Integer rowId = (Integer) rowData.get("rowId");
				if (rowId == null)
					continue;

				JsonNode incomingNode = mapper.valueToTree(rowData);

				// Find the entity based on the unique combination (kraPeriod + rowId)
				Optional<KraEntity> existingOpt = kraRepository.findByKraPeriodAndRowId(request.getKraPeriod(), rowId);

				if (existingOpt.isPresent()) {
					KraEntity existing = existingOpt.get();
					boolean entityChanged = false; // Flag to track if ANY change occurred

					// --- 1. Check for KRA Row Data change (JSON column) ---
					JsonNode existingNode = existing.getKraRow() == null ? mapper.createObjectNode()
							: existing.getKraRow();

					// Compare JSON string representations for change detection
					if (!existingNode.toString().equals(incomingNode.toString())) {
						existing.setKraRow(incomingNode);
						entityChanged = true;
					}

					// --- 2. Check for Header field changes (title) ---
					// Use null-safe comparison logic
					if (existing.getTitle() == null ? request.getTitle() != null
							: !existing.getTitle().equals(request.getTitle())) {
						existing.setTitle(request.getTitle());
						entityChanged = true;
					}

					// --- 3. Check for Header field changes (reference) ---
					if (existing.getReference() == null ? request.getReference() != null
							: !existing.getReference().equals(request.getReference())) {
						existing.setReference(request.getReference());
						entityChanged = true;
					}

					// --- 4. Final Save/Detach decision ---
					if (entityChanged) {
						// Update audit fields MANUALLY (since @UpdateTimestamp was removed)
						existing.setUpdatedBy(currentUser);
						existing.setUpdatedAt(now);
						existing.setFlag("U");
						kraRepository.save(existing);
						msg.append("Updated rowId: ").append(rowId).append(" | ");
					} else {
						// If no change, detach to break the link with the persistence context
						// This is VITAL to stop phantom updates.
						entityManager.detach(existing);
						msg.append("No change for rowId: ").append(rowId).append(" | ");
					}

				} else {
					// --- Creation Logic ---
					KraEntity entity = new KraEntity();
					entity.setTitle(request.getTitle());
					entity.setKraPeriod(request.getKraPeriod());
					entity.setReference(request.getReference());
					entity.setKraRow(incomingNode);
					entity.setRowId(rowId);

					// Manually set all audit fields for creation
					entity.setCreatedBy(currentUser);
					entity.setCreatedAt(now);
					entity.setUpdatedBy(currentUser);
					entity.setUpdatedAt(now);
					entity.setFlag("C");
					kraRepository.save(entity);
					msg.append("Created rowId: ").append(rowId).append(" | ");
				}
			}

			error.setErrorCode("0");
			error.setErrorDescription("Success");
			kraResponse.setErrorDetails(error);
			kraResponse.setMessage(msg.toString());

		} catch (Exception e) {
			error.setErrorCode("1");
			error.setErrorDescription("Error while saving: " + e.getMessage());
			kraResponse.setErrorDetails(error);
			kraResponse.setMessage("Operation failed.");
			// Re-throw to ensure the transaction rolls back
			throw new RuntimeException("KRA save failed due to an exception.", e);
		}

		return kraResponse;
	}

	@Override
	public KraResponse getKraByPeriod(String kraPeriod, int page, int size) {
		KraResponse kraResponse = new KraResponse();
		ApplicationError error = new ApplicationError();

		try {
			Pageable pageable = PageRequest.of(page, size);
			Page<KraEntity> kraPage = kraRepository.findByKraPeriod(kraPeriod, pageable);

			// ✅ Sort by rowId ascending before mapping
			List<JsonNode> kraData = kraPage.getContent().stream().sorted(Comparator.comparingLong(e -> e.getRowId())) // <--
																														// Sort
																														// by
																														// rowId
					.map(KraEntity::getKraRow).collect(Collectors.toList());

			ObjectMapper mapper = this.objectMapper;
			ObjectNode responseNode = mapper.createObjectNode();
			responseNode.put("kraPeriod", kraPeriod);
			responseNode.put("page", kraPage.getNumber());
			responseNode.put("size", kraPage.getSize());
			responseNode.put("totalPages", kraPage.getTotalPages());
			responseNode.put("totalElements", kraPage.getTotalElements());
			responseNode.set("kraData", mapper.valueToTree(kraData));

			kraResponse.setResponseData(responseNode);
			error.setErrorCode("0");
			error.setErrorDescription("Success");
			kraResponse.setErrorDetails(error);
			kraResponse.setMessage("Fetched successfully");

		} catch (Exception e) {
			error.setErrorCode("1");
			error.setErrorDescription("Error fetching KRA: " + e.getMessage());
			kraResponse.setErrorDetails(error);
			kraResponse.setMessage("Operation failed.");
		}

		return kraResponse;
	}

	@Override
	public ByteArrayInputStream generateKraExcel(String kraPeriod) throws IOException {
		List<KraEntity> records = kraRepository.findByKraPeriod(kraPeriod).stream()
				.sorted(Comparator.comparing(KraEntity::getRowId)) // ✅ Sort by DB rowId instead of kraNo
				.toList();

		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

			Sheet sheet = workbook.createSheet("KRA Report");

			// Fonts
			Font headerFont = workbook.createFont();
			headerFont.setFontName("Mangal");
			headerFont.setBold(true);
			headerFont.setFontHeightInPoints((short) 12);

			Font boldFont = workbook.createFont();
			boldFont.setFontName("Mangal");
			boldFont.setBold(true);
			boldFont.setFontHeightInPoints((short) 11);

			Font normalFont = workbook.createFont();
			normalFont.setFontName("Mangal");
			normalFont.setFontHeightInPoints((short) 11);

			// Styles
			CellStyle headerCenter = workbook.createCellStyle();
			headerCenter.setFont(headerFont);
			headerCenter.setAlignment(HorizontalAlignment.CENTER);
			headerCenter.setVerticalAlignment(VerticalAlignment.CENTER);

			CellStyle boldRight = workbook.createCellStyle();
			boldRight.setFont(boldFont);
			boldRight.setAlignment(HorizontalAlignment.RIGHT);

			CellStyle boldLeft = workbook.createCellStyle();
			boldLeft.setFont(boldFont);
			boldLeft.setAlignment(HorizontalAlignment.LEFT);

			CellStyle textStyle = workbook.createCellStyle();
			textStyle.setFont(normalFont);
			textStyle.setWrapText(true);
			textStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			textStyle.setBorderBottom(BorderStyle.THIN);
			textStyle.setBorderTop(BorderStyle.THIN);
			textStyle.setBorderLeft(BorderStyle.THIN);
			textStyle.setBorderRight(BorderStyle.THIN);

			CellStyle headerStyle = workbook.createCellStyle();
			headerStyle.cloneStyleFrom(textStyle);
			headerStyle.setFont(boldFont);
			headerStyle.setAlignment(HorizontalAlignment.CENTER);

			// --- 1️⃣ Title ---
			int titleRowIndex = 2;
			Row titleRow = sheet.createRow(titleRowIndex);
			Cell titleCell = titleRow.createCell(0);
			titleCell.setCellValue("पुणे पाटबंधारे प्रकल्प मंडळ, पुणे");
			titleCell.setCellStyle(headerCenter);
			sheet.addMergedRegion(new CellRangeAddress(titleRowIndex, titleRowIndex, 0, 4));

			// --- 2️⃣ KRA Period Title ---
			Row periodRow = sheet.createRow(titleRowIndex + 1);
			Cell periodCell = periodRow.createCell(0);
			periodCell.setCellValue(
					"KRA " + kraPeriod + " उद्दिष्टे (जुन 30/06/20" + kraPeriod.split("-")[1] + " अखेर सद्यस्थिती)");
			periodCell.setCellStyle(headerCenter);
			sheet.addMergedRegion(new CellRangeAddress(titleRowIndex + 1, titleRowIndex + 1, 0, 4));

			// --- 3️⃣ Reference line ---
			Row refLineRow = sheet.createRow(titleRowIndex + 2);
			Cell refLineCell = refLineRow.createCell(0);
			String referenceText = "संदर्भ :- प्रदेश कार्यालयाचे पत्र जा.क्र.मुअ.(जसं)/काअ-2/उअ-5/06/5444/2023 दि.13/12/2024";
			refLineCell.setCellValue(referenceText);
			refLineCell.setCellStyle(boldLeft);
			sheet.addMergedRegion(new CellRangeAddress(titleRowIndex + 2, titleRowIndex + 2, 0, 4));

			// --- 4️⃣ Table Header ---
			int headerRowIndex = titleRowIndex + 4;
			Row headerRow = sheet.createRow(headerRowIndex);
			String[] columns = { "KRA No", "विषय", "उद्दिष्ट", "साध्य", "शेरा" };
			for (int i = 0; i < columns.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(columns[i]);
				cell.setCellStyle(headerStyle);
			}

			// --- 5️⃣ Data Rows ---
			int rowNum = headerRowIndex + 1;
			for (KraEntity entity : records) {
				JsonNode rowData = entity.getKraRow();
				Row row = sheet.createRow(rowNum++);

				row.createCell(0).setCellValue(rowData.path("kraNo").asInt(0));
				row.createCell(1).setCellValue(rowData.path("vishaya").asText(""));
				row.createCell(2).setCellValue(rowData.path("uddishta").asDouble(0));
				row.createCell(3).setCellValue(rowData.path("sadhya").asDouble(0));
				row.createCell(4).setCellValue(rowData.path("shera").asText(""));

				for (int i = 0; i <= 4; i++)
					row.getCell(i).setCellStyle(textStyle);
			}

			// --- 6️⃣ Footer Section (parallel layout) ---
			int footerStart = rowNum + 2;

			Row leftRow1 = sheet.createRow(footerStart);
			Cell left1 = leftRow1.createCell(0);
			left1.setCellValue("जा.क्र.पुपाप्रमं/प्रशा-5/                    /सन " + kraPeriod);
			left1.setCellStyle(boldLeft);

			Row leftRow2 = sheet.createRow(footerStart + 1);
			Cell left2 = leftRow2.createCell(0);
			left2.setCellValue("पुणे पाटबंधारे प्रकल्प मंडळ, पुणे-01.");
			left2.setCellStyle(boldLeft);

			Row leftRow3 = sheet.createRow(footerStart + 2);
			Cell left3 = leftRow3.createCell(0);
			left3.setCellValue("दिनांक :- ");
			left3.setCellStyle(boldLeft);

			Row rightRow1 = sheet.getRow(footerStart);
			if (rightRow1 == null)
				rightRow1 = sheet.createRow(footerStart);
			Cell right1 = rightRow1.createCell(3);
			right1.setCellValue("उपअधीक्षक अभियंता,");
			right1.setCellStyle(boldRight);

			Row rightRow2 = sheet.getRow(footerStart + 1);
			if (rightRow2 == null)
				rightRow2 = sheet.createRow(footerStart + 1);
			Cell right2 = rightRow2.createCell(3);
			right2.setCellValue("पुणे पाटबंधारे प्रकल्प मंडळ, पुणे-01.");
			right2.setCellStyle(boldRight);

			sheet.addMergedRegion(new CellRangeAddress(footerStart, footerStart, 0, 1));
			sheet.addMergedRegion(new CellRangeAddress(footerStart + 1, footerStart + 1, 0, 1));
			sheet.addMergedRegion(new CellRangeAddress(footerStart + 2, footerStart + 2, 0, 1));
			sheet.addMergedRegion(new CellRangeAddress(footerStart, footerStart, 3, 4));
			sheet.addMergedRegion(new CellRangeAddress(footerStart + 1, footerStart + 1, 3, 4));

			Row copyRow = sheet.createRow(footerStart + 4);
			Cell copyCell = copyRow.createCell(0);
			copyCell.setCellValue(
					"प्रति :- मा. मुख्य अभियंता, जलसंपदा विभाग, पुणे-11 यांना माहितीसाठी व पुढील कार्यवाहीसाठी सविनय सादर.");
			copyCell.setCellStyle(boldLeft);
			sheet.addMergedRegion(new CellRangeAddress(footerStart + 4, footerStart + 4, 0, 4));

			// Auto-size columns
			for (int i = 0; i < 5; i++)
				sheet.autoSizeColumn(i);

			workbook.write(out);
			return new ByteArrayInputStream(out.toByteArray());
		}
	}

	private String getValue(JsonNode node, String key) {
		return node != null && node.has(key) && !node.get(key).isNull() ? node.get(key).asText() : "";
	}
}