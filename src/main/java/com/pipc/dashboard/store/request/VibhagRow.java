package com.pipc.dashboard.store.request;

import lombok.Data;

@Data
public class VibhagRow {

	private Integer a_kr; // अ. क्र

	private String varsh; // वर्ष
	private String pralambitParichhedTapsheel; // प्रलंबित परिच्छेदांचा तपशील
	private Integer pralambitParichhedSankhya; // प्रलंबित परिच्छेदांची संख्या
	private Sadyasthiti sadyasthiti; // सद्यस्थिती
	private Integer rowId;
	private String flag;
	private Long deleteId;
}
