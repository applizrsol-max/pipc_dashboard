package com.pipc.dashboard.establishment.request;

import java.util.List;

import lombok.Data;

@Data
public class MedicalBillData {
	private Long rowId;
	private String title;
	private List<String> reference;
	private EmployeeDetails employeeDetails;
	private ApprovalDetails approvalDetails;
	private List<KharchaTapsil> kharchaTapsil;
	private VaidyakKharchaPariganana vaidyakKharchaPariganana;
	private String flag;

}