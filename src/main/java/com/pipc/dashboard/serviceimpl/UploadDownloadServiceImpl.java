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

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UploadDownloadServiceImpl implements UploadDownloadService {

	private final UploadDownloadRepo uploadDownloadRepo;

	@Value("${file.upload-dir}")
	private String uploadDir;

	public UploadDownloadServiceImpl(UploadDownloadRepo uploadDownloadRepo) {
		this.uploadDownloadRepo = uploadDownloadRepo;
	}

	/* ============================ SAVE FILE ============================ */

	@Override
	public String saveFile(MultipartFile file, String name, String date, String type) throws IOException {

		log.info("START saveFile | type={} | name={} | date={}", type, name, date);

		try {

			LocalDate parsedDate = LocalDate.parse(date);

			String year = String.valueOf(parsedDate.getYear());
			String month = parsedDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
			String day = String.valueOf(parsedDate.getDayOfMonth());

			Path folderPath = Paths.get(uploadDir, sanitize(type), sanitize(name), year, month, day);

			Files.createDirectories(folderPath);

			String storedFileName = file.getOriginalFilename();
			Path filePath = folderPath.resolve(storedFileName);

			Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

			UploadDownloadEntity entity = new UploadDownloadEntity();
			entity.setType(type);
			entity.setName(name);
			entity.setYear(year);
			entity.setMonth(month);
			entity.setDay(day);
			entity.setOriginalFileName(storedFileName);
			entity.setStoredFileName(storedFileName);
			entity.setFilePath(filePath.toString());
			entity.setFileSize(file.getSize());
			entity.setContentType(file.getContentType());
			entity.setUploadedAt(LocalDateTime.now());

			uploadDownloadRepo.save(entity);

			log.info("SUCCESS saveFile | file={} | path={}", storedFileName, filePath);
			return storedFileName;

		} catch (IOException e) {
			log.error("ERROR saveFile | type={} | name={}", type, name, e);
			throw e;
		}
	}

	private String sanitize(String value) {
		return value == null ? "" : value.replace(" ", "_");
	}

	/* ============================ FILE LIST ============================ */

	@Override
	public Object getFileList(String type, String damName, String year, String month, String day) {

		log.info("START getFileList | type={} | name={} | year={} | month={} | day={}", type, damName, year, month,
				day);

		List<UploadDownloadEntity> entities;
		String name = damName == null ? "" : damName;

		if (day != null && !day.isBlank()) {
			entities = uploadDownloadRepo.findByTypeAndYearAndMonthAndDayAndNameContainingIgnoreCase(type, year, month,
					day, name);

		} else if (month != null && !month.isBlank()) {
			entities = uploadDownloadRepo.findByTypeAndYearAndMonthAndNameContainingIgnoreCase(type, year, month, name);

		} else {
			entities = uploadDownloadRepo.findByTypeAndYearAndNameContainingIgnoreCase(type, year, name);
		}

		log.info("SUCCESS getFileList | records={}", entities.size());

		return entities.stream().map(this::toDto).collect(Collectors.toList());
	}

	private UploadDownloadListDTO toDto(UploadDownloadEntity e) {
		UploadDownloadListDTO dto = new UploadDownloadListDTO();
		dto.setId(e.getId());
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

	/* ============================ SINGLE FILE ============================ */

	@Override
	public Resource getSingleFile(Long id) throws Exception {

		log.info("START getSingleFile | id={}", id);

		UploadDownloadEntity entity = uploadDownloadRepo.findById(id)
				.orElseThrow(() -> new RuntimeException("File not found"));

		Path path = Paths.get(entity.getFilePath());

		if (!Files.exists(path)) {
			log.error("File missing on server | path={}", path);
			throw new RuntimeException("File missing on server");
		}

		log.info("SUCCESS getSingleFile | file={}", entity.getOriginalFileName());
		return new UrlResource(path.toUri());
	}

	/* ============================ ZIP WRITE ============================ */

	@Override
	public String writeZip(String type, String year, String month, ZipOutputStream zos) throws IOException {

		log.info("START writeZip | type={} | year={} | month={}", type, year, month);

		List<UploadDownloadEntity> list = (month != null && !month.isBlank())
				? uploadDownloadRepo.findByTypeAndYearAndMonth(type, year, month)
				: uploadDownloadRepo.findByTypeAndYear(type, year);

		int count = 0;

		for (UploadDownloadEntity e : list) {

			Path path = Paths.get(e.getFilePath());
			if (!Files.exists(path)) {
				log.warn("Skipping missing file | {}", path);
				continue;
			}

			String entryName = sanitize(e.getName()) + "/" + e.getOriginalFileName();

			zos.putNextEntry(new ZipEntry(entryName));
			Files.copy(path, zos);
			zos.closeEntry();
			count++;
		}

		log.info("SUCCESS writeZip | filesAdded={}", count);
		return month;
	}
}
