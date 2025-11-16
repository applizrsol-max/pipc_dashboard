package com.pipc.dashboard.establishment.repository;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "medical_bill_approval_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "medicalBill")

public class ApprovalDetailsEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String approvingAuthority;
	private String approvalDate;
	private Double approvalAmount;
	private String approvedBy;
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "medical_bill_id")
	private MedicalBillMasterEntity medicalBill;
}
