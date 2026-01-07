package com.pipc.dashboard.bhusmapadan.request;

import java.util.List;

import lombok.Data;

@Data
public class PraptraMasterDataRequest {

	private String year;
	private List<ProjectBlock> projects;
	
}
