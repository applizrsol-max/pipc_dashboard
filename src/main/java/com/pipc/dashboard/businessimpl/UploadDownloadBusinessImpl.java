package com.pipc.dashboard.businessimpl;

import java.io.IOException;
import java.util.zip.ZipOutputStream;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.pipc.dashboard.business.UploadDownloadBusiness;
import com.pipc.dashboard.service.UploadDownloadService;

@Component
public class UploadDownloadBusinessImpl implements UploadDownloadBusiness {

	private final UploadDownloadService uploadDownloadService;

	public UploadDownloadBusinessImpl(UploadDownloadService uploadDownloadService) {
		this.uploadDownloadService = uploadDownloadService;
	}

	@Override
	public String saveFile(MultipartFile file, String name, String date, String type) throws IOException {
		return this.uploadDownloadService.saveFile(file, name, date, type);

	}

	@Override
	public Object getFileList(String type, String damName, String year, String month, String day) {
		return this.uploadDownloadService.getFileList(type, damName, year, month, day);
	}

	@Override
	public Resource getSingleFile(Long id) throws Exception {
		return this.uploadDownloadService.getSingleFile(id);
	}

	@Override
	public String writeZip(String type, String year, String month, ZipOutputStream zos) throws Exception {
		return this.uploadDownloadService.writeZip(type, year, month, zos);

	}

}
