package com.pipc.dashboard.establishment.request;

import lombok.Data;

@Data
public class VaidyakTapshil {
	private Long subId;
	private int akr;
	private String tapsil;
	private Double ekunKharch;
	private Double nadeya;
	private String flag;
}