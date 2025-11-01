package com.pipc.dashboard.establishment.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "medical_bill_master")
@Data
public class MedicalBillMasterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // Primary key

    @Column(unique = true)
    private Long rowId;  // Row ID for business identification

    private String title;
    private String period;
    private String month;
    private String year;

    private String createdBy;
    private String updatedBy;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    // Relations
    @OneToMany(mappedBy = "medicalBill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReferenceEntity> references = new ArrayList<>();

    @OneToOne(mappedBy = "medicalBill", cascade = CascadeType.ALL, orphanRemoval = true)
    private EmployeeDetailsEntity employeeDetails;

    @OneToOne(mappedBy = "medicalBill", cascade = CascadeType.ALL, orphanRemoval = true)
    private ApprovalDetailsEntity approvalDetails;

    @OneToMany(mappedBy = "medicalBill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KharchaTapsilEntity> kharchaTapsil = new ArrayList<>();

    @OneToOne(mappedBy = "medicalBill", cascade = CascadeType.ALL, orphanRemoval = true)
    private VaidyakKharchaParigananaEntity vaidyakKharchaPariganana;
}