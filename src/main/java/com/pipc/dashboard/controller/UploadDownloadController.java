package com.pipc.dashboard.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pipc.dashboard.business.UploadDownloadBusiness;
import com.pipc.dashboard.logging.aspect.SkipLogging;
import com.pipc.dashboard.utility.ApplicationError;
import com.pipc.dashboard.utility.BaseResponse;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/pipc/dashboard/uploadDownload")
public class UploadDownloadController {

	private final UploadDownloadBusiness uploadDownloadBusiness;

	@Autowired
	public UploadDownloadController(UploadDownloadBusiness uploadDownloadBusiness) {
		this.uploadDownloadBusiness = uploadDownloadBusiness;
	}

	@PostMapping("/uploadFile")
	public BaseResponse uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("name") String name,
			@RequestParam("date") String date, @RequestParam("type") String type) throws IOException {

		uploadDownloadBusiness.saveFile(file, name, date, type);

		return new BaseResponse(new ApplicationError("200", "File uploaded successfully"));
	}

	@GetMapping("/getFileList")
	public ResponseEntity<Object> getFileList(@RequestParam String type, @RequestParam(required = false) String name,
			@RequestParam String year, @RequestParam(required = false) String month,
			@RequestParam(required = false) String day) {
		return ResponseEntity.ok(uploadDownloadBusiness.getFileList(type, name, year, month, day));
	}

	@SkipLogging
	@GetMapping("/downloadFile")
	public void downloadFile(@RequestParam(required = false) Long id, @RequestParam(required = false) String type,
			@RequestParam(required = false) String year, @RequestParam(required = false) String month,
			HttpServletResponse response) throws Exception {

		// ---------------- SINGLE FILE ----------------
		if (id != null) {

			Resource resource = uploadDownloadBusiness.getSingleFile(id);

			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + resource.getFilename() + "\"");

			try (InputStream in = resource.getInputStream()) {
				in.transferTo(response.getOutputStream());
			}
			return;
		}

		// ---------------- BULK ZIP ----------------
		if (year != null && type != null) {

			String zipName = type + "_" + year + (month != null ? "_" + month : "") + ".zip";

			response.setContentType("application/zip");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + zipName + "\"");

			try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
				uploadDownloadBusiness.writeZip(type, year, month, zos);
				zos.finish();
			}
			return;
		}

		// ---------------- INVALID ----------------
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters for download");
	}

}
