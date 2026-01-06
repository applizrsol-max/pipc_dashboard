package com.pipc.dashboard.establishment.request;

import lombok.Data;

@Data
public class MonthlyDetailBhanini {
	private String month; // April-2024 etc.

	private Long purnaRupyaMadhilVargni; // basic
	private Long kadhlelyaRakmanchiParatFed; // allowance
	private Long ekun; // total

	private Long kadhlelyaRakam; // deduction
	private Long jayavarilVyajachi; // interest / arrears

	private String sheraOrArrears;
}
