package com.pipc.dashboard.establishment.request;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
public class AppealRequest2 {
	private Long rowId;
	private String year;
	private String date;
	private String flag; // C = Create, U = Update, D = Delete
	private Long deleteId;

	private String arjachaNondaniKramank;
	private String arjdarNavPatta;
	private String arjPraptDinank;
	private String darikhvReshBabli;
	private String mahitiChaPrakar;
	private String maangItlelyaMahitichiRuprekha;
	private String yogyMahitiAaheKa;
	private String arjShulk;
	private String shulkPramanitDinank;
	private String mahitiDilyaDinank;
	private String mahitiNghitAaheKa;
	private String konKshaAwarNokri;
	private String prathamAppeal;
	private String shera;

	private JsonNode dynamicColumns;
}
