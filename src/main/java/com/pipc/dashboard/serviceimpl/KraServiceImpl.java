package com.pipc.dashboard.serviceimpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
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
import org.springframework.beans.factory.annotation.Autowired;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

		final String user = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
		final String corrId = MDC.get("correlationId");
		final LocalDateTime now = LocalDateTime.now();
		final ObjectMapper mapper = this.objectMapper;

		log.info("START saveOrUpdateKra | kraPeriod={} | user={} | corrId={}", request.getKraPeriod(), user, corrId);

		try {

			for (Map<String, Object> rowData : request.getKraData()) {

				Integer rowId = (Integer) rowData.get("rowId");
				if (rowId == null) {
					continue;
				}

				// 🔹 Safe deleteId conversion
				Long deleteId = null;
				Object deleteIdObj = rowData.get("deleteId");
				if (deleteIdObj instanceof Number) {
					deleteId = ((Number) deleteIdObj).longValue();
				} else if (deleteIdObj instanceof String && !((String) deleteIdObj).isBlank()) {
					deleteId = Long.parseLong((String) deleteIdObj);
				}

				String flag = String.valueOf(rowData.getOrDefault("flag", "")).trim().toUpperCase();

				Optional<KraEntity> existingByRow = kraRepository.findByKraPeriodAndRowId(request.getKraPeriod(),
						rowId);

				Optional<KraEntity> existingByDeleteId = (deleteId != null)
						? kraRepository.findByKraPeriodAndDeleteId(request.getKraPeriod(), deleteId)
						: Optional.empty();

				// ================= DELETE =================
				if ("D".equals(flag)) {

					if (existingByDeleteId.isPresent()) {
						kraRepository.delete(existingByDeleteId.get());
						msg.append("Deleted deleteId: ").append(deleteId).append(" | ");
					} else {
						msg.append("Delete requested but deleteId not found: ").append(deleteId).append(" | ");
					}
					continue;
				}

				// ================= CREATE / UPDATE =================
				JsonNode incomingNode = mapper.valueToTree(rowData);

				if (existingByRow.isPresent()) {

					KraEntity existing = existingByRow.get();
					boolean changed = false;

					JsonNode existingNode = existing.getKraRow() == null ? mapper.createObjectNode()
							: existing.getKraRow();

					if (!existingNode.equals(incomingNode)) {
						existing.setKraRow(incomingNode);
						changed = true;
					}

					if (!Objects.equals(existing.getTitle(), request.getTitle())) {
						existing.setTitle(request.getTitle());
						changed = true;
					}

					if (!Objects.equals(existing.getReference(), request.getReference())) {
						existing.setReference(request.getReference());
						changed = true;
					}

					if (changed) {
						existing.setUpdatedBy(user);
						existing.setUpdatedAt(now);
						existing.setFlag("U");
						kraRepository.save(existing);
						msg.append("Updated rowId: ").append(rowId).append(" | ");
					} else {
						entityManager.detach(existing); // 🔒 keep existing behavior
						msg.append("No change for rowId: ").append(rowId).append(" | ");
					}

				} else {

					KraEntity entity = new KraEntity();
					entity.setTitle(request.getTitle());
					entity.setKraPeriod(request.getKraPeriod());
					entity.setReference(request.getReference());
					entity.setKraRow(incomingNode);
					entity.setRowId(rowId);
					entity.setDeleteId(deleteId);
					entity.setCreatedBy(user);
					entity.setCreatedAt(now);
					entity.setUpdatedBy(user);
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

			log.info("SUCCESS saveOrUpdateKra | kraPeriod={} | message={} | corrId={}", request.getKraPeriod(), msg,
					corrId);

		} catch (Exception e) {

			log.error("ERROR saveOrUpdateKra | kraPeriod={} | corrId={}", request.getKraPeriod(), corrId, e);

			error.setErrorCode("1");
			error.setErrorDescription("Error while saving KRA: " + e.getMessage());
			kraResponse.setErrorDetails(error);
			kraResponse.setMessage("Operation failed.");
		}

		return kraResponse;
	}

	@Override
	public KraResponse getKraByPeriod(String kraPeriod) {

		KraResponse kraResponse = new KraResponse();
		ApplicationError error = new ApplicationError();

		final String user = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
		final String corrId = MDC.get("correlationId");

		log.info("START getKraByPeriod | kraPeriod={} | user={} | corrId={}", kraPeriod, user, corrId);

		try {

			// 🔹 Single DB call (no pagination)
			List<KraEntity> entities = kraRepository.findByKraPeriod(kraPeriod);

			if (entities == null || entities.isEmpty()) {

				log.warn("No KRA data found | kraPeriod={} | corrId={}", kraPeriod, corrId);

				ObjectNode emptyNode = objectMapper.createObjectNode();
				emptyNode.put("kraPeriod", kraPeriod);
				emptyNode.set("kraData", objectMapper.createArrayNode());

				kraResponse.setResponseData(emptyNode);
				error.setErrorCode("0");
				error.setErrorDescription("Success");
				kraResponse.setErrorDetails(error);
				kraResponse.setMessage("No data found");

				return kraResponse;
			}

			// 🔹 Sort by rowId ASC and map to JSON
			List<JsonNode> kraData = entities.stream().sorted(Comparator.comparingLong(KraEntity::getRowId))
					.map(KraEntity::getKraRow).collect(Collectors.toList());

			ObjectNode responseNode = objectMapper.createObjectNode();
			responseNode.put("kraPeriod", kraPeriod);
			responseNode.set("kraData", objectMapper.valueToTree(kraData));

			kraResponse.setResponseData(responseNode);
			error.setErrorCode("0");
			error.setErrorDescription("Success");
			kraResponse.setErrorDetails(error);
			kraResponse.setMessage("Fetched successfully");

			log.info("SUCCESS getKraByPeriod | kraPeriod={} | records={} | corrId={}", kraPeriod, kraData.size(),
					corrId);

			return kraResponse;

		} catch (Exception e) {

			log.error("ERROR getKraByPeriod | kraPeriod={} | corrId={}", kraPeriod, corrId, e);

			error.setErrorCode("1");
			error.setErrorDescription("Error fetching KRA: " + e.getMessage());
			kraResponse.setErrorDetails(error);
			kraResponse.setMessage("Operation failed.");
			return kraResponse;
		}
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

}