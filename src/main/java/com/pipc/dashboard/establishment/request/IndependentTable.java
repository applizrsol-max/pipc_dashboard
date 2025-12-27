package com.pipc.dashboard.establishment.request;

import java.util.List;

import lombok.Data;

@Data
public class IndependentTable {
	private String title;
	private List<MahaparRegisterRowRequest> rows;
}
