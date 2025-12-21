package com.pipc.dashboard.serviceimpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.pipc.dashboard.service.UploadDownloadService;
import com.pipc.dashboard.uploaddownload.repository.UploadDownloadEntity;
import com.pipc.dashboard.uploaddownload.repository.UploadDownloadRepo;
import com.pipc.dashboard.utility.UploadDownloadListDTO;

@Service
public class UploadDownloadServiceImpl implements UploadDownloadService {

	private final UploadDownloadRepo uploadDownloadRepo;

	public UploadDownloadServiceImpl(UploadDownloadRepo uploadDownloadRepo) {
		this.uploadDownloadRepo = uploadDownloadRepo;
	}

	private static final String UPLOAD_DIR = "/var/www/app/uploads/";
	@Value("${file.upload-dir}")
	private String uploadDir;

	public String saveFile(MultipartFile file, String name, String date, String type) throws IOException {

		// -------- DATE SPLIT --------
		LocalDate parsedDate = LocalDate.parse(date);

		String year = String.valueOf(parsedDate.getYear());
		String month = parsedDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
		String day = String.valueOf(parsedDate.getDayOfMonth());

		// -------- FOLDER STRUCTURE --------
		Path folderPath = Paths.get(uploadDir, sanitize(type), sanitize(name), year, month, day);

		Files.createDirectories(folderPath);

		// -------- FILE SAVE --------
		String storedFileName = file.getOriginalFilename();

		Path filePath = folderPath.resolve(storedFileName);

		Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

		// -------- DB SAVE --------
		UploadDownloadEntity entity = new UploadDownloadEntity();
		entity.setType(type);
		entity.setName(name);
		entity.setYear(year);
		entity.setMonth(month);
		entity.setDay(day);
		entity.setOriginalFileName(file.getOriginalFilename());
		entity.setStoredFileName(storedFileName);
		entity.setFilePath(filePath.toString());
		entity.setFileSize(file.getSize());
		entity.setContentType(file.getContentType());
		entity.setUploadedAt(LocalDateTime.now());

		uploadDownloadRepo.save(entity);
		return storedFileName;
	}

	private String sanitize(String value) {
		return value.replace(" ", "_");
	}

	@Override
	public Object getFileList(String type, String damName, String year, String month, String day) {

		List<UploadDownloadEntity> entities;

		if (day != null && !day.isBlank()) {
			entities = uploadDownloadRepo.findByTypeAndNameAndYearAndMonthAndDay(type, damName, year, month, day);
		} else if (month != null && !month.isBlank()) {
			entities = uploadDownloadRepo.findByTypeAndNameAndYearAndMonth(type, damName, year, month);
		} else {
			entities = uploadDownloadRepo.findByTypeAndNameAndYear(type, damName, year);
		}

		return entities.stream().map(this::toDto).collect(Collectors.toList());
	}

	private UploadDownloadListDTO toDto(UploadDownloadEntity e) {
		UploadDownloadListDTO dto = new UploadDownloadListDTO();
		dto.setId(e.getId()); // 🔥 ID yahin se milegi
		dto.setOriginalFileName(e.getOriginalFileName());
		dto.setType(e.getType());
		dto.setName(e.getName());
		dto.setYear(e.getYear());
		dto.setMonth(e.getMonth());
		dto.setDay(e.getDay());
		dto.setFileSize(e.getFileSize());
		dto.setUploadedAt(e.getUploadedAt());
		return dto;
	}

	@Override
	public Resource getSingleFile(Long id) throws Exception {
		UploadDownloadEntity e = uploadDownloadRepo.findById(id)
				.orElseThrow(() -> new RuntimeException("File not found"));

		Path path = Paths.get(e.getFilePath());
		if (!Files.exists(path)) {
			throw new RuntimeException("File missing on server");
		}
		return new UrlResource(path.toUri());
	}

	@Override
	public String writeZip(String type, String year, String month, ZipOutputStream zos) throws IOException {
		 List<UploadDownloadEntity> list =
	                (month != null && !month.isBlank())
	                        ? uploadDownloadRepo.findByTypeAndYearAndMonth(type, year, month)
	                        : uploadDownloadRepo.findByTypeAndYear(type, year);

	        for (UploadDownloadEntity e : list) {
	            Path path = Paths.get(e.getFilePath());
	            if (!Files.exists(path)) continue;

	            String entry =
	                    e.getName().replace(" ", "_")
	                            + "/" + e.getOriginalFileName();

	            zos.putNextEntry(new ZipEntry(entry));
	            Files.copy(path, zos);
	            zos.closeEntry();
	        }
			return month;
	    }
	}


