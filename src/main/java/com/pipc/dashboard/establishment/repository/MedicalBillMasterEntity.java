package com.pipc.dashboard.establishment.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "medical_bill_master")
@Data
@ToString(exclude = {
	    "references",
	    "employeeDetails",
	    "approvalDetails",
	    "kharchaTapsil",
	    "vaidyakKharchaPariganana"
	})

public class MedicalBillMasterEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // Primary key

	@Column(unique = true)
	private Long rowId; // Row ID for business identification

	private String title;
	private String period;
	private String month;
	private String year;
	@Column(name = "bill_date")
	private String billDate;


	private String createdBy;
	private String updatedBy;

	private LocalDateTime createdAt = LocalDateTime.now();
	private String flag;  // 'C' for create, 'U' for update, 'D' for delete
	private LocalDateTime updatedTime;

	// Relations
	@OneToMany(mappedBy = "medicalBill", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<ReferenceEntity> references = new ArrayList<>();

	@OneToOne(mappedBy = "medicalBill", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private EmployeeDetailsEntity employeeDetails;

	@OneToOne(mappedBy = "medicalBill", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private ApprovalDetailsEntity approvalDetails;

	@OneToMany(mappedBy = "medicalBill", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<KharchaTapsilEntity> kharchaTapsil = new ArrayList<>();

	@OneToOne(mappedBy = "medicalBill", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private VaidyakKharchaParigananaEntity vaidyakKharchaPariganana;

	
}