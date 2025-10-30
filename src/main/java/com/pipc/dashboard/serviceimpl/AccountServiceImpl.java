package com.pipc.dashboard.serviceimpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

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

import jakarta.transaction.Transactional;

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

			// Loop through all categories (both ekun & normal)
			for (Map.Entry<String, JsonNode> entry : request.getReports().entrySet()) {
				String category = entry.getKey();
				JsonNode valueNode = entry.getValue();

				// 🟦 Case 1: Ekun data (aggregated totals)
				if (category.startsWith("ekun")) {
					// derive base category (e.g. ekunMajorProjects → majorProjects)
					String tempBase = category.replaceFirst("^ekun", "");
					if (tempBase.length() > 0) {
						tempBase = Character.toLowerCase(tempBase.charAt(0)) + tempBase.substring(1);
					}
					final String baseCategory = tempBase; // ✅ make effectively final

					AccountsEntity ekunEntity = accountRepo
							.findByCategoryNameAndProjectYearAndRecordType(baseCategory, accountsYear, "E")
							.orElseGet(() -> AccountsEntity.builder().createdBy(currentUser).updatedBy(currentUser)
									.recordFlag("C").categoryName(baseCategory).projectYear(accountsYear)
									.recordType("E") // 'E' means ekun
									.build());

					ObjectNode existingNode = JsonUtils.ensureObjectNode(ekunEntity.getAccountsData());
					ObjectNode incomingNode = JsonUtils.ensureObjectNode(valueNode);

					boolean changed = JsonUtils.mergeAndDetectChanges(existingNode, incomingNode);
					ekunEntity.setAccountsData(existingNode);
					ekunEntity.setUpdatedBy(currentUser);
					if (changed)
						ekunEntity.setRecordFlag("U");

					accountRepo.save(ekunEntity);
					actionSummary.append(category).append(": ekun data saved; ");
				}

				// 🟩 Case 2: Regular category (array of rows)
				else if (valueNode.isArray()) {
					ArrayNode rowsArray = (ArrayNode) valueNode;

					for (JsonNode rowNode : rowsArray) {
						int rowId = rowNode.has("rowId") ? rowNode.get("rowId").asInt() : -1;
						if (rowId == -1)
							continue;

						AccountsEntity entity = accountRepo
								.findByCategoryNameAndProjectYearAndRowId(category, accountsYear, rowId)
								.orElseGet(() -> AccountsEntity.builder().createdBy(currentUser).updatedBy(currentUser)
										.recordFlag("C").categoryName(category).projectYear(accountsYear).rowId(rowId)
										.recordType("R") // 'R' means regular row
										.build());

						ObjectNode existingNode = JsonUtils.ensureObjectNode(entity.getAccountsData());
						ObjectNode incomingNode = JsonUtils.ensureObjectNode(rowNode);

						boolean hasChanged = JsonUtils.mergeAndDetectChanges(existingNode, incomingNode);
						entity.setAccountsData(existingNode);
						entity.setUpdatedBy(currentUser);
						if (hasChanged)
							entity.setRecordFlag("U");

						accountRepo.save(entity);
					}

					actionSummary.append(category).append(": ").append(rowsArray.size()).append(" rows saved; ");
				}
			}

			error.setErrorCode("0");
			error.setErrorDescription("Saved Successfully → " + actionSummary);

		} catch (Exception e) {
			error.setErrorCode("1");
			error.setErrorDescription("Error while saving: " + e.getMessage());
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
		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

			Sheet sheet = workbook.createSheet("PIPC " + year);
			int rowIdx = 0;

			// ------------------ Styles ------------------
			CellStyle boldStyle = workbook.createCellStyle();
			Font boldFont = workbook.createFont();
			boldFont.setBold(true);
			boldStyle.setFont(boldFont);

			CellStyle categoryStyle = workbook.createCellStyle();
			categoryStyle.setAlignment(HorizontalAlignment.CENTER);
			Font categoryFont = workbook.createFont();
			categoryFont.setBold(true);
			categoryStyle.setFont(categoryFont);

			// ------------------ Title ------------------
			Row titleRow = sheet.createRow(rowIdx++);
			Cell titleCell = titleRow.createCell(0);
			titleCell.setCellValue("महाराष्ट्र कृष्णा खोरे विकास महामंडळ, पुणे");
			titleCell.setCellStyle(boldStyle);
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 11));

			Row subTitleRow = sheet.createRow(rowIdx++);
			subTitleRow.createCell(0).setCellValue("सन " + year + " आर्थिक वर्षातील तरतूद व खर्चाची माहिती");
			sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 11));

			rowIdx++;

			// ------------------ Header ------------------
			String[] columns = { "अ. क्र.", "प्रकल्पाचे नाव", "जिल्हा", "प्रकल्पाची मंजूर प्रशासकीय किंमत",
					"प्रकल्पाच्या खात्यात आजपर्यंतचा खर्च", "उर्वरित किंमत", "सन २०२५-२६ मध्ये अर्थसंकल्पीय तरतूद",
					"सन २०२५-२६ मध्ये तरतुदीवरून प्राप्त निधी", "सन २०२५-२६ मध्ये प्राप्त निधीमधून झालेला खर्च",
					"सन २०२५-२६ मध्ये शिल्लक निधी", "सन २०२५-२६ मध्ये शिल्लक निधीवरून शिल्लक तरतूद", "शेरा" };

			Row headerRow = sheet.createRow(rowIdx++);
			for (int i = 0; i < columns.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(columns[i]);
				cell.setCellStyle(boldStyle);
			}

			// Set reasonable column widths
			for (int i = 0; i < columns.length; i++) {
				sheet.setColumnWidth(i, 20 * 256); // 20 chars wide
			}

			// ------------------ Data ------------------
			List<AccountsEntity> entities = accountRepo.findByProjectYear(year);

			// Define category keys in order
			Map<String, String> categories = Map.of("majorProjects", "मोठे प्रकल्प - लेखाशिर्ष 4700 0096",
					"expansionAndImprovement", "विस्तार व सुधारणा प्रकल्प", "damSafety", "सिंचन व धरण सुरक्षा प्रकल्प",
					"mediumProjects", "मध्यम प्रकल्प", "pmksy", "PMKSY प्रकल्प", "pmksyCADA", "PMKSY CADA प्रकल्प");

			int srNo = 1;

			for (Map.Entry<String, String> categoryEntry : categories.entrySet()) {
				String categoryKey = categoryEntry.getKey();
				String categoryName = categoryEntry.getValue();

				// Category header
				Row categoryRow = sheet.createRow(rowIdx++);
				Cell categoryCell = categoryRow.createCell(0);
				categoryCell.setCellValue(categoryName);
				categoryCell.setCellStyle(categoryStyle);
				sheet.addMergedRegion(
						new CellRangeAddress(categoryRow.getRowNum(), categoryRow.getRowNum(), 0, columns.length - 1));

				int startRow = rowIdx + 1; // Start of data rows for this category

				// Add rows for each entity in this category
				for (AccountsEntity entity : entities) {
					JsonNode reportsNode = entity.getAccountsData().get("reports");
					if (reportsNode != null && reportsNode.has(categoryKey)) {
						JsonNode categoryArray = reportsNode.get(categoryKey);
						for (JsonNode project : categoryArray) {
							Row row = sheet.createRow(rowIdx++);
							row.createCell(0).setCellValue(srNo++);
							row.createCell(1).setCellValue(project.path("projectName").asText(""));
							row.createCell(2).setCellValue(project.path("district").asText(""));
							row.createCell(3).setCellValue(project.path("sanctionedCost").asDouble(0));
							row.createCell(4).setCellValue(project.path("expenditureTillNow").asDouble(0));
							row.createCell(5).setCellValue(project.path("remainingCost").asDouble(0));
							row.createCell(6).setCellValue(project.path("budget2025_26").asDouble(0));
							row.createCell(7).setCellValue(project.path("fundReceived2025_26").asDouble(0));
							row.createCell(8).setCellValue(project.path("expenditure2025_26").asDouble(0));
							row.createCell(9).setCellValue(project.path("balanceFund2025_26").asDouble(0));
							row.createCell(10).setCellValue(project.path("balanceProvision2025_26").asDouble(0));
							row.createCell(11).setCellValue(project.path("remarks").asText(""));
						}
					}
				}

				int endRow = rowIdx; // Last row of data for this category

				// Total row for numeric columns
				Row totalRow = sheet.createRow(rowIdx++);
				totalRow.createCell(0).setCellValue("एकूण");
				totalRow.getCell(0).setCellStyle(boldStyle);

				for (int col = 3; col <= 10; col++) {
					String colLetter = CellReference.convertNumToColString(col);
					String formula = "SUM(" + colLetter + (startRow + 1) + ":" + colLetter + (endRow) + ")";
					Cell cell = totalRow.createCell(col);
					cell.setCellFormula(formula);
					cell.setCellStyle(boldStyle);
				}
			}

			workbook.write(out);
			return new ByteArrayInputStream(out.toByteArray());
		}
	}

}
