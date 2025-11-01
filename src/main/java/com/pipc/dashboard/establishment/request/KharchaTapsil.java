package com.pipc.dashboard.establishment.request;

import lombok.Data;

@Data
public class KharchaTapsil {
	private Long subId;
	private int akr;
	private String tapsil;
	private double rakkam;
	private String flag; // only used for "D"
}