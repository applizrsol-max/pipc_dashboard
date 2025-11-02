package com.pipc.dashboard.serviceimpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import com.pipc.dashboard.establishment.request.AppealRequest;
import com.pipc.dashboard.establishment.request.EmployeePostingRequest;
import com.pipc.dashboard.establishment.request.IncomeTaxDeductionRequest;
import com.pipc.dashboard.establishment.request.LeaveRequest;
import com.pipc.dashboard.establishment.request.MedicalBillData;
import com.pipc.dashboard.establishment.request.MedicalBillRequest;
import com.pipc.dashboard.establishment.request.PassportNocRequest;
import com.pipc.dashboard.establishment.response.AppealResponse;
import com.pipc.dashboard.establishment.response.EmployeePostingResponse;
import com.pipc.dashboard.establishment.response.IncomeTaxDeductionResponse;
import com.pipc.dashboard.establishment.response.LeaveResponse;
import com.pipc.dashboard.establishment.response.MedicalBillResponse;
import com.pipc.dashboard.establishment.response.PassportNocResponse;
import com.pipc.dashboard.service.EstablishmentService;
import com.pipc.dashboard.utility.ApplicationError;

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

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	public EstablishmentServiceImpl(MedicalBillMasterRepository masterRepo, ReferenceRepository refRepo,
			EmployeeDetailsRepository empRepo, ApprovalDetailsRepository apprRepo, KharchaTapsilRepository kharchaRepo,
			VaidyakKharchaParigananaRepository vaidyaRepo, VaidyakTapshilRepository tapshilRepo,
			VastavyaDetailsRepository vastavyaRepo, LeaveRepository leaveRepository, AppealRepository appealRepository,
			EmployeePostingRepository employeePostingRepository,
			IncomeTaxDeductionRepository incomeTaxDeductionRepository, PassportNocRepository passportNocRepository) {
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

	public AppealResponse saveOrUpdateAppeal(AppealRequest dto) {
		AppealResponse response = new AppealResponse();
		ApplicationError error = new ApplicationError();

		response.setData(new ArrayList<>());

		try {
			String username = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
			String flag = Optional.ofNullable(dto.getFlag()).map(String::toUpperCase).orElse(null);

			Optional<AppealEntity> existingOpt = Optional.empty();
			if (dto.getRowId() != null)
				existingOpt = appealRepository.findByRowId(dto.getRowId());

			// 🗑️ DELETE
			if ("D".equals(flag)) {
				if (existingOpt.isPresent()) {
					appealRepository.delete(existingOpt.get());
					response.setMessage("Record deleted successfully.");
					error.setErrorCode("200");
					error.setErrorDescription("Success");
				} else {
					response.setMessage("No record found for deletion.");
					error.setErrorCode("404");
					error.setErrorDescription("Not Found");
				}
				response.setErrorDetails(error);
				return response;
			}

			// 🆕 or 🔁 CREATE / UPDATE
			AppealEntity entity = existingOpt.orElse(new AppealEntity());

			// decide flag automatically
			String resolvedFlag = existingOpt.isPresent() ? "U" : "C";

			entity.setRowId(dto.getRowId());
			entity.setYear(dto.getYear());
			entity.setFlag(resolvedFlag);
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

			LocalDateTime now = LocalDateTime.now();

			if (entity.getId() == null) {
				// new record
				entity.setCreatedBy(username);
				entity.setCreatedDate(now);
				entity.setUpdatedBy(username);
				entity.setUpdatedDate(now);
			} else {
				// update
				entity.setUpdatedBy(username);
				entity.setUpdatedDate(now);
				if (entity.getCreatedBy() == null)
					entity.setCreatedBy(username);
				if (entity.getCreatedDate() == null)
					entity.setCreatedDate(now);
			}

			AppealEntity saved = appealRepository.save(entity);

			Map<String, Object> map = new LinkedHashMap<>();
			map.put("rowId", saved.getRowId());
			map.put("year", saved.getYear());
			map.put("flag", saved.getFlag());
			map.put("apeelkaracheNav", saved.getApeelkaracheNav());
			map.put("status", "SUCCESS");
			response.getData().add(map);

			response.setMessage(
					resolvedFlag.equals("U") ? "Record updated successfully." : "Record created successfully.");
			error.setErrorCode("200");
			error.setErrorDescription("Success");

		} catch (Exception e) {
			log.error("Error in saveOrUpdateAppeal: {}", e.getMessage(), e);
			response.setMessage("Error while saving record.");
			error.setErrorCode("500");
			error.setErrorDescription(e.getMessage());
		}

		response.setErrorDetails(error);
		return response;
	}

	@Override
	public AppealResponse getAppealData(String year, int page, int size) {
		AppealResponse response = new AppealResponse();
		ApplicationError error = new ApplicationError();
		response.setData(new ArrayList<>());

		try {
			Pageable pageable = PageRequest.of(page, size, Sort.by("updatedDate").descending());
			Page<AppealEntity> appealPage;

			// 🧭 Filter logic
			if (year != null) {
				appealPage = appealRepository.findByYear(year, pageable);
			} else {
				appealPage = appealRepository.findAll(pageable);
			}

			for (AppealEntity e : appealPage.getContent()) {
				Map<String, Object> map = new LinkedHashMap<>();

				map.put("id", e.getId());
				map.put("rowId", e.getRowId());
				map.put("year", e.getYear());
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

				// 🧩 Dynamic columns
				if (e.getDynamicColumns() != null && !e.getDynamicColumns().isEmpty()) {
					Map<String, Object> dynamic = objectMapper.convertValue(e.getDynamicColumns(), Map.class);
					map.put("dynamicColumns", dynamic);
				}

				// Audit Info
				map.put("createdBy", e.getCreatedBy());
				map.put("createdDate", e.getCreatedDate());
				map.put("updatedBy", e.getUpdatedBy());
				map.put("updatedDate", e.getUpdatedDate());

				response.getData().add(map);
			}

			// Pagination Meta
			Map<String, Object> meta = new LinkedHashMap<>();
			meta.put("pageNumber", appealPage.getNumber());
			meta.put("pageSize", appealPage.getSize());
			meta.put("totalElements", appealPage.getTotalElements());
			meta.put("totalPages", appealPage.getTotalPages());
			response.setMeta(meta);

			response.setMessage("Appeal register data fetched successfully.");
			error.setErrorCode("200");
			error.setErrorDescription("Success");

		} catch (Exception ex) {
			log.error("Error fetching appeal data: {}", ex.getMessage(), ex);
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

}
