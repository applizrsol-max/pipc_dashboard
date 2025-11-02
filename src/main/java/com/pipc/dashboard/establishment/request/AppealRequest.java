package com.pipc.dashboard.establishment.request;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
public class AppealRequest {
	private Long rowId;
	private String year;
	private String flag; // C, U, D

	private String apeelArjachaNondaniKramank;
	private String apeelkaracheNav;
	private String apeelArjKonakadeKelaAahe;
	private String apeelArjPraptJhalyachaDinank;
	private String pratidariYancheNavPatta;
	private String apeelarthiDurustReneSathiKaam;
	private String apeelchaThodkyaTathya;
	private String apeelArjacheShulk;
	private String konakadeApeelSwikarBharle;
	private String apeelArjachiVidhikShulkBharalDinank;
	private String konalyaJanmahitiAdhikariYanchikadeApeelKeleTathyaTapshil;
	private String apeelvarNirnayDilyachaDinank;
	private String shera;

	// For any future dynamic fields
	private JsonNode dynamicColumns;
}
