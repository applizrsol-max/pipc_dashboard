package com.pipc.dashboard.establishment.request;

import lombok.Data;

@Data
public class ExcludedDetails {
	private Long subId;
	private String description;
	private Double totalRakkam;
	private String flag;
}
