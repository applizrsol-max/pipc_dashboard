package com.pipc.dashboard.utility;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UploadDownloadListDTO {

	private Long id;
	private String originalFileName;
	private String type;
	private String name;
	private String year;
	private String month;
	private String day;
	private Long fileSize;
	private LocalDateTime uploadedAt;
}