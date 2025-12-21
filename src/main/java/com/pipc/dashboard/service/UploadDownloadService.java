package com.pipc.dashboard.service;

import java.io.IOException;
import java.util.zip.ZipOutputStream;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface UploadDownloadService {

	String saveFile(MultipartFile file, String name, String date, String type) throws IOException;

	Object getFileList(String type, String damName, String year, String month, String day);

	Resource getSingleFile(Long id) throws Exception;

	String writeZip(String type, String year, String month, ZipOutputStream zos) throws IOException;
	

}
