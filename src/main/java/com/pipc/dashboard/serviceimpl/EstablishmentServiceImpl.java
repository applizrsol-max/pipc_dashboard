package com.pipc.dashboard.serviceimpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipc.dashboard.establishment.repository.ApprovalDetailsEntity;
import com.pipc.dashboard.establishment.repository.ApprovalDetailsRepository;
import com.pipc.dashboard.establishment.repository.EmployeeDetailsEntity;
import com.pipc.dashboard.establishment.repository.EmployeeDetailsRepository;
import com.pipc.dashboard.establishment.repository.KharchaTapsilEntity;
import com.pipc.dashboard.establishment.repository.KharchaTapsilRepository;
import com.pipc.dashboard.establishment.repository.LeaveEntity;
import com.pipc.dashboard.establishment.repository.LeaveRepository;
import com.pipc.dashboard.establishment.repository.MedicalBillMasterEntity;
import com.pipc.dashboard.establishment.repository.MedicalBillMasterRepository;
import com.pipc.dashboard.establishment.repository.ReferenceEntity;
import com.pipc.dashboard.establishment.repository.ReferenceRepository;
import com.pipc.dashboard.establishment.repository.VaidyakExcludedDetailsEntity;
import com.pipc.dashboard.establishment.repository.VaidyakKharchaParigananaEntity;
import com.pipc.dashboard.establishment.repository.VaidyakKharchaParigananaRepository;
import com.pipc.dashboard.establishment.repository.VaidyakTapshilEntity;
import com.pipc.dashboard.establishment.repository.VaidyakTapshilRepository;
import com.pipc.dashboard.establishment.repository.VastavyaDetailsEntity;
import com.pipc.dashboard.establishment.repository.VastavyaDetailsRepository;
import com.pipc.dashboard.establishment.request.LeaveRequest;
import com.pipc.dashboard.establishment.request.MedicalBillData;
import com.pipc.dashboard.establishment.request.MedicalBillRequest;
import com.pipc.dashboard.establishment.response.LeaveResponse;
import com.pipc.dashboard.establishment.response.MedicalBillResponse;
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

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	public EstablishmentServiceImpl(MedicalBillMasterRepository masterRepo, ReferenceRepository refRepo,
			EmployeeDetailsRepository empRepo, ApprovalDetailsRepository apprRepo, KharchaTapsilRepository kharchaRepo,
			VaidyakKharchaParigananaRepository vaidyaRepo, VaidyakTapshilRepository tapshilRepo,
			VastavyaDetailsRepository vastavyaRepo, LeaveRepository leaveRepository) {
		this.apprRepo = apprRepo;
		this.empRepo = empRepo;
		this.kharchaRepo = kharchaRepo;
		this.masterRepo = masterRepo;
		this.refRepo = refRepo;
		this.tapshilRepo = tapshilRepo;
		this.vaidyaRepo = vaidyaRepo;
		this.vastavyaRepo = vastavyaRepo;
		this.leaveRepository = leaveRepository;
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

}
