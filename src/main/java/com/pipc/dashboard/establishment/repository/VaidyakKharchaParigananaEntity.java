package com.pipc.dashboard.establishment.repository;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "medical_bill_vaidyak_pariganana")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "medicalBill")

public class VaidyakKharchaParigananaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String shasanNirdesh;
	private String employeeName;
	private String designation;
	private String patientName;
	private String hospitalName;
	private String fromDate;
	private String toDate;

	// ✅ Parent relation
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "medical_bill_id")
	private MedicalBillMasterEntity medicalBill;

	// ✅ Child relation 1: Tapshil List
	@OneToMany(mappedBy = "vaidyakKharchaPariganana", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<VaidyakTapshilEntity> tapshilList = new ArrayList<>();

	// ✅ Child relation 2: Vastavya Details List
	@OneToMany(mappedBy = "vaidyakKharchaPariganana", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<VastavyaDetailsEntity> vastavyaDetailsList = new ArrayList<>();

	@OneToMany(mappedBy = "vaidyakKharchaPariganana", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<VaidyakExcludedDetailsEntity> excludedDetails;

}
