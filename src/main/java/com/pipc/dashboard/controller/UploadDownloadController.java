package com.pipc.dashboard.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipOutputStream;

import org.slf4j.MDC;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/pipc/dashboard/uploadDownload")
@RequiredArgsConstructor
public class UploadDownloadController {

	private final UploadDownloadBusiness uploadDownloadBusiness;

	/*
	 * ========================= UPLOAD FILE =========================
	 */
	@PostMapping("/uploadFile")
	public BaseResponse uploadFile(@RequestParam MultipartFile file, @RequestParam String name,
			@RequestParam String date, @RequestParam String type) throws IOException {

		log.info("Upload file | type={} | corrId={}", type, MDC.get("correlationId"));

		uploadDownloadBusiness.saveFile(file, name, date, type);
		return new BaseResponse(new ApplicationError("200", "File uploaded successfully"));
	}

	/*
	 * ========================= GET FILE LIST =========================
	 */
	@GetMapping("/getFileList")
	public ResponseEntity<Object> getFileList(@RequestParam String type, @RequestParam(required = false) String name,
			@RequestParam String year, @RequestParam(required = false) String month,
			@RequestParam(required = false) String day) {

		log.debug("Get file list | type={} year={} | corrId={}", type, year, MDC.get("correlationId"));

		return ResponseEntity.ok(uploadDownloadBusiness.getFileList(type, name, year, month, day));
	}

	/*
	 * ========================= DOWNLOAD FILE / ZIP =========================
	 */
	@SkipLogging
	@GetMapping("/downloadFile")
	public void downloadFile(@RequestParam(required = false) Long id, @RequestParam(required = false) String type,
			@RequestParam(required = false) String year, @RequestParam(required = false) String month,
			HttpServletResponse response) throws Exception {

		// -------- SINGLE FILE --------
		if (id != null) {

			log.info("Download single file | id={} | corrId={}", id, MDC.get("correlationId"));

			Resource resource = uploadDownloadBusiness.getSingleFile(id);

			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + resource.getFilename() + "\"");

			try (InputStream in = resource.getInputStream()) {
				in.transferTo(response.getOutputStream());
			}
			return;
		}

		// -------- BULK ZIP --------
		if (year != null && type != null) {

			String zipName = type + "_" + year + (month != null ? "_" + month : "") + ".zip";

			log.info("Download ZIP | {} | corrId={}", zipName, MDC.get("correlationId"));

			response.setContentType("application/zip");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + zipName + "\"");

			try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
				uploadDownloadBusiness.writeZip(type, year, month, zos);
				zos.finish();
			}
			return;
		}

		// -------- INVALID REQUEST --------
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters for download");
	}
}
