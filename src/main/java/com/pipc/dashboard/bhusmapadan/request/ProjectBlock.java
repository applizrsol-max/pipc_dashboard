package com.pipc.dashboard.bhusmapadan.request;

import java.util.List;

import lombok.Data;

@Data
public class ProjectBlock {
	private String projectName;
	private List<PraptraMasterDataRowRequest> rows;
}
