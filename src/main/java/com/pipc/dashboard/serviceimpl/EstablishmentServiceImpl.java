package com.pipc.dashboard.serviceimpl;

import java.util.ArrayList;
import java.util.Optional;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pipc.dashboard.establishment.repository.ApprovalDetailsEntity;
import com.pipc.dashboard.establishment.repository.ApprovalDetailsRepository;
import com.pipc.dashboard.establishment.repository.EmployeeDetailsEntity;
import com.pipc.dashboard.establishment.repository.EmployeeDetailsRepository;
import com.pipc.dashboard.establishment.repository.KharchaTapsilEntity;
import com.pipc.dashboard.establishment.repository.KharchaTapsilRepository;
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
import com.pipc.dashboard.establishment.request.MedicalBillData;
import com.pipc.dashboard.establishment.request.MedicalBillRequest;
import com.pipc.dashboard.establishment.response.MedicalBillResponse;
import com.pipc.dashboard.service.EstablishmentService;
import com.pipc.dashboard.utility.ApplicationError;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service

public class EstablishmentServiceImpl implements EstablishmentService {

	private final MedicalBillMasterRepository masterRepo;
	private final ReferenceRepository refRepo;
	private final EmployeeDetailsRepository empRepo;
	private final ApprovalDetailsRepository apprRepo;
	private final KharchaTapsilRepository kharchaRepo;
	private final VaidyakKharchaParigananaRepository vaidyaRepo;
	private final VaidyakTapshilRepository tapshilRepo;
	private final VastavyaDetailsRepository vastavyaRepo;

	@Autowired
	public EstablishmentServiceImpl(MedicalBillMasterRepository masterRepo, ReferenceRepository refRepo,
			EmployeeDetailsRepository empRepo, ApprovalDetailsRepository apprRepo, KharchaTapsilRepository kharchaRepo,
			VaidyakKharchaParigananaRepository vaidyaRepo, VaidyakTapshilRepository tapshilRepo,
			VastavyaDetailsRepository vastavyaRepo) {
		this.apprRepo = apprRepo;
		this.empRepo = empRepo;
		this.kharchaRepo = kharchaRepo;
		this.masterRepo = masterRepo;
		this.refRepo = refRepo;
		this.tapshilRepo = tapshilRepo;
		this.vaidyaRepo = vaidyaRepo;
		this.vastavyaRepo = vastavyaRepo;
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

}
