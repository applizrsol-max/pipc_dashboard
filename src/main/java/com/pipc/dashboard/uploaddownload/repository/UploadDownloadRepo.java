package com.pipc.dashboard.uploaddownload.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadDownloadRepo extends JpaRepository<UploadDownloadEntity, Long> {
	List<UploadDownloadEntity> findByTypeAndNameAndYear(String type, String damName, String year);

	List<UploadDownloadEntity> findByTypeAndNameAndYearAndMonth(String type, String damName, String year, String month);

	List<UploadDownloadEntity> findByTypeAndNameAndYearAndMonthAndDay(String type, String damName, String year,
			String month, String day);

	List<UploadDownloadEntity> findByTypeAndYear(String type, String year);

	List<UploadDownloadEntity> findByTypeAndYearAndMonth(String type, String year, String month);
}
