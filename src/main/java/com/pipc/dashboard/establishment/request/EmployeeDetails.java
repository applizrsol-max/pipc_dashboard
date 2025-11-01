package com.pipc.dashboard.establishment.request;

import lombok.Data;

@Data
public class EmployeeDetails {
    private String employeeName;
    private String designation;
    private String department;
    private String patientName;
    private String hospitalName;
    private TreatmentPeriod treatmentPeriod;
}
