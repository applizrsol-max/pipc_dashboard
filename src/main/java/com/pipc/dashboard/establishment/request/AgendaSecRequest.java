package com.pipc.dashboard.establishment.request;

import java.util.List;

import lombok.Data;

@Data
public class AgendaSecRequest {
	private AgendaSecMeta meta;
	private List<AgendaSecRow> rows;
	private String sectionFlag;
}
