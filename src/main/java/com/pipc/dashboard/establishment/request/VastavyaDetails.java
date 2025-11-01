package com.pipc.dashboard.establishment.request;

import lombok.Data;

@Data
public class VastavyaDetails {
	private Long subId;
	private String vastavyaPrakar;
	private Double pratyakshaKharch;
	private String anugyaRakkam;
	private Double deyaRakkam;
	private String flag;
}