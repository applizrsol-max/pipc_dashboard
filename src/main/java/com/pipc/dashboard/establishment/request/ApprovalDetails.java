package com.pipc.dashboard.establishment.request;

import lombok.Data;

@Data
public class ApprovalDetails {
    private String approvingAuthority;
    private String approvalDate;
    private double approvalAmount;
}