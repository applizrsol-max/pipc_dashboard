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
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
		String corrId = MDC.get("correlationId");

		log.info("processPraptraMasterData START | year={} | user={} | corrId={}", request.getYear(), currentUser,
				corrId);
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

			log.info("processPraptraMasterData SUCCESS | year={} | corrId={}", request.getYear(), corrId);

		} catch (Exception e) {
			log.error("processPraptraMasterData FAILED | year={} | corrId={}", request.getYear(), corrId, e);

			// ❌ FAILURE RESPONSE
			response.setMessage("Failed to process Praptra Master Data");
			response.setData(null);
			response.setErrorDetails(new ApplicationError("500", e.getMessage()));

		}
		return response;
	}

	@Override
	@Transactional(readOnly = true)
	public PraptraMasterDataResponse getPraptraMasterData(String year) {
		String corrId = MDC.get("correlationId");
		log.info("getPraptraMasterData START | year={} | corrId={}", year, corrId);
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

			log.info("getPraptraMasterData SUCCESS | year={} | records={} | corrId={}", year, dataList.size(), corrId);

		} catch (Exception e) {
			log.error("getPraptraMasterData FAILED | year={} | corrId={}", year, corrId, e);
			response.setMessage("Failed to fetch Praptra Master Data");
			response.setData(null);
			response.setErrorDetails(new ApplicationError("500", e.getMessage()));

		}
		return response;
	}

	@Override
	@Transactional
	public PraptraMasterDataResponse savePraptra1MasterData(PraptraMasterDataRequest request) {
		log.info("savePraptra1MasterData START | year={} | corrId={}", request.getYear(), MDC.get("correlationId"));
		return saveGeneric(request, 1);
	}

	@Override
	@Transactional(readOnly = true)
	public PraptraMasterDataResponse getPraptra1MasterData(String year) {
		log.info("getPraptra1MasterData | year={} | corrId={}", year, MDC.get("correlationId"));
		return getGeneric(year, 1);
	}

	@Override
	@Transactional
	public PraptraMasterDataResponse savePraptra2MasterData(PraptraMasterDataRequest request) {
		log.info("savePraptra2MasterData START | year={} | corrId={}", request.getYear(), MDC.get("correlationId"));
		return saveGeneric(request, 2);
	}

	@Override
	@Transactional(readOnly = true)
	public PraptraMasterDataResponse getPraptra2MasterData(String year) {
		log.info("getPraptra2MasterData | year={} | corrId={}", year, MDC.get("correlationId"));
		return getGeneric(year, 2);
	}

	@Override
	@Transactional
	public PraptraMasterDataResponse savePraptra3MasterData(PraptraMasterDataRequest request) {
		log.info("savePraptra3MasterData START | year={} | corrId={}", request.getYear(), MDC.get("correlationId"));
		return saveGeneric(request, 3);
	}

	@Override
	@Transactional(readOnly = true)
	public PraptraMasterDataResponse getPraptra3MasterData(String year) {
		log.info("getPraptra3MasterData | year={} | corrId={}", year, MDC.get("correlationId"));
		return getGeneric(year, 3);
	}

	private PraptraMasterDataResponse saveGeneric(PraptraMasterDataRequest request, int type) {

		String currentUser = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
		String corrId = MDC.get("correlationId");

		PraptraMasterDataResponse response = new PraptraMasterDataResponse();

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

					switch (type) {
					case 1 -> praptra1MasterDataRepository.deleteByYearProjectNameAndDeleteId(request.getYear(),
							projectName, row.getDeleteId());
					case 2 -> praptra2MasterDataRepository.deleteByYearProjectNameAndDeleteId(request.getYear(),
							projectName, row.getDeleteId());
					case 3 -> praptra3MasterDataRepository.deleteByYearProjectNameAndDeleteId(request.getYear(),
							projectName, row.getDeleteId());
					}
					continue;
				}

				// 🔍 FIND EXISTING
				Optional<?> existing = switch (type) {
				case 1 -> praptra1MasterDataRepository.findByYearRowIdAndProjectName(request.getYear(), row.getRowId(),
						projectName);
				case 2 -> praptra2MasterDataRepository.findByYearRowIdAndProjectName(request.getYear(), row.getRowId(),
						projectName);
				case 3 -> praptra3MasterDataRepository.findByYearRowIdAndProjectName(request.getYear(), row.getRowId(),
						projectName);
				default -> Optional.empty();
				};

				if (existing.isPresent()) {

					if (type == 1) {
						Praptra1MasterDataEntity e = (Praptra1MasterDataEntity) existing.get();
						e.setData(row.getData());
						e.setFlag("U");
						e.setUpdatedAt(LocalDateTime.now());
						e.setUpdatedBy(currentUser);
						praptra1MasterDataRepository.save(e);
					}
					if (type == 2) {
						Praptra2MasterDataEntity e = (Praptra2MasterDataEntity) existing.get();
						e.setData(row.getData());
						e.setFlag("U");
						e.setUpdatedAt(LocalDateTime.now());
						e.setUpdatedBy(currentUser);
						praptra2MasterDataRepository.save(e);
					}
					if (type == 3) {
						Praptra3MasterDataEntity e = (Praptra3MasterDataEntity) existing.get();
						e.setData(row.getData());
						e.setFlag("U");
						e.setUpdatedAt(LocalDateTime.now());
						e.setUpdatedBy(currentUser);
						praptra3MasterDataRepository.save(e);
					}

				} else {

					if (type == 1) {
						Praptra1MasterDataEntity e = new Praptra1MasterDataEntity();
						e.setYear(request.getYear());
						e.setRowId(row.getRowId());
						e.setDeleteId(row.getDeleteId());
						e.setData(row.getData());
						e.setFlag("C");
						e.setCreatedAt(LocalDateTime.now());
						e.setCreatedBy(currentUser);
						e.setUpdatedAt(LocalDateTime.now());
						e.setUpdatedBy(currentUser);
						praptra1MasterDataRepository.save(e);
					}
					if (type == 2) {
						Praptra2MasterDataEntity e = new Praptra2MasterDataEntity();
						e.setYear(request.getYear());
						e.setRowId(row.getRowId());
						e.setDeleteId(row.getDeleteId());
						e.setData(row.getData());
						e.setFlag("C");
						e.setCreatedAt(LocalDateTime.now());
						e.setCreatedBy(currentUser);
						e.setUpdatedAt(LocalDateTime.now());
						e.setUpdatedBy(currentUser);
						praptra2MasterDataRepository.save(e);
					}
					if (type == 3) {
						Praptra3MasterDataEntity e = new Praptra3MasterDataEntity();
						e.setYear(request.getYear());
						e.setRowId(row.getRowId());
						e.setDeleteId(row.getDeleteId());
						e.setData(row.getData());
						e.setFlag("C");
						e.setCreatedAt(LocalDateTime.now());
						e.setCreatedBy(currentUser);
						e.setUpdatedAt(LocalDateTime.now());
						e.setUpdatedBy(currentUser);
						praptra3MasterDataRepository.save(e);
					}
				}
			}

			response.setMessage("Praptra-" + type + " Master Data processed successfully");
			response.setData(List.of());
			response.setErrorDetails(new ApplicationError("200", "Success"));

			log.info("saveGeneric SUCCESS | praptra={} | year={} | corrId={}", type, request.getYear(), corrId);

		} catch (Exception e) {

			log.error("saveGeneric FAILED | praptra={} | year={} | corrId={}", type, request.getYear(), corrId, e);

			response.setMessage("Failed to process Praptra-" + type + " Master Data");
			response.setData(null);
			response.setErrorDetails(new ApplicationError("500", e.getMessage()));
		}

		return response;
	}

	private PraptraMasterDataResponse getGeneric(String year, int type) {

		String corrId = MDC.get("correlationId");
		PraptraMasterDataResponse response = new PraptraMasterDataResponse();

		try {

			if (year == null || year.isBlank()) {
				throw new RuntimeException("year is mandatory");
			}

			List<?> entities = switch (type) {
			case 1 -> praptra1MasterDataRepository.findAllByYear(year);
			case 2 -> praptra2MasterDataRepository.findAllByYear(year);
			case 3 -> praptra3MasterDataRepository.findAllByYear(year);
			default -> List.of();
			};

			List<Map<String, Object>> dataList = new ArrayList<>();

			for (Object obj : entities) {

				Map<String, Object> map = new LinkedHashMap<>();

				if (obj instanceof Praptra1MasterDataEntity e) {
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
				}

				if (obj instanceof Praptra2MasterDataEntity e) {
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
				}

				if (obj instanceof Praptra3MasterDataEntity e) {
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
				}

				dataList.add(map);
			}

			response.setMessage("Praptra-" + type + " Master Data fetched successfully");
			response.setData(dataList);
			response.setErrorDetails(new ApplicationError("200", "Success"));

			log.info("getGeneric SUCCESS | praptra={} | year={} | records={} | corrId={}", type, year, dataList.size(),
					corrId);

		} catch (Exception e) {

			log.error("getGeneric FAILED | praptra={} | year={} | corrId={}", type, year, corrId, e);

			response.setMessage("Failed to fetch Praptra-" + type + " Master Data");
			response.setData(null);
			response.setErrorDetails(new ApplicationError("500", e.getMessage()));
		}

		return response;
	}

}
