package com.pipc.dashboard.establishment.request;

import java.util.List;

import lombok.Data;

@Data
public class VaidyakKharchaPariganana {
    private String shasanNirdesh;
    private String employeeName;
    private String designation;
    private String patientName;
    private String hospitalName;
    private TreatmentPeriod treatmentPeriod;
    private List<VaidyakTapshil> tapshil;
    private List<VastavyaDetails> vastavyaDetails;
    private List<ExcludedDetails> excludedDetails;
}