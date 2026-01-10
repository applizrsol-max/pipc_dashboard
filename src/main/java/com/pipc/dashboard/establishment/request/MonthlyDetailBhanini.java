package com.pipc.dashboard.establishment.request;

import lombok.Data;

@Data
public class MonthlyDetailBhanini {
	private String month;

	private String purnaRupyaMadhilVargniRu;
	private String purnaRupyaMadhilVargniPay;

	private String kadhlelyaRakmanchiParatFedRu;
	private String kadhlelyaRakmanchiParatFedPay;

	private Integer ekunRu;
	private String ekunPay;

	private Integer kadhlelyaRakamRu;
	private String kadhlelyaRakamPay;

	private Integer jayavarilVyajachiRu;
	private String jayavarilVyajachiPay;

	private String sheraOrArrears;
}
