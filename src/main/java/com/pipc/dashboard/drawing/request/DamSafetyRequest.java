package com.pipc.dashboard.drawing.request;

import java.util.List;

import lombok.Data;

@Data
public class DamSafetyRequest {
	private DamMetaData damMetaData;
	private List<DamDynamicRow> rows;
}
