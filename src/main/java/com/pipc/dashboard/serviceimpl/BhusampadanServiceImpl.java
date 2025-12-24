package com.pipc.dashboard.serviceimpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pipc.dashboard.bhusmapadan.repository.Praptra1MasterDataEntity;
import com.pipc.dashboard.bhusmapadan.repository.Praptra1MasterDataRepository;
import com.pipc.dashboard.bhusmapadan.repository.Praptra2MasterDataEntity;
import com.pipc.dashboard.bhusmapadan.repository.Praptra2MasterDataRepository;
import com.pipc.dashboard.bhusmapadan.repository.Praptra3MasterDataEntity;
import com.pipc.dashboard.bhusmapadan.repository.Praptra3MasterDataRepository;
import com.pipc.dashboard.bhusmapadan.repository.PraptraMasterDataEntity;
import com.pipc.dashboard.bhusmapadan.repository.PraptraMasterDataRepository;
import com.pipc.dashboard.bhusmapadan.request.PraptraMasterDataRequest;
import com.pipc.dashboard.bhusmapadan.request.PraptraMasterDataRowRequest;
import com.pipc.dashboard.bhusmapadan.response.PraptraMasterDataResponse;
import com.pipc.dashboard.service.BhusampadanService;
import com.pipc.dashboard.utility.ApplicationError;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class BhusampadanServiceImpl implements BhusampadanService {

	private final PraptraMasterDataRepository praptraMasterDataRepository;
	private final Praptra1MasterDataRepository praptra1MasterDataRepository;
	private final Praptra2MasterDataRepository praptra2MasterDataRepository;
	private final Praptra3MasterDataRepository praptra3MasterDataRepository;

	@Override
	@Transactional
	public PraptraMasterDataResponse processPraptraMasterData(PraptraMasterDataRequest request) {
		String currentUser = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
		PraptraMasterDataResponse response = new PraptraMasterDataResponse();

		try {

			for (PraptraMasterDataRowRequest row : request.getRows()) {

				// ✅ mandatory validation
				if (row.getData() == null || row.getData().get("projectName") == null) {

					throw new RuntimeException("projectName is mandatory in data");
				}

				String projectName = row.getData().get("projectName").asText();

				// 🔴 HARD DELETE
				if ("D".equalsIgnoreCase(row.getFlag())) {
					praptraMasterDataRepository.deleteByYearDeleteIdAndProjectName(request.getYear(), row.getDeleteId(),
							projectName);
					continue;
				}

				// 🔍 EXIST CHECK
				Optional<PraptraMasterDataEntity> existing = praptraMasterDataRepository
						.findByYearRowIdAndProjectName(request.getYear(), row.getRowId(), projectName);

				if (existing.isPresent()) {
					// 🟡 UPDATE
					PraptraMasterDataEntity entity = existing.get();
					entity.setData(row.getData());
					entity.setFlag("U");
					entity.setUpdatedAt(LocalDateTime.now());
					entity.setUpdatedBy(currentUser);

					praptraMasterDataRepository.save(entity);
				} else {
					// 🟢 CREATE
					PraptraMasterDataEntity entity = new PraptraMasterDataEntity();

					entity.setYear(request.getYear());
					entity.setRowId(row.getRowId());
					entity.setDeleteId(row.getDeleteId());
					entity.setData(row.getData());
					entity.setFlag("C");
					entity.setCreatedAt(LocalDateTime.now());
					entity.setCreatedBy(currentUser);
					entity.setUpdatedAt(LocalDateTime.now());
					entity.setUpdatedBy(currentUser);

					praptraMasterDataRepository.save(entity);
				}
			}

			// ✅ SUCCESS RESPONSE
			response.setMessage("Praptra Master Data processed successfully");
			response.setData(List.of());
			response.setErrorDetails(new ApplicationError("200", "Success"));

			return response;

		} catch (Exception e) {

			// ❌ FAILURE RESPONSE
			response.setMessage("Failed to process Praptra Master Data");
			response.setData(null);
			response.setErrorDetails(new ApplicationError("500", e.getMessage()));

			return response;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public PraptraMasterDataResponse getPraptraMasterData(String year) {

		PraptraMasterDataResponse response = new PraptraMasterDataResponse();

		try {

			if (year == null || year.isBlank()) {
				throw new RuntimeException("year is mandatory");
			}

			List<PraptraMasterDataEntity> entities = praptraMasterDataRepository.findAllByYear(year);

			List<Map<String, Object>> dataList = new ArrayList<>();

			for (PraptraMasterDataEntity e : entities) {

				Map<String, Object> map = new LinkedHashMap<>();
				map.put("id", e.getId());
				map.put("year", e.getYear());
				map.put("rowId", e.getRowId());
				map.put("deleteId", e.getDeleteId());
				map.put("flag", e.getFlag());
				map.put("data", e.getData());
				map.put("createdBy", e.getCreatedBy());
				map.put("createdAt", e.getCreatedAt());
				map.put("updatedBy", e.getUpdatedBy());
				map.put("updatedAt", e.getUpdatedAt());

				dataList.add(map);
			}

			response.setMessage("Praptra Master Data fetched successfully");
			response.setData(dataList);
			response.setErrorDetails(new ApplicationError("200", "Success"));

			return response;

		} catch (Exception e) {

			response.setMessage("Failed to fetch Praptra Master Data");
			response.setData(null);
			response.setErrorDetails(new ApplicationError("500", e.getMessage()));

			return response;
		}
	}

	@Override
	@Transactional
	public PraptraMasterDataResponse savePraptra1MasterData(PraptraMasterDataRequest request) {
		String currentUser = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");

		PraptraMasterDataResponse response = new PraptraMasterDataResponse();

		try {

			if (request.getYear() == null || request.getYear().isBlank()) {
				throw new RuntimeException("year is mandatory");
			}

			for (PraptraMasterDataRowRequest row : request.getRows()) {

				// 🔐 projectName mandatory
				if (row.getData() == null || row.getData().get("projectName") == null) {
					throw new RuntimeException("projectName is mandatory");
				}

				String projectName = row.getData().get("projectName").asText();

				// 🔴 DELETE FLOW
				if ("D".equalsIgnoreCase(row.getFlag())) {

					if (row.getDeleteId() == null) {
						throw new RuntimeException("deleteId is mandatory for deletion");
					}

					praptra1MasterDataRepository.deleteByYearProjectNameAndDeleteId(request.getYear(), projectName,
							row.getDeleteId());

					continue;
				}

				// 🔍 UPDATE / INSERT FLOW
				Optional<Praptra1MasterDataEntity> existing = praptra1MasterDataRepository
						.findByYearRowIdAndProjectName(request.getYear(), row.getRowId(), projectName);

				if (existing.isPresent()) {
					// 🟡 UPDATE
					Praptra1MasterDataEntity entity = existing.get();
					entity.setData(row.getData());
					entity.setFlag("U");
					entity.setUpdatedAt(LocalDateTime.now());
					entity.setUpdatedBy(currentUser);

					praptra1MasterDataRepository.save(entity);

				} else {
					// 🟢 INSERT
					Praptra1MasterDataEntity entity = new Praptra1MasterDataEntity();

					entity.setYear(request.getYear());
					entity.setRowId(row.getRowId());
					entity.setDeleteId(row.getDeleteId());
					entity.setData(row.getData());
					entity.setFlag("C");
					entity.setCreatedAt(LocalDateTime.now());
					entity.setCreatedBy(currentUser);
					entity.setUpdatedAt(LocalDateTime.now());
					entity.setUpdatedBy(currentUser);

					praptra1MasterDataRepository.save(entity);
				}
			}

			// ✅ SUCCESS RESPONSE
			response.setMessage("Praptra-1 Master Data processed successfully");
			response.setData(List.of());
			response.setErrorDetails(new ApplicationError("200", "Success"));
			return response;

		} catch (Exception e) {

			// ❌ FAILURE RESPONSE
			response.setMessage("Failed to process Praptra-1 Master Data");
			response.setData(null);
			response.setErrorDetails(new ApplicationError("500", e.getMessage()));
			return response;
		}

	}

	@Override
	@Transactional(readOnly = true)
	public PraptraMasterDataResponse getPraptra1MasterData(String year) {

		PraptraMasterDataResponse response = new PraptraMasterDataResponse();

		try {

			if (year == null || year.isBlank()) {
				throw new RuntimeException("year is mandatory");
			}

			List<Praptra1MasterDataEntity> entities = praptra1MasterDataRepository.findAllByYear(year);

			List<Map<String, Object>> dataList = new ArrayList<>();

			for (Praptra1MasterDataEntity e : entities) {

				Map<String, Object> row = new LinkedHashMap<>();

				row.put("id", e.getId());
				row.put("year", e.getYear());
				row.put("rowId", e.getRowId());
				row.put("deleteId", e.getDeleteId());
				row.put("flag", e.getFlag());

				// 🔥 dynamic praptra-1 data
				row.put("data", e.getData());

				row.put("createdBy", e.getCreatedBy());
				row.put("createdAt", e.getCreatedAt());
				row.put("updatedBy", e.getUpdatedBy());
				row.put("updatedAt", e.getUpdatedAt());

				dataList.add(row);
			}

			response.setMessage("Praptra-1 Master Data fetched successfully");
			response.setData(dataList);
			response.setErrorDetails(new ApplicationError("200", "Success"));
			return response;

		} catch (Exception e) {

			response.setMessage("Failed to fetch Praptra-1 Master Data");
			response.setData(null);
			response.setErrorDetails(new ApplicationError("500", e.getMessage()));
			return response;
		}
	}

	@Transactional
	@Override
	public PraptraMasterDataResponse savePraptra2MasterData(PraptraMasterDataRequest request) {
		String currentUser = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");

		PraptraMasterDataResponse response = new PraptraMasterDataResponse();

		try {

			if (request.getYear() == null || request.getYear().isBlank()) {
				throw new RuntimeException("year is mandatory");
			}

			for (PraptraMasterDataRowRequest row : request.getRows()) {

				// projectName mandatory
				if (row.getData() == null || row.getData().get("projectName") == null) {
					throw new RuntimeException("projectName is mandatory");
				}

				String projectName = row.getData().get("projectName").asText();

				// 🔴 DELETE FLOW
				if ("D".equalsIgnoreCase(row.getFlag())) {

					if (row.getDeleteId() == null) {
						throw new RuntimeException("deleteId is mandatory for deletion");
					}

					praptra2MasterDataRepository.deleteByYearProjectNameAndDeleteId(request.getYear(), projectName,
							row.getDeleteId());
					continue;
				}

				// 🔍 UPDATE / INSERT FLOW
				Optional<Praptra2MasterDataEntity> existing = praptra2MasterDataRepository
						.findByYearRowIdAndProjectName(request.getYear(), row.getRowId(), projectName);

				if (existing.isPresent()) {
					// 🟡 UPDATE
					Praptra2MasterDataEntity entity = existing.get();
					entity.setData(row.getData());
					entity.setFlag("U");
					entity.setUpdatedAt(LocalDateTime.now());
					entity.setUpdatedBy(currentUser);

					praptra2MasterDataRepository.save(entity);

				} else {
					// 🟢 INSERT
					Praptra2MasterDataEntity entity = new Praptra2MasterDataEntity();

					entity.setYear(request.getYear());
					entity.setRowId(row.getRowId());
					entity.setDeleteId(row.getDeleteId());
					entity.setData(row.getData());
					entity.setFlag("C");
					entity.setCreatedAt(LocalDateTime.now());
					entity.setCreatedBy(currentUser);
					entity.setUpdatedAt(LocalDateTime.now());
					entity.setUpdatedBy(currentUser);

					praptra2MasterDataRepository.save(entity);
				}
			}

			// ✅ SUCCESS RESPONSE
			response.setMessage("Praptra-2 Master Data processed successfully");
			response.setData(List.of());
			response.setErrorDetails(new ApplicationError("200", "Success"));
			return response;

		} catch (Exception e) {

			// ❌ FAILURE RESPONSE
			response.setMessage("Failed to process Praptra-2 Master Data");
			response.setData(null);
			response.setErrorDetails(new ApplicationError("500", e.getMessage()));
			return response;
		}
	}

	// ================= GET (YEAR ONLY) =================
	@Override
	@Transactional(readOnly = true)
	public PraptraMasterDataResponse getPraptra2MasterData(String year) {

		PraptraMasterDataResponse response = new PraptraMasterDataResponse();

		try {

			if (year == null || year.isBlank()) {
				throw new RuntimeException("year is mandatory");
			}

			List<Praptra2MasterDataEntity> entities = praptra2MasterDataRepository.findAllByYear(year);

			List<Map<String, Object>> dataList = new ArrayList<>();

			for (Praptra2MasterDataEntity e : entities) {

				Map<String, Object> map = new LinkedHashMap<>();
				map.put("id", e.getId());
				map.put("year", e.getYear());
				map.put("rowId", e.getRowId());
				map.put("deleteId", e.getDeleteId());
				map.put("flag", e.getFlag());
				map.put("data", e.getData());
				map.put("createdBy", e.getCreatedBy());
				map.put("createdAt", e.getCreatedAt());
				map.put("updatedBy", e.getUpdatedBy());
				map.put("updatedAt", e.getUpdatedAt());

				dataList.add(map);
			}

			response.setMessage("Praptra-2 Master Data fetched successfully");
			response.setData(dataList);
			response.setErrorDetails(new ApplicationError("200", "Success"));
			return response;

		} catch (Exception e) {

			response.setMessage("Failed to fetch Praptra-2 Master Data");
			response.setData(null);
			response.setErrorDetails(new ApplicationError("500", e.getMessage()));
			return response;
		}
	}

	@Transactional
	@Override
	public PraptraMasterDataResponse savePraptra3MasterData(PraptraMasterDataRequest request) {
		PraptraMasterDataResponse response = new PraptraMasterDataResponse();
		String currentUser = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");

		try {

			if (request.getYear() == null || request.getYear().isBlank()) {
				throw new RuntimeException("year is mandatory");
			}

			for (PraptraMasterDataRowRequest row : request.getRows()) {

				if (row.getData() == null || row.getData().get("projectName") == null) {
					throw new RuntimeException("projectName is mandatory");
				}

				String projectName = row.getData().get("projectName").asText();

				// 🔴 DELETE
				if ("D".equalsIgnoreCase(row.getFlag())) {

					if (row.getDeleteId() == null) {
						throw new RuntimeException("deleteId is mandatory for deletion");
					}

					praptra3MasterDataRepository.deleteByYearProjectNameAndDeleteId(request.getYear(), projectName,
							row.getDeleteId());
					continue;
				}

				// 🔍 UPDATE / INSERT
				Optional<Praptra3MasterDataEntity> existing = praptra3MasterDataRepository
						.findByYearRowIdAndProjectName(request.getYear(), row.getRowId(), projectName);

				if (existing.isPresent()) {
					// 🟡 UPDATE
					Praptra3MasterDataEntity entity = existing.get();
					entity.setData(row.getData());
					entity.setFlag("U");
					entity.setUpdatedAt(LocalDateTime.now());
					entity.setUpdatedBy(currentUser);

					praptra3MasterDataRepository.save(entity);

				} else {
					// 🟢 INSERT
					Praptra3MasterDataEntity entity = new Praptra3MasterDataEntity();

					entity.setYear(request.getYear());
					entity.setRowId(row.getRowId());
					entity.setDeleteId(row.getDeleteId());
					entity.setData(row.getData());
					entity.setFlag("C");
					entity.setCreatedAt(LocalDateTime.now());
					entity.setCreatedBy(currentUser);
					entity.setUpdatedAt(LocalDateTime.now());
					entity.setUpdatedBy(currentUser);

					praptra3MasterDataRepository.save(entity);
				}
			}

			response.setMessage("Praptra-3 Master Data processed successfully");
			response.setData(List.of());
			response.setErrorDetails(new ApplicationError("200", "Success"));
			return response;

		} catch (Exception e) {

			response.setMessage("Failed to process Praptra-3 Master Data");
			response.setData(null);
			response.setErrorDetails(new ApplicationError("500", e.getMessage()));
			return response;
		}
	}

	// ================= GET =================
	@Override
	@Transactional(readOnly = true)
	public PraptraMasterDataResponse getPraptra3MasterData(String year) {

		PraptraMasterDataResponse response = new PraptraMasterDataResponse();

		try {

			if (year == null || year.isBlank()) {
				throw new RuntimeException("year is mandatory");
			}

			List<Praptra3MasterDataEntity> entities = praptra3MasterDataRepository.findAllByYear(year);

			List<Map<String, Object>> dataList = new ArrayList<>();

			for (Praptra3MasterDataEntity e : entities) {

				Map<String, Object> map = new LinkedHashMap<>();
				map.put("id", e.getId());
				map.put("year", e.getYear());
				map.put("rowId", e.getRowId());
				map.put("deleteId", e.getDeleteId());
				map.put("flag", e.getFlag());
				map.put("data", e.getData());
				map.put("createdBy", e.getCreatedBy());
				map.put("createdAt", e.getCreatedAt());
				map.put("updatedBy", e.getUpdatedBy());
				map.put("updatedAt", e.getUpdatedAt());

				dataList.add(map);
			}

			response.setMessage("Praptra-3 Master Data fetched successfully");
			response.setData(dataList);
			response.setErrorDetails(new ApplicationError("200", "Success"));
			return response;

		} catch (Exception e) {

			response.setMessage("Failed to fetch Praptra-3 Master Data");
			response.setData(null);
			response.setErrorDetails(new ApplicationError("500", e.getMessage()));
			return response;
		}
	}

}
