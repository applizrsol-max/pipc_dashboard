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
import java.util.Optional;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
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
import com.pipc.dashboard.establishment.repository.AgendaOfficerEntity;
import com.pipc.dashboard.establishment.repository.AgendaOfficerRepository;
import com.pipc.dashboard.establishment.repository.AppealEntity;
import com.pipc.dashboard.establishment.repository.AppealRepository;
import com.pipc.dashboard.establishment.repository.ApprovalDetailsEntity;
import com.pipc.dashboard.establishment.repository.ApprovalDetailsRepository;
import com.pipc.dashboard.establishment.repository.EmployeeDetailsEntity;
import com.pipc.dashboard.establishment.repository.EmployeeDetailsRepository;
import com.pipc.dashboard.establishment.repository.EmployeePostingEntity;
import com.pipc.dashboard.establishment.repository.EmployeePostingRepository;
import com.pipc.dashboard.establishment.repository.IncomeTaxDeductionEntity;
import com.pipc.dashboard.establishment.repository.IncomeTaxDeductionRepository;
import com.pipc.dashboard.establishment.repository.KharchaTapsilEntity;
import com.pipc.dashboard.establishment.repository.KharchaTapsilRepository;
import com.pipc.dashboard.establishment.repository.LeaveEntity;
import com.pipc.dashboard.establishment.repository.LeaveRepository;
import com.pipc.dashboard.establishment.repository.MedicalBillMasterEntity;
import com.pipc.dashboard.establishment.repository.MedicalBillMasterRepository;
import com.pipc.dashboard.establishment.repository.PassportNocEntity;
import com.pipc.dashboard.establishment.repository.PassportNocRepository;
import com.pipc.dashboard.establishment.repository.ReferenceEntity;
import com.pipc.dashboard.establishment.repository.ReferenceRepository;
import com.pipc.dashboard.establishment.repository.VaidyakExcludedDetailsEntity;
import com.pipc.dashboard.establishment.repository.VaidyakKharchaParigananaEntity;
import com.pipc.dashboard.establishment.repository.VaidyakKharchaParigananaRepository;
import com.pipc.dashboard.establishment.repository.VaidyakTapshilEntity;
import com.pipc.dashboard.establishment.repository.VaidyakTapshilRepository;
import com.pipc.dashboard.establishment.repository.VastavyaDetailsEntity;
import com.pipc.dashboard.establishment.repository.VastavyaDetailsRepository;
import com.pipc.dashboard.establishment.request.AgendaRequest;
import com.pipc.dashboard.establishment.request.AgendaRow;
import com.pipc.dashboard.establishment.request.AppealRequest;
import com.pipc.dashboard.establishment.request.AppealWrapper;
import com.pipc.dashboard.establishment.request.EmployeePostingRequest;
import com.pipc.dashboard.establishment.request.IncomeTaxDeductionRequest;
import com.pipc.dashboard.establishment.request.LeaveRequest;
import com.pipc.dashboard.establishment.request.MedicalBillData;
import com.pipc.dashboard.establishment.request.MedicalBillRequest;
import com.pipc.dashboard.establishment.request.PassportNocRequest;
import com.pipc.dashboard.establishment.response.AgendaResponse;
import com.pipc.dashboard.establishment.response.AppealResponse;
import com.pipc.dashboard.establishment.response.EmployeePostingResponse;
import com.pipc.dashboard.establishment.response.IncomeTaxDeductionResponse;
import com.pipc.dashboard.establishment.response.LeaveResponse;
import com.pipc.dashboard.establishment.response.MedicalBillResponse;
import com.pipc.dashboard.establishment.response.PassportNocResponse;
import com.pipc.dashboard.service.EstablishmentService;
import com.pipc.dashboard.utility.ApplicationError;
import com.pipc.dashboard.utility.PdfUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
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

	private static final String TEMPLATE = "/templates/medical_bill_template.docx";

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	public EstablishmentServiceImpl(MedicalBillMasterRepository masterRepo, ReferenceRepository refRepo,
			EmployeeDetailsRepository empRepo, ApprovalDetailsRepository apprRepo, KharchaTapsilRepository kharchaRepo,
			VaidyakKharchaParigananaRepository vaidyaRepo, VaidyakTapshilRepository tapshilRepo,
			VastavyaDetailsRepository vastavyaRepo, LeaveRepository leaveRepository, AppealRepository appealRepository,
			EmployeePostingRepository employeePostingRepository,
			IncomeTaxDeductionRepository incomeTaxDeductionRepository, PassportNocRepository passportNocRepository,
			AgendaOfficerRepository agendaOfficerRepository) {
		this.apprRepo = apprRepo;
		this.empRepo = empRepo;
		this.kharchaRepo = kharchaRepo;
		this.masterRepo = masterRepo;
		this.refRepo = refRepo;
		this.tapshilRepo = tapshilRepo;
		this.vaidyaRepo = vaidyaRepo;
		this.vastavyaRepo = vastavyaRepo;
		this.leaveRepository = leaveRepository;
		this.appealRepository = appealRepository;
		this.employeePostingRepository = employeePostingRepository;
		this.incomeTaxDeductionRepository = incomeTaxDeductionRepository;
		this.passportNocRepository = passportNocRepository;
		this.agendaOfficerRepository = agendaOfficerRepository;
	}

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
				 * ----------------------------------------- 🔥 HARD DELETE LOGIC
				 * -----------------------------------------
				 */
				// HARD DELETE
				if ("D".equalsIgnoreCase(deleteFlag)) {

					Long deleteId = row.getDeleteId(); // now Long

					if (deleteId != null && deleteId > 0) {

						agendaOfficerRepository.findByDeleteIdAndYearAndTargetDate(deleteId, year, targetDate)
								.ifPresent(agendaOfficerRepository::delete);
						error.setErrorCode("200");
						error.setErrorDescription("DeleteId " + deleteId + " deleted successfully.");

					}

					continue; // skip update/create
				}

				/*
				 * ----------------------------------------- ✏️ CREATE OR UPDATE LOGIC
				 * -----------------------------------------
				 */
				Optional<AgendaOfficerEntity> existingOpt = agendaOfficerRepository
						.findByRowIdAndYearAndTargetDate(rowId, year, targetDate);

				AgendaOfficerEntity entity = existingOpt.orElse(new AgendaOfficerEntity());
				LocalDateTime now = LocalDateTime.now();

				entity.setRowId(rowId);
				entity.setYear(year);
				entity.setTargetDate(targetDate);
				entity.setColumnData(row.getColumnData());
				entity.setDeleteId(row.getDeleteId()); // store deleteId always
				entity.setUpAdhikshakAbhiyantaName(row.getUpAdhikshakAbhiyantaName());

				if (entity.getId() == null) {
					entity.setFlag("C");
					entity.setCreatedBy(username);
					entity.setCreatedAt(now);
					error.setErrorCode("200");
					error.setErrorDescription("Agenda saved successfully.");
				} else {
					entity.setFlag("U");
					error.setErrorCode("200");
					error.setErrorDescription("Agenda updated successfully.");
				}

				entity.setUpdatedBy(username);
				entity.setUpdatedAt(now);

				agendaOfficerRepository.save(entity);
			}

			response.setMessage("Success");

		} catch (Exception e) {

			error.setErrorCode("500");
			error.setErrorDescription("Error while processing agenda: " + e.getMessage());
			response.setMessage("Failed to process agenda.");
		}

		response.setErrorDetails(error);
		return response;
	}

	@Override
	public AgendaResponse getAgendaByYearAndTargetDate(String year, String targetDate) {

		AgendaResponse response = new AgendaResponse();
		ApplicationError error = new ApplicationError();

		try {
			List<AgendaOfficerEntity> list = agendaOfficerRepository.findByYearAndTargetDate(year, targetDate);

			response.setData(list);
			response.setMessage("Success");

			error.setErrorCode("200");
			error.setErrorDescription("Agenda fetched successfully.");

		} catch (Exception e) {
			error.setErrorCode("500");
			error.setErrorDescription("Error while fetching agenda: " + e.getMessage());
			response.setMessage("Failed");
		}

		response.setErrorDetails(error);
		return response;
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadAgendaExcel(String year, String targetDate) throws Exception {

		List<AgendaOfficerEntity> list = agendaOfficerRepository.findByYearAndTargetDate(year, targetDate);

		// ======= FOOTER NAME RESOLUTION ONLY FROM ENTITY =======
		String footerName = "( नि.लि.हैमे )";

		if (!list.isEmpty()) {

			// 1) Try direct field in entity
			String fromEntity = list.get(0).getUpAdhikshakAbhiyantaName();
			if (fromEntity != null && !fromEntity.isBlank()) {
				footerName = fromEntity;
			}

			// 2) Try inside columnData JSON
			else {
				JsonNode cd = list.get(0).getColumnData();
				if (cd != null && cd.has("upAdhikshakAbhiyantaName")) {
					String fromJson = cd.get("upAdhikshakAbhiyantaName").asText();
					if (fromJson != null && !fromJson.isBlank()) {
						footerName = fromJson;
					}
				}
			}
		}

		// ---------- build workbook ----------
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("Agenda Officers");

		// ====== COMMON STYLES ======
		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerStyle.setWrapText(true);
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);

		CellStyle borderStyle = workbook.createCellStyle();
		borderStyle.setBorderBottom(BorderStyle.THIN);
		borderStyle.setBorderTop(BorderStyle.THIN);
		borderStyle.setBorderLeft(BorderStyle.THIN);
		borderStyle.setBorderRight(BorderStyle.THIN);
		borderStyle.setWrapText(true);

		int rowIdx = 0;

		// ---------------------------------------------------
		// 🟦 TITLE ROWS
		// ---------------------------------------------------
		Row t1 = sheet.createRow(rowIdx++);
		t1.createCell(0).setCellValue("परिशिष्ट-ब");
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));

		Row t2 = sheet.createRow(rowIdx++);
		t2.createCell(0).setCellValue("1 ऑगस्ट रोजी वयाची 49/ 54 वर्षे पूर्ण झालेल्या गट-अ मधील शासकीय अधिकारी");
		sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 10));

		Row t3 = sheet.createRow(rowIdx++);
		t3.createCell(0).setCellValue("विभागाचे नाव- जलसंपदा विभाग.");
		sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 10));

		Row t4 = sheet.createRow(rowIdx++);
		t4.createCell(0).setCellValue("मंडळ कार्यालयाचे नाव- अधीक्षक अभियंता, पुणे पाटबंधारे प्रकल्प मंडळ, पुणे");
		sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, 10));

		Row t5 = sheet.createRow(rowIdx++);
		t5.createCell(0).setCellValue("पदनाम- उपविभागीय अभियंता.");
		sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 10));

		rowIdx++;

		// ---------------------------------------------------
		// 🟦 MAIN TABLE HEADER (TWO ROWS)
		// ---------------------------------------------------
		Row hdr1 = sheet.createRow(rowIdx++);
		Row hdr2 = sheet.createRow(rowIdx++);

		String[] h1 = { "अ. क्र.", "अधिकारी नाव", "पदनाम", "जन्मतारखेचा वर्ष", "सेवेतिल प्रथम नियुक्ती दिनांक",
				"झालेली एकुण सेवा", "विभागीय चौकशी सुरु / प्रस्तावित", "", "गोपनीय अहवालातील बाबी." };

		String[] h2 = { "", "", "", "", "", "", "", "गोपनीय अहवाल वर्ष", "सचोटी", "प्रकृतीमान", "प्रतवारी" };

		// merging
		for (int c = 0; c <= 6; c++) {
			sheet.addMergedRegion(new CellRangeAddress(hdr1.getRowNum(), hdr2.getRowNum(), c, c));
		}
		sheet.addMergedRegion(new CellRangeAddress(hdr1.getRowNum(), hdr1.getRowNum(), 7, 10));

		// fill header row 1
		for (int i = 0; i < h1.length; i++) {
			Cell cell = hdr1.createCell(i);
			cell.setCellValue(h1[i]);
			cell.setCellStyle(headerStyle);
		}

		// fill header row 2
		for (int i = 0; i < h2.length; i++) {
			Cell cell = hdr2.createCell(i + 7);
			cell.setCellValue(h2[i]);
			cell.setCellStyle(headerStyle);
		}

		// ---------------------------------------------------
		// 🟦 DATA ROWS
		// ---------------------------------------------------
		for (AgendaOfficerEntity entity : list) {

			JsonNode data = entity.getColumnData();
			JsonNode gList = (data != null && data.has("gopniyaAhawalList")) ? data.get("gopniyaAhawalList") : null;

			int gSize = (gList != null && gList.isArray()) ? gList.size() : 1;

			if (gSize > 1) {
				for (int c = 0; c <= 6; c++) {
					sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx + gSize - 1, c, c));
				}
			}

			Row r = sheet.createRow(rowIdx);

			r.createCell(0).setCellValue(data.get("kramank").asInt());
			r.createCell(1).setCellValue(data.get("adhikariNav").asText());
			r.createCell(2).setCellValue(data.get("padnaam").asText());
			r.createCell(3).setCellValue(data.get("janmaTarikh").asText());
			r.createCell(4).setCellValue(data.get("sevetIlPrathamNiyuktiDinank").asText());
			r.createCell(5).setCellValue(data.get("jhaleliEkunaSeva").asText());
			r.createCell(6).setCellValue(data.get("vibhagiyaChokashiStatus").asText());

			if (gList != null && gList.size() > 0) {
				JsonNode g0 = gList.get(0);
				r.createCell(7).setCellValue(g0.get("varsh").asText());
				r.createCell(8).setCellValue(g0.get("sachoti").asText());
				r.createCell(9).setCellValue(g0.get("prakrutiman").asText());
				r.createCell(10).setCellValue(g0.get("pratavaari").asText());
			}

			for (int i = 1; i < gSize; i++) {
				JsonNode g = gList.get(i);
				Row rr = sheet.createRow(rowIdx + i);

				rr.createCell(7).setCellValue(g.get("varsh").asText());
				rr.createCell(8).setCellValue(g.get("sachoti").asText());
				rr.createCell(9).setCellValue(g.get("prakrutiman").asText());
				rr.createCell(10).setCellValue(g.get("pratavaari").asText());
			}

			rowIdx += gSize;
		}

		// ---------------------------------------------------
		// 🟦 FOOTER (uses value from AgendaOfficerEntity)
		// ---------------------------------------------------
		rowIdx += 2;

		Row f1 = sheet.createRow(rowIdx++);
		f1.createCell(0).setCellValue("सल्ला प्र.अ.अ.यांना माघ्य असे.");
		sheet.addMergedRegion(new CellRangeAddress(f1.getRowNum(), f1.getRowNum(), 0, 10));

		rowIdx++;

		Row f2 = sheet.createRow(rowIdx++);
		f2.createCell(0).setCellValue(footerName);
		sheet.addMergedRegion(new CellRangeAddress(f2.getRowNum(), f2.getRowNum(), 0, 10));

		Row f3 = sheet.createRow(rowIdx++);
		f3.createCell(0).setCellValue("उप अधीक्षक अभियंता");
		sheet.addMergedRegion(new CellRangeAddress(f3.getRowNum(), f3.getRowNum(), 0, 10));

		Row f4 = sheet.createRow(rowIdx++);
		f4.createCell(0).setCellValue("पुणे पाटबंधारे प्रकल्प मंडळ");
		sheet.addMergedRegion(new CellRangeAddress(f4.getRowNum(), f4.getRowNum(), 0, 10));

		Row f5 = sheet.createRow(rowIdx++);
		f5.createCell(0).setCellValue("पुणे.");
		sheet.addMergedRegion(new CellRangeAddress(f5.getRowNum(), f5.getRowNum(), 0, 10));

		// Autosize columns
		for (int i = 0; i <= 10; i++)
			sheet.autoSizeColumn(i);

		// Return file
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		workbook.write(out);
		workbook.close();

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=agenda.xlsx");

		return ResponseEntity.ok().headers(headers).contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
				.body(new InputStreamResource(in));
	}

	// helper for merged row
	private int createMergedRow(Sheet sheet, int rowIdx, String text, int lastCol) {
		Row r = sheet.createRow(rowIdx);
		r.createCell(0).setCellValue(text);
		sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, lastCol));
		return rowIdx + 1;
	}

	private String convertKey(String key) {
		return Character.toUpperCase(key.charAt(0)) + key.substring(1).replaceAll("([a-z])([A-Z])", "$1 $2");
	}

}
