package com.pipc.dashboard.serviceimpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFAbstractNum;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFNumbering;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTAbstractNum;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTLvl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STNumberFormat;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pipc.dashboard.bhusmapadan.request.PraptraMasterDataRequest;
import com.pipc.dashboard.bhusmapadan.request.PraptraMasterDataRowRequest;
import com.pipc.dashboard.bhusmapadan.response.PraptraMasterDataResponse;
import com.pipc.dashboard.establishment.repository.AgendaOfficerEntity;
import com.pipc.dashboard.establishment.repository.AgendaOfficerRepository;
import com.pipc.dashboard.establishment.repository.AgendaSecBRepository;
import com.pipc.dashboard.establishment.repository.AgendaSecDRepository;
import com.pipc.dashboard.establishment.repository.AgendaSecEntityGATB;
import com.pipc.dashboard.establishment.repository.AgendaSecEntityGATD;
import com.pipc.dashboard.establishment.repository.AgendaThirteenEntity;
import com.pipc.dashboard.establishment.repository.AgendaThirteenRepository;
import com.pipc.dashboard.establishment.repository.AppealEntity;
import com.pipc.dashboard.establishment.repository.AppealRepository;
import com.pipc.dashboard.establishment.repository.AppealRequestEntity;
import com.pipc.dashboard.establishment.repository.AppealRequestRepository;
import com.pipc.dashboard.establishment.repository.ApprovalDetailsEntity;
import com.pipc.dashboard.establishment.repository.ApprovalDetailsRepository;
import com.pipc.dashboard.establishment.repository.CrFileListEntity;
import com.pipc.dashboard.establishment.repository.CrFileListRepository;
import com.pipc.dashboard.establishment.repository.CrFileListRtrEntity;
import com.pipc.dashboard.establishment.repository.CrFileListRtrRepository;
import com.pipc.dashboard.establishment.repository.EmployeeDetailsEntity;
import com.pipc.dashboard.establishment.repository.EmployeeDetailsRepository;
import com.pipc.dashboard.establishment.repository.EmployeePostingEntity;
import com.pipc.dashboard.establishment.repository.EmployeePostingRepository;
import com.pipc.dashboard.establishment.repository.IncomeTaxDeductionEntity;
import com.pipc.dashboard.establishment.repository.IncomeTaxDeductionRepository;
import com.pipc.dashboard.establishment.repository.KaryaratGopniyaAhwalEntity;
import com.pipc.dashboard.establishment.repository.KaryaratGopniyaAhwalRepository;
import com.pipc.dashboard.establishment.repository.KharchaTapsilEntity;
import com.pipc.dashboard.establishment.repository.KharchaTapsilRepository;
import com.pipc.dashboard.establishment.repository.LeaveEntity;
import com.pipc.dashboard.establishment.repository.LeaveRepository;
import com.pipc.dashboard.establishment.repository.MahaparRegisterEntity;
import com.pipc.dashboard.establishment.repository.MahaparRegisterRepository;
import com.pipc.dashboard.establishment.repository.MasterDataEntity;
import com.pipc.dashboard.establishment.repository.MasterDataRepository;
import com.pipc.dashboard.establishment.repository.MedicalBillMasterEntity;
import com.pipc.dashboard.establishment.repository.MedicalBillMasterRepository;
import com.pipc.dashboard.establishment.repository.PassportNocEntity;
import com.pipc.dashboard.establishment.repository.PassportNocRepository;
import com.pipc.dashboard.establishment.repository.ReferenceEntity;
import com.pipc.dashboard.establishment.repository.ReferenceRepository;
import com.pipc.dashboard.establishment.repository.RtrGopniyaAhwal;
import com.pipc.dashboard.establishment.repository.RtrGopniyaAhwalRepository;
import com.pipc.dashboard.establishment.repository.VaidyakExcludedDetailsEntity;
import com.pipc.dashboard.establishment.repository.VaidyakKharchaParigananaEntity;
import com.pipc.dashboard.establishment.repository.VaidyakKharchaParigananaRepository;
import com.pipc.dashboard.establishment.repository.VaidyakTapshilEntity;
import com.pipc.dashboard.establishment.repository.VaidyakTapshilRepository;
import com.pipc.dashboard.establishment.repository.VastavyaDetailsEntity;
import com.pipc.dashboard.establishment.repository.VastavyaDetailsRepository;
import com.pipc.dashboard.establishment.request.AgendaRequest;
import com.pipc.dashboard.establishment.request.AgendaRow;
import com.pipc.dashboard.establishment.request.AgendaSecRequest;
import com.pipc.dashboard.establishment.request.AgendaSecRow;
import com.pipc.dashboard.establishment.request.AppealRequest;
import com.pipc.dashboard.establishment.request.AppealRequest2;
import com.pipc.dashboard.establishment.request.AppealWrapper;
import com.pipc.dashboard.establishment.request.AppealWrapper2;
import com.pipc.dashboard.establishment.request.EmployeePostingRequest;
import com.pipc.dashboard.establishment.request.IncomeTaxDeductionRequest;
import com.pipc.dashboard.establishment.request.LeaveRequest;
import com.pipc.dashboard.establishment.request.MahaparRegisterRequest;
import com.pipc.dashboard.establishment.request.MahaparRegisterRowRequest;
import com.pipc.dashboard.establishment.request.MahaparRegisterSectionRequest;
import com.pipc.dashboard.establishment.request.MedicalBillData;
import com.pipc.dashboard.establishment.request.MedicalBillRequest;
import com.pipc.dashboard.establishment.request.PassportNocRequest;
import com.pipc.dashboard.establishment.request.ThirteenRequest;
import com.pipc.dashboard.establishment.request.ThirteenRow;
import com.pipc.dashboard.establishment.response.AgendaResponse;
import com.pipc.dashboard.establishment.response.AgendaSecResponse;
import com.pipc.dashboard.establishment.response.AppealResponse;
import com.pipc.dashboard.establishment.response.EmployeePostingResponse;
import com.pipc.dashboard.establishment.response.IncomeTaxDeductionResponse;
import com.pipc.dashboard.establishment.response.LeaveResponse;
import com.pipc.dashboard.establishment.response.MedicalBillResponse;
import com.pipc.dashboard.establishment.response.PassportNocResponse;
import com.pipc.dashboard.establishment.response.ThirteenResponse;
import com.pipc.dashboard.service.EstablishmentService;
import com.pipc.dashboard.utility.ApplicationError;
import com.pipc.dashboard.utility.PdfUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EstablishmentServiceImpl implements EstablishmentService {

	private final MedicalBillMasterRepository masterRepo;
	private final ReferenceRepository refRepo;
	private final EmployeeDetailsRepository empRepo;
	private final ApprovalDetailsRepository apprRepo;
	private final KharchaTapsilRepository kharchaRepo;
	private final VaidyakKharchaParigananaRepository vaidyaRepo;
	private final VaidyakTapshilRepository tapshilRepo;
	private final VastavyaDetailsRepository vastavyaRepo;
	private final LeaveRepository leaveRepository;
	private final AppealRepository appealRepository;
	private final EmployeePostingRepository employeePostingRepository;
	private final IncomeTaxDeductionRepository incomeTaxDeductionRepository;
	private final PassportNocRepository passportNocRepository;
	private final AgendaOfficerRepository agendaOfficerRepository;
	private final AgendaSecBRepository agendaSecBRepository;
	private final AgendaSecDRepository agendaSecDRepository;
	private final AgendaThirteenRepository agendaThirteenRepository;
	private final AppealRequestRepository appealRequestRepository;
	private static final String TEMPLATE = "/templates/medical_bill_template.docx";
	private final MasterDataRepository masterDataRepository;
	private final CrFileListRepository crFileListRepository;
	private final CrFileListRtrRepository crFileListRtrRepository;
	private final MahaparRegisterRepository mahaparRegisterRepository;
	private final KaryaratGopniyaAhwalRepository karyaratGopniyaAhwalRepository;
	private final RtrGopniyaAhwalRepository rtrGopniyaAhwalRepository;
	@Autowired
	private ObjectMapper objectMapper;

	@Override
	@Transactional
	public MedicalBillResponse saveOrUpdateMedicalBill(MedicalBillRequest request) {
		MedicalBillResponse response = new MedicalBillResponse();
		ApplicationError error = new ApplicationError();
		String userFromMDC = MDC.get("user");

		try {
			for (MedicalBillData bill : request.getData()) {

				// 1️⃣ DELETE if flag == "D"
				if ("D".equalsIgnoreCase(bill.getFlag())) {
					if (bill.getRowId() != null) {
						masterRepo.findByRowId(bill.getRowId()).ifPresent(master -> {
							log.info("Deleting medical bill for rowId {}", bill.getRowId());
							masterRepo.delete(master);
						});
					}
					continue;
				}

				// 2️⃣ FIND EXISTING or CREATE NEW
				// 2️⃣ FIND EXISTING or CREATE NEW
				MedicalBillMasterEntity master = Optional.ofNullable(bill.getRowId()).flatMap(masterRepo::findByRowId)
						.orElseGet(MedicalBillMasterEntity::new);

				boolean isNew = master.getId() == null;

				// 3️⃣ BASIC DETAILS
				master.setTitle(bill.getTitle());
				master.setPeriod(request.getPeriod());
				master.setMonth(request.getMonth());
				master.setYear(request.getYear());
				master.setCreatedBy(userFromMDC);
				master.setUpdatedBy(userFromMDC);
				master.setRowId(bill.getRowId());
				master.setBillDate(request.getDate());

				// ✅ Flag and updated time logic
				if (isNew) {
					master.setFlag("C");
				} else {
					master.setFlag("U");
				}
				master.setUpdatedTime(LocalDateTime.now());

				// 4️⃣ REFERENCES
				if (bill.getReference() != null) {
					master.getReferences().clear();
					bill.getReference().forEach(refText -> {
						ReferenceEntity ref = new ReferenceEntity();
						ref.setReference(refText);
						ref.setMedicalBill(master);
						master.getReferences().add(ref);
					});
				}

				// 5️⃣ EMPLOYEE DETAILS
				if (bill.getEmployeeDetails() != null) {
					var src = bill.getEmployeeDetails();
					EmployeeDetailsEntity emp = Optional.ofNullable(master.getEmployeeDetails())
							.orElse(new EmployeeDetailsEntity());

					emp.setEmployeeName(src.getEmployeeName());
					emp.setDesignation(src.getDesignation());
					emp.setDepartment(src.getDepartment());
					emp.setPatientName(src.getPatientName());
					emp.setHospitalName(src.getHospitalName());
					emp.setFromDate(src.getTreatmentPeriod().getFromDate());
					emp.setToDate(src.getTreatmentPeriod().getToDate());
					emp.setMedicalBill(master);

					master.setEmployeeDetails(emp);
				}

				// 6️⃣ APPROVAL DETAILS
				if (bill.getApprovalDetails() != null) {
					var src = bill.getApprovalDetails();
					ApprovalDetailsEntity appr = Optional.ofNullable(master.getApprovalDetails())
							.orElse(new ApprovalDetailsEntity());

					appr.setApprovingAuthority(src.getApprovingAuthority());
					appr.setApprovalDate(src.getApprovalDate());
					appr.setApprovalAmount(src.getApprovalAmount());
					appr.setApprovedBy(src.getApprovedBy());
					appr.setMedicalBill(master);

					master.setApprovalDetails(appr);
				}

				// 7️⃣ KHARCHA TAPSIL
				if (bill.getKharchaTapsil() != null) {
					master.getKharchaTapsil().clear();
					bill.getKharchaTapsil().forEach(k -> {
						if ("D".equalsIgnoreCase(k.getFlag()))
							return;
						KharchaTapsilEntity kt = new KharchaTapsilEntity();
						kt.setSubId(k.getSubId());
						kt.setAkr(k.getAkr());
						kt.setTapsil(k.getTapsil());
						kt.setRakkam(k.getRakkam());
						kt.setMedicalBill(master);
						master.getKharchaTapsil().add(kt);
					});
				}

				// 8️⃣ VAIDYAK KHARCHA PARIGANANA
				if (bill.getVaidyakKharchaPariganana() != null) {
					var src = bill.getVaidyakKharchaPariganana();
					VaidyakKharchaParigananaEntity vaidya = Optional.ofNullable(master.getVaidyakKharchaPariganana())
							.orElse(new VaidyakKharchaParigananaEntity());

					vaidya.setShasanNirdesh(src.getShasanNirdesh());
					vaidya.setEmployeeName(src.getEmployeeName());
					vaidya.setDesignation(src.getDesignation());
					vaidya.setPatientName(src.getPatientName());
					vaidya.setHospitalName(src.getHospitalName());
					vaidya.setFromDate(src.getTreatmentPeriod().getFromDate());
					vaidya.setToDate(src.getTreatmentPeriod().getToDate());
					vaidya.setMedicalBill(master);

					// Tapshil list
					vaidya.getTapshilList().clear();
					if (src.getTapshil() != null) {
						src.getTapshil().forEach(t -> {
							if ("D".equalsIgnoreCase(t.getFlag()))
								return;
							VaidyakTapshilEntity vt = new VaidyakTapshilEntity();
							vt.setSubId(t.getSubId());
							vt.setAkr(t.getAkr());
							vt.setTapsil(t.getTapsil());
							vt.setEkunKharch(t.getEkunKharch());
							vt.setVaidyakKharchaPariganana(vaidya);
							vaidya.getTapshilList().add(vt);
						});
					}

					// Vastavya Details
					vaidya.getVastavyaDetailsList().clear();
					if (src.getVastavyaDetails() != null) {
						src.getVastavyaDetails().forEach(v -> {
							if ("D".equalsIgnoreCase(v.getFlag()))
								return;
							VastavyaDetailsEntity ve = new VastavyaDetailsEntity();
							ve.setSubId(v.getSubId());
							ve.setVastavyaPrakar(v.getVastavyaPrakar());
							ve.setPratyakshaKharch(v.getPratyakshaKharch());
							ve.setAnugya_rakkam(v.getAnugyaRakkam());
							ve.setDeyaRakkam(v.getDeyaRakkam());
							ve.setVaidyakKharchaPariganana(vaidya);
							vaidya.getVastavyaDetailsList().add(ve);
						});
					}

					// Excluded Details
					// Excluded Details
					if (src.getExcludedDetails() != null) {
						if (vaidya.getExcludedDetails() == null) {
							vaidya.setExcludedDetails(new ArrayList<>());
						} else {
							vaidya.getExcludedDetails().clear();
						}

						src.getExcludedDetails().forEach(e -> {
							if ("D".equalsIgnoreCase(e.getFlag()))
								return;
							VaidyakExcludedDetailsEntity ed = new VaidyakExcludedDetailsEntity();
							ed.setSubId(e.getSubId());
							ed.setDescription(e.getDescription());
							ed.setAmount(e.getTotalRakkam());
							ed.setVaidyakKharchaPariganana(vaidya);
							vaidya.getExcludedDetails().add(ed);
						});
					}

					master.setVaidyakKharchaPariganana(vaidya);
				}

				// 9️⃣ SAVE (cascade = ALL)
				masterRepo.save(master);
				log.info("{} medical bill processed successfully", isNew ? "New" : "Updated");
			}

			// ✅ SUCCESS RESPONSE
			response.setMessage("Medical bills processed successfully");
			error.setErrorCode("200");
			error.setErrorDescription("SUCCESS");
			response.setErrorDetails(error);

		} catch (Exception e) {
			log.error("Error while saving medical bills", e);
			error.setErrorCode("500");
			error.setErrorDescription(e.getMessage());
			response.setErrorDetails(error);
			response.setMessage("Error while processing medical bills");
		}

		return response;
	}

	@Override
	@Transactional(readOnly = true)
	public MedicalBillResponse getMedicalBills(String employeeName, String month, String year, String period,
			String date) {
		MedicalBillResponse response = new MedicalBillResponse();
		ApplicationError error = new ApplicationError();

		try {

			List<MedicalBillMasterEntity> bills = masterRepo.findByFilters(employeeName, month, year, period, date);

			if (bills.isEmpty()) {
				error.setErrorCode("404");
				error.setErrorDescription("No records found for given filters");
				response.setErrorDetails(error);
				response.setMessage("No medical bills found");
				response.setData(Collections.emptyList());
				return response;
			}

			List<Map<String, Object>> result = new ArrayList<>();

			for (MedicalBillMasterEntity m : bills) {
				Map<String, Object> map = new LinkedHashMap<>();
				map.put("rowId", m.getRowId());
				map.put("title", m.getTitle());
				map.put("period", m.getPeriod());
				map.put("month", m.getMonth());
				map.put("year", m.getYear());
				map.put("flag", m.getFlag());
				map.put("billDate", m.getBillDate()); // 🆕 added in response
				map.put("createdBy", m.getCreatedBy());
				map.put("updatedBy", m.getUpdatedBy());
				map.put("updatedAt", m.getUpdatedTime());

				// ✅ References
				map.put("reference", Optional.ofNullable(m.getReferences()).orElse(Collections.emptyList()).stream()
						.map(ReferenceEntity::getReference).toList());

				// ✅ Employee Details
				if (m.getEmployeeDetails() != null) {
					Map<String, Object> emp = new LinkedHashMap<>();
					var e = m.getEmployeeDetails();
					emp.put("employeeName", e.getEmployeeName());
					emp.put("designation", e.getDesignation());
					emp.put("department", e.getDepartment());
					emp.put("patientName", e.getPatientName());
					emp.put("hospitalName", e.getHospitalName());
					emp.put("treatmentPeriod", Map.of("fromDate", e.getFromDate(), "toDate", e.getToDate()));
					map.put("employeeDetails", emp);
				}

				// ✅ Approval Details
				if (m.getApprovalDetails() != null) {
					var a = m.getApprovalDetails();
					map.put("approvalDetails",
							Map.of("approvingAuthority", a.getApprovingAuthority(), "approvalDate", a.getApprovalDate(),
									"approvalAmount", a.getApprovalAmount(), "approvedBy", a.getApprovedBy()));
				}

				// ✅ Kharcha Tapsil
				map.put("kharchaTapsil",
						Optional.ofNullable(m.getKharchaTapsil()).orElse(Collections.emptyList()).stream()
								.map(k -> Map.of("subId", k.getSubId(), "akr", k.getAkr(), "tapsil", k.getTapsil(),
										"rakkam", k.getRakkam()))
								.toList());

				// ✅ Vaidyak Kharcha Pariganana
				if (m.getVaidyakKharchaPariganana() != null) {
					var v = m.getVaidyakKharchaPariganana();
					Map<String, Object> vk = new LinkedHashMap<>();
					vk.put("shasanNirdesh", v.getShasanNirdesh());
					vk.put("employeeName", v.getEmployeeName());
					vk.put("designation", v.getDesignation());
					vk.put("patientName", v.getPatientName());
					vk.put("hospitalName", v.getHospitalName());
					vk.put("treatmentPeriod", Map.of("fromDate", v.getFromDate(), "toDate", v.getToDate()));

					vk.put("tapshil",
							Optional.ofNullable(v.getTapshilList()).orElse(Collections.emptyList()).stream()
									.map(t -> Map.of("subId", t.getSubId(), "akr", t.getAkr(), "tapsil", t.getTapsil(),
											"ekunKharch", t.getEkunKharch()))
									.toList());

					vk.put("vastavyaDetails",
							Optional.ofNullable(v.getVastavyaDetailsList()).orElse(Collections.emptyList()).stream()
									.map(vd -> Map.of("subId", vd.getSubId(), "vastavyaPrakar", vd.getVastavyaPrakar(),
											"pratyakshaKharch", vd.getPratyakshaKharch(), "anugyaRakkam",
											vd.getAnugya_rakkam(), "deyaRakkam", vd.getDeyaRakkam()))
									.toList());

					vk.put("excludedDetails",
							Optional.ofNullable(v.getExcludedDetails()).orElse(Collections.emptyList()).stream()
									.map(ed -> Map.of("subId", ed.getSubId(), "description", ed.getDescription(),
											"totalRakkam", ed.getAmount()))
									.toList());

					map.put("vaidyaKharchaPariganana", vk);
				}

				result.add(map);
			}

			response.setMessage("Medical bills fetched successfully");
			error.setErrorCode("200");
			error.setErrorDescription("SUCCESS");
			response.setErrorDetails(error);
			response.setData(result);

		} catch (Exception e) {
			log.error("Error fetching medical bills", e);
			error.setErrorCode("500");
			error.setErrorDescription(e.getMessage());
			response.setErrorDetails(error);
			response.setMessage("Error while fetching medical bills");
		}

		return response;
	}

	@Override
	public LeaveResponse saveOrUpdateLeave(LeaveRequest dto) {
		LeaveResponse response = new LeaveResponse();
		ApplicationError error = new ApplicationError();
		response.setData(new ArrayList<>());

		try {
			String username = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
			LocalDateTime now = LocalDateTime.now();

			Optional<LeaveEntity> existingOpt = leaveRepository.findByRowId(dto.getRowId());

			// 🗑️ DELETE case
			if ("D".equalsIgnoreCase(dto.getFlag())) {
				if (existingOpt.isPresent()) {
					leaveRepository.delete(existingOpt.get());
					response.setMessage("Leave order deleted successfully.");
					error.setErrorCode("200");
					error.setErrorDescription("Success");
				} else {
					response.setMessage("No record found for deletion with rowId: " + dto.getRowId());
					error.setErrorCode("404");
					error.setErrorDescription("Record not found");
				}
				response.setErrorDetails(error);
				return response;
			}

			LeaveEntity entity;
			String flag;

			if (existingOpt.isPresent()) {
				// 🔁 UPDATE existing record
				entity = existingOpt.get();
				flag = "U";
			} else {
				// 🆕 CREATE new record
				entity = new LeaveEntity();
				flag = "C";
			}

			// 🔹 Common field mapping
			entity.setRowId(dto.getRowId());
			entity.setYear(dto.getYear());
			entity.setMonth(dto.getMonth());
			entity.setDate(dto.getDate());
			entity.setEmployeeName(dto.getApplicantDetails().getEmployeeName());
			entity.setDesignation(dto.getApplicantDetails().getDesignation());
			entity.setLeaveFromDate(dto.getLeaveDetails().getFromDate());
			entity.setLeaveToDate(dto.getLeaveDetails().getToDate());
			entity.setFlag(flag);
			entity.setKramankNo(dto.getKramankNo());

			// 🔹 Store full JSON data
			JsonNode jsonData = objectMapper.convertValue(dto, JsonNode.class);
			entity.setData(jsonData);

			// 🔹 Created / Updated handling
			if (entity.getId() == null) {
				// First time create
				entity.setCreatedBy(username);
				entity.setCreatedDate(now);
				entity.setUpdatedBy(username);
				entity.setUpdatedDate(now);
			} else {
				// Updating
				if (entity.getCreatedBy() == null)
					entity.setCreatedBy(username);
				if (entity.getCreatedDate() == null)
					entity.setCreatedDate(now);

				entity.setUpdatedBy(username);
				entity.setUpdatedDate(now);
			}

			// 💾 Save entity
			LeaveEntity saved = leaveRepository.save(entity);

			// ✅ Prepare response data
			Map<String, Object> map = new LinkedHashMap<>();
			map.put("rowId", saved.getRowId());
			map.put("flag", saved.getFlag());
			map.put("employeeName", saved.getEmployeeName());
			map.put("year", saved.getYear());
			map.put("month", saved.getMonth());
			map.put("createdBy", saved.getCreatedBy());
			map.put("createdDate", saved.getCreatedDate());
			map.put("updatedBy", saved.getUpdatedBy());
			map.put("updatedDate", saved.getUpdatedDate());
			map.put("kramankNo", saved.getKramankNo());
			map.put("status", "SUCCESS");

			response.getData().add(map);
			response.setMessage(
					flag.equals("U") ? "Leave order updated successfully." : "Leave order created successfully.");

			error.setErrorCode("200");
			error.setErrorDescription("Success");
		} catch (Exception e) {
			log.error("Error in saveOrUpdateLeave: {}", e.getMessage(), e);
			response.setMessage("Error while saving leave order.");
			error.setErrorCode("500");
			error.setErrorDescription(e.getMessage());
		}

		response.setErrorDetails(error);
		return response;
	}

	@Override
	public LeaveResponse getLeaveDetails(String employeeName, String year, String month, String date) {
		LeaveResponse response = new LeaveResponse();
		ApplicationError error = new ApplicationError();
		response.setData(new ArrayList<>());

		try {
			// 🧭 Fetch data using dynamic filters
			List<LeaveEntity> results = leaveRepository.findByFilters(employeeName, year, month, date);

			if (results.isEmpty()) {
				response.setMessage("No leave records found for the given filters.");
				error.setErrorCode("404");
				error.setErrorDescription("No data found");
			} else {
				List<Map<String, Object>> mappedList = new ArrayList<>();

				for (LeaveEntity entity : results) {
					Map<String, Object> map = new LinkedHashMap<>();
					map.put("rowId", entity.getRowId());
					map.put("employeeName", entity.getEmployeeName());
					map.put("designation", entity.getDesignation());
					map.put("year", entity.getYear());
					map.put("month", entity.getMonth());
					map.put("date", entity.getDate());
					map.put("leaveFromDate", entity.getLeaveFromDate());
					map.put("leaveToDate", entity.getLeaveToDate());
					map.put("flag", entity.getFlag());
					map.put("createdBy", entity.getCreatedBy());
					map.put("createdDate", entity.getCreatedDate());
					map.put("updatedBy", entity.getUpdatedBy());
					map.put("updatedDate", entity.getUpdatedDate());
					map.put("fullJson", entity.getData());
					mappedList.add(map);
				}

				response.setData(mappedList);
				response.setMessage("Leave details fetched successfully.");
				error.setErrorCode("200");
				error.setErrorDescription("Success");
			}
		} catch (Exception e) {
			log.error("Error in getLeaveDetails: {}", e.getMessage(), e);
			response.setMessage("Error while fetching leave records.");
			error.setErrorCode("500");
			error.setErrorDescription(e.getMessage());
		}

		response.setErrorDetails(error);
		return response;
	}

	@Transactional
	public AppealResponse saveOrUpdateAppeal(AppealWrapper wrapper) {

		AppealResponse response = new AppealResponse();
		ApplicationError error = new ApplicationError();
		response.setData(new ArrayList<>());

		try {

			String username = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
			LocalDateTime now = LocalDateTime.now();

			for (AppealRequest dto : wrapper.getAppealData()) {

				// -----------------------------
				// FIND RECORD USING deleteId + year/date
				// -----------------------------
				Optional<AppealEntity> existingOpt = Optional.empty();

				if (dto.getDeleteId() != null && dto.getDate() != null) {
					existingOpt = appealRepository.findByDeleteIdAndDate(dto.getDeleteId(), dto.getDate());
				}

				String flag = Optional.ofNullable(dto.getFlag()).orElse("C").toUpperCase();

				// -----------------------------
				// DELETE LOGIC BY deleteId + date/year
				// -----------------------------
				if (flag.equals("D")) {

					if (existingOpt.isPresent()) {
						appealRepository.delete(existingOpt.get());

						Map<String, Object> delMap = new LinkedHashMap<>();
						delMap.put("deleteId", dto.getDeleteId());
						delMap.put("date", dto.getDate());
						delMap.put("status", "DELETED");
						response.getData().add(delMap);
					}
					continue;
				}

				// -----------------------------
				// CREATE / UPDATE LOGIC
				// -----------------------------
				// CREATE / UPDATE LOGIC
				AppealEntity entity = existingOpt.orElse(new AppealEntity());
				boolean isUpdate = existingOpt.isPresent();

				entity.setDeleteId(dto.getDeleteId());
				entity.setYear(dto.getYear());
				entity.setRowId(dto.getRowId());
				entity.setDate(dto.getDate());

				if (isUpdate) {
					entity.setFlag("U");
					entity.setUpdatedBy(username);
					entity.setUpdatedDate(now);
				} else {
					entity.setFlag("C");
					entity.setCreatedBy(username);
					entity.setCreatedDate(now);
					entity.setUpdatedBy(username);
					entity.setUpdatedDate(now);
				}

				// SET ALL FIELDS
				entity.setApeelArjachaNondaniKramank(dto.getApeelArjachaNondaniKramank());
				entity.setApeelkaracheNav(dto.getApeelkaracheNav());
				entity.setApeelArjKonakadeKelaAahe(dto.getApeelArjKonakadeKelaAahe());
				entity.setApeelArjPraptJhalyachaDinank(dto.getApeelArjPraptJhalyachaDinank());
				entity.setPratidariYancheNavPatta(dto.getPratidariYancheNavPatta());
				entity.setApeelarthiDurustReneSathiKaam(dto.getApeelarthiDurustReneSathiKaam());
				entity.setApeelchaThodkyaTathya(dto.getApeelchaThodkyaTathya());
				entity.setApeelArjacheShulk(dto.getApeelArjacheShulk());
				entity.setKonakadeApeelSwikarBharle(dto.getKonakadeApeelSwikarBharle());
				entity.setApeelArjachiVidhikShulkBharalDinank(dto.getApeelArjachiVidhikShulkBharalDinank());
				entity.setKonalyaJanmahitiAdhikariYanchikadeApeelKeleTathyaTapshil(
						dto.getKonalyaJanmahitiAdhikariYanchikadeApeelKeleTathyaTapshil());
				entity.setApeelvarNirnayDilyachaDinank(dto.getApeelvarNirnayDilyachaDinank());
				entity.setShera(dto.getShera());

				entity.setDynamicColumns(dto.getDynamicColumns());

				AppealEntity saved = appealRepository.save(entity);

				Map<String, Object> map = new LinkedHashMap<>();
				map.put("deleteId", saved.getDeleteId());
				map.put("year", saved.getYear());
				map.put("flag", saved.getFlag());
				map.put("status", isUpdate ? "UPDATED" : "CREATED");
				response.getData().add(map);
			}

			error.setErrorCode("200");
			error.setErrorDescription("Success");
			response.setErrorDetails(error);

		} catch (Exception e) {
			error.setErrorCode("500");
			error.setErrorDescription(e.getMessage());
			response.setErrorDetails(error);
		}

		return response;
	}

	@Override
	public AppealResponse getAppealData(String year, int page, int size) {

		AppealResponse response = new AppealResponse();
		ApplicationError error = new ApplicationError();
		response.setData(new ArrayList<>());

		try {

			// Validate Page & Size
			int pageNum = page >= 0 ? page : 0;
			int pageSize = size > 0 ? size : 10;

			Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by("updatedDate").descending());

			Page<AppealEntity> appealPage;

			// 🎯 Filter by year OR fetch all
			if (year != null && !year.isBlank()) {
				appealPage = appealRepository.findByYear(year, pageable);
			} else {
				appealPage = appealRepository.findAll(pageable);
			}

			// Build Response Data
			for (AppealEntity e : appealPage.getContent()) {

				Map<String, Object> map = new LinkedHashMap<>();

				map.put("id", e.getId());
				map.put("rowId", e.getRowId());
				map.put("year", e.getYear());
				map.put("date", e.getDate());
				map.put("deleteId", e.getDeleteId());
				map.put("flag", e.getFlag());

				map.put("apeelArjachaNondaniKramank", e.getApeelArjachaNondaniKramank());
				map.put("apeelkaracheNav", e.getApeelkaracheNav());
				map.put("apeelArjKonakadeKelaAahe", e.getApeelArjKonakadeKelaAahe());
				map.put("apeelArjPraptJhalyachaDinank", e.getApeelArjPraptJhalyachaDinank());
				map.put("pratidariYancheNavPatta", e.getPratidariYancheNavPatta());
				map.put("apeelarthiDurustReneSathiKaam", e.getApeelarthiDurustReneSathiKaam());
				map.put("apeelchaThodkyaTathya", e.getApeelchaThodkyaTathya());
				map.put("apeelArjacheShulk", e.getApeelArjacheShulk());
				map.put("konakadeApeelSwikarBharle", e.getKonakadeApeelSwikarBharle());
				map.put("apeelArjachiVidhikShulkBharalDinank", e.getApeelArjachiVidhikShulkBharalDinank());
				map.put("konalyaJanmahitiAdhikariYanchikadeApeelKeleTathyaTapshil",
						e.getKonalyaJanmahitiAdhikariYanchikadeApeelKeleTathyaTapshil());
				map.put("apeelvarNirnayDilyachaDinank", e.getApeelvarNirnayDilyachaDinank());
				map.put("shera", e.getShera());

				// 🧩 Dynamic Columns (convert JsonNode → Map)
				if (e.getDynamicColumns() != null && !e.getDynamicColumns().isEmpty()) {
					Map<String, Object> dynMap = objectMapper.convertValue(e.getDynamicColumns(), Map.class);
					map.put("dynamicColumns", dynMap);
				} else {
					map.put("dynamicColumns", new LinkedHashMap<>());
				}

				// Audit Fields
				map.put("createdBy", e.getCreatedBy());
				map.put("createdDate", e.getCreatedDate());
				map.put("updatedBy", e.getUpdatedBy());
				map.put("updatedDate", e.getUpdatedDate());

				response.getData().add(map);
			}
			// SORT BY rowId
			response.getData().sort(Comparator.comparing(m -> Integer.parseInt(m.get("rowId").toString())));

			// Pagination Meta
			Map<String, Object> meta = new LinkedHashMap<>();
			meta.put("pageNumber", appealPage.getNumber());
			meta.put("pageSize", appealPage.getSize());
			meta.put("totalElements", appealPage.getTotalElements());
			meta.put("totalPages", appealPage.getTotalPages());
			response.setMeta(meta);

			// Success
			response.setMessage("Appeal register data fetched successfully.");
			error.setErrorCode("200");
			error.setErrorDescription("Success");

		} catch (Exception ex) {
			response.setMessage("Error while fetching appeal data.");
			error.setErrorCode("500");
			error.setErrorDescription(ex.getMessage());
		}

		response.setErrorDetails(error);
		return response;
	}

	@Override
	@Transactional
	public EmployeePostingResponse saveOrUpdateEmployeePosting(EmployeePostingRequest dto) {
		EmployeePostingResponse response = new EmployeePostingResponse();
		ApplicationError error = new ApplicationError();
		response.setData(new ArrayList<>());

		try {
			String username = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");

			for (Map<String, Object> row : dto.getData()) {
				Long rowId = ((Number) row.get("rowId")).longValue();
				String year = (String) row.get("year");
				String flag = (String) row.getOrDefault("flag", "");

				Optional<EmployeePostingEntity> existingOpt = employeePostingRepository.findByRowId(rowId);

				// 🗑️ Delete
				if ("D".equalsIgnoreCase(flag)) {
					if (existingOpt.isPresent()) {
						employeePostingRepository.delete(existingOpt.get());
						log.info("Deleted record with rowId: {}", rowId);
					}
					continue;
				}

				// 🆕 Create or 🔁 Update
				EmployeePostingEntity entity = existingOpt.orElse(new EmployeePostingEntity());

				// detect automatically
				if (existingOpt.isEmpty()) {
					entity.setFlag("C");
				} else {
					entity.setFlag("U");
				}

				entity.setRowId(rowId);
				entity.setYear(year);

				entity.setKramank((Integer) row.get("kramank"));
				entity.setAdhikariKarmacharyacheNav((String) row.get("adhikariKarmacharyacheNav"));
				entity.setPadnaam((String) row.get("padnaam"));
				entity.setDharika((String) row.get("dharika"));
				entity.setKalavadhi((String) row.get("kalavadhi"));

				// 🔹 dynamic fields
				ObjectNode dynamic = objectMapper.createObjectNode();
				for (Map.Entry<String, Object> entry : row.entrySet()) {
					if (!List.of("rowId", "year", "flag", "kramank", "adhikariKarmacharyacheNav", "padnaam", "dharika",
							"kalavadhi").contains(entry.getKey())) {
						dynamic.putPOJO(entry.getKey(), entry.getValue());
					}
				}
				entity.setDynamicColumns(dynamic);

				// 🕒 Timestamps
				LocalDateTime now = LocalDateTime.now();
				if (entity.getId() == null) {
					entity.setCreatedBy(username);
					entity.setCreatedDate(now);
					entity.setUpdatedBy(username);
					entity.setUpdatedDate(now);
				} else {
					// retain created info
					if (entity.getCreatedBy() == null)
						entity.setCreatedBy(username);
					if (entity.getCreatedDate() == null)
						entity.setCreatedDate(now);

					entity.setUpdatedBy(username);
					entity.setUpdatedDate(now);
				}

				employeePostingRepository.save(entity);
				log.info("{} record processed (rowId={})", entity.getFlag(), rowId);
			}

			response.setMessage("Records processed successfully.");
			error.setErrorCode("200");
			error.setErrorDescription("Success");

		} catch (Exception e) {
			log.error("Error while saving employee posting data: {}", e.getMessage(), e);
			response.setMessage("Error while saving data.");
			error.setErrorCode("500");
			error.setErrorDescription(e.getMessage());
		}

		response.setErrorDetails(error);
		return response;
	}

	@Override
	public EmployeePostingResponse getEmployeePostingData(String name, String year, int page, int size) {
		EmployeePostingResponse response = new EmployeePostingResponse();
		ApplicationError error = new ApplicationError();
		response.setData(new ArrayList<>());

		try {
			Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
			Page<EmployeePostingEntity> result = employeePostingRepository.findByNameAndYear(name, year, pageable);

			for (EmployeePostingEntity e : result.getContent()) {
				Map<String, Object> map = new LinkedHashMap<>();

				map.put("rowId", e.getRowId());
				map.put("year", e.getYear());
				map.put("kramank", e.getKramank());
				map.put("adhikariKarmacharyacheNav", e.getAdhikariKarmacharyacheNav());
				map.put("padnaam", e.getPadnaam());
				map.put("dharika", e.getDharika());
				map.put("kalavadhi", e.getKalavadhi());
				map.put("flag", e.getFlag());
				map.put("createdBy", e.getCreatedBy());
				map.put("createdDate", e.getCreatedDate());
				map.put("updatedBy", e.getUpdatedBy());
				map.put("updatedDate", e.getUpdatedDate());

				if (e.getDynamicColumns() != null) {
					map.put("dynamicColumns", e.getDynamicColumns());
				}

				response.getData().add(map);
			}

			response.setMessage("Records fetched successfully.");
			error.setErrorCode("200");
			error.setErrorDescription("Success");

		} catch (Exception ex) {
			log.error("Error fetching employee posting data: {}", ex.getMessage(), ex);
			response.setMessage("Error while fetching records.");
			error.setErrorCode("500");
			error.setErrorDescription(ex.getMessage());
		}

		response.setErrorDetails(error);
		return response;
	}

	@Override
	public IncomeTaxDeductionResponse saveOrUpdateIncomeTaxDeduc(IncomeTaxDeductionRequest request) {
		IncomeTaxDeductionResponse response = new IncomeTaxDeductionResponse();
		ApplicationError error = new ApplicationError();
		response.setData(new ArrayList<>());

		try {
			String username = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
			LocalDateTime now = LocalDateTime.now();

			for (Map<String, Object> row : request.getData()) {
				Long rowId = ((Number) row.get("rowId")).longValue();
				String flag = (String) row.getOrDefault("flag", "");

				Optional<IncomeTaxDeductionEntity> existingOpt = incomeTaxDeductionRepository.findByRowId(rowId);

				// 🗑️ DELETE case
				if ("D".equalsIgnoreCase(flag)) {
					existingOpt.ifPresent(incomeTaxDeductionRepository::delete);
					continue;
				}

				IncomeTaxDeductionEntity entity = existingOpt.orElse(new IncomeTaxDeductionEntity());

				// Detect CREATE / UPDATE
				String detectedFlag = existingOpt.isPresent() ? "U" : "C";
				entity.setFlag(detectedFlag);

				// Map static fields
				entity.setRowId(rowId);
				entity.setYear(request.getYear());
				entity.setMonth(request.getMonth());
				entity.setSrNo((Integer) row.get("srNo"));
				entity.setNameAndDesignation((String) row.get("nameAndDesignation"));

				if (row.get("amountOfIncomeTaxDeducted") != null) {
					entity.setAmountOfIncomeTaxDeducted(
							new java.math.BigDecimal(row.get("amountOfIncomeTaxDeducted").toString()));
				}

				entity.setRemarks((String) row.get("remarks"));

				// Detect dynamic keys
				Map<String, Object> dynamic = new LinkedHashMap<>();
				for (Map.Entry<String, Object> entry : row.entrySet()) {
					String key = entry.getKey();
					if (!List.of("rowId", "srNo", "nameAndDesignation", "amountOfIncomeTaxDeducted", "remarks", "flag")
							.contains(key)) {
						dynamic.put(key, entry.getValue());
					}
				}

				JsonNode dynamicJson = objectMapper.convertValue(dynamic, JsonNode.class);
				entity.setDynamicColumns(dynamicJson);

				// Audit fields
				if (entity.getId() == null) {
					entity.setCreatedBy(username);
					entity.setCreatedDate(now);
					entity.setUpdatedBy(username);
					entity.setUpdatedDate(now);
				} else {
					entity.setUpdatedBy(username);
					entity.setUpdatedDate(now);
				}

				incomeTaxDeductionRepository.save(entity);
			}

			response.setMessage("Income Tax data saved/updated successfully.");
			error.setErrorCode("200");
			error.setErrorDescription("Success");
		} catch (Exception e) {
			log.error("Error saving IncomeTax data: {}", e.getMessage(), e);
			response.setMessage("Error while saving data.");
			error.setErrorCode("500");
			error.setErrorDescription(e.getMessage());
		}

		response.setErrorDetails(error);
		return response;
	}

	@Override
	public Page<IncomeTaxDeductionResponse> getIncomeTaxDeductionData(String year, String month, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("rowId").ascending());
		List<IncomeTaxDeductionResponse> responseList = new ArrayList<>();
		ApplicationError error = new ApplicationError();

		try {
			Page<IncomeTaxDeductionEntity> entities;

			// 🧠 Dynamic filtering logic
			if ((year == null || year.isBlank()) && (month == null || month.isBlank())) {
				entities = incomeTaxDeductionRepository.findAll(pageable);
			} else if (year != null && (month == null || month.isBlank())) {
				entities = incomeTaxDeductionRepository.findByYear(year, pageable);
			} else if ((year == null || year.isBlank()) && month != null) {
				entities = incomeTaxDeductionRepository.findByMonthContainingIgnoreCase(month, pageable);
			} else {
				entities = incomeTaxDeductionRepository.findByYearAndMonthContainingIgnoreCase(year, month, pageable);
			}

			for (IncomeTaxDeductionEntity e : entities.getContent()) {
				Map<String, Object> map = new LinkedHashMap<>();

				map.put("rowId", e.getRowId());
				map.put("year", e.getYear());
				map.put("month", e.getMonth());
				map.put("flag", e.getFlag());
				map.put("srNo", e.getSrNo());
				map.put("nameAndDesignation", e.getNameAndDesignation());
				map.put("amountOfIncomeTaxDeducted", e.getAmountOfIncomeTaxDeducted());
				map.put("remarks", e.getRemarks());

				// ✅ Include dynamic columns (if present)
				if (e.getDynamicColumns() != null) {
					e.getDynamicColumns().fields().forEachRemaining(entry -> {
						map.put(entry.getKey(), entry.getValue().asText());
					});
				}

				map.put("createdBy", e.getCreatedBy());
				map.put("createdDate", e.getCreatedDate());
				map.put("updatedBy", e.getUpdatedBy());
				map.put("updatedDate", e.getUpdatedDate());

				IncomeTaxDeductionResponse response = new IncomeTaxDeductionResponse();
				error.setErrorCode("200");
				error.setErrorDescription("SUCCESS");
				response.setMessage("Fetched successfully");
				response.setErrorDetails(error);
				response.setData(List.of(map));

				responseList.add(response);
			}

			return new PageImpl<>(responseList, pageable, entities.getTotalElements());

		} catch (Exception ex) {
			IncomeTaxDeductionResponse errorResponse = new IncomeTaxDeductionResponse();

			errorResponse.setMessage("Error occurred while fetching data");
			error.setErrorCode("500");
			error.setErrorDescription(ex.getMessage());
			errorResponse.setErrorDetails(error);
			errorResponse.setData(null);

			return new PageImpl<>(List.of(errorResponse), pageable, 0);
		}
	}

	@Override
	public PassportNocResponse saveOrUpdatePassportNoc(PassportNocRequest dto) {
		PassportNocResponse response = new PassportNocResponse();
		ApplicationError error = new ApplicationError();
		response.setData(new ArrayList<>());

		try {
			String username = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
			String flag = Optional.ofNullable(dto.getFlag()).map(String::toUpperCase).orElse("C");

			Optional<PassportNocEntity> existingOpt = passportNocRepository.findByRowId(dto.getRowId());

			// 🗑 DELETE logic
			if ("D".equals(flag)) {
				if (existingOpt.isPresent()) {
					passportNocRepository.delete(existingOpt.get());
					response.setMessage("Record deleted successfully.");
					error.setErrorCode("200");
					error.setErrorDescription("Success");
				} else {
					response.setMessage("No record found to delete with rowId: " + dto.getRowId());
					error.setErrorCode("404");
					error.setErrorDescription("Not Found");
				}
				response.setErrorDetails(error);
				return response;
			}

			// 📝 CREATE / UPDATE logic
			PassportNocEntity entity = existingOpt.orElse(new PassportNocEntity());
			LocalDateTime now = LocalDateTime.now();

			entity.setRowId(dto.getRowId());
			entity.setYear(dto.getYear());
			entity.setMonth(dto.getMonth());
			entity.setDate(LocalDate.parse(dto.getDate()));

			// ✅ Assign JSONs
			entity.setNoObjectionCertificate(dto.getNoObjectionCertificate());
			entity.setIdentityConfirmation(dto.getIdentityConfirmation());
			entity.setDynamicColumns(dto.getDynamicColumns());

			// ✅ Extract employeeName (if present inside JSON)
			String empName = null;
			if (dto.getNoObjectionCertificate() != null && dto.getNoObjectionCertificate().has("employeeName")) {
				empName = dto.getNoObjectionCertificate().get("employeeName").asText();
			} else if (dto.getIdentityConfirmation() != null && dto.getIdentityConfirmation().has("employeeName")) {
				empName = dto.getIdentityConfirmation().get("employeeName").asText();
			}
			entity.setEmployeeName(empName);

			// ✅ Detect CREATE or UPDATE automatically
			if (entity.getId() == null) {
				flag = "C";
				entity.setCreatedBy(username);
				entity.setCreatedDate(now);
				entity.setUpdatedBy(username);
				entity.setUpdatedDate(now);
			} else {
				flag = "U";
				if (entity.getCreatedBy() == null)
					entity.setCreatedBy(username);
				if (entity.getCreatedDate() == null)
					entity.setCreatedDate(now);
				entity.setUpdatedBy(username);
				entity.setUpdatedDate(now);
			}

			entity.setFlag(flag);
			PassportNocEntity saved = passportNocRepository.save(entity);

			Map<String, Object> result = new LinkedHashMap<>();
			result.put("rowId", saved.getRowId());
			result.put("year", saved.getYear());
			result.put("month", saved.getMonth());
			result.put("employeeName", saved.getEmployeeName());
			result.put("flag", saved.getFlag());
			result.put("createdBy", saved.getCreatedBy());
			result.put("createdDate", saved.getCreatedDate());
			result.put("updatedBy", saved.getUpdatedBy());
			result.put("updatedDate", saved.getUpdatedDate());

			response.getData().add(result);
			response.setMessage(flag.equals("U") ? "Record updated successfully." : "Record created successfully.");
			error.setErrorCode("200");
			error.setErrorDescription("Success");

		} catch (Exception e) {
			log.error("Error in saveOrUpdatePassportNoc: {}", e.getMessage(), e);
			response.setMessage("Error while saving passport NOC data.");
			error.setErrorCode("500");
			error.setErrorDescription(e.getMessage());
		}

		response.setErrorDetails(error);
		return response;
	}

	@Override
	public Page<PassportNocResponse> getPassportNocData(String year, String month, String employeeName, int page,
			int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("rowId").ascending());
		ApplicationError error = new ApplicationError();

		try {
			// Default empty filter handling
			String filterYear = (year != null && !year.isBlank()) ? year : "";
			String filterMonth = (month != null && !month.isBlank()) ? month : "";
			String filterName = (employeeName != null && !employeeName.isBlank()) ? employeeName : "";

			Page<PassportNocEntity> entities = passportNocRepository
					.findByYearContainingIgnoreCaseAndMonthContainingIgnoreCaseAndEmployeeNameContainingIgnoreCase(
							filterYear, filterMonth, filterName, pageable);

			List<Map<String, Object>> resultList = new ArrayList<>();

			for (PassportNocEntity e : entities.getContent()) {
				Map<String, Object> map = new LinkedHashMap<>();
				map.put("rowId", e.getRowId());
				map.put("year", e.getYear());
				map.put("month", e.getMonth());
				map.put("date", e.getDate());
				map.put("flag", e.getFlag());
				map.put("employeeName", e.getEmployeeName());
				map.put("noObjectionCertificate", e.getNoObjectionCertificate());
				map.put("identityConfirmation", e.getIdentityConfirmation());

				// ✅ Add dynamic columns if any
				if (e.getDynamicColumns() != null) {
					e.getDynamicColumns().fields().forEachRemaining(entry -> {
						map.put(entry.getKey(), entry.getValue().asText());
					});
				}

				map.put("createdBy", e.getCreatedBy());
				map.put("createdDate", e.getCreatedDate());
				map.put("updatedBy", e.getUpdatedBy());
				map.put("updatedDate", e.getUpdatedDate());

				resultList.add(map);
			}

			PassportNocResponse response = new PassportNocResponse();
			response.setMessage("Fetched successfully");
			response.setData(resultList);

			error.setErrorCode("200");
			error.setErrorDescription("Success");
			response.setErrorDetails(error);

			return new PageImpl<>(List.of(response), pageable, entities.getTotalElements());

		} catch (Exception e) {
			PassportNocResponse response = new PassportNocResponse();
			error.setErrorCode("500");
			error.setErrorDescription(e.getMessage());
			response.setMessage("Error while fetching passport NOC data");
			response.setErrorDetails(error);
			return new PageImpl<>(List.of(response), pageable, 0);
		}
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadAppealArj(String year) throws IOException {

		List<AppealEntity> list = (year != null && !year.isBlank()) ? appealRepository.findByYear(year)
				: appealRepository.findAll();
		list.sort(Comparator.comparing(AppealEntity::getRowId));

		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sh = wb.createSheet("Appeal Register");

		// ---------- COLUMN WIDTHS ----------
		int[] colWidths = { 2000, 4500, 4500, 5500, 4500, 4500, 4500, 5500, 3000, 4500, 5500, 3500, 3000 };
		for (int i = 0; i < colWidths.length; i++) {
			sh.setColumnWidth(i, colWidths[i]);
		}

		// ---------- MARATHI FONT ----------
		XSSFFont marathiFont = wb.createFont();
		marathiFont.setFontName("Mangal");
		marathiFont.setFontHeightInPoints((short) 12);

		// ---------- BOLD HEADER FONT ----------
		XSSFFont headerFont = wb.createFont();
		headerFont.setFontName("Mangal");
		headerFont.setFontHeightInPoints((short) 12);
		headerFont.setBold(true);

		// ---------- BASE BORDER STYLE ----------
		CellStyle baseBorder = wb.createCellStyle();
		baseBorder.setBorderBottom(BorderStyle.MEDIUM);
		baseBorder.setBorderTop(BorderStyle.MEDIUM);
		baseBorder.setBorderLeft(BorderStyle.MEDIUM);
		baseBorder.setBorderRight(BorderStyle.MEDIUM);
		baseBorder.setAlignment(HorizontalAlignment.CENTER);
		baseBorder.setVerticalAlignment(VerticalAlignment.CENTER);
		baseBorder.setWrapText(true);

		// ---------- HEADER STYLE ----------
		CellStyle headerStyle = wb.createCellStyle();
		headerStyle.cloneStyleFrom(baseBorder);
		headerStyle.setFont(headerFont);

		// ---------- DATA STYLE ----------
		CellStyle dataStyle = wb.createCellStyle();
		dataStyle.cloneStyleFrom(baseBorder);
		dataStyle.setFont(marathiFont);

		// ---------- NUMBER STYLE ----------
		CellStyle numberStyle = wb.createCellStyle();
		numberStyle.cloneStyleFrom(baseBorder);
		numberStyle.setFont(marathiFont);

		// ---------- TITLE STYLE (NO BORDER) ----------
		CellStyle titleStyle = wb.createCellStyle();
		titleStyle.setFont(headerFont);
		titleStyle.setAlignment(HorizontalAlignment.CENTER);
		titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		titleStyle.setWrapText(true);
		// NOTE: NO BORDERS (clean look)

		// ---------- ROW 0 : EMPTY ----------
		sh.createRow(0);

		// ---------- ROW 1 : TITLE ----------
		Row titleRow = sh.createRow(1);
		Cell titleCell = titleRow.createCell(0);
		titleCell.setCellValue("माहिती अधिकारांतर्गत खूड मंडळ कार्यालयास प्राप्त झालेले अपील अर्ज");
		titleCell.setCellStyle(titleStyle);
		sh.addMergedRegion(new CellRangeAddress(1, 1, 0, 12));

		// ---------- HEADER TEXT ----------
		String[] headers = { "अ.क्र.", "अपील अर्जाचा नोंदणी क्रमांक", "अपीलकाचे नाव", "अपील अर्ज कोणाकडे केला आहे",
				"अपील अर्ज प्राप्त झाल्याचा दिनांक", "प्रतिवादी यांचे नाव व पत्ता",
				"अपीलार्थीदारिद्रय रेषेखालील आहे काय", "अपीलाचा थोडक्यात तपशील",
				"अपील अर्जाचे शुल्क कोणत्या स्वरुपात भरले", "अपील अर्जाचे विहीत शुल्क भरल्याचा दिनांक",
				"कोणत्या जनमाहिती अधिकारी यांचेविरुद्ध अपील केले त्याचा तपशील", "अपीलवर निर्णय दिल्याचा दिनांक",
				"शेरा" };

		// ---------- ROW 2 : HEADER ----------
		Row headerRow = sh.createRow(2);
		headerRow.setHeightInPoints(40);

		for (int i = 0; i < headers.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(headers[i]);
			cell.setCellStyle(headerStyle);
		}

		// ---------- ROW 3 : COLUMN NUMBERS ----------
		Row numberRow = sh.createRow(3);
		numberRow.setHeightInPoints(22);

		for (int i = 0; i < headers.length; i++) {
			Cell numCell = numberRow.createCell(i);
			numCell.setCellValue(i + 1);
			numCell.setCellStyle(numberStyle);
		}

		// ---------- ROW 4 : DATA START ----------
		int rowNum = 4;
		int sr = 1;

		for (AppealEntity e : list) {

			Row row = sh.createRow(rowNum++);
			row.setHeightInPoints(-1); // auto stretch

			int c = 0;

			addCell(row, c++, String.valueOf(sr++), dataStyle);
			addCell(row, c++, nvl(e.getApeelArjachaNondaniKramank()), dataStyle);
			addCell(row, c++, nvl(e.getApeelkaracheNav()), dataStyle);
			addCell(row, c++, nvl(e.getApeelArjKonakadeKelaAahe()), dataStyle);
			addCell(row, c++, nvl(e.getApeelArjPraptJhalyachaDinank()), dataStyle);
			addCell(row, c++, nvl(e.getPratidariYancheNavPatta()), dataStyle);
			addCell(row, c++, nvl(e.getApeelarthiDurustReneSathiKaam()), dataStyle);
			addCell(row, c++, nvl(e.getApeelchaThodkyaTathya()), dataStyle);
			addCell(row, c++, nvl(e.getApeelArjacheShulk()), dataStyle);
			addCell(row, c++, nvl(e.getApeelArjachiVidhikShulkBharalDinank()), dataStyle);
			addCell(row, c++, nvl(e.getKonalyaJanmahitiAdhikariYanchikadeApeelKeleTathyaTapshil()), dataStyle);
			addCell(row, c++, nvl(e.getApeelvarNirnayDilyachaDinank()), dataStyle);
			addCell(row, c++, nvl(e.getShera()), dataStyle);
		}

		// ---------- WRITE OUTPUT ----------
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		wb.write(out);
		wb.close();

		HttpHeaders headersHttp = new HttpHeaders();
		headersHttp.add("Content-Disposition", "attachment; filename=Appeal_Register.xlsx");

		return ResponseEntity.ok().headers(headersHttp)
				.contentType(
						MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())));
	}

	private void addCell(Row row, int col, String val, CellStyle style) {
		Cell cell = row.createCell(col);
		cell.setCellValue(val);
		cell.setCellStyle(style);
	}

	private String nvl(String v) {
		return v == null ? "" : v;
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadMedicalBill(String employeeName, String date) throws Exception {

		MedicalBillMasterEntity entity = masterRepo.findByEmployeeDetails_EmployeeNameAndBillDate(employeeName, date)
				.orElseThrow(() -> new RuntimeException("Record not found"));

		// Load template
		InputStream is = this.getClass().getResourceAsStream(TEMPLATE);
		if (is == null) {
			throw new RuntimeException("Template not found");
		}

		XWPFDocument doc = new XWPFDocument(is);

		// -------------------- PLACEHOLDER MAP ---------------------
		Map<String, String> map = new HashMap<>();
		map.put("${title}", entity.getTitle());
		map.put("${period}", entity.getPeriod());
		map.put("${year}", entity.getYear());
		map.put("${billDate}", entity.getBillDate());

		if (entity.getEmployeeDetails() != null) {
			var e = entity.getEmployeeDetails();
			map.put("${employeeName}", nvl(e.getEmployeeName()));
			map.put("${designation}", nvl(e.getDesignation()));
			map.put("${department}", nvl(e.getDepartment()));
			map.put("${patientName}", nvl(e.getPatientName()));
			map.put("${hospitalName}", nvl(e.getHospitalName()));
			map.put("${treatFrom}", nvl(e.getFromDate()));
			map.put("${treatTo}", nvl(e.getToDate()));
		}

		if (entity.getApprovalDetails() != null) {
			var a = entity.getApprovalDetails();
			map.put("${approvalAuthority}", nvl(a.getApprovingAuthority()));
			map.put("${approvalDate}", nvl(a.getApprovalDate()));
			map.put("${approvalAmount}", String.valueOf(a.getApprovalAmount()));
			map.put("${approvedBy}", nvl(a.getApprovedBy()));
		}

		// -------------------- REPLACE IN PARAGRAPHS --------------------
		for (XWPFParagraph p : doc.getParagraphs()) {
			replaceParagraph(p, map);
		}

		// -------------------- REPLACE IN TABLES ------------------------
		for (XWPFTable tbl : doc.getTables()) {
			for (XWPFTableRow row : tbl.getRows()) {
				for (XWPFTableCell cell : row.getTableCells()) {
					for (XWPFParagraph p : cell.getParagraphs()) {
						replaceParagraph(p, map);
					}
				}
			}
		}

		// -------------------- INSERT REFERENCE LIST ------------------
		insertReferenceBulletList(doc, entity.getReferences());

		// -------------------- INSERT KHARCHA TAPSHIL ROWS ------------
		insertKharchaRows(doc, entity.getKharchaTapsil());

		// -------------------- WRITE RESULT ------------------
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		doc.write(baos);
		doc.close();

		byte[] bytes = baos.toByteArray();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(
				MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
		headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Medical_Bill.docx\"");

		return ResponseEntity.ok().headers(headers).body(new InputStreamResource(new ByteArrayInputStream(bytes)));
	}

	// --------------------------------------------------------------------
	// HELPER → Replace tokens in paragraph
	private void replaceParagraph(XWPFParagraph p, Map<String, String> map) {
		List<XWPFRun> runs = p.getRuns();
		if (runs == null || runs.isEmpty())
			return;

		String full = "";
		for (XWPFRun r : runs)
			full += (r.getText(0) == null ? "" : r.getText(0));

		String replaced = full;
		for (var e : map.entrySet()) {
			String key = e.getKey();
			String token = "${" + key + "}";
			replaced = replaced.replace(token, e.getValue() == null ? "" : e.getValue());
		}

		if (!replaced.equals(full)) {
			// remove all runs text
			for (XWPFRun r : runs)
				r.setText("", 0);
			// put everything in first run
			runs.get(0).setText(replaced, 0);
		}
	}

	// HELPER → Replace tokens in table
	private void replaceTable(XWPFTable table, Map<String, String> map) {
		for (XWPFTableRow row : table.getRows())
			for (XWPFTableCell cell : row.getTableCells())
				for (XWPFParagraph p : cell.getParagraphs())
					replaceParagraph(p, map);
	}

	// --------------------------------------------------------------------
	// C : Insert Bullet Reference List
	private void insertReferenceBulletList(XWPFDocument doc, List<ReferenceEntity> refs) throws Exception {

		if (refs == null || refs.isEmpty())
			return;

		for (XWPFParagraph p : doc.getParagraphs()) {
			if (p.getText().contains("${REFERENCE_LIST}")) {

				p.removeRun(0);

				for (ReferenceEntity r : refs) {
					XWPFParagraph bullet = doc.insertNewParagraph(p.getCTP().newCursor());
					bullet.setNumID(addBulletStyle(doc));

					XWPFRun run = bullet.createRun();
					run.setFontFamily("Mangal");
					run.setFontSize(12);
					run.setText(r.getReference());
				}

				return;
			}
		}
	}

	public BigInteger addBulletStyle(XWPFDocument doc) throws Exception {
		XWPFNumbering numbering = doc.createNumbering();

		CTAbstractNum ctAbstractNum = CTAbstractNum.Factory.newInstance();
		ctAbstractNum.setAbstractNumId(BigInteger.valueOf(1));

		CTLvl lvl = ctAbstractNum.addNewLvl();
		lvl.setIlvl(BigInteger.ZERO);
		lvl.addNewNumFmt().setVal(STNumberFormat.BULLET);
		lvl.addNewLvlText().setVal("•");

		XWPFAbstractNum abs = new XWPFAbstractNum(ctAbstractNum);

		BigInteger abstractNumId = numbering.addAbstractNum(abs);
		return numbering.addNum(abstractNumId);
	}

	// --------------------------------------------------------------------
	// B : Expand Kharcha Table Rows
	private void insertKharchaRows(XWPFDocument doc, List<KharchaTapsilEntity> list) {
		if (list == null || list.isEmpty())
			return;

		for (XWPFTable tbl : doc.getTables()) {
			if (tbl.getText().contains("${KHARCHA_TABLE}")) {

				XWPFTableRow sampleRow = tbl.getRow(1); // 2nd row = template
				tbl.removeRow(1);

				for (KharchaTapsilEntity k : list) {
					XWPFTableRow newRow = tbl.createRow();

					copyRowStyle(sampleRow, newRow);

					newRow.getCell(0).setText(String.valueOf(k.getAkr()));
					newRow.getCell(1).setText(nvl(k.getTapsil()));
					newRow.getCell(2).setText(String.valueOf(k.getRakkam()));
				}

				return;
			}
		}
	}

	private void copyRowStyle(XWPFTableRow src, XWPFTableRow dest) {
		for (int i = 0; i < src.getTableCells().size(); i++) {
			XWPFTableCell cSrc = src.getCell(i);
			XWPFTableCell cDest = dest.getCell(i);

			if (cSrc == null || cDest == null)
				continue;

			cDest.getCTTc().setTcPr(cSrc.getCTTc().getTcPr());
			cDest.setColor(cSrc.getColor());
		}
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadLeaveDetails(String employeeName, String date) throws Exception {

		// 🔥 1) DB FETCH based on employeeName + date
		LeaveEntity entity = leaveRepository.findByEmployeeNameAndDate(employeeName, date)
				.orElseThrow(() -> new RuntimeException("Data not found"));

		// 🔥 2) JSON column ko Map me convert karo
		Map<String, Object> data = objectMapper.convertValue(entity.getData(), Map.class);

		// 🔥 3) PDF START
		PDDocument pdf = new PDDocument();
		PDPage page = new PDPage(PDRectangle.A4);
		pdf.addPage(page);

		PDPageContentStream cs = new PDPageContentStream(pdf, page);

		PDType0Font font = PdfUtil.loadFont(pdf);

		float y = 790;

		// ---------- TITLE ----------
		PdfUtil.drawText(cs, "कार्यालयीन आदेश क्रमांक", 210, y, font, 16, true);
		y -= 25;

		PdfUtil.drawText(cs, "सन " + data.get("year").toString(), 260, y, font, 16, true);
		y -= 40;

		// ---------- SUBJECT ----------
		PdfUtil.drawText(cs, "संदर्भ :- " + data.get("subjectReference"), 50, y, font, 12, true);
		y -= 30;

		// ---------- MAIN BODY ----------
		Map<String, Object> applicant = (Map) data.get("applicantDetails");
		Map<String, Object> leaveDetails = (Map) data.get("leaveDetails");

		String p1 = "महाराष्ट्र नागरी सेवा (रजा) नियम 1981 मधील " + leaveDetails.get("ruleReference") + " अनुसार "
				+ applicant.get("employeeName") + ", " + applicant.get("designation") + " यांना "
				+ leaveDetails.get("reason") + " कारणासाठी " + leaveDetails.get("fromDate") + " ते "
				+ leaveDetails.get("toDate") + " या कालावधीतील " + leaveDetails.get("totalDays")
				+ " दिवसांची रजा मंजूर करण्यात येत आहे.";

		y = drawParagraph(cs, font, p1, 12, 50, y - 10, 520);

		// ---------- REJOIN ----------
		Map<String, Object> rejoin = (Map) data.get("rejoiningDetails");
		y = drawParagraph(cs, font, rejoin.get("remark").toString(), 12, 50, y - 15, 520);

		// ---------- HOLIDAY ----------
		Map<String, Object> holiday = (Map) data.get("holidayJoinApproval");
		y = drawParagraph(cs, font, holiday.get("remark").toString(), 12, 50, y - 10, 520);

		// ---------- CERTIFICATION ----------
		Map<String, Object> cert = (Map) data.get("certification");

		y = drawParagraph(cs, font, cert.get("employmentContinuation").toString(), 12, 50, y - 10, 520);

		y = drawParagraph(cs, font, "रजा शिल्लक : " + cert.get("leaveBalanceBefore") + " → "
				+ cert.get("leaveBalanceAfter") + " (दि. " + cert.get("asOnDate") + " रोजी)", 12, 50, y - 5, 520);

		cs.close();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		pdf.save(baos);
		pdf.close();

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=Leave_Order.pdf");

		return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF)
				.body(new InputStreamResource(new ByteArrayInputStream(baos.toByteArray())));
	}

	private float drawParagraph(PDPageContentStream cs, PDType0Font font, String text, int fontSize, float x, float y,
			float width) throws IOException {

		List<String> lines = splitText(text, font, fontSize, width);

		for (String line : lines) {
			PdfUtil.drawText(cs, line, x, y, font, fontSize, false);
			y -= (fontSize + 5);
		}
		return y;
	}

	private List<String> splitText(String text, PDType0Font font, int fontSize, float width) throws IOException {

		List<String> lines = new ArrayList<>();
		String[] words = text.split(" ");

		StringBuilder line = new StringBuilder();

		for (String w : words) {
			String tmp = line + " " + w;
			float size = font.getStringWidth(tmp) / 1000 * fontSize;

			if (size > width) {
				lines.add(line.toString());
				line = new StringBuilder(w);
			} else {
				line.append(" ").append(w);
			}
		}
		lines.add(line.toString());
		return lines;
	}

	@Override
	@Transactional
	public AgendaResponse saveOrUpdateAgenda(AgendaRequest dto) {

		AgendaResponse response = new AgendaResponse();
		ApplicationError error = new ApplicationError();

		try {

			String username = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
			String year = dto.getMeta().getYear();
			String targetDate = dto.getMeta().getTargetDate();

			for (AgendaRow row : dto.getRows()) {

				long rowId = row.getRowId();
				String deleteFlag = Optional.ofNullable(row.getDeleteFlag()).orElse("");

				/*
				 * -------------------- HARD DELETE --------------------
				 */
				if ("D".equalsIgnoreCase(deleteFlag)) {

					Long deleteId = row.getDeleteId();
					if (deleteId != null && deleteId > 0) {

						agendaOfficerRepository.findByDeleteIdAndYearAndTargetDate(deleteId, year, targetDate)
								.ifPresent(agendaOfficerRepository::delete);

						error.setErrorCode("200");
						error.setErrorDescription("Deleted successfully.");
					}

					continue;
				}

				/*
				 * -------------------- CREATE / UPDATE --------------------
				 */
				Optional<AgendaOfficerEntity> existingOpt = agendaOfficerRepository
						.findByRowIdAndYearAndTargetDate(rowId, year, targetDate);

				AgendaOfficerEntity entity = existingOpt.orElse(new AgendaOfficerEntity());
				LocalDateTime now = LocalDateTime.now();

				entity.setRowId(rowId);
				entity.setYear(year);
				entity.setTargetDate(targetDate);
				entity.setColumnData(row.getColumnData()); // DIRECT SAVE
				entity.setDeleteId(row.getDeleteId());
				entity.setUpAdhikshakAbhiyantaName(row.getUpAdhikshakAbhiyantaName());

				if (entity.getId() == null) {
					entity.setFlag("C");
					entity.setCreatedBy(username);
					entity.setCreatedAt(now);
				} else {
					entity.setFlag("U");
				}

				entity.setUpdatedBy(username);
				entity.setUpdatedAt(now);

				agendaOfficerRepository.save(entity);
			}

			response.setMessage("Success");

			error.setErrorCode("200");
			error.setErrorDescription("Agenda saved successfully.");

		} catch (Exception e) {
			error.setErrorCode("500");
			error.setErrorDescription("Error: " + e.getMessage());
			response.setMessage("Failed");
		}

		response.setErrorDetails(error);
		return response;
	}

	@Override
	public AgendaResponse getAgendaByYearAndTargetDate(String year, String targetDate) {

		AgendaResponse response = new AgendaResponse();
		ApplicationError error = new ApplicationError();

		try {
			// Direct fetch from DB
			List<AgendaOfficerEntity> list = agendaOfficerRepository.findByYearAndTargetDate(year, targetDate);

			// ===== SORT BY rowId ASC =====
			list.sort(Comparator.comparing(AgendaOfficerEntity::getRowId));

			// No transformation – return exactly what was saved
			response.setData(list);
			response.setMessage("Success");

			error.setErrorCode("200");
			error.setErrorDescription("Agenda fetched successfully.");

		} catch (Exception e) {

			response.setMessage("Failed");
			error.setErrorCode("500");
			error.setErrorDescription("Error: " + e.getMessage());
		}

		response.setErrorDetails(error);
		return response;
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadAgendaExcel(String year, String targetDate) throws Exception {

		List<AgendaOfficerEntity> list = agendaOfficerRepository.findByYearAndTargetDate(year, targetDate);
		// ===== SORT BY ROW ID =====
		list.sort(Comparator.comparing(AgendaOfficerEntity::getRowId));

		// ===== FOOTER NAME =====
		String footerName = "( नि.लि.हैमे )";
		if (!list.isEmpty() && list.get(0).getUpAdhikshakAbhiyantaName() != null)
			footerName = list.get(0).getUpAdhikshakAbhiyantaName();

		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("Agenda");

		// ===== SMALL FONT =====
		Font smallFont = workbook.createFont();
		smallFont.setFontHeightInPoints((short) 10);

		// ===== TITLE STYLE =====
		CellStyle noBorder = workbook.createCellStyle();
		noBorder.setAlignment(HorizontalAlignment.CENTER);
		noBorder.setVerticalAlignment(VerticalAlignment.CENTER);
		Font titleFont = workbook.createFont();
		titleFont.setBold(true);
		titleFont.setFontHeightInPoints((short) 14);
		noBorder.setFont(titleFont);
		noBorder.setWrapText(true);

		// ===== CENTER BORDER =====
		CellStyle center = workbook.createCellStyle();
		center.setAlignment(HorizontalAlignment.CENTER);
		center.setVerticalAlignment(VerticalAlignment.CENTER);
		center.setBorderBottom(BorderStyle.THIN);
		center.setBorderTop(BorderStyle.THIN);
		center.setBorderLeft(BorderStyle.THIN);
		center.setBorderRight(BorderStyle.THIN);
		center.setFont(smallFont);
		center.setWrapText(true);

		// ===== FULL BORDER FOR DATA =====
		CellStyle fullBorder = workbook.createCellStyle();
		fullBorder.cloneStyleFrom(center);
		fullBorder.setWrapText(false);

		// ===== HEADER BOLD BORDER =====
		CellStyle boldHeader = workbook.createCellStyle();
		boldHeader.cloneStyleFrom(center);
		Font boldHeaderFont = workbook.createFont();
		boldHeaderFont.setBold(true);
		boldHeaderFont.setFontHeightInPoints((short) 11);
		boldHeader.setFont(boldHeaderFont);

		// ===== LEFT ALIGN =====
		CellStyle leftAlign = workbook.createCellStyle();
		leftAlign.setAlignment(HorizontalAlignment.LEFT);
		leftAlign.setVerticalAlignment(VerticalAlignment.CENTER);
		leftAlign.setFont(smallFont);

		// ===== FOOTER ALIGN =====
		CellStyle footerStyle = workbook.createCellStyle();
		footerStyle.setAlignment(HorizontalAlignment.RIGHT);
		footerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		Font footFont = workbook.createFont();
		footFont.setBold(true);
		footFont.setFontHeightInPoints((short) 11);
		footerStyle.setFont(footFont);

		int rowIdx = 0;

		// =================== TITLE SECTION ===================
		rowIdx = merged(sheet, rowIdx, "परिशिष्ट-ब", noBorder, 10);
		rowIdx = merged(sheet, rowIdx, "1 ऑगस्ट रोजी वयाची 49/ 54 वर्षे पूर्ण झालेले गट-अ मधील शासकीय अधिकारी",
				noBorder, 10);
		rowIdx = merged(sheet, rowIdx, "विभागाचे नाव- जलसंपदा विभाग.", noBorder, 10);
		rowIdx = merged(sheet, rowIdx, "मंडळ कार्यालयाचे नाव- अधीक्षक अभियंता, पुणे पाटबंधारे प्रकल्प मंडळ,पुणे",
				leftAlign, 10);
		rowIdx = merged(sheet, rowIdx, "पदनाम- उपविभागीय अभियंता.", leftAlign, 10);
		rowIdx++;

		// =================== COMPACT COLUMN WIDTHS ===================
		int[] widths = { 1800, 5500, 4200, 4200, 5200, 3500, 5500, 3800, 3000, 3000, 3000 };
		for (int c = 0; c < widths.length; c++)
			sheet.setColumnWidth(c, widths[c]);

		// =================== HEADER ROWS ===================
		Row h1 = sheet.createRow(rowIdx++);
		Row h2 = sheet.createRow(rowIdx++);
		h1.setHeightInPoints(24);
		h2.setHeightInPoints(22);

		String[] mainHeaders = { "अ. क्र.", "अधिकारी नाव", "पदनाम", "जन्मतारीख व वय",
				" सेवेतील प्रथम नियुक्तीचा दिनांक", "झालेली एकुण सेवा", "विभागीय चौकशी सुरू / प्रस्तावित",
				"गोपनीय अहवालामधील बाबी." };

		String[] subHeaders = { "गोपनीय अहवाल वर्ष", "सचोटी", "प्रकृतीमान", "प्रतवारी" };

		// Merge left block (first 7 cols)
		for (int c = 0; c <= 6; c++)
			sheet.addMergedRegion(new CellRangeAddress(h1.getRowNum(), h2.getRowNum(), c, c));

		// Merge group header for confidential fields
		sheet.addMergedRegion(new CellRangeAddress(h1.getRowNum(), h1.getRowNum(), 7, 10));

		// Header row 1
		for (int i = 0; i < mainHeaders.length; i++) {
			Cell cell = h1.createCell(i);
			cell.setCellValue(mainHeaders[i]);
			cell.setCellStyle(boldHeader);
		}

		// Header row 2
		for (int i = 0; i < subHeaders.length; i++) {
			Cell cell = h2.createCell(i + 7);
			cell.setCellValue(subHeaders[i]);
			cell.setCellStyle(boldHeader);
		}

		// =================== COLUMN NUMBER ROW ===================
		Row colNumRow = sheet.createRow(rowIdx++);
		colNumRow.setHeightInPoints(16);

		String[] colNums = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11" };
		for (int c = 0; c < colNums.length; c++) {
			Cell cell = colNumRow.createCell(c);
			cell.setCellValue(colNums[c]);
			cell.setCellStyle(center);
		}

		// ===== Fix border of merged header cells =====
		for (int c = 0; c <= 10; c++) {
			Cell c1 = h1.getCell(c);
			if (c1 == null)
				c1 = h1.createCell(c);
			c1.setCellStyle(boldHeader);

			Cell c2 = h2.getCell(c);
			if (c2 == null)
				c2 = h2.createCell(c);
			c2.setCellStyle(boldHeader);
		}

		// =================== DATA ROWS ===================
		for (AgendaOfficerEntity entity : list) {

			JsonNode cd = entity.getColumnData();
			JsonNode g = cd.get("gopniyaAhawal");

			String[] years = g.get("varsh").asText().split(",");
			String[] sachoti = g.get("sachoti").asText().split(",");
			String[] prakrutiman = g.get("prakrutiman").asText().split(",");
			String[] pratavaari = g.get("pratavaari").asText().split(",");

			int n = years.length;

			// Merge left 7 columns for multi-row entries
			if (n > 1)
				for (int c = 0; c <= 6; c++)
					sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx + n - 1, c, c));

			// FIRST ROW
			Row r = sheet.createRow(rowIdx);
			r.setHeightInPoints(22);

			r.createCell(0).setCellValue(cd.get("kramank").asInt());
			r.createCell(1).setCellValue(cd.get("adhikariNav").asText());
			r.createCell(2).setCellValue(cd.get("padnaam").asText());
			r.createCell(3).setCellValue(cd.get("janmaTarikh").asText());
			r.createCell(4).setCellValue(cd.get("sevetIlPrathamNiyuktiDinank").asText());
			r.createCell(5).setCellValue(cd.get("jhaleliEkunaSeva").asText());
			r.createCell(6).setCellValue(cd.get("vibhagiyaChokashiStatus").asText());

			r.createCell(7).setCellValue(years[0].trim());
			r.createCell(8).setCellValue(sachoti[0].trim());
			r.createCell(9).setCellValue(prakrutiman[0].trim());
			r.createCell(10).setCellValue(pratavaari[0].trim());

			apply(fullBorder, r, 11);

			// CHILD ROW(S)
			for (int i = 1; i < n; i++) {
				Row rr = sheet.createRow(rowIdx + i);
				rr.setHeightInPoints(22);

				rr.createCell(7).setCellValue(years[i].trim());
				rr.createCell(8).setCellValue(sachoti[i].trim());
				rr.createCell(9).setCellValue(prakrutiman[i].trim());
				rr.createCell(10).setCellValue(pratavaari[i].trim());

				apply(fullBorder, rr, 11);
			}

			rowIdx += n;
		}

		// =================== FOOTER ===================
		rowIdx += 2;

		footerName = "(" + footerName + ")";
		rowIdx = merged(sheet, rowIdx, "स्थळ प्रत अ.अ.यांना मान्य असे.", leftAlign, 3);
		rowIdx++;
		rowIdx = merged(sheet, rowIdx, footerName, footerStyle, 10);
		rowIdx = merged(sheet, rowIdx, "उप अधीक्षक अभियंता", footerStyle, 10);
		rowIdx = merged(sheet, rowIdx, "पुणे पाटबंधारे प्रकल्प मंडळ", footerStyle, 10);
		rowIdx = merged(sheet, rowIdx, "पुणे.", footerStyle, 10);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		workbook.write(out);
		workbook.close();

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=agenda.xlsx");

		return ResponseEntity.ok().headers(headers).contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
				.body(new InputStreamResource(in));
	}

	private int merged(Sheet sheet, int rowIdx, String text, CellStyle style, int lastCol) {
		Row r = sheet.createRow(rowIdx);
		Cell c = r.createCell(0);
		c.setCellValue(text);
		c.setCellStyle(style);
		sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, lastCol));
		return rowIdx + 1;
	}

	private void apply(CellStyle st, Row r, int colCount) {
		for (int c = 0; c < colCount; c++) {
			Cell cl = r.getCell(c);
			if (cl == null)
				cl = r.createCell(c);
			cl.setCellStyle(st);
		}
	}

	@Override
	public AgendaSecResponse saveOrUpdateAgendaSec(AgendaSecRequest dto) {
		AgendaSecResponse response = new AgendaSecResponse();
		ApplicationError error = new ApplicationError();
		String username = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
		if (dto.getSectionFlag().equalsIgnoreCase("GATB")) {

			try {

				String year = dto.getMeta().getYear();
				String targetDate = dto.getMeta().getTargetDate();

				for (AgendaSecRow row : dto.getRows()) {

					long rowId = row.getRowId();
					String deleteFlag = Optional.ofNullable(row.getDeleteFlag()).orElse("");

					// ---------------- HARD DELETE ----------------
					if ("D".equalsIgnoreCase(deleteFlag)) {

						Long deleteId = row.getDeleteId();
						if (deleteId != null && deleteId > 0) {
							agendaSecBRepository.findByDeleteIdAndYearAndTargetDate(deleteId, year, targetDate)
									.ifPresent(agendaSecBRepository::delete);
						}
						continue;
					}

					// ---------------- SAVE / UPDATE ----------------
					Optional<AgendaSecEntityGATB> existing = agendaSecBRepository.findByRowIdAndYearAndTargetDate(rowId,
							year, targetDate);

					AgendaSecEntityGATB entity = existing.orElse(new AgendaSecEntityGATB());
					LocalDateTime now = LocalDateTime.now();

					entity.setRowId(rowId);
					entity.setYear(year);
					entity.setTargetDate(targetDate);
					entity.setColumnData(row.getColumnData());
					entity.setDeleteId(row.getDeleteId());

					if (entity.getId() == null) {
						entity.setFlag("C");
						entity.setCreatedBy(username);
						entity.setCreatedAt(now);
					} else {
						entity.setFlag("U");
					}

					entity.setUpdatedBy(username);
					entity.setUpdatedAt(now);

					agendaSecBRepository.save(entity);
				}

				error.setErrorCode("200");
				error.setErrorDescription("AgendaSec saved successfully.");
				response.setMessage("Success");

			} catch (Exception e) {

				error.setErrorCode("500");
				error.setErrorDescription("Error: " + e.getMessage());
				response.setMessage("Failed");
			}

		}

		else {

			try {

				String year = dto.getMeta().getYear();
				String targetDate = dto.getMeta().getTargetDate();

				for (AgendaSecRow row : dto.getRows()) {

					long rowId = row.getRowId();
					String deleteFlag = Optional.ofNullable(row.getDeleteFlag()).orElse("");

					// ---------------- HARD DELETE ----------------
					if ("D".equalsIgnoreCase(deleteFlag)) {

						Long deleteId = row.getDeleteId();
						if (deleteId != null && deleteId > 0) {
							agendaSecDRepository.findByDeleteIdAndYearAndTargetDate(deleteId, year, targetDate)
									.ifPresent(agendaSecDRepository::delete);
						}
						continue;
					}

					// ---------------- SAVE / UPDATE ----------------
					Optional<AgendaSecEntityGATD> existing = agendaSecDRepository.findByRowIdAndYearAndTargetDate(rowId,
							year, targetDate);

					AgendaSecEntityGATD entity = existing.orElse(new AgendaSecEntityGATD());
					LocalDateTime now = LocalDateTime.now();

					entity.setRowId(rowId);
					entity.setYear(year);
					entity.setTargetDate(targetDate);
					entity.setColumnData(row.getColumnData());
					entity.setDeleteId(row.getDeleteId());

					if (entity.getId() == null) {
						entity.setFlag("C");
						entity.setCreatedBy(username);
						entity.setCreatedAt(now);
					} else {
						entity.setFlag("U");
					}

					entity.setUpdatedBy(username);
					entity.setUpdatedAt(now);

					agendaSecDRepository.save(entity);
				}

				error.setErrorCode("200");
				error.setErrorDescription("AgendaSec saved successfully.");
				response.setMessage("Success");

			} catch (Exception e) {

				error.setErrorCode("500");
				error.setErrorDescription("Error: " + e.getMessage());
				response.setMessage("Failed");
			}

		}
		response.setErrorDetails(error);
		return response;
	}

	@Override
	public AgendaSecResponse getAgendaSecByYearAndTargetDate(String year, String targetDate, String section) {
		AgendaSecResponse response = new AgendaSecResponse();
		ApplicationError error = new ApplicationError();
		if (section.equalsIgnoreCase("GATB")) {

			try {

				List<AgendaSecEntityGATB> list = agendaSecBRepository.findByYearAndTargetDate(year, targetDate);

				list.sort(Comparator.comparingLong(AgendaSecEntityGATB::getRowId));

				response.setDataGatB(list);
				response.setMessage("Success");

				error.setErrorCode("200");
				error.setErrorDescription("Fetched successfully.");

			} catch (Exception e) {
				error.setErrorCode("500");
				error.setErrorDescription(e.getMessage());
				response.setMessage("Failed");
			}
		} else {
			try {

				List<AgendaSecEntityGATD> list = agendaSecDRepository.findByYearAndTargetDate(year, targetDate);

				list.sort(Comparator.comparingLong(AgendaSecEntityGATD::getRowId));

				response.setDataGatD(list);
				response.setMessage("Success");

				error.setErrorCode("200");
				error.setErrorDescription("Fetched successfully.");

			} catch (Exception e) {
				error.setErrorCode("500");
				error.setErrorDescription(e.getMessage());
				response.setMessage("Failed");
			}
		}

		response.setErrorDetails(error);
		return response;
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadAgendaSecExcel(String year, String targetDate, String section)
			throws Exception {

		if (section.equalsIgnoreCase("GATB")) {

			List<AgendaSecEntityGATB> list = agendaSecBRepository.findByYearAndTargetDate(year, targetDate);

			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("AgendaSec");

			// ==== FONTS ====
			Font smallFont = workbook.createFont();
			smallFont.setFontHeightInPoints((short) 10);

			Font titleFont = workbook.createFont();
			titleFont.setFontHeightInPoints((short) 11);
			titleFont.setBold(true);

			Font headerFont = workbook.createFont();
			headerFont.setFontHeightInPoints((short) 10);
			headerFont.setBold(true);

			// ==== TITLE STYLE ====
			CellStyle titleStyle = workbook.createCellStyle();
			titleStyle.setAlignment(HorizontalAlignment.CENTER);
			titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			titleStyle.setFont(titleFont);
			titleStyle.setWrapText(true);

			// ==== CELL CENTER STYLE ====
			CellStyle cellCenter = workbook.createCellStyle();
			cellCenter.setAlignment(HorizontalAlignment.CENTER);
			cellCenter.setVerticalAlignment(VerticalAlignment.CENTER);
			cellCenter.setFont(smallFont);
			cellCenter.setWrapText(true);
			cellCenter.setBorderTop(BorderStyle.THIN);
			cellCenter.setBorderBottom(BorderStyle.THIN);
			cellCenter.setBorderLeft(BorderStyle.THIN);
			cellCenter.setBorderRight(BorderStyle.THIN);

			// ==== CELL LEFT STYLE ====
			CellStyle cellLeft = workbook.createCellStyle();
			cellLeft.cloneStyleFrom(cellCenter);
			cellLeft.setAlignment(HorizontalAlignment.LEFT);

			// ==== HEADER STYLE ====
			CellStyle headerStyle = workbook.createCellStyle();
			headerStyle.cloneStyleFrom(cellCenter);
			headerStyle.setFont(headerFont);

			// ==== FOOTER STYLE (NO BORDER) ====
			CellStyle footerStyle = workbook.createCellStyle();
			footerStyle.setAlignment(HorizontalAlignment.CENTER);
			footerStyle.setVerticalAlignment(VerticalAlignment.TOP);
			footerStyle.setBorderTop(BorderStyle.NONE);
			footerStyle.setBorderBottom(BorderStyle.NONE);
			footerStyle.setBorderLeft(BorderStyle.NONE);
			footerStyle.setBorderRight(BorderStyle.NONE);
			footerStyle.setWrapText(true);
			footerStyle.setFont(headerFont);

			final int LAST_COL = 9;
			int rowIdx = 0;

			// ================== TITLES ==================
			rowIdx = mergedNoBorder(sheet, rowIdx, "परिशिष्ट-ब", titleStyle, LAST_COL);
			rowIdx = mergedNoBorder(sheet, rowIdx,
					"1 ऑगस्ट  रोजी वयाची 49/54 वर्षे पुर्ण झालेले गट-क मधील शासकीय अधिकारी/कर्मचारी", titleStyle,
					LAST_COL);

			rowIdx = mergedNoBorder(sheet, rowIdx,
					"मंडळ कार्यालयाचे नाव- अधीक्षक अभियंता, पुणे पाटबंधारे प्रकल्प मंडळ,पुणे", titleStyle, LAST_COL);

			rowIdx++;

			// ================== HEADER ROWS ==================
			Row h1 = sheet.createRow(rowIdx++);
			Row h2 = sheet.createRow(rowIdx++);
			int[] widths = { 1500, 3500, 3500, 3500, 4000, 4000, 4000, 4000, 4000, 5000 };

			String[] headers = { "अ. क्र.", "शासकीय कर्मचाऱ्यांचे पुर्ण नांव व त्याने धारण केलेले पद", "जन्म दिनांक",
					"सेवेतील ‍ नेमणुकीचा दिनांक",
					"वयाची 35 वर्ष पुर्ण होण्यापुर्वी शासकीय सेवेत (भारतातील कुठलीही शासकीय सेवा धरुन) प्रवेश केला काय?",
					"विभागीय  चौकशी / कार्यवाही चालु असल्यास त्या संबंधीचे आरोप व त्याची सद्य:स्थिती यांचा तपशील",
					"मागासवर्गीय असल्यास कोणत्या वर्गात मोडतात",
					"शारिरिक दृष्ट्या शासकीय सेवेत राहण्यास पात्र आहेत काय?", "इतर अभिप्राय असल्यास", "शेरा" };

			for (int c = 0; c <= LAST_COL; c++) {

				Cell ch = h1.createCell(c);
				ch.setCellValue(headers[c]);
				ch.setCellStyle(headerStyle);

				Cell ch2 = h2.createCell(c);
				ch2.setCellStyle(headerStyle);

				sheet.addMergedRegion(new CellRangeAddress(h1.getRowNum(), h2.getRowNum(), c, c));
				RegionUtil.setBorderTop(BorderStyle.THIN, new CellRangeAddress(h1.getRowNum(), h2.getRowNum(), c, c),
						sheet);
				RegionUtil.setBorderBottom(BorderStyle.THIN, new CellRangeAddress(h1.getRowNum(), h2.getRowNum(), c, c),
						sheet);
				RegionUtil.setBorderLeft(BorderStyle.THIN, new CellRangeAddress(h1.getRowNum(), h2.getRowNum(), c, c),
						sheet);
				RegionUtil.setBorderRight(BorderStyle.THIN, new CellRangeAddress(h1.getRowNum(), h2.getRowNum(), c, c),
						sheet);

			}
			float headerHeight = calculateDynamicHeight(h1, widths);
			h1.setHeightInPoints(Math.max(headerHeight, 25));
			h2.setHeightInPoints(5);

			// ================== COLUMN NUMBER ROW ==================
			Row colNumRow = sheet.createRow(rowIdx++);
			for (int c = 0; c <= LAST_COL; c++) {
				Cell cn = colNumRow.createCell(c);
				cn.setCellValue(c + 1);
				cn.setCellStyle(cellCenter);
			}

			// ================== FIXED COLUMN WIDTHS ==================

			for (int c = 0; c <= LAST_COL; c++)
				sheet.setColumnWidth(c, widths[c]);

			// ================== DATA ROWS ==================
			for (AgendaSecEntityGATB entity : list) {

				JsonNode cd = entity.getColumnData();

				Row r = sheet.createRow(rowIdx);

				// PRE-CREATE CELLS WITH BORDER
				for (int c = 0; c <= LAST_COL; c++) {
					Cell cl = r.createCell(c);
					cl.setCellStyle(cellCenter);
				}

				if (cd.has("kramank"))
					r.getCell(0).setCellValue(cd.get("kramank").asText());
				if (cd.has("purnNavPad"))
					r.getCell(1).setCellValue(cd.get("purnNavPad").asText());
				if (cd.has("janmaDinank"))
					r.getCell(2).setCellValue(cd.get("janmaDinank").asText());
				if (cd.has("seveNiyuktiDinank"))
					r.getCell(3).setCellValue(cd.get("seveNiyuktiDinank").asText());
				if (cd.has("vay35PurviPravesh"))
					r.getCell(4).setCellValue(cd.get("vay35PurviPravesh").asText());
				if (cd.has("vibhagiyaChaukashi"))
					r.getCell(5).setCellValue(cd.get("vibhagiyaChaukashi").asText());
				if (cd.has("magasVargiya"))
					r.getCell(6).setCellValue(cd.get("magasVargiya").asText());
				if (cd.has("shasakiyaHattavaAdhikar"))
					r.getCell(7).setCellValue(cd.get("shasakiyaHattavaAdhikar").asText());
				if (cd.has("itarAbhipray"))
					r.getCell(8).setCellValue(cd.get("itarAbhipray").asText());
				if (cd.has("shera"))
					r.getCell(9).setCellValue(cd.get("shera").asText());

				// DYNAMIC ROW HEIGHT FIX
				float maxHeight = calculateDynamicHeight(r, widths);
				r.setHeightInPoints(maxHeight);

				rowIdx++;
			}

			// ================== FOOTER ==================
			rowIdx += 2;

			Row fr = sheet.createRow(rowIdx);
			fr.setHeightInPoints(70);

			String t1 = "सदस्य\nकार्यकारी अभियंता\nभामा आसखेड धरण विभाग,\nपुणे";
			String t2 = "सदस्य\nकार्यकारी अभियंता\nपाटबंधारे प्रकल्प अन्वेषण विभाग,\n(भिमा उपखोरे) पुणे";
			String t3 = "सदस्य सचिव\nअधीक्षक\nपुणे पाटबंधारे प्रकल्प मंडळ\nपुणे";
			String t4 = "अध्यक्ष\nउपअधीक्षक अभियंता\nपुणे पाटबंधारे प्रकल्प मंडळ\nपुणे.";

			sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 2));
			sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 3, 5));
			sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 6, 7));
			sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 8, 9));

			Cell f1 = fr.createCell(0);
			f1.setCellValue(t1);
			f1.setCellStyle(footerStyle);
			Cell f2 = fr.createCell(3);
			f2.setCellValue(t2);
			f2.setCellStyle(footerStyle);
			Cell f3 = fr.createCell(6);
			f3.setCellValue(t3);
			f3.setCellStyle(footerStyle);
			Cell f4 = fr.createCell(8);
			f4.setCellValue(t4);
			f4.setCellStyle(footerStyle);

			clearMergedBorders(sheet, rowIdx, 0, 2);
			clearMergedBorders(sheet, rowIdx, 3, 5);
			clearMergedBorders(sheet, rowIdx, 6, 7);
			clearMergedBorders(sheet, rowIdx, 8, 9);

			// ================== SAVE FILE ==================
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			workbook.write(out);
			workbook.close();

			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			HttpHeaders headers1 = new HttpHeaders();
			headers1.add("Content-Disposition", "attachment; filename=agenda_sec.xlsx");

			return ResponseEntity.ok().headers(headers1)
					.contentType(MediaType
							.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
					.body(new InputStreamResource(in));
		} else {
			List<AgendaSecEntityGATD> list = agendaSecDRepository.findByYearAndTargetDate(year, targetDate);

			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("AgendaSec");

			// ==== FONTS ====
			Font smallFont = workbook.createFont();
			smallFont.setFontHeightInPoints((short) 10);

			Font titleFont = workbook.createFont();
			titleFont.setFontHeightInPoints((short) 11);
			titleFont.setBold(true);

			Font headerFont = workbook.createFont();
			headerFont.setFontHeightInPoints((short) 10);
			headerFont.setBold(true);

			// ==== TITLE STYLE ====
			CellStyle titleStyle = workbook.createCellStyle();
			titleStyle.setAlignment(HorizontalAlignment.CENTER);
			titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			titleStyle.setFont(titleFont);
			titleStyle.setWrapText(true);

			// ==== CELL CENTER STYLE ====
			CellStyle cellCenter = workbook.createCellStyle();
			cellCenter.setAlignment(HorizontalAlignment.CENTER);
			cellCenter.setVerticalAlignment(VerticalAlignment.CENTER);
			cellCenter.setFont(smallFont);
			cellCenter.setWrapText(true);
			cellCenter.setBorderTop(BorderStyle.THIN);
			cellCenter.setBorderBottom(BorderStyle.THIN);
			cellCenter.setBorderLeft(BorderStyle.THIN);
			cellCenter.setBorderRight(BorderStyle.THIN);

			// ==== CELL LEFT STYLE ====
			CellStyle cellLeft = workbook.createCellStyle();
			cellLeft.cloneStyleFrom(cellCenter);
			cellLeft.setAlignment(HorizontalAlignment.LEFT);

			// ==== HEADER STYLE ====
			CellStyle headerStyle = workbook.createCellStyle();
			headerStyle.cloneStyleFrom(cellCenter);
			headerStyle.setFont(headerFont);

			// ==== FOOTER STYLE (NO BORDER) ====
			CellStyle footerStyle = workbook.createCellStyle();
			footerStyle.setAlignment(HorizontalAlignment.CENTER);
			footerStyle.setVerticalAlignment(VerticalAlignment.TOP);
			footerStyle.setBorderTop(BorderStyle.NONE);
			footerStyle.setBorderBottom(BorderStyle.NONE);
			footerStyle.setBorderLeft(BorderStyle.NONE);
			footerStyle.setBorderRight(BorderStyle.NONE);
			footerStyle.setWrapText(true);
			footerStyle.setFont(headerFont);

			final int LAST_COL = 9;
			int rowIdx = 0;

			// ================== TITLES ==================
			rowIdx = mergedNoBorder(sheet, rowIdx, "परिशिष्ट-ब", titleStyle, LAST_COL);
			rowIdx = mergedNoBorder(sheet, rowIdx,
					"1 ऑगस्ट  रोजी वयाची 49/54 वर्षे पुर्ण झालेले गट-क मधील शासकीय अधिकारी/कर्मचारी", titleStyle,
					LAST_COL);

			rowIdx = mergedNoBorder(sheet, rowIdx,
					"मंडळ कार्यालयाचे नाव- अधीक्षक अभियंता, पुणे पाटबंधारे प्रकल्प मंडळ,पुणे", titleStyle, LAST_COL);

			rowIdx++;

			// ================== HEADER ROWS ==================
			Row h1 = sheet.createRow(rowIdx++);
			Row h2 = sheet.createRow(rowIdx++);
			int[] widths = { 1500, 3500, 3500, 3500, 4000, 4000, 4000, 4000, 4000, 5000 };

			String[] headers = { "अ. क्र.", "शासकीय कर्मचाऱ्यांचे पुर्ण नांव व त्याने धारण केलेले पद", "जन्म दिनांक",
					"सेवेतील ‍ नेमणुकीचा दिनांक",
					"वयाची 35 वर्ष पुर्ण होण्यापुर्वी शासकीय सेवेत (भारतातील कुठलीही शासकीय सेवा धरुन) प्रवेश केला काय?",
					"विभागीय  चौकशी / कार्यवाही चालु असल्यास त्या संबंधीचे आरोप व त्याची सद्य:स्थिती यांचा तपशील",
					"मागासवर्गीय असल्यास कोणत्या वर्गात मोडतात",
					"शारिरिक दृष्ट्या शासकीय सेवेत राहण्यास पात्र आहेत काय?", "इतर अभिप्राय असल्यास", "शेरा" };

			for (int c = 0; c <= LAST_COL; c++) {

				Cell ch = h1.createCell(c);
				ch.setCellValue(headers[c]);
				ch.setCellStyle(headerStyle);

				Cell ch2 = h2.createCell(c);
				ch2.setCellStyle(headerStyle);

				sheet.addMergedRegion(new CellRangeAddress(h1.getRowNum(), h2.getRowNum(), c, c));
				RegionUtil.setBorderTop(BorderStyle.THIN, new CellRangeAddress(h1.getRowNum(), h2.getRowNum(), c, c),
						sheet);
				RegionUtil.setBorderBottom(BorderStyle.THIN, new CellRangeAddress(h1.getRowNum(), h2.getRowNum(), c, c),
						sheet);
				RegionUtil.setBorderLeft(BorderStyle.THIN, new CellRangeAddress(h1.getRowNum(), h2.getRowNum(), c, c),
						sheet);
				RegionUtil.setBorderRight(BorderStyle.THIN, new CellRangeAddress(h1.getRowNum(), h2.getRowNum(), c, c),
						sheet);

			}
			float headerHeight = calculateDynamicHeight(h1, widths);
			h1.setHeightInPoints(Math.max(headerHeight, 25));
			h2.setHeightInPoints(5);

			// ================== COLUMN NUMBER ROW ==================
			Row colNumRow = sheet.createRow(rowIdx++);
			for (int c = 0; c <= LAST_COL; c++) {
				Cell cn = colNumRow.createCell(c);
				cn.setCellValue(c + 1);
				cn.setCellStyle(cellCenter);
			}

			// ================== FIXED COLUMN WIDTHS ==================

			for (int c = 0; c <= LAST_COL; c++)
				sheet.setColumnWidth(c, widths[c]);

			// ================== DATA ROWS ==================
			for (AgendaSecEntityGATD entity : list) {

				JsonNode cd = entity.getColumnData();

				Row r = sheet.createRow(rowIdx);

				// PRE-CREATE CELLS WITH BORDER
				for (int c = 0; c <= LAST_COL; c++) {
					Cell cl = r.createCell(c);
					cl.setCellStyle(cellCenter);
				}

				if (cd.has("kramank"))
					r.getCell(0).setCellValue(cd.get("kramank").asText());
				if (cd.has("purnNavPad"))
					r.getCell(1).setCellValue(cd.get("purnNavPad").asText());
				if (cd.has("janmaDinank"))
					r.getCell(2).setCellValue(cd.get("janmaDinank").asText());
				if (cd.has("seveNiyuktiDinank"))
					r.getCell(3).setCellValue(cd.get("seveNiyuktiDinank").asText());
				if (cd.has("vay35PurviPravesh"))
					r.getCell(4).setCellValue(cd.get("vay35PurviPravesh").asText());
				if (cd.has("vibhagiyaChaukashi"))
					r.getCell(5).setCellValue(cd.get("vibhagiyaChaukashi").asText());
				if (cd.has("magasVargiya"))
					r.getCell(6).setCellValue(cd.get("magasVargiya").asText());
				if (cd.has("shasakiyaHattavaAdhikar"))
					r.getCell(7).setCellValue(cd.get("shasakiyaHattavaAdhikar").asText());
				if (cd.has("itarAbhipray"))
					r.getCell(8).setCellValue(cd.get("itarAbhipray").asText());
				if (cd.has("shera"))
					r.getCell(9).setCellValue(cd.get("shera").asText());

				// DYNAMIC ROW HEIGHT FIX
				float maxHeight = calculateDynamicHeight(r, widths);
				r.setHeightInPoints(maxHeight);

				rowIdx++;
			}

			// ================== FOOTER ==================
			rowIdx += 2;

			Row fr = sheet.createRow(rowIdx);
			fr.setHeightInPoints(70);

			String t1 = "सदस्य\nकार्यकारी अभियंता\nभामा आसखेड धरण विभाग,\nपुणे";
			String t2 = "सदस्य\nकार्यकारी अभियंता\nपाटबंधारे प्रकल्प अन्वेषण विभाग,\n(भिमा उपखोरे) पुणे";
			String t3 = "सदस्य सचिव\nअधीक्षक\nपुणे पाटबंधारे प्रकल्प मंडळ\nपुणे";
			String t4 = "अध्यक्ष\nउपअधीक्षक अभियंता\nपुणे पाटबंधारे प्रकल्प मंडळ\nपुणे.";

			sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 2));
			sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 3, 5));
			sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 6, 7));
			sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 8, 9));

			Cell f1 = fr.createCell(0);
			f1.setCellValue(t1);
			f1.setCellStyle(footerStyle);
			Cell f2 = fr.createCell(3);
			f2.setCellValue(t2);
			f2.setCellStyle(footerStyle);
			Cell f3 = fr.createCell(6);
			f3.setCellValue(t3);
			f3.setCellStyle(footerStyle);
			Cell f4 = fr.createCell(8);
			f4.setCellValue(t4);
			f4.setCellStyle(footerStyle);

			clearMergedBorders(sheet, rowIdx, 0, 2);
			clearMergedBorders(sheet, rowIdx, 3, 5);
			clearMergedBorders(sheet, rowIdx, 6, 7);
			clearMergedBorders(sheet, rowIdx, 8, 9);

			// ================== SAVE FILE ==================
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			workbook.write(out);
			workbook.close();

			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			HttpHeaders headers1 = new HttpHeaders();
			headers1.add("Content-Disposition", "attachment; filename=agenda_sec.xlsx");

			return ResponseEntity.ok().headers(headers1)
					.contentType(MediaType
							.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
					.body(new InputStreamResource(in));
		}
	}

	// ---------------- HELPERS ----------------
	private int mergedNoBorder(Sheet sheet, int rowIdx, String text, CellStyle style, int lastCol) {
		Row r = sheet.createRow(rowIdx);
		Cell c = r.createCell(0);
		c.setCellValue(text);
		c.setCellStyle(style);
		sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, lastCol));
		return rowIdx + 1;
	}

	private void clearMergedBorders(Sheet sheet, int row, int startCol, int endCol) {
		CellRangeAddress range = new CellRangeAddress(row, row, startCol, endCol);
		RegionUtil.setBorderTop(BorderStyle.NONE, range, sheet);
		RegionUtil.setBorderBottom(BorderStyle.NONE, range, sheet);
		RegionUtil.setBorderLeft(BorderStyle.NONE, range, sheet);
		RegionUtil.setBorderRight(BorderStyle.NONE, range, sheet);
	}

	private float calculateDynamicHeight(Row row, int[] columnWidths) {
		float maxHeight = 22f; // default

		for (int c = 0; c < row.getLastCellNum(); c++) {
			Cell cell = row.getCell(c);
			if (cell == null || cell.getCellType() != CellType.STRING)
				continue;

			String text = cell.getStringCellValue();
			int colWidthPx = columnWidths[c] / 40; // rough width

			int approxCharsPerLine = Math.max(1, colWidthPx / 7);
			int lines = (int) Math.ceil((double) text.length() / approxCharsPerLine);

			float height = Math.max(22, lines * 15);
			maxHeight = Math.max(maxHeight, height);
		}
		return maxHeight;
	}

	@Override
	public ThirteenResponse saveOrUpdateAnukampa(ThirteenRequest dto) {
		ThirteenResponse response = new ThirteenResponse();
		ApplicationError error = new ApplicationError();

		try {
			String username = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
			String year = dto.getMeta().getYear();
			String targetDate = dto.getMeta().getTargetDate();

			for (ThirteenRow row : dto.getRows()) {

				long rowId = row.getRowId();
				String deleteFlag = Optional.ofNullable(row.getDeleteFlag()).orElse("");

				// -------------- HARD DELETE ---------------
				if ("D".equalsIgnoreCase(deleteFlag)) {

					if (row.getDeleteId() != null) {
						agendaThirteenRepository.findByDeleteIdAndYearAndTargetDate(row.getDeleteId(), year, targetDate)
								.ifPresent(agendaThirteenRepository::delete);
					}

					continue;
				}

				// -------------- CREATE / UPDATE -----------
				Optional<AgendaThirteenEntity> existing = agendaThirteenRepository
						.findByRowIdAndYearAndTargetDate(rowId, year, targetDate);

				AgendaThirteenEntity entity = existing.orElse(new AgendaThirteenEntity());
				LocalDateTime now = LocalDateTime.now();

				entity.setRowId(rowId);
				entity.setYear(year);
				entity.setTargetDate(targetDate);
				entity.setColumnData(row.getColumnData());
				entity.setDeleteId(row.getDeleteId());

				if (entity.getId() == null) {
					entity.setFlag("C");
					entity.setCreatedBy(username);
					entity.setCreatedAt(now);
				} else {
					entity.setFlag("U");
				}

				entity.setUpdatedBy(username);
				entity.setUpdatedAt(now);

				agendaThirteenRepository.save(entity);
			}

			response.setMessage("Success");
			error.setErrorCode("200");
			error.setErrorDescription("Saved successfully");

		} catch (Exception e) {
			error.setErrorCode("500");
			error.setErrorDescription(e.getMessage());
			response.setMessage("Failed");
		}

		response.setErrorDetails(error);
		return response;

	}

	@Override
	public ThirteenResponse getAnukampaData(String year, String targetDate) {
		ThirteenResponse res = new ThirteenResponse();
		ApplicationError err = new ApplicationError();

		try {
			List<AgendaThirteenEntity> list = agendaThirteenRepository.findByYearAndTargetDate(year, targetDate)
					.stream().sorted(Comparator.comparingLong(AgendaThirteenEntity::getRowId)).toList();

			res.setData(list);
			res.setMessage("Success");

			err.setErrorCode("200");
			err.setErrorDescription("Fetched successfully");

		} catch (Exception e) {
			err.setErrorCode("500");
			err.setErrorDescription(e.getMessage());
			res.setMessage("Failed");
		}

		res.setErrorDetails(err);
		return res;
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadAnukampaExcel(String year, String targetDate) throws Exception {

		List<AgendaThirteenEntity> list = agendaThirteenRepository.findByYearAndTargetDate(year, targetDate);

		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("Anukampa");
		sheet.setZoom(70);

		// ----------------------------------------------------------------
		// FONT & STYLES
		// ----------------------------------------------------------------
		XSSFFont boldFont = workbook.createFont();
		boldFont.setBold(true);
		boldFont.setFontHeight(11);

		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setWrapText(true);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);
		headerStyle.setFont(boldFont);

		CellStyle centerStyle = workbook.createCellStyle();
		centerStyle.setWrapText(true);
		centerStyle.setAlignment(HorizontalAlignment.CENTER);
		centerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		centerStyle.setBorderBottom(BorderStyle.THIN);
		centerStyle.setBorderTop(BorderStyle.THIN);
		centerStyle.setBorderLeft(BorderStyle.THIN);
		centerStyle.setBorderRight(BorderStyle.THIN);

		CellStyle titleStyle = workbook.createCellStyle();
		XSSFFont titleFont = workbook.createFont();
		titleFont.setBold(true);
		titleFont.setFontHeight(16);
		titleStyle.setFont(titleFont);
		titleStyle.setAlignment(HorizontalAlignment.CENTER);
		titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

		// ----------------------------------------------------------------
		// TITLE ROW
		// ----------------------------------------------------------------
		Row titleRow = sheet.createRow(0);
		Cell titleCell = titleRow.createCell(0);
		titleCell.setCellValue("अधीक्षक अभियंता,पुणे पाटबंधारे प्रकल्प मंडळ,पुणे");
		titleCell.setCellStyle(titleStyle);

		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 12));

		// ----------------------------------------------------------------
		// MAIN HEADER ROW (ROW 1)
		// ----------------------------------------------------------------
		Row mainHeader = sheet.createRow(1);
		mainHeader.setHeight((short) 900);

		String[] mainHeaders = { "अ.क्र.", "अनुकंपा सामाईक जेष्ठता क्रमांक", "कार्यालयाचे नांव",
				"दिवंगत कर्मचा-यांचे नांव", "दिवंगत झालेला दिनांक",
				"अनुकंपा कारणास्तव नोकरीसाठी अर्ज केलेल्या नातेवाईकाचे नाव", "नोकरीसाठी अर्ज केल्याचा दिनांक",
				"दिवंगत कर्मचारी यांचेशी नाते", "जन्मतारीख", "शैक्षणिक अर्हता", "जातीचा प्रवर्ग", "शेरा" // merged
																											// header
																											// for 11 &
																											// 12
		};

		for (int i = 0; i < mainHeaders.length; i++) {
			Cell c = mainHeader.createCell(i);
			c.setCellValue(mainHeaders[i]);
			c.setCellStyle(headerStyle);
		}

		// IMPORTANT: Create cell(12) because loop created only 0–11
		Cell sheraExtra = mainHeader.createCell(12);
		sheraExtra.setCellStyle(headerStyle);

		// APPLY BORDER STYLE to both merged shera cells
		Cell sheraH1 = mainHeader.getCell(11);
		Cell sheraH2 = mainHeader.getCell(12);
		sheraH1.setCellStyle(headerStyle);
		sheraH2.setCellStyle(headerStyle);

		// MERGE SHERA HEADER
		sheet.addMergedRegion(new CellRangeAddress(1, 1, 11, 12));

		// ----------------------------------------------------------------
		// SUB HEADER ROW (ROW 2)
		// ----------------------------------------------------------------
		Row subHeader = sheet.createRow(2);
		subHeader.setHeight((short) 900);

		// Merge vertically rows 1–2 for col 0 to col 10
		for (int col = 0; col <= 10; col++) {
			sheet.addMergedRegion(new CellRangeAddress(1, 2, col, col));
			Cell mergedCell = subHeader.createCell(col);
			mergedCell.setCellStyle(headerStyle);
		}

		// Create shera sub-columns
		Cell sh1 = subHeader.createCell(11);
		sh1.setCellValue("नियुक्ती दिली असल्यास");
		sh1.setCellStyle(headerStyle);

		Cell sh2 = subHeader.createCell(12);
		sh2.setCellValue("नियुक्ती आदेश दिनांक");
		sh2.setCellStyle(headerStyle);

		// ----------------------------------------------------------------
		// COLUMN WIDTHS
		// ----------------------------------------------------------------
		int[] columnWidths = { 2000, 5000, 6000, 8000, 5000, 9000, 5000, 6000, 5000, 5000, 4000, 5000, 6000 };
		for (int i = 0; i < columnWidths.length; i++) {
			sheet.setColumnWidth(i, columnWidths[i]);
		}

		// ----------------------------------------------------------------
		// NUMBERING ROW (ROW 3)
		// ----------------------------------------------------------------
		Row numberingRow = sheet.createRow(3);
		for (int i = 0; i < 13; i++) {
			Cell c = numberingRow.createCell(i);
			c.setCellValue(i + 1);
			c.setCellStyle(centerStyle);
		}

		// ----------------------------------------------------------------
		// DATA ROWS START AT ROW 4
		// ----------------------------------------------------------------
		int rowIndex = 4;

		for (AgendaThirteenEntity entity : list) {
			JsonNode json = entity.getColumnData();
			Row row = sheet.createRow(rowIndex++);

			fill(row, 0, json.get("kramank"), centerStyle);
			fill(row, 1, json.get("anukramank"), centerStyle);
			fill(row, 2, json.get("karyalayacheNav"), centerStyle);
			fill(row, 3, json.get("divangatKarmachariNav"), centerStyle);
			fill(row, 4, json.get("divangatDinank"), centerStyle);
			fill(row, 5, json.get("navteekKaranyasathiNoKS"), centerStyle);
			fill(row, 6, json.get("narkhasathiArjiDinank"), centerStyle);
			fill(row, 7, json.get("divangatKarmachariYogyaNate"), centerStyle);
			fill(row, 8, json.get("janmTarikh"), centerStyle);
			fill(row, 9, json.get("shaikshanikArhata"), centerStyle);
			fill(row, 10, json.get("jatPramanpatra"), centerStyle);

			if (json.has("shera")) {
				fill(row, 11, json.get("shera").get("nirNay"), centerStyle);
				fill(row, 12, json.get("shera").get("nikalDinank"), centerStyle);
			}
		}

		// ----------------------------------------------------------------
		// EXPORT FILE
		// ----------------------------------------------------------------
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		workbook.write(out);
		workbook.close();

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=Anukampa.xlsx");

		return ResponseEntity.ok().headers(headers)
				.contentType(
						MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(new InputStreamResource(in));
	}

	// ----------------------------------------------------------------
	// HELPER
	// ----------------------------------------------------------------
	private void fill(Row row, int col, JsonNode node, CellStyle style) {
		Cell cell = row.createCell(col);
		cell.setCellStyle(style);
		cell.setCellValue(node != null ? node.asText() : "");
	}

	@Override
	public AppealResponse saveOrUpdateAppeal2(AppealWrapper2 request) {

		AppealResponse response = new AppealResponse();
		ApplicationError error = new ApplicationError();
		response.setData(new ArrayList<>());

		try {

			String username = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
			LocalDateTime now = LocalDateTime.now();

			for (AppealRequest2 dto : request.getAppealData()) {

				// -----------------------------
				// FIND RECORD USING deleteId + year/date
				// -----------------------------
				Optional<AppealRequestEntity> existingOpt = Optional.empty();

				if (dto.getDeleteId() != null && dto.getDate() != null) {
					existingOpt = appealRequestRepository.findByDeleteIdAndDate(dto.getDeleteId(), dto.getDate());
				}

				String flag = Optional.ofNullable(dto.getFlag()).orElse("C").toUpperCase();

				// -----------------------------
				// DELETE LOGIC BY deleteId + date/year
				// -----------------------------
				if (flag.equals("D")) {

					if (existingOpt.isPresent()) {
						appealRequestRepository.delete(existingOpt.get());

						Map<String, Object> delMap = new LinkedHashMap<>();
						delMap.put("deleteId", dto.getDeleteId());
						delMap.put("date", dto.getDate());
						delMap.put("status", "DELETED");
						response.getData().add(delMap);
					}
					continue;
				}

				// -----------------------------
				// CREATE / UPDATE LOGIC
				// -----------------------------
				// CREATE / UPDATE LOGIC
				AppealRequestEntity entity = existingOpt.orElse(new AppealRequestEntity());
				boolean isUpdate = existingOpt.isPresent();

				entity.setDeleteId(dto.getDeleteId());
				entity.setYear(dto.getYear());
				entity.setRowId(dto.getRowId());
				entity.setDate(dto.getDate());

				if (isUpdate) {
					entity.setFlag("U");
					entity.setUpdatedBy(username);
					entity.setUpdatedDate(now);
				} else {
					entity.setFlag("C");
					entity.setCreatedBy(username);
					entity.setCreatedDate(now);
					entity.setUpdatedBy(username);
					entity.setUpdatedDate(now);
				}

				// SET ALL FIELDS
				entity.setArjachaNondaniKramank(dto.getArjachaNondaniKramank());
				entity.setArjdarNavPatta(dto.getArjdarNavPatta());
				entity.setArjPraptDinank(dto.getArjPraptDinank());
				entity.setDarikhvReshBabli(dto.getDarikhvReshBabli());
				entity.setMahitiChaPrakar(dto.getMahitiChaPrakar());
				entity.setMaangItlelyaMahitichiRuprekha(dto.getMaangItlelyaMahitichiRuprekha());
				entity.setYogyMahitiAaheKa(dto.getYogyMahitiAaheKa());
				entity.setArjShulk(dto.getArjShulk());
				entity.setShulkPramanitDinank(dto.getShulkPramanitDinank());
				entity.setMahitiDilyaDinank(dto.getMahitiDilyaDinank());
				entity.setMahitiNghitAaheKa(dto.getMahitiNghitAaheKa());
				entity.setKonKshaAwarNokri(dto.getKonKshaAwarNokri());
				entity.setPrathamAppeal(dto.getPrathamAppeal());
				entity.setShera(dto.getShera());

				entity.setDynamicColumns(dto.getDynamicColumns());

				AppealRequestEntity saved = appealRequestRepository.save(entity);

				Map<String, Object> map = new LinkedHashMap<>();
				map.put("deleteId", saved.getDeleteId());
				map.put("year", saved.getYear());
				map.put("flag", saved.getFlag());
				map.put("status", isUpdate ? "UPDATED" : "CREATED");
				response.getData().add(map);
			}

			error.setErrorCode("200");
			error.setErrorDescription("Success");
			response.setErrorDetails(error);

		} catch (Exception e) {
			error.setErrorCode("500");
			error.setErrorDescription(e.getMessage());
			response.setErrorDetails(error);
		}

		return response;
	}

	@Override
	public AppealResponse getAppealData2(String year) {
		AppealResponse response = new AppealResponse();
		ApplicationError error = new ApplicationError();
		response.setData(new ArrayList<>());

		try {
			List<AppealRequestEntity> appealList;

			// 🎯 Filter by year OR fetch all
			if (year != null && !year.isBlank()) {
				appealList = appealRequestRepository.findByYear(year);
			} else {
				appealList = appealRequestRepository.findAll();
			}

			for (AppealRequestEntity e : appealList) {

				Map<String, Object> map = new LinkedHashMap<>();

				map.put("id", e.getId());
				map.put("rowId", e.getRowId());
				map.put("year", e.getYear());
				map.put("date", e.getDate());
				map.put("deleteId", e.getDeleteId());
				map.put("flag", e.getFlag());

				// ✅ NEW FIELD NAMES
				map.put("arjachaNondaniKramank", e.getArjachaNondaniKramank());
				map.put("arjdarNavPatta", e.getArjdarNavPatta());
				map.put("arjPraptDinank", e.getArjPraptDinank());
				map.put("darikhvReshBabli", e.getDarikhvReshBabli());
				map.put("mahitiChaPrakar", e.getMahitiChaPrakar());
				map.put("maangItlelyaMahitichiRuprekha", e.getMaangItlelyaMahitichiRuprekha());
				map.put("yogyMahitiAaheKa", e.getYogyMahitiAaheKa());
				map.put("arjShulk", e.getArjShulk());
				map.put("shulkPramanitDinank", e.getShulkPramanitDinank());
				map.put("mahitiDilyaDinank", e.getMahitiDilyaDinank());
				map.put("mahitiNghitAaheKa", e.getMahitiNghitAaheKa());
				map.put("konKshaAwarNokri", e.getKonKshaAwarNokri());
				map.put("prathamAppeal", e.getPrathamAppeal());
				map.put("shera", e.getShera());

				// 🧩 Dynamic Columns
				if (e.getDynamicColumns() != null && !e.getDynamicColumns().isEmpty()) {
					Map<String, Object> dynMap = objectMapper.convertValue(e.getDynamicColumns(), Map.class);
					map.put("dynamicColumns", dynMap);
				} else {
					map.put("dynamicColumns", new LinkedHashMap<>());
				}

				// Audit
				map.put("createdBy", e.getCreatedBy());
				map.put("createdDate", e.getCreatedDate());
				map.put("updatedBy", e.getUpdatedBy());
				map.put("updatedDate", e.getUpdatedDate());

				response.getData().add(map);
			}

			// SORT by rowId
			response.getData().sort(Comparator.comparing(m -> Integer.parseInt(m.get("rowId").toString())));

			// Meta Info (No Pagination)
			Map<String, Object> meta = new LinkedHashMap<>();
			meta.put("totalRecords", response.getData().size());
			meta.put("filterYear", year);
			response.setMeta(meta);

			response.setMessage("Appeal register data fetched successfully.");
			error.setErrorCode("200");
			error.setErrorDescription("Success");

		} catch (Exception ex) {
			response.setMessage("Error while fetching appeal data.");
			error.setErrorCode("500");
			error.setErrorDescription(ex.getMessage());
		}

		response.setErrorDetails(error);
		return response;
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadAppealArj2(String year) throws IOException {
		List<AppealRequestEntity> list = (year != null && !year.isBlank()) ? appealRequestRepository.findByYear(year)
				: appealRequestRepository.findAll();

		list.sort(Comparator.comparing(AppealRequestEntity::getRowId));

		XSSFWorkbook wb = new XSSFWorkbook();

		XSSFSheet sh = wb.createSheet("Appeal Register");
		sh.setZoom(80);

		// ---------- COLUMN WIDTH ----------
		int[] colWidths = { 2000, 4500, 4500, 4500, 4500, 4500, 5500, 4500, 3500, 4500, 4500, 4000, 4000, 4000, 4500 };
		for (int i = 0; i < colWidths.length; i++)
			sh.setColumnWidth(i, colWidths[i]);

		// ---------- FONTS ----------
		XSSFFont marathiFont = wb.createFont();
		marathiFont.setFontName("Mangal");
		marathiFont.setFontHeightInPoints((short) 12);

		XSSFFont headerFont = wb.createFont();
		headerFont.setFontName("Mangal");
		headerFont.setFontHeightInPoints((short) 12);
		headerFont.setBold(true);

		// ---------- BORDER STYLE ----------
		CellStyle baseBorder = wb.createCellStyle();
		baseBorder.setBorderBottom(BorderStyle.MEDIUM);
		baseBorder.setBorderTop(BorderStyle.MEDIUM);
		baseBorder.setBorderLeft(BorderStyle.MEDIUM);
		baseBorder.setBorderRight(BorderStyle.MEDIUM);
		baseBorder.setAlignment(HorizontalAlignment.CENTER);
		baseBorder.setVerticalAlignment(VerticalAlignment.CENTER);
		baseBorder.setWrapText(true);

		CellStyle headerStyle = wb.createCellStyle();
		headerStyle.cloneStyleFrom(baseBorder);
		headerStyle.setFont(headerFont);

		CellStyle dataStyle = wb.createCellStyle();
		dataStyle.cloneStyleFrom(baseBorder);
		dataStyle.setFont(marathiFont);

		CellStyle numberStyle = wb.createCellStyle();
		numberStyle.cloneStyleFrom(baseBorder);
		numberStyle.setFont(marathiFont);

		// ---------- TITLE ----------
		sh.createRow(0);

		Row titleRow = sh.createRow(1);
		Cell titleCell = titleRow.createCell(0);
		titleCell.setCellValue("माहिती अधिकारांतर्गत  मंडळ कार्यालयास प्राप्त झालेले अर्ज");
		titleCell.setCellStyle(headerStyle);
		sh.addMergedRegion(new CellRangeAddress(1, 1, 0, 14));

		// ---------- HEADERS ----------
		String[] headers = { "अ.क्र.", "अर्जाचा नोंदणी क्रमांक", "अर्जदाराचे नाव व पत्ता",
				"अर्ज प्राप्त झाल्याचा दिनांक", "अर्जदार दारिद्रय रेषेखालील आहे काय", "माहितीचा थोडक्यात तपशील",
				"मागितलेल्या माहितीचे स्वरुप", "त्रयस्थ पक्षाची माहिती आहे काय?", "अर्जाचे शुल्क रु.",
				"माहिती शुल्क कळविल्याचा दिनांक", "माहिती दिल्याचा दिनांक", "माहिती नाकारली आहे का",
				"कोणत्या कलमा आधारे नाकारली", "प्रथम अपील झाले आहे काय?", "शेरा" };

		Row headerRow = sh.createRow(2);
		headerRow.setHeightInPoints(40);

		for (int i = 0; i < headers.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(headers[i]);
			cell.setCellStyle(headerStyle);
		}

		// ---------- COLUMN NUMBERS ----------
		Row numRow = sh.createRow(3);
		for (int i = 0; i < headers.length; i++) {
			Cell num = numRow.createCell(i);
			num.setCellValue(i + 1);
			num.setCellStyle(numberStyle);
		}

		// ---------- DATA ----------
		int rowNum = 4;
		int sr = 1;

		for (AppealRequestEntity e : list) {

			Row r = sh.createRow(rowNum++);

			int c = 0;

			addCell(r, c++, String.valueOf(sr++), dataStyle);
			addCell(r, c++, nvl(e.getArjachaNondaniKramank()), dataStyle);
			addCell(r, c++, nvl(e.getArjdarNavPatta()), dataStyle);
			addCell(r, c++, nvl(e.getArjPraptDinank()), dataStyle);
			addCell(r, c++, nvl(e.getDarikhvReshBabli()), dataStyle);
			addCell(r, c++, nvl(e.getMahitiChaPrakar()), dataStyle);
			addCell(r, c++, nvl(e.getMaangItlelyaMahitichiRuprekha()), dataStyle);
			addCell(r, c++, nvl(e.getYogyMahitiAaheKa()), dataStyle);
			addCell(r, c++, nvl(e.getArjShulk()), dataStyle);
			addCell(r, c++, nvl(e.getShulkPramanitDinank()), dataStyle);
			addCell(r, c++, nvl(e.getMahitiDilyaDinank()), dataStyle);
			addCell(r, c++, nvl(e.getMahitiNghitAaheKa()), dataStyle);
			addCell(r, c++, nvl(e.getKonKshaAwarNokri()), dataStyle);
			addCell(r, c++, nvl(e.getPrathamAppeal()), dataStyle);
			addCell(r, c++, nvl(e.getShera()), dataStyle);
		}

		// WRITE STREAM
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		wb.write(out);
		wb.close();

		HttpHeaders hh = new HttpHeaders();
		hh.add("Content-Disposition", "attachment; filename=Appeal_Register.xlsx");

		return ResponseEntity.ok().headers(hh)
				.contentType(
						MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())));
	}

	@Transactional
	public PraptraMasterDataResponse saveMasterData(PraptraMasterDataRequest request) {
		String username = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");

		PraptraMasterDataResponse response = new PraptraMasterDataResponse();

		try {
			for (PraptraMasterDataRowRequest row : request.getRows()) {

				// 🔴 DELETE
				if ("D".equalsIgnoreCase(row.getFlag())) {

					if (row.getDeleteId() == null) {
						throw new RuntimeException("deleteId is mandatory");
					}

					masterDataRepository.deleteByYearAndDeleteId(request.getYear(), row.getDeleteId());
					continue;
				}

				// 🔍 UPDATE / INSERT
				Optional<MasterDataEntity> existing = masterDataRepository.findByYearAndRowId(request.getYear(),
						row.getRowId());

				if (existing.isPresent()) {
					MasterDataEntity e = existing.get();
					e.setData(row.getData());
					e.setFlag("U");
					e.setUpdatedAt(LocalDateTime.now());
					e.setUpdatedBy(username);
					masterDataRepository.save(e);
				} else {
					MasterDataEntity e = new MasterDataEntity();
					e.setYear(request.getYear());
					e.setRowId(row.getRowId());
					e.setDeleteId(row.getDeleteId());
					e.setData(row.getData());
					e.setFlag("C");
					e.setCreatedAt(LocalDateTime.now());
					e.setUpdatedAt(LocalDateTime.now());
					e.setUpdatedBy(username);
					e.setCreatedBy(username);
					masterDataRepository.save(e);
				}
			}

			response.setMessage("Master Data saved successfully");
			response.setData(List.of());
			response.setErrorDetails(new ApplicationError("200", "Success"));
			return response;

		} catch (Exception e) {
			response.setMessage("Failed to save Master Data");
			response.setData(null);
			response.setErrorDetails(new ApplicationError("500", e.getMessage()));
			return response;
		}
	}

	@Override
	public PraptraMasterDataResponse getMasterData(String year) {
		PraptraMasterDataResponse response = new PraptraMasterDataResponse();

		List<Map<String, Object>> list = new ArrayList<>();

		for (MasterDataEntity e : masterDataRepository.findAllByYearOrderByRowId(year)) {
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("rowId", e.getRowId());
			m.put("deleteId", e.getDeleteId());
			m.put("data", e.getData());
			list.add(m);
		}

		response.setMessage("Master Data fetched");
		response.setData(list);
		response.setErrorDetails(new ApplicationError("200", "Success"));
		return response;
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadMasterData(String year) throws IOException {

		List<MasterDataEntity> list = masterDataRepository.findAllByYearOrderByRowId(year);

		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = wb.createSheet("विवरणपत्र");
		sheet.setZoom(70);

		// ======================= FONT =======================
		XSSFFont boldFont = wb.createFont();
		boldFont.setBold(true);

		// ======================= STYLES =====================
		XSSFCellStyle titleStyle = wb.createCellStyle();
		titleStyle.setFont(boldFont);
		titleStyle.setAlignment(HorizontalAlignment.CENTER);
		titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

		XSSFCellStyle centerStyle = wb.createCellStyle();
		centerStyle.setAlignment(HorizontalAlignment.CENTER);
		centerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

		XSSFCellStyle headerTop = wb.createCellStyle();
		headerTop.setFont(boldFont);
		headerTop.setWrapText(true);
		headerTop.setAlignment(HorizontalAlignment.CENTER);
		headerTop.setVerticalAlignment(VerticalAlignment.CENTER);
		headerTop.setBorderTop(BorderStyle.THIN);
		headerTop.setBorderLeft(BorderStyle.THIN);
		headerTop.setBorderRight(BorderStyle.THIN);

		XSSFCellStyle headerBottom = wb.createCellStyle();
		headerBottom.cloneStyleFrom(headerTop);
		headerBottom.setBorderBottom(BorderStyle.THIN);

		XSSFCellStyle headerFull = wb.createCellStyle();
		headerFull.cloneStyleFrom(headerBottom);
		headerFull.setBorderTop(BorderStyle.THIN);

		XSSFCellStyle dataStyle = wb.createCellStyle();
		dataStyle.setWrapText(true);
		dataStyle.setAlignment(HorizontalAlignment.CENTER);
		dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		dataStyle.setBorderTop(BorderStyle.THIN);
		dataStyle.setBorderBottom(BorderStyle.THIN);
		dataStyle.setBorderLeft(BorderStyle.THIN);
		dataStyle.setBorderRight(BorderStyle.THIN);

		// ======================= BORDER HELPER ======================
		BiConsumer<CellRangeAddress, CellStyle> applyBorder = (region, style) -> {
			for (int r = region.getFirstRow(); r <= region.getLastRow(); r++) {
				Row row = sheet.getRow(r);
				if (row == null)
					row = sheet.createRow(r);
				for (int c = region.getFirstColumn(); c <= region.getLastColumn(); c++) {
					Cell cell = row.getCell(c);
					if (cell == null)
						cell = row.createCell(c);
					cell.setCellStyle(style);
				}
			}
		};

		// ======================= ROW 1–2 ======================
		Row r1 = sheet.createRow(0);
		r1.createCell(0).setCellValue("पुणे पाटबंधारे प्रकल्प मंडळ, पुणे 01");
		r1.getCell(0).setCellStyle(titleStyle);

		CellRangeAddress regTitle = new CellRangeAddress(0, 1, 0, 27);
		sheet.addMergedRegion(regTitle);
		applyBorder.accept(regTitle, titleStyle);

		// ======================= ROW 3 ======================
		Row r3 = sheet.createRow(2);
		r3.createCell(0).setCellValue("विवरणपत्र");
		r3.getCell(0).setCellStyle(centerStyle);

		CellRangeAddress regSubTitle = new CellRangeAddress(2, 2, 0, 27);
		sheet.addMergedRegion(regSubTitle);
		applyBorder.accept(regSubTitle, centerStyle);

		// ======================= ROW 4 ======================
		Row r4 = sheet.createRow(3);

		// LEFT aligned style
		XSSFCellStyle leftStyle = wb.createCellStyle();
		leftStyle.setAlignment(HorizontalAlignment.LEFT);
		leftStyle.setVerticalAlignment(VerticalAlignment.CENTER);

		r4.createCell(1).setCellValue(
				"विभागाचे/उपविभागाचे नाव - कार्यकारी अभियंता, पाटबंधारे प्रकल्प अन्वेष्ण विभाग (भिमा उपखोरे) पुणे 11");
		r4.getCell(1).setCellStyle(leftStyle);

		// 🔥 Merge from B to AC (column 1 to 27)
		CellRangeAddress regDept = new CellRangeAddress(3, 3, 1, 27);
		sheet.addMergedRegion(regDept);
		applyBorder.accept(regDept, leftStyle);

		// ======================= HEADER ROWS ======================
		Row h1 = sheet.createRow(5);
		h1.setHeightInPoints(90);

		Row h2 = sheet.createRow(6);
		h2.setHeightInPoints(60);

		String[] mainHeaders = { "अ. क्र.", "कर्मचाऱ्‍याचे/अधिकाऱ्याचे  नाव", "पदनाम",
				"सेवेत नियुक्ती सरळसेवा/दिव्यांग/खेळाडू /प्रकल्पग्रस्त/अनुकंपा", "जन्मदिनांक", "सेवानिवृत्ती दिनांक",
				"जातीचा प्रवर्ग (खुला/इमाव/अजा/अज) व जात वैधता झाली आहे काय",
				"सेवेत मूळ नेमणुकीचा दिनांक व मूळ नियुक्तीचे पदनाम (RT/CRT नमूद करावे)",
				"सध्याचे पदावर कार्यरत असलेला दिनांक व पदनाम",
				"कर्मचाऱ्‍याचे/अधिकाऱ्यांची मूळ नियुक्तीपासून बदली झाली असल्यास त्याचा तपशील", "", "कार्यालयाचे नाव",
				"स्थायीत्व प्रमाणपत्र", "खात्याची परीक्षा उत्तीर्ण आहे किंवा कसे?",
				"हिंदी/मराठी भाषा परीक्षा उत्तीर्ण/सूट आहे काय?", "",
				"विभागीय चौकशी प्रस्तावित / चालू असल्यास त्याचा तपशिल", "यापूर्वी नियमित पदोन्नती नाकारली किंवा कसे?",
				"संगणक परिक्षा उत्तीर्ण", "50/55 पुनर्विलोकन झाले आहे किंवा कसे ?",
				"मत्ता व दायित्व सादर केले किंवा कसे ?",
				"सेवांतर्गत आश्वासित प्रगती योजनेचा पहिला लाभ अनुज्ञेय केलेचा दिनांक व पद",
				"सुधारित सेवांतर्गत आश्वासित प्रगती योजनेचा दुसरा लाभ अनुज्ञेय केलेचा दिनांक व पदनाम",
				"सेवांतर्गत आश्वासित प्रगती योजनेंतर्गत पहिला /दुसरा लाभ मंजूरीनंतर पदोन्नती मिळाली असल्यास पदोन्नतीचे पदनाम व दिनांक",
				"सुधारित सेवांतर्गत आश्वासित प्रगती योजना /  तीन लाभांची सुधारित सेवांतर्गत आश्वासित प्रगती योजनेचा लाभास पात्र होत असल्याचा दिनांक",
				"DCPS ची नोंद", "वेतन पडताळणी झाली आहे का ? असल्यास दिनांक", "शेरा" };

		for (int i = 0; i < mainHeaders.length; i++) {
			Cell cell = h1.createCell(i);
			cell.setCellValue(mainHeaders[i]);
			cell.setCellStyle(headerTop);

			if (i == 9 || i == 10 || i == 14 || i == 15)
				continue;

			CellRangeAddress region = new CellRangeAddress(5, 6, i, i);
			sheet.addMergedRegion(region);
			applyBorder.accept(region, headerTop);
		}

		// parent header colspan
		CellRangeAddress badli = new CellRangeAddress(5, 5, 9, 10);
		sheet.addMergedRegion(badli);
		applyBorder.accept(badli, headerTop);

		CellRangeAddress lang = new CellRangeAddress(5, 5, 14, 15);
		sheet.addMergedRegion(lang);
		applyBorder.accept(lang, headerTop);

		// sub headers
		h2.createCell(9).setCellValue("पासून");
		h2.createCell(10).setCellValue("पर्यंत");
		h2.createCell(14).setCellValue("हिंदी");
		h2.createCell(15).setCellValue("मराठी");

		h2.getCell(9).setCellStyle(headerBottom);
		h2.getCell(10).setCellStyle(headerBottom);
		h2.getCell(14).setCellStyle(headerBottom);
		h2.getCell(15).setCellStyle(headerBottom);

		// ======================= COLUMN NUMBERS ======================
		Row r8 = sheet.createRow(7);
		for (int i = 0; i < 28; i++) {
			Cell c = r8.createCell(i);
			c.setCellValue(i + 1);
			c.setCellStyle(headerFull);
		}

		// ======================= DATA ======================
		int rowIdx = 8;

		for (MasterDataEntity e : list) {

			JsonNode d = e.getData();

			String[] pasunArr = d.path("sevaTapshilPasun").asText("").split("\\s+");
			String[] paryantArr = d.path("sevaTapshilParyant").asText("").split("\\s+");
			String[] karyalayArr = d.path("karyalayNav").asText("").trim().split("\\s{3,}");

			int blockSize = Math.max(Math.max(pasunArr.length, paryantArr.length), karyalayArr.length);

			int startRow = rowIdx;

			for (int i = 0; i < blockSize; i++) {
				Row row = sheet.createRow(rowIdx++);

				row.createCell(9).setCellValue(i < pasunArr.length ? pasunArr[i] : "");
				row.createCell(10).setCellValue(i < paryantArr.length ? paryantArr[i] : "");
				row.createCell(11).setCellValue(i < karyalayArr.length ? karyalayArr[i] : "");

				for (int c = 0; c < 28; c++) {
					Cell cell = row.getCell(c);
					if (cell == null)
						cell = row.createCell(c);
					cell.setCellStyle(dataStyle);
				}
			}

			int endRow = startRow + blockSize - 1;
			Row fr = sheet.getRow(startRow);

			fr.getCell(0).setCellValue(d.path("srNo").asText(""));
			fr.getCell(1).setCellValue(d.path("adhikariNav").asText(""));
			fr.getCell(2).setCellValue(d.path("padnaam").asText(""));
			fr.getCell(3).setCellValue(d.path("sevetNiyukti").asText(""));
			fr.getCell(4).setCellValue(d.path("janmTarikh").asText(""));
			fr.getCell(5).setCellValue(d.path("sevaNivruttiTarikh").asText(""));
			fr.getCell(6).setCellValue(d.path("jatichaPravarg").asText(""));
			fr.getCell(7).setCellValue(d.path("sevetMulNemnukichaDinank").asText(""));
			fr.getCell(8).setCellValue(d.path("sadyacheyPadavarDinank").asText(""));
			fr.getCell(12).setCellValue(d.path("statyitvaPraanPatra").asText(""));
			fr.getCell(13).setCellValue(d.path("khtyachiParikshaUtrinAheKa").asText(""));
			fr.getCell(14).setCellValue(d.path("hindiPariksha").asText(""));
			fr.getCell(15).setCellValue(d.path("marathiPariksha").asText(""));
			fr.getCell(16).setCellValue(d.path("vibhagiyaChouksi").asText(""));
			fr.getCell(17).setCellValue(d.path("yapurviNiyamitPadonnatiKasey").asText(""));
			fr.getCell(18).setCellValue(d.path("sangnakParikshaUtrin").asText(""));
			fr.getCell(19).setCellValue(d.path("purnavilokan5050Jhaley").asText(""));
			fr.getCell(20).setCellValue(d.path("mattaVaDaitvaSadarKasey").asText(""));
			fr.getCell(21).setCellValue(d.path("sevaantrgatAshvashitPragatiYojnachaLabh").asText(""));
			fr.getCell(22).setCellValue(d.path("sudhariSevaAntargatKelechaDinank").asText(""));
			fr.getCell(23).setCellValue(d.path("sevaAntargatAshvashitPrgatiYojnaLabh").asText(""));
			fr.getCell(24).setCellValue(d.path("sudharitSevantargatAshvashitPrgatiYojna").asText(""));
			fr.getCell(25).setCellValue(d.path("dcpsChiNond").asText(""));
			fr.getCell(26).setCellValue(d.path("vetanPadtaliniJhali").asText(""));
			fr.getCell(27).setCellValue(d.path("shera").asText(""));

			for (int c = 0; c < 28; c++) {

				// ❌ DO NOT MERGE split columns
				if (c == 9 || c == 10 || c == 11)
					continue;

				// ❌ DO NOT MERGE if only one row
				if (startRow == endRow)
					continue;

				CellRangeAddress region = new CellRangeAddress(startRow, endRow, c, c);

				sheet.addMergedRegion(region);
				applyBorder.accept(region, dataStyle);
			}

		}

		for (int i = 0; i < 28; i++) {
			sheet.setColumnWidth(i, 5000);
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		wb.write(out);
		wb.close();

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=MasterData_" + year + ".xlsx")
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())));
	}

	@Override
	public PraptraMasterDataResponse saveCrFileList(PraptraMasterDataRequest request) {
		String username = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
		PraptraMasterDataResponse response = new PraptraMasterDataResponse();

		try {
			for (PraptraMasterDataRowRequest r : request.getRows()) {

				// 🔴 HARD DELETE
				if ("D".equalsIgnoreCase(r.getFlag())) {
					crFileListRepository.deleteByYearAndDeleteId(request.getYear(), r.getDeleteId());
					continue;
				}

				// SAVE / UPDATE
				Optional<CrFileListEntity> opt = crFileListRepository.findByYearAndRowId(request.getYear(),
						r.getRowId());

				CrFileListEntity e;

				if (opt.isPresent()) {
					// UPDATE
					e = opt.get();
					e.setFlag("U");
					e.setUpdatedAt(LocalDateTime.now());
					e.setUpdatedBy(username);
				} else {
					// CREATE
					e = new CrFileListEntity();
					e.setYear(request.getYear());
					e.setRowId(r.getRowId());
					e.setDeleteId(r.getDeleteId());
					e.setFlag("C");
					e.setCreatedAt(LocalDateTime.now());
					e.setCreatedBy(username);
					e.setUpdatedAt(LocalDateTime.now());
					e.setUpdatedBy(username);
				}

				e.setData(r.getData());
				crFileListRepository.save(e);
			}

			response.setMessage("CrFileList processed successfully");
			response.setData(List.of());
			response.setErrorDetails(new ApplicationError("200", "Success"));

		} catch (Exception ex) {
			response.setMessage("Failed to process CrFileList");
			response.setData(null);
			response.setErrorDetails(new ApplicationError("500", ex.getMessage()));
		}

		return response;

	}

	@Override
	public PraptraMasterDataResponse getCrFileList(String year) {
		List<CrFileListEntity> list = crFileListRepository.findAllByYearOrderByRowId(year);

		List<Map<String, Object>> data = list.stream()
				.map(e -> Map.of("rowId", e.getRowId(), "deleteId", e.getDeleteId(), "data", e.getData())).toList();

		PraptraMasterDataResponse res = new PraptraMasterDataResponse();
		res.setMessage("Success");
		res.setData(data);
		res.setErrorDetails(new ApplicationError("200", "Fetched successfully"));
		return res;
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadCrFileList(String year) throws IOException {

		List<CrFileListEntity> list = crFileListRepository.findAllByYearOrderByRowId(year);

		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = wb.createSheet("CR File List");

		// ================= FONT =================
		XSSFFont boldFont = wb.createFont();
		boldFont.setBold(true);

		// ================= STYLES =================
		XSSFCellStyle headerStyle = wb.createCellStyle();
		headerStyle.setFont(boldFont);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);

		XSSFCellStyle dataStyle = wb.createCellStyle();
		dataStyle.setAlignment(HorizontalAlignment.CENTER);
		dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		dataStyle.setBorderTop(BorderStyle.THIN);
		dataStyle.setBorderBottom(BorderStyle.THIN);
		dataStyle.setBorderLeft(BorderStyle.THIN);
		dataStyle.setBorderRight(BorderStyle.THIN);
		dataStyle.setWrapText(true);
		// ---------- ROW 1 : TITLE ----------
		Row titleRow = sheet.createRow(0);

		Cell titleCell = titleRow.createCell(0);
		titleCell.setCellValue("कार्यरत अधिकारी व कर्मचारी यांची धारिका यादी");
		titleCell.setCellStyle(headerStyle);

		// merge A1 to E1 (0–4)
		CellRangeAddress titleRegion = new CellRangeAddress(0, 0, 0, 4);
		sheet.addMergedRegion(titleRegion);

		// apply border + style to merged region
		for (int c = 0; c <= 4; c++) {
			Cell cell = titleRow.getCell(c);
			if (cell == null)
				cell = titleRow.createCell(c);
			cell.setCellStyle(headerStyle);
		}

		// ================= HEADER ROW =================
		Row header = sheet.createRow(2);

		String[] headers = { "अ. क्र.", "अधिकारी / कर्मचाऱ्याचे नाव", "पदनाम", "धारिका", "कालावधी" };

		for (int i = 0; i < headers.length; i++) {
			Cell c = header.createCell(i);
			c.setCellValue(headers[i]);
			c.setCellStyle(headerStyle);
			sheet.setColumnWidth(i, 6000);
		}

		// ================= DATA ROWS =================
		int rowIdx = 3;

		for (CrFileListEntity e : list) {

			JsonNode d = e.getData();
			Row row = sheet.createRow(rowIdx++);

			row.createCell(0).setCellValue(d.path("srNo").asText(""));
			row.createCell(1).setCellValue(d.path("adhikariNav").asText(""));
			row.createCell(2).setCellValue(d.path("padnaam").asText(""));
			row.createCell(3).setCellValue(d.path("dharika").asText(""));
			row.createCell(4).setCellValue(d.path("kalavdhi").asText(""));

			for (int c = 0; c < 5; c++) {
				row.getCell(c).setCellStyle(dataStyle);
			}
		}

		// ================= RESPONSE =================
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		wb.write(out);
		wb.close();

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=CrFileList_" + year + ".xlsx")
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())));
	}

	@Transactional
	@Override
	public PraptraMasterDataResponse saveCrFileRtrList(PraptraMasterDataRequest request) {
		String username = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
		PraptraMasterDataResponse response = new PraptraMasterDataResponse();

		try {
			for (PraptraMasterDataRowRequest r : request.getRows()) {

				// 🔴 HARD DELETE
				if ("D".equalsIgnoreCase(r.getFlag())) {
					crFileListRtrRepository.deleteByYearAndDeleteId(request.getYear(), r.getDeleteId());
					continue;
				}

				// SAVE / UPDATE
				Optional<CrFileListRtrEntity> opt = crFileListRtrRepository.findByYearAndRowId(request.getYear(),
						r.getRowId());

				CrFileListRtrEntity e;

				if (opt.isPresent()) {
					// UPDATE
					e = opt.get();
					e.setFlag("U");
					e.setUpdatedAt(LocalDateTime.now());
					e.setUpdatedBy(username);
				} else {
					// CREATE
					e = new CrFileListRtrEntity();
					e.setYear(request.getYear());
					e.setRowId(r.getRowId());
					e.setDeleteId(r.getDeleteId());
					e.setFlag("C");
					e.setCreatedAt(LocalDateTime.now());
					e.setCreatedBy(username);
					e.setUpdatedAt(LocalDateTime.now());
					e.setUpdatedBy(username);
				}

				e.setData(r.getData());
				crFileListRtrRepository.save(e);
			}

			response.setMessage("CrFileList processed successfully");
			response.setData(List.of());
			response.setErrorDetails(new ApplicationError("200", "Success"));

		} catch (Exception ex) {
			response.setMessage("Failed to process CrFileList");
			response.setData(null);
			response.setErrorDetails(new ApplicationError("500", ex.getMessage()));
		}

		return response;
	}

	@Override
	public PraptraMasterDataResponse getCrFileRtrList(String year) {
		List<CrFileListRtrEntity> list = crFileListRtrRepository.findAllByYearOrderByRowId(year);

		List<Map<String, Object>> data = list.stream()
				.map(e -> Map.of("rowId", e.getRowId(), "deleteId", e.getDeleteId(), "data", e.getData())).toList();

		PraptraMasterDataResponse res = new PraptraMasterDataResponse();
		res.setMessage("Success");
		res.setData(data);
		res.setErrorDetails(new ApplicationError("200", "Fetched successfully"));
		return res;
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadCrFileRtrList(String year) throws IOException {
		List<CrFileListRtrEntity> list = crFileListRtrRepository.findAllByYearOrderByRowId(year);

		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = wb.createSheet("CR File Rtr List");

		// ================= FONT =================
		XSSFFont boldFont = wb.createFont();
		boldFont.setBold(true);

		// ================= STYLES =================
		XSSFCellStyle headerStyle = wb.createCellStyle();
		headerStyle.setFont(boldFont);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);

		XSSFCellStyle dataStyle = wb.createCellStyle();
		dataStyle.setAlignment(HorizontalAlignment.CENTER);
		dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		dataStyle.setBorderTop(BorderStyle.THIN);
		dataStyle.setBorderBottom(BorderStyle.THIN);
		dataStyle.setBorderLeft(BorderStyle.THIN);
		dataStyle.setBorderRight(BorderStyle.THIN);
		dataStyle.setWrapText(true);
		// ---------- ROW 1 : TITLE ----------
		Row titleRow = sheet.createRow(0);

		Cell titleCell = titleRow.createCell(0);
		titleCell.setCellValue("सेवानिवृत्त अधिकारी व कर्मचारी यांची धारिका यादी");
		titleCell.setCellStyle(headerStyle);

		// merge A1 to E1 (0–4)
		CellRangeAddress titleRegion = new CellRangeAddress(0, 0, 0, 4);
		sheet.addMergedRegion(titleRegion);

		// apply border + style to merged region
		for (int c = 0; c <= 4; c++) {
			Cell cell = titleRow.getCell(c);
			if (cell == null)
				cell = titleRow.createCell(c);
			cell.setCellStyle(headerStyle);
		}

		// ================= HEADER ROW =================
		Row header = sheet.createRow(2);

		String[] headers = { "अ. क्र.", "अधिकारी / कर्मचाऱ्याचे नाव", "पदनाम", "धारिका", "कालावधी" };

		for (int i = 0; i < headers.length; i++) {
			Cell c = header.createCell(i);
			c.setCellValue(headers[i]);
			c.setCellStyle(headerStyle);
			sheet.setColumnWidth(i, 6000);
		}

		// ================= DATA ROWS =================
		int rowIdx = 3;

		for (CrFileListRtrEntity e : list) {

			JsonNode d = e.getData();
			Row row = sheet.createRow(rowIdx++);

			row.createCell(0).setCellValue(d.path("srNo").asText(""));
			row.createCell(1).setCellValue(d.path("adhikariNav").asText(""));
			row.createCell(2).setCellValue(d.path("padnaam").asText(""));
			row.createCell(3).setCellValue(d.path("dharika").asText(""));
			row.createCell(4).setCellValue(d.path("kalavdhi").asText(""));

			for (int c = 0; c < 5; c++) {
				row.getCell(c).setCellStyle(dataStyle);
			}
		}

		// ================= RESPONSE =================
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		wb.write(out);
		wb.close();

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=CrFileRtrList_" + year + ".xlsx")
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())));
	}

	@Transactional
	@Override
	public PraptraMasterDataResponse saveMahaparRegister(MahaparRegisterRequest request) {

		PraptraMasterDataResponse response = new PraptraMasterDataResponse();

		try {
			String year = request.getYear();
			String username = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");

			if (request.getSections() == null || request.getSections().isEmpty()) {
				throw new IllegalArgumentException("sections cannot be empty");
			}

			for (MahaparRegisterSectionRequest section : request.getSections()) {

				Long sectionId = section.getSectionId(); // 0 = independent
				String sectionName = section.getSectionName();

				if (section.getRows() == null)
					continue;

				for (MahaparRegisterRowRequest row : section.getRows()) {

					/* ------------ HARD DELETE ------------ */
					if ("D".equalsIgnoreCase(row.getFlag())) {

						if (row.getDeleteId() == null) {
							throw new IllegalArgumentException("deleteId is mandatory for deletion");
						}

						mahaparRegisterRepository.deleteByYearAndDeleteId(year, row.getDeleteId());
						continue;
					}

					upsertEntity(year, sectionId, sectionName, row, username);
				}
			}

			response.setMessage("Mahapar Register saved successfully");
			response.setData(List.of());
			response.setErrorDetails(new ApplicationError("200", "Success"));

		} catch (Exception e) {
			response.setMessage("Failed to save Mahapar Register");
			response.setData(null);
			response.setErrorDetails(new ApplicationError("500", e.getMessage()));
		}

		return response;
	}

	private void upsertEntity(String year, Long sectionId, String sectionName, MahaparRegisterRowRequest row,
			String username) {

		Optional<MahaparRegisterEntity> opt = mahaparRegisterRepository.findByYearAndSectionIdAndRowId(year, sectionId,
				row.getRowId());

		MahaparRegisterEntity entity;

		if (opt.isPresent()) {
			/* -------- UPDATE -------- */
			entity = opt.get();
			entity.setFlag("U");
			entity.setUpdatedAt(LocalDateTime.now());
			entity.setUpdatedBy(username);

		} else {
			/* -------- CREATE -------- */
			entity = new MahaparRegisterEntity();
			entity.setYear(year);
			entity.setSectionId(sectionId); // 0 allowed
			entity.setSectionName(sectionName);
			entity.setRowId(row.getRowId());
			entity.setDeleteId(row.getDeleteId());
			entity.setFlag("C");
			entity.setCreatedAt(LocalDateTime.now());
			entity.setCreatedBy(username);
			entity.setUpdatedAt(LocalDateTime.now());
			entity.setUpdatedBy(username);
		}

		entity.setData(row.getData());
		mahaparRegisterRepository.save(entity);
	}

	@Override
	public PraptraMasterDataResponse getMahaparRegister(String year) {

		PraptraMasterDataResponse response = new PraptraMasterDataResponse();

		try {
			List<MahaparRegisterEntity> entities = mahaparRegisterRepository
					.findAllByYearOrderBySectionIdAscRowIdAsc(year);

			MahaparRegisterRequest result = new MahaparRegisterRequest();
			result.setYear(year);

			Map<Long, MahaparRegisterSectionRequest> sectionMap = new LinkedHashMap<>();

			for (MahaparRegisterEntity e : entities) {

				MahaparRegisterRowRequest row = new MahaparRegisterRowRequest();
				row.setRowId(e.getRowId());
				row.setDeleteId(e.getDeleteId());
				row.setFlag(e.getFlag());
				row.setData(e.getData());

				MahaparRegisterSectionRequest section = sectionMap.computeIfAbsent(e.getSectionId(), k -> {
					MahaparRegisterSectionRequest s = new MahaparRegisterSectionRequest();
					s.setSectionId(e.getSectionId()); // 0 or >0
					s.setSectionName(e.getSectionName());
					s.setRows(new ArrayList<>());
					return s;
				});

				section.getRows().add(row);
			}

			result.setSections(new ArrayList<>(sectionMap.values()));

			response.setMessage("Fetched successfully");
			response.setData(List.of(Map.of("request", result)));
			response.setErrorDetails(new ApplicationError("200", "Success"));

		} catch (Exception e) {
			response.setMessage("Failed to fetch Mahapar Register");
			response.setData(null);
			response.setErrorDetails(new ApplicationError("500", e.getMessage()));
		}

		return response;
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadMahaparRegister(String year) throws IOException {

		List<MahaparRegisterEntity> list = mahaparRegisterRepository.findAllByYearOrderBySectionIdAscRowIdAsc(year);

		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = wb.createSheet("कार्य मूल्यमापन");

		/* ===================== STYLES ===================== */

		XSSFFont boldFont = wb.createFont();
		boldFont.setBold(true);

		XSSFCellStyle centerBold = wb.createCellStyle();
		centerBold.setFont(boldFont);
		centerBold.setAlignment(HorizontalAlignment.CENTER);
		centerBold.setVerticalAlignment(VerticalAlignment.CENTER);

		XSSFCellStyle headerStyle = wb.createCellStyle();
		headerStyle.setFont(boldFont);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);

		XSSFCellStyle dataStyle = wb.createCellStyle();
		dataStyle.setAlignment(HorizontalAlignment.CENTER);
		dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		dataStyle.setBorderTop(BorderStyle.THIN);
		dataStyle.setBorderBottom(BorderStyle.THIN);
		dataStyle.setBorderLeft(BorderStyle.THIN);
		dataStyle.setBorderRight(BorderStyle.THIN);
		dataStyle.setWrapText(true);
		XSSFCellStyle leftBold = wb.createCellStyle();
		leftBold.setFont(boldFont);
		leftBold.setAlignment(HorizontalAlignment.LEFT);
		leftBold.setVerticalAlignment(VerticalAlignment.CENTER);

		int rowIdx = 0;

		/*
		 * ================================================== FIRST TABLE HEADER
		 * (INDEPENDENT) ==================================================
		 */

		rowIdx = writeMainHeader(sheet, rowIdx, "पुणे पाटबंधारे प्रकल्प मंडळ, पुणे-01",
				"मंडळ कार्यालय महापार प्रणालीमध्ये भरण्यात आलेल्या कार्यमुल्यमापन अहवालाची यादी", "सन " + year,
				centerBold);

		rowIdx = writeColumnHeader(sheet, rowIdx, headerStyle);

		/*
		 * ================================================== DATA PROCESSING
		 * ==================================================
		 */

		Long currentSectionId = null;
		boolean sectionHeaderPrinted = false;
		boolean firstSectionStarted = false;

		for (MahaparRegisterEntity e : list) {

			/* ---------- SWITCH FROM INDEPENDENT → SECTION TABLE ---------- */
			if (currentSectionId == null && e.getSectionId() != 0) {

				rowIdx += 2; // spacing

				rowIdx = writeMainHeader(sheet, rowIdx, "पुणे पाटबंधारे प्रकल्प मंडळ, पुणे-01",
						"मंडळांतर्गत विभागीय कार्यालयात महापार प्रणालीमध्ये भरण्यात आलेल्या कार्यमुल्यमापन अहवालाची यादी",
						"सन " + year, centerBold);
				rowIdx++;
				sectionHeaderPrinted = true;
				currentSectionId = -1L; // force section detection
			}

			// ---------- NEW SECTION ----------
			if (e.getSectionId() != 0 && !Objects.equals(currentSectionId, e.getSectionId())) {

				// ✅ if this is NOT the very first section → leave one empty row
				if (firstSectionStarted) {
					rowIdx++; // blank row between sections
				}

				firstSectionStarted = true;

				Row secRow = sheet.createRow(rowIdx++);

				// Column A → sectionId (CENTER)
				secRow.createCell(0).setCellValue(e.getSectionId());
				secRow.getCell(0).setCellStyle(centerBold);

				// Column B → sectionName (LEFT)
				secRow.createCell(1).setCellValue(e.getSectionName());
				secRow.getCell(1).setCellStyle(leftBold);

				// Merge B–F
				sheet.addMergedRegion(new CellRangeAddress(secRow.getRowNum(), secRow.getRowNum(), 1, 5));

				// Column header after section title
				rowIdx = writeColumnHeader(sheet, rowIdx, headerStyle);

				currentSectionId = e.getSectionId();
			}

			/* ---------- DATA ROW ---------- */

			JsonNode d = e.getData();
			Row r = sheet.createRow(rowIdx++);

			r.createCell(0).setCellValue(d.path("srNo").asText(""));
			r.createCell(1).setCellValue(d.path("adhikariNav").asText(""));
			r.createCell(2).setCellValue(d.path("padnaam").asText(""));
			r.createCell(3).setCellValue(d.path("kalavadhi").asText(""));
			r.createCell(4).setCellValue(d.path("keleliKaryavahi").asText(""));
			r.createCell(5).setCellValue(d.path("shera").asText(""));

			for (int i = 0; i < 6; i++) {
				r.getCell(i).setCellStyle(dataStyle);
			}
		}

		/* ===================== WIDTH ===================== */

		sheet.setColumnWidth(0, 2500);
		sheet.setColumnWidth(1, 8000);
		sheet.setColumnWidth(2, 6000);
		sheet.setColumnWidth(3, 6000);
		sheet.setColumnWidth(4, 9000);
		sheet.setColumnWidth(5, 5000);

		/* ===================== RESPONSE ===================== */

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		wb.write(out);
		wb.close();

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=MahaparRegister_" + year + ".xlsx")
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())));
	}

	private int writeMainHeader(XSSFSheet sheet, int rowIdx, String l1, String l2, String l3, CellStyle style) {
		Row r1 = sheet.createRow(rowIdx++);
		r1.createCell(0).setCellValue(l1);
		r1.getCell(0).setCellStyle(style);
		sheet.addMergedRegion(new CellRangeAddress(r1.getRowNum(), r1.getRowNum(), 0, 5));

		Row r2 = sheet.createRow(rowIdx++);
		r2.createCell(0).setCellValue(l2);
		r2.getCell(0).setCellStyle(style);
		sheet.addMergedRegion(new CellRangeAddress(r2.getRowNum(), r2.getRowNum(), 0, 5));

		Row r3 = sheet.createRow(rowIdx++);
		r3.createCell(0).setCellValue(l3);
		r3.getCell(0).setCellStyle(style);
		sheet.addMergedRegion(new CellRangeAddress(r3.getRowNum(), r3.getRowNum(), 0, 5));

		return rowIdx;
	}

	private int writeColumnHeader(XSSFSheet sheet, int rowIdx, CellStyle headerStyle) {
		Row h = sheet.createRow(rowIdx++);
		String[] cols = { "अ.क्र.", "अधिकाऱ्याचे नांव", "पदनाम", "कालावधी", "केलेली कार्यवाही", "शेरा" };

		for (int i = 0; i < cols.length; i++) {
			Cell c = h.createCell(i);
			c.setCellValue(cols[i]);
			c.setCellStyle(headerStyle);
		}
		return rowIdx;
	}

	@Transactional
	@Override
	public PraptraMasterDataResponse saveKaryaratGopniyaAhwal(MahaparRegisterRequest request) {

		PraptraMasterDataResponse response = new PraptraMasterDataResponse();
		String user = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");

		if (!request.getType().equalsIgnoreCase("Retired")) {

			try {
				String year = request.getYear();

				if (request.getSections() == null || request.getSections().isEmpty()) {
					response.setMessage("No sections found");
					response.setData(List.of());
					response.setErrorDetails(new ApplicationError("400", "Sections are empty"));
					return response;
				}

				for (MahaparRegisterSectionRequest div : request.getSections()) {

					if (div.getRows() == null || div.getRows().isEmpty()) {
						continue;
					}

					for (MahaparRegisterRowRequest row : div.getRows()) {

						// ✅ HARD DELETE (YEAR + DIVISION + DELETE_ID)
						if ("D".equalsIgnoreCase(row.getFlag())) {
							karyaratGopniyaAhwalRepository.deleteByYearAndDivisionIdAndDeleteId(year,
									div.getSectionId(), row.getDeleteId());
							continue;
						}

						Optional<KaryaratGopniyaAhwalEntity> opt = karyaratGopniyaAhwalRepository
								.findByYearAndDivisionIdAndRowId(year, div.getSectionId(), row.getRowId());

						KaryaratGopniyaAhwalEntity entity;

						if (opt.isPresent()) {
							// -------- UPDATE --------
							entity = opt.get();
							entity.setFlag("U");
							entity.setUpdatedAt(LocalDateTime.now());
							entity.setUpdatedBy(user);
						} else {
							// -------- CREATE --------
							entity = new KaryaratGopniyaAhwalEntity();
							entity.setYear(year);
							entity.setDivisionId(div.getSectionId());
							entity.setDivisionName(div.getSectionName());
							entity.setRowId(row.getRowId());
							entity.setDeleteId(row.getDeleteId());
							entity.setFlag("C");
							entity.setCreatedAt(LocalDateTime.now());
							entity.setCreatedBy(user);
							entity.setUpdatedAt(LocalDateTime.now());
							entity.setUpdatedBy(user);
						}

						entity.setData(row.getData());
						karyaratGopniyaAhwalRepository.save(entity);
					}
				}

				response.setMessage("Karyarat Gopniya Ahwal saved successfully");
				response.setData(List.of());
				response.setErrorDetails(new ApplicationError("200", "Success"));

			} catch (Exception e) {
				response.setMessage("Failed to save Karyarat Gopniya Ahwal");
				response.setData(null);
				response.setErrorDetails(new ApplicationError("500", e.getMessage()));
			}
		} else {
			try {
				String year = request.getYear();

				if (request.getSections() == null || request.getSections().isEmpty()) {
					response.setMessage("No sections found");
					response.setData(List.of());
					response.setErrorDetails(new ApplicationError("400", "Sections are empty"));
					return response;
				}

				for (MahaparRegisterSectionRequest div : request.getSections()) {

					if (div.getRows() == null || div.getRows().isEmpty()) {
						continue;
					}

					for (MahaparRegisterRowRequest row : div.getRows()) {

						// ✅ HARD DELETE (YEAR + DIVISION + DELETE_ID)
						if ("D".equalsIgnoreCase(row.getFlag())) {
							rtrGopniyaAhwalRepository.deleteByYearAndDivisionIdAndDeleteId(year, div.getSectionId(),
									row.getDeleteId());
							continue;
						}

						Optional<RtrGopniyaAhwal> opt = rtrGopniyaAhwalRepository.findByYearAndDivisionIdAndRowId(year,
								div.getSectionId(), row.getRowId());

						RtrGopniyaAhwal entity;

						if (opt.isPresent()) {
							// -------- UPDATE --------
							entity = opt.get();
							entity.setFlag("U");
							entity.setUpdatedAt(LocalDateTime.now());
							entity.setUpdatedBy(user);
						} else {
							// -------- CREATE --------
							entity = new RtrGopniyaAhwal();
							entity.setYear(year);
							entity.setDivisionId(div.getSectionId());
							entity.setDivisionName(div.getSectionName());
							entity.setRowId(row.getRowId());
							entity.setDeleteId(row.getDeleteId());
							entity.setFlag("C");
							entity.setCreatedAt(LocalDateTime.now());
							entity.setCreatedBy(user);
							entity.setUpdatedAt(LocalDateTime.now());
							entity.setUpdatedBy(user);
						}

						entity.setData(row.getData());
						rtrGopniyaAhwalRepository.save(entity);
					}
				}

				response.setMessage("Retired Gopniya Ahwal saved successfully");
				response.setData(List.of());
				response.setErrorDetails(new ApplicationError("200", "Success"));

			} catch (Exception e) {
				response.setMessage("Failed to save Retired Gopniya Ahwal");
				response.setData(null);
				response.setErrorDetails(new ApplicationError("500", e.getMessage()));
			}

		}

		return response;
	}

	@Override
	public PraptraMasterDataResponse getKaryaratGopniyaAhwal(String year, String type) {

		PraptraMasterDataResponse response = new PraptraMasterDataResponse();

		if (!type.equalsIgnoreCase("Retired")) {

			try {
				// ✅ DB level sorting: divisionId ASC, rowId ASC
				List<KaryaratGopniyaAhwalEntity> entities = karyaratGopniyaAhwalRepository
						.findAllByYearOrderByDivisionIdAscRowIdAsc(year);

				// -------- BUILD SAME REQUEST STRUCTURE BACK --------
				MahaparRegisterRequest result = new MahaparRegisterRequest();
				result.setYear(year);

				// divisionId → section mapping
				Map<Long, MahaparRegisterSectionRequest> divisionMap = new LinkedHashMap<>();

				for (KaryaratGopniyaAhwalEntity e : entities) {

					// ---------- ROW ----------
					MahaparRegisterRowRequest row = new MahaparRegisterRowRequest();
					row.setRowId(e.getRowId());
					row.setDeleteId(e.getDeleteId());
					row.setFlag(e.getFlag());
					row.setData(e.getData());

					// ---------- DIVISION / SECTION ----------
					MahaparRegisterSectionRequest section = divisionMap.computeIfAbsent(e.getDivisionId(), k -> {
						MahaparRegisterSectionRequest s = new MahaparRegisterSectionRequest();
						s.setSectionId(e.getDivisionId());
						s.setSectionName(e.getDivisionName());
						s.setRows(new ArrayList<>());
						return s;
					});

					section.getRows().add(row);
				}

				result.setSections(new ArrayList<>(divisionMap.values()));

				response.setMessage("Fetched successfully");
				response.setData(List.of(Map.of("request", result)));
				response.setErrorDetails(new ApplicationError("200", "Success"));

			} catch (Exception e) {
				response.setMessage("Failed to fetch Karyarat Gopniya Ahwal");
				response.setData(null);
				response.setErrorDetails(new ApplicationError("500", e.getMessage()));
			}
		} else {
			try {
				// ✅ DB level sorting: divisionId ASC, rowId ASC
				List<RtrGopniyaAhwal> entities = rtrGopniyaAhwalRepository
						.findAllByYearOrderByDivisionIdAscRowIdAsc(year);

				// -------- BUILD SAME REQUEST STRUCTURE BACK --------
				MahaparRegisterRequest result = new MahaparRegisterRequest();
				result.setYear(year);

				// divisionId → section mapping
				Map<Long, MahaparRegisterSectionRequest> divisionMap = new LinkedHashMap<>();

				for (RtrGopniyaAhwal e : entities) {

					// ---------- ROW ----------
					MahaparRegisterRowRequest row = new MahaparRegisterRowRequest();
					row.setRowId(e.getRowId());
					row.setDeleteId(e.getDeleteId());
					row.setFlag(e.getFlag());
					row.setData(e.getData());

					// ---------- DIVISION / SECTION ----------
					MahaparRegisterSectionRequest section = divisionMap.computeIfAbsent(e.getDivisionId(), k -> {
						MahaparRegisterSectionRequest s = new MahaparRegisterSectionRequest();
						s.setSectionId(e.getDivisionId());
						s.setSectionName(e.getDivisionName());
						s.setRows(new ArrayList<>());
						return s;
					});

					section.getRows().add(row);
				}

				result.setSections(new ArrayList<>(divisionMap.values()));

				response.setMessage("Fetched successfully");
				response.setData(List.of(Map.of("request", result)));
				response.setErrorDetails(new ApplicationError("200", "Success"));

			} catch (Exception e) {
				response.setMessage("Failed to fetch Retired Gopniya Ahwal");
				response.setData(null);
				response.setErrorDetails(new ApplicationError("500", e.getMessage()));
			}
		}

		return response;
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadKaryaratGopniyaAhwal(String year, String type)
			throws IOException {

		if (!type.equalsIgnoreCase("Retired")) {

			List<KaryaratGopniyaAhwalEntity> list = karyaratGopniyaAhwalRepository
					.findAllByYearOrderByDivisionIdAscRowIdAsc(year);

			XSSFWorkbook wb = new XSSFWorkbook();
			XSSFSheet sheet = wb.createSheet("कार्यरत गोपनीय अहवाल");

			/* ===================== STYLES ===================== */

			XSSFFont boldFont = wb.createFont();
			boldFont.setBold(true);

			XSSFCellStyle centerBold = wb.createCellStyle();
			centerBold.setFont(boldFont);
			centerBold.setAlignment(HorizontalAlignment.CENTER);
			centerBold.setVerticalAlignment(VerticalAlignment.CENTER);

			XSSFCellStyle headerStyle = wb.createCellStyle();
			headerStyle.setFont(boldFont);
			headerStyle.setAlignment(HorizontalAlignment.CENTER);
			headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			headerStyle.setBorderTop(BorderStyle.THIN);
			headerStyle.setBorderBottom(BorderStyle.THIN);
			headerStyle.setBorderLeft(BorderStyle.THIN);
			headerStyle.setBorderRight(BorderStyle.THIN);
			headerStyle.setWrapText(true);

			XSSFCellStyle dataStyle = wb.createCellStyle();
			dataStyle.setAlignment(HorizontalAlignment.CENTER);
			dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			dataStyle.setBorderTop(BorderStyle.THIN);
			dataStyle.setBorderBottom(BorderStyle.THIN);
			dataStyle.setBorderLeft(BorderStyle.THIN);
			dataStyle.setBorderRight(BorderStyle.THIN);
			dataStyle.setWrapText(true);

			int rowIdx = 0;
			Long currentDivisionId = null;
			boolean firstDivision = true;

			/* ===================== DATA ===================== */

			for (KaryaratGopniyaAhwalEntity e : list) {

				// ---------- NEW DIVISION ----------
				if (!Objects.equals(currentDivisionId, e.getDivisionId())) {

					if (!firstDivision) {
						rowIdx++; // 🔥 blank row after each division
					}
					firstDivision = false;

					// ---------- DIVISION NAME (CENTER, MERGED) ----------
					Row divRow = sheet.createRow(rowIdx++);
					Cell divCell = divRow.createCell(0);
					divCell.setCellValue(e.getDivisionName());
					divCell.setCellStyle(centerBold);

					// Merge A–F
					sheet.addMergedRegion(new CellRangeAddress(divRow.getRowNum(), divRow.getRowNum(), 0, 5));

					// ---------- GAP ROW ----------
					rowIdx++;

					// ---------- COLUMN HEADER ----------
					rowIdx = writeColumnHeaderForKaryarat(sheet, rowIdx, headerStyle);

					currentDivisionId = e.getDivisionId();
				}

				// ---------- DATA ROW ----------
				JsonNode d = e.getData();
				Row r = sheet.createRow(rowIdx++);

				r.createCell(0).setCellValue(d.path("srNo").asText(""));
				r.createCell(1).setCellValue(d.path("nav").asText(""));
				r.createCell(2).setCellValue(d.path("padnaam").asText(""));
				r.createCell(3).setCellValue(d.path("mulGopniyaAhval").asText(""));
				r.createCell(4).setCellValue(d.path("dusraGopniyaAhval").asText(""));
				r.createCell(5).setCellValue(d.path("sthiti").asText(""));

				for (int i = 0; i < 6; i++) {
					r.getCell(i).setCellStyle(dataStyle);
				}
			}
			/* ===================== FOOTER ===================== */

			// Blank row
			rowIdx++;

			// ---------- ORDER TEXT ----------
			/* ===================== FOOTER ===================== */

			// Blank row
			rowIdx++;

			// ---------- ORDER TEXT ----------
			Row orderRow = sheet.createRow(rowIdx++);

			/* 🔥 IMPORTANT: increase row height */
			orderRow.setHeightInPoints(80); // <-- THIS FIXES CUTTING (60–100 adjust if needed)

			Cell orderCell = orderRow.createCell(1); // Column B

			orderCell.setCellValue("मंडळ कार्यालयाचे कार्यालयीन आदेश क्र. 40 जा.क्र. पुपाप्रमं/आ 1/872 दि. 29/02/2024 "
					+ "अन्वये अनु. क्र. 1 ते 35 सौ. माधवी राहुल ढेंबरे, प्रथम लिपीक यांचेकडे "
					+ "हस्तांतरित करण्यात येत आहे.");

			XSSFCellStyle orderStyle = wb.createCellStyle();
			orderStyle.setAlignment(HorizontalAlignment.CENTER);
			orderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			orderStyle.setWrapText(true);

			orderCell.setCellStyle(orderStyle);

			// Merge B to F
			sheet.addMergedRegion(new CellRangeAddress(orderRow.getRowNum(), orderRow.getRowNum(), 1, 5));

			// Blank row
			rowIdx++;

			/* ---------- SIGNATURE ROW ---------- */
			Row signRow = sheet.createRow(rowIdx++);

			// LEFT SIGNATURE (Column B)
			Cell leftSign = signRow.createCell(1);
			leftSign.setCellValue("कार्यभार देणार\n" + "(संदीप चं. अमृतकर)\n" + "व.लि.");

			XSSFCellStyle signLeftStyle = wb.createCellStyle();
			signLeftStyle.setAlignment(HorizontalAlignment.CENTER);
			signLeftStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			signLeftStyle.setWrapText(true);

			leftSign.setCellStyle(signLeftStyle);

			// RIGHT SIGNATURE (Column E)
			Cell rightSign = signRow.createCell(4);
			rightSign.setCellValue("कार्यभार घेणार\n" + "(माधवी रा. ढेंबरे)\n" + "प्रथम लिपीक");

			XSSFCellStyle signRightStyle = wb.createCellStyle();
			signRightStyle.setAlignment(HorizontalAlignment.CENTER);
			signRightStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			signRightStyle.setWrapText(true);

			rightSign.setCellStyle(signRightStyle);

			/* ===================== COLUMN WIDTH ===================== */

			sheet.setColumnWidth(0, 2500);
			sheet.setColumnWidth(1, 7000);
			sheet.setColumnWidth(2, 6000);
			sheet.setColumnWidth(3, 9000);
			sheet.setColumnWidth(4, 9000);
			sheet.setColumnWidth(5, 6000);

			/* ===================== RESPONSE ===================== */

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			wb.write(out);
			wb.close();

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION,
							"attachment; filename=KaryaratGopniyaAhwal_" + year + ".xlsx")
					.contentType(MediaType.APPLICATION_OCTET_STREAM)
					.body(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())));
		} else {
			List<RtrGopniyaAhwal> list = rtrGopniyaAhwalRepository.findAllByYearOrderByDivisionIdAscRowIdAsc(year);

			XSSFWorkbook wb = new XSSFWorkbook();
			XSSFSheet sheet = wb.createSheet("कार्यरत गोपनीय अहवाल");

			/* ===================== STYLES ===================== */

			XSSFFont boldFont = wb.createFont();
			boldFont.setBold(true);

			XSSFCellStyle centerBold = wb.createCellStyle();
			centerBold.setFont(boldFont);
			centerBold.setAlignment(HorizontalAlignment.CENTER);
			centerBold.setVerticalAlignment(VerticalAlignment.CENTER);

			XSSFCellStyle headerStyle = wb.createCellStyle();
			headerStyle.setFont(boldFont);
			headerStyle.setAlignment(HorizontalAlignment.CENTER);
			headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			headerStyle.setBorderTop(BorderStyle.THIN);
			headerStyle.setBorderBottom(BorderStyle.THIN);
			headerStyle.setBorderLeft(BorderStyle.THIN);
			headerStyle.setBorderRight(BorderStyle.THIN);
			headerStyle.setWrapText(true);

			XSSFCellStyle dataStyle = wb.createCellStyle();
			dataStyle.setAlignment(HorizontalAlignment.CENTER);
			dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			dataStyle.setBorderTop(BorderStyle.THIN);
			dataStyle.setBorderBottom(BorderStyle.THIN);
			dataStyle.setBorderLeft(BorderStyle.THIN);
			dataStyle.setBorderRight(BorderStyle.THIN);
			dataStyle.setWrapText(true);

			int rowIdx = 0;
			Long currentDivisionId = null;
			boolean firstDivision = true;

			/* ===================== DATA ===================== */

			for (RtrGopniyaAhwal e : list) {

				// ---------- NEW DIVISION ----------
				if (!Objects.equals(currentDivisionId, e.getDivisionId())) {

					if (!firstDivision) {
						rowIdx++; // 🔥 blank row after each division
					}
					firstDivision = false;

					// ---------- DIVISION NAME (CENTER, MERGED) ----------
					Row divRow = sheet.createRow(rowIdx++);
					Cell divCell = divRow.createCell(0);
					divCell.setCellValue(e.getDivisionName());
					divCell.setCellStyle(centerBold);

					// Merge A–F
					sheet.addMergedRegion(new CellRangeAddress(divRow.getRowNum(), divRow.getRowNum(), 0, 5));

					// ---------- GAP ROW ----------
					rowIdx++;

					// ---------- COLUMN HEADER ----------
					rowIdx = writeColumnHeaderForKaryarat(sheet, rowIdx, headerStyle);

					currentDivisionId = e.getDivisionId();
				}

				// ---------- DATA ROW ----------
				JsonNode d = e.getData();
				Row r = sheet.createRow(rowIdx++);

				r.createCell(0).setCellValue(d.path("srNo").asText(""));
				r.createCell(1).setCellValue(d.path("nav").asText(""));
				r.createCell(2).setCellValue(d.path("padnaam").asText(""));
				r.createCell(3).setCellValue(d.path("mulGopniyaAhval").asText(""));
				r.createCell(4).setCellValue(d.path("dusraGopniyaAhval").asText(""));
				r.createCell(5).setCellValue(d.path("sthiti").asText(""));

				for (int i = 0; i < 6; i++) {
					r.getCell(i).setCellStyle(dataStyle);
				}
			}
			/* ===================== FOOTER ===================== */

			// Blank row
			rowIdx++;

			// ---------- ORDER TEXT ----------
			/* ===================== FOOTER ===================== */

			// Blank row
			rowIdx++;

			// ---------- ORDER TEXT ----------
			Row orderRow = sheet.createRow(rowIdx++);

			/* 🔥 IMPORTANT: increase row height */
			orderRow.setHeightInPoints(80); // <-- THIS FIXES CUTTING (60–100 adjust if needed)

			Cell orderCell = orderRow.createCell(1); // Column B

			orderCell.setCellValue("मंडळ कार्यालयाचे कार्यालयीन आदेश क्र. 40 जा.क्र. पुपाप्रमं/आ 1/872 दि. 29/02/2024 "
					+ "अन्वये अनु. क्र. 1 ते 35 सौ. माधवी राहुल ढेंबरे, प्रथम लिपीक यांचेकडे "
					+ "हस्तांतरित करण्यात येत आहे.");

			XSSFCellStyle orderStyle = wb.createCellStyle();
			orderStyle.setAlignment(HorizontalAlignment.CENTER);
			orderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			orderStyle.setWrapText(true);

			orderCell.setCellStyle(orderStyle);

			// Merge B to F
			sheet.addMergedRegion(new CellRangeAddress(orderRow.getRowNum(), orderRow.getRowNum(), 1, 5));

			// Blank row
			rowIdx++;

			/* ---------- SIGNATURE ROW ---------- */
			Row signRow = sheet.createRow(rowIdx++);

			// LEFT SIGNATURE (Column B)
			Cell leftSign = signRow.createCell(1);
			leftSign.setCellValue("कार्यभार देणार\n" + "(संदीप चं. अमृतकर)\n" + "व.लि.");

			XSSFCellStyle signLeftStyle = wb.createCellStyle();
			signLeftStyle.setAlignment(HorizontalAlignment.CENTER);
			signLeftStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			signLeftStyle.setWrapText(true);

			leftSign.setCellStyle(signLeftStyle);

			// RIGHT SIGNATURE (Column E)
			Cell rightSign = signRow.createCell(4);
			rightSign.setCellValue("कार्यभार घेणार\n" + "(माधवी रा. ढेंबरे)\n" + "प्रथम लिपीक");

			XSSFCellStyle signRightStyle = wb.createCellStyle();
			signRightStyle.setAlignment(HorizontalAlignment.CENTER);
			signRightStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			signRightStyle.setWrapText(true);

			rightSign.setCellStyle(signRightStyle);

			/* ===================== COLUMN WIDTH ===================== */

			sheet.setColumnWidth(0, 2500);
			sheet.setColumnWidth(1, 7000);
			sheet.setColumnWidth(2, 6000);
			sheet.setColumnWidth(3, 9000);
			sheet.setColumnWidth(4, 9000);
			sheet.setColumnWidth(5, 6000);

			/* ===================== RESPONSE ===================== */

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			wb.write(out);
			wb.close();

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION,
							"attachment; filename=RetiredGopniyaAhwal" + year + ".xlsx")
					.contentType(MediaType.APPLICATION_OCTET_STREAM)
					.body(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())));

		}
	}

	private int writeColumnHeaderForKaryarat(XSSFSheet sheet, int rowIdx, CellStyle headerStyle) {
		Row h = sheet.createRow(rowIdx++);
		String[] cols = { "अनु क्र", "नाव", "पदनाम", "मूळ गोपनीय अहवाल", "दुय्यम गोपनीय अहवाल",
				"कार्यरत/बदली/सेवानिवृत्त" };

		for (int i = 0; i < cols.length; i++) {
			Cell c = h.createCell(i);
			c.setCellValue(cols[i]);
			c.setCellStyle(headerStyle);
		}
		return rowIdx;
	}

}