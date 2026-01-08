package com.pipc.dashboard.establishment.request;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
public class JeReturnRequest {

	private String year;
	private String upAdhikshakAbhiyanta;
	private List<DivisionDto> division;

	@Data
	public static class DivisionDto {
		private String karyalayacheNav;
		private List<RowDto> rows;
	}

	@Data
	public static class RowDto {
		private Long rowId;
		private Long deleteId;
		private String flag; // D for delete
		private JsonNode data;
	}
}
