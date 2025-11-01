package com.pipc.dashboard.establishment.request;

import java.util.List;

import lombok.Data;

@Data
public class LeaveRequest {
	private Long rowId;
	private String year;
	private String month;
	private String date; // yyyy-MM-dd format
	private String officeOrderNumber;
	private String officeName;
	private String subjectReference;
	private String flag;

	private ApplicantDetails applicantDetails;
	private LeaveDetails leaveDetails;
	private RejoiningDetails rejoiningDetails;
	private HolidayJoinApproval holidayJoinApproval;
	private Certification certification;
	private SignatoryDetails signatoryDetails;

	private List<CopyDetail> copies;

}
