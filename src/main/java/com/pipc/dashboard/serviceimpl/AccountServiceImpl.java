package com.pipc.dashboard.serviceimpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pipc.dashboard.accounts.repository.AccountsEntity;
import com.pipc.dashboard.accounts.repository.AccountsRepository;
import com.pipc.dashboard.accounts.request.AccountsRequest;
import com.pipc.dashboard.accounts.response.AccountsResponse;
import com.pipc.dashboard.service.AcountService;
import com.pipc.dashboard.utility.ApplicationError;
import com.pipc.dashboard.utility.JsonUtils;

@Service
@Transactional
public class AccountServiceImpl implements AcountService {

	private final AccountsRepository accountRepo;

	public AccountServiceImpl(AccountsRepository accountRepo) {
		this.accountRepo = accountRepo;
	}

	@Override
	public AccountsResponse saveOrUpdateAccounts(AccountsRequest request) {

		AccountsResponse response = new AccountsResponse();
		ApplicationError error = new ApplicationError();

		try {
			String userFromMDC = MDC.get("user");
			final String currentUser = (userFromMDC != null) ? userFromMDC : "SYSTEM";

			String accountsYear = request.getAccountsYear();
			StringBuilder actionSummary = new StringBuilder();

			// 🔹 Loop through all categories (both ekun & normal)
			for (Map.Entry<String, JsonNode> entry : request.getReports().entrySet()) {
				String category = entry.getKey();
				JsonNode valueNode = entry.getValue();

				// 🟦 Case 1: Ekun data (aggregated totals)
				if (category.startsWith("ekun")) {
					String tempBase = category.replaceFirst("^ekun", "");
					if (tempBase.length() > 0) {
						tempBase = Character.toLowerCase(tempBase.charAt(0)) + tempBase.substring(1);
					}
					final String baseCategory = tempBase;

					AccountsEntity ekunEntity = accountRepo
							.findByCategoryNameAndProjectYearAndRecordType(baseCategory, accountsYear, "E")
							.orElseGet(() -> AccountsEntity.builder().createdBy(currentUser).updatedBy(currentUser)
									.recordFlag("C").categoryName(baseCategory).projectYear(accountsYear).deleteId(0L)
									.recordType("E").build());

					ObjectNode existingNode = JsonUtils.ensureObjectNode(ekunEntity.getAccountsData());
					ObjectNode incomingNode = JsonUtils.ensureObjectNode(valueNode);

					boolean changed = JsonUtils.mergeAndDetectChanges(existingNode, incomingNode);
					ekunEntity.setAccountsData(existingNode.deepCopy());
					ekunEntity.setUpdatedBy(currentUser);
					ekunEntity.setUpdatedDatetime(LocalDateTime.now());

					if (changed) {
						ekunEntity.setRecordFlag("U");
						actionSummary.append("Updated ekun data for ").append(baseCategory).append(". ");
					} else if (ekunEntity.getId() == null) {
						actionSummary.append("Created ekun data for ").append(baseCategory).append(". ");
					}

					accountRepo.saveAndFlush(ekunEntity);
				}

				// 🟩 Case 2: Regular category (array of rows)
				else if (valueNode.isArray()) {
					ArrayNode rowsArray = (ArrayNode) valueNode;

					for (JsonNode rowNode : rowsArray) {
						int rowId = rowNode.has("rowId") ? rowNode.get("rowId").asInt() : -1;
						Long deleteId = rowNode.has("deleteId") ? rowNode.get("deleteId").asLong() : null;
						String flag = rowNode.has("flag") ? rowNode.get("flag").asText().trim() : null;

						if (rowId == -1)
							continue;

						// ✅ DELETE LOGIC
						if ("D".equalsIgnoreCase(flag) && deleteId != null) {
							accountRepo.findByCategoryNameAndProjectYearAndDeleteId(category, accountsYear, deleteId)
									.ifPresent(entity -> {
										accountRepo.delete(entity);
										actionSummary.append("Deleted row [deleteId=").append(deleteId)
												.append("] for category '").append(category).append("'. ");
									});
							continue;
						}

						// ✅ SAVE / UPDATE LOGIC
						AccountsEntity entity = accountRepo
								.findByCategoryNameAndProjectYearAndRowId(category, accountsYear, rowId)
								.orElseGet(() -> AccountsEntity.builder().createdBy(currentUser).updatedBy(currentUser)
										.recordFlag("C").categoryName(category).projectYear(accountsYear).rowId(rowId)
										.recordType("R").deleteId(deleteId).build());

						ObjectNode existingNode = JsonUtils.ensureObjectNode(entity.getAccountsData());
						ObjectNode incomingNode = JsonUtils.ensureObjectNode(rowNode);

						boolean hasChanged = JsonUtils.mergeAndDetectChanges(existingNode, incomingNode);
						entity.setAccountsData(existingNode.deepCopy());
						entity.setUpdatedBy(currentUser);
						entity.setUpdatedDatetime(LocalDateTime.now());

						// 🔸 Status message handling
						if (entity.getId() == null) {
							entity.setRecordFlag("C");
							actionSummary.append("Created rowId [").append(rowId).append("] for category '")
									.append(category).append("'. ");
						} else if (hasChanged) {
							entity.setRecordFlag("U");
							actionSummary.append("Updated rowId [").append(rowId).append("] for category '")
									.append(category).append("'. ");
						}

						accountRepo.saveAndFlush(entity);
					}
				}
			}

			error.setErrorCode("0");
			error.setErrorDescription("✅ Saved Successfully → " + actionSummary);

		} catch (Exception e) {
			error.setErrorCode("1");
			error.setErrorDescription("❌ Error while saving: " + e.getMessage());
			e.printStackTrace();
		}

		response.setErrorDetails(error);
		return response;
	}

	public Page<AccountsEntity> getAllAccounts(int page, int size) {
		return accountRepo.findAll(PageRequest.of(page, size));
	}

	@Override
	public List<AccountsEntity> getAllAccountsByYear(String year) {
		return accountRepo.findByProjectYear(year);
	}

	@Override
	public ByteArrayInputStream generateMarathiExcelForYear(String year) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public ResponseEntity<InputStreamResource> downloadAccountsReport(String year) throws IOException {

		List<AccountsEntity> allForYear = accountRepo.findByProjectYear(year);
		if (allForYear == null || allForYear.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}

		List<AccountsEntity> regular = allForYear.stream().filter(e -> "R".equalsIgnoreCase(e.getRecordType()))
				.collect(Collectors.toList());

		Map<String, List<AccountsEntity>> grouped = regular.stream().collect(
				Collectors.groupingBy(AccountsEntity::getCategoryName, LinkedHashMap::new, Collectors.toList()));

		try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

			Sheet sheet = wb.createSheet("Accounts-" + year);

			Font titleFont = wb.createFont();
			titleFont.setFontName("Mangal");
			titleFont.setBold(true);
			titleFont.setFontHeightInPoints((short) 12);

			Font boldFont = wb.createFont();
			boldFont.setFontName("Mangal");
			boldFont.setBold(true);
			boldFont.setFontHeightInPoints((short) 11);

			Font normalFont = wb.createFont();
			normalFont.setFontName("Mangal");
			normalFont.setFontHeightInPoints((short) 10);

			// === Styles ===
			CellStyle titleCenter = wb.createCellStyle();
			titleCenter.setFont(titleFont);
			titleCenter.setAlignment(HorizontalAlignment.CENTER);
			titleCenter.setVerticalAlignment(VerticalAlignment.CENTER);

			CellStyle sectionHeader = wb.createCellStyle();
			sectionHeader.setFont(boldFont);
			sectionHeader.setAlignment(HorizontalAlignment.CENTER);
			sectionHeader.setVerticalAlignment(VerticalAlignment.CENTER);

			CellStyle tableHeader = wb.createCellStyle();
			tableHeader.setFont(boldFont);
			tableHeader.setAlignment(HorizontalAlignment.CENTER);
			tableHeader.setVerticalAlignment(VerticalAlignment.CENTER);
			tableHeader.setWrapText(true);
			tableHeader.setBorderTop(BorderStyle.THIN);
			tableHeader.setBorderBottom(BorderStyle.THIN);
			tableHeader.setBorderLeft(BorderStyle.THIN);
			tableHeader.setBorderRight(BorderStyle.THIN);

			CellStyle dataCell = wb.createCellStyle();
			dataCell.setFont(normalFont);
			dataCell.setVerticalAlignment(VerticalAlignment.CENTER);
			dataCell.setBorderTop(BorderStyle.THIN);
			dataCell.setBorderBottom(BorderStyle.THIN);
			dataCell.setBorderLeft(BorderStyle.THIN);
			dataCell.setBorderRight(BorderStyle.THIN);

			CellStyle sumRowStyle = wb.createCellStyle();
			sumRowStyle.cloneStyleFrom(dataCell);
			sumRowStyle.setFont(boldFont);
			sumRowStyle.setAlignment(HorizontalAlignment.RIGHT);

			int rowNum = 0;

			// === Title ===
			Row titleRow = sheet.createRow(rowNum++);
			Cell titleCell = titleRow.createCell(0);
			titleCell.setCellValue("महाराष्ट्र कृष्णा खोरे विकास महामंडळ, पुणे");
			titleCell.setCellStyle(titleCenter);
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 11));

			Row sub = sheet.createRow(rowNum++);
			Cell c1 = sub.createCell(0);
			c1.setCellValue("सन " + year + " आर्थिक वर्षामधील प्रकल्पनिहाय तरतूद माहिती. (रु.कोटी)");
			c1.setCellStyle(titleCenter);
			sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 11));

			rowNum++;

			// === Table Header ===
			String[] tableCols = { "अ.क्र.", "प्रकल्पाचे नांव", "जिल्हा", "प्रकल्पाची मंजूर प्रमा/सुप्रमा किंमत",
					"प्रकल्पावर झालेला अद्यावत खर्च", "प्रकल्पाची उर्वरित किंमत",
					"सन " + year + " ची अर्थसंकल्पीत तरतूद", "सन " + year + " चे तरतूदीमधून प्राप्त निधी",
					"सन " + year + " च्या प्राप्त निधीमधून झालेला खर्च", "सन " + year + " मधील प्राप्त निधीमधून शिल्लक",
					"सन " + year + " मधील शिल्लक निधी/तरतूद", "शेरा" };

			Row headerRow = sheet.createRow(rowNum++);
			for (int i = 0; i < tableCols.length; i++) {
				Cell h = headerRow.createCell(i);
				h.setCellValue(tableCols[i]);
				h.setCellStyle(tableHeader);
				sheet.setColumnWidth(i, 4500);
			}

			Row numberRow = sheet.createRow(rowNum++);
			for (int i = 0; i < tableCols.length; i++) {
				Cell numCell = numberRow.createCell(i);
				numCell.setCellValue(i + 1);
				numCell.setCellStyle(tableHeader);
			}

			int serial = 1;
			double[] overallTotals = new double[8];

			// === Category-wise Loop ===
			for (Map.Entry<String, List<AccountsEntity>> entry : grouped.entrySet()) {
				String category = entry.getKey();
				List<AccountsEntity> list = entry.getValue();

				Row catRow = sheet.createRow(rowNum++);
				Cell catCell = catRow.createCell(0);
				catCell.setCellValue(CATEGORY_DISPLAY.getOrDefault(category, category));
				catCell.setCellStyle(sectionHeader);
				sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 11));

				double[] categorySum = new double[8];

				for (AccountsEntity e : list) {
					JsonNode d = e.getAccountsData();
					Row r = sheet.createRow(rowNum++);
					int c = 0;

					r.createCell(c).setCellValue(serial++);
					r.getCell(c++).setCellStyle(dataCell);

					r.createCell(c).setCellValue(d.path("projectName").asText(""));
					r.getCell(c++).setCellStyle(dataCell);

					r.createCell(c).setCellValue(d.path("district").asText(""));
					r.getCell(c++).setCellStyle(dataCell);

					double[] vals = { d.path("sanctionedCost").asDouble(0), d.path("expenditureTillNow").asDouble(0),
							d.path("remainingCost").asDouble(0), d.path("budget2025_26").asDouble(0),
							d.path("fundReceived2025_26").asDouble(0), d.path("expenditure2025_26").asDouble(0),
							d.path("balanceFund2025_26").asDouble(0), d.path("balanceProvision2025_26").asDouble(0) };

					for (int i = 0; i < vals.length; i++) {
						categorySum[i] += vals[i];
						overallTotals[i] += vals[i];
						Cell cell = r.createCell(c++);
						cell.setCellValue(vals[i]);
						cell.setCellStyle(dataCell);
					}

					r.createCell(c).setCellValue(d.path("remarks").asText(""));
					r.getCell(c).setCellStyle(dataCell);
				}

				// === Category Total Row ===
				Row sumRow = sheet.createRow(rowNum++);
				Cell label = sumRow.createCell(0);
				label.setCellValue("एकूण " + CATEGORY_TOTAL_NAMES.getOrDefault(category, ""));
				label.setCellStyle(sumRowStyle);
				sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 2));

				int c = 3;
				for (double v : categorySum) {
					Cell cell = sumRow.createCell(c++);
					cell.setCellValue(v);
					cell.setCellStyle(sumRowStyle);
				}

				rowNum++;
			}

			// === Overall Total Row ===
			Row totalRow = sheet.createRow(rowNum++);
			Cell tLabel = totalRow.createCell(0);
			tLabel.setCellValue("एकूण एकंदर");
			tLabel.setCellStyle(sumRowStyle);
			sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 2));

			int c = 3;
			for (double v : overallTotals) {
				Cell cell = totalRow.createCell(c++);
				cell.setCellValue(v);
				cell.setCellStyle(sumRowStyle);
			}

			// === Footer ===

			wb.write(out);
			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			String filename = "Accounts-PIPC-" + year + ".xlsx";

			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
					.contentType(MediaType
							.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
					.body(new InputStreamResource(in));
		}
	}

	private static final Map<String, String> CATEGORY_DISPLAY = Map.of("majorProjects",
			"मोठे प्रकल्प - लेखाशिर्ष 4700 0096", "expansionAndImprovement", "विस्तार व सुधारणा - लेखाशिर्ष 4700 0238",
			"damSafety", "धरण सुरक्षितता - लेखाशिर्ष 2700 0154", "mediumProjects",
			"मध्यम प्रकल्प - लेखाशिर्ष 4701 H629", "pmksy", "प्रधानमंत्री कृषि सिंचाई योजना (PMKSY)", "pmksyCADA",
			"प्रधानमंत्री कृषि सिंचाई योजना (PMKSY)(CADA)");

	private static final Map<String, String> CATEGORY_TOTAL_NAMES = Map.of("majorProjects", "मोठे प्रकल्प",
			"expansionAndImprovement", "विस्तार व सुधारणा", "damSafety", "धरण सुरक्षितता", "mediumProjects",
			"मध्यम प्रकल्प", "pmksy", "प्रधानमंत्री कृषि सिंचाई योजना (PMKSY)", "pmksyCADA",
			"प्रधानमंत्री कृषि सिंचाई योजना (PMKSY)(CADA)");

}
