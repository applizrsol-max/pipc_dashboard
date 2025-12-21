package com.pipc.dashboard.uploaddownload.repository;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "upload_download_file_details")
public class UploadDownloadEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String type;
	private String name;
	private String year;
	private String month;
	private String day;

	@Column(columnDefinition = "TEXT")
	private String originalFileName;

	@Column(columnDefinition = "TEXT")
	private String storedFileName;

	@Column(columnDefinition = "TEXT")
	private String filePath;

	private Long fileSize;
	private String contentType;
	private LocalDateTime uploadedAt;

	// getters & setters
}