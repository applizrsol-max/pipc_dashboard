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

@Entity
@Table(name = "medical_bill_employee_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String employeeName;
    private String designation;
    private String department;
    private String patientName;
    private String hospitalName;

    private String fromDate;
    private String toDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_bill_id")
    private MedicalBillMasterEntity medicalBill;
}
