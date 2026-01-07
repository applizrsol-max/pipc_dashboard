package com.pipc.dashboard.serviceimpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
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
import com.pipc.dashboard.bhusmapadan.request.ProjectBlock;
import com.pipc.dashboard.bhusmapadan.response.PraptraMasterDataResponse;
import com.pipc.dashboard.establishment.response.MasterDataResponse;
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
		ApplicationError error = new ApplicationError();

		try {

			// -------- BASIC VALIDATION --------
			if (request.getYear() == null || request.getYear().isBlank()) {
				error.setErrorCode("400");
				error.setErrorDescription("Year is mandatory");
				response.setErrorDetails(error);
				return response;
			}

			if (request.getProjects() == null || request.getProjects().isEmpty()) {
				error.setErrorCode("400");
				error.setErrorDescription("Projects list is empty");
				response.setErrorDetails(error);
				return response;
			}

			// -------- LOOP : PROJECT → ROWS --------
			for (ProjectBlock project : request.getProjects()) {

				String projectName = project.getProjectName();

				if (projectName == null || projectName.isBlank()) {
					log.warn("Skipping project with empty projectName | year={} | corrId={}", request.getYear(),
							corrId);
					continue;
				}

				if (project.getRows() == null || project.getRows().isEmpty()) {
					log.warn("No rows found for project={} | year={} | corrId={}", projectName, request.getYear(),
							corrId);
					continue;
				}

				for (PraptraMasterDataRowRequest row : project.getRows()) {

					// -------- HARD DELETE --------
					if ("D".equalsIgnoreCase(row.getFlag())) {

						praptraMasterDataRepository.deleteByYearDeleteIdAndProjectName(request.getYear(),
								row.getDeleteId(), projectName);

						log.debug("Deleted row | year={} | project={} | deleteId={} | corrId={}", request.getYear(),
								projectName, row.getDeleteId(), corrId);

						continue;
					}

					// -------- EXIST CHECK --------
					Optional<PraptraMasterDataEntity> existingOpt = praptraMasterDataRepository
							.findByYearRowIdAndProjectName(request.getYear(), row.getRowId(), projectName);

					if (existingOpt.isPresent()) {
						// -------- UPDATE --------
						PraptraMasterDataEntity entity = existingOpt.get();

						entity.setData(row.getData());
						entity.setFlag("U");
						entity.setUpdatedAt(LocalDateTime.now());
						entity.setUpdatedBy(currentUser);

						praptraMasterDataRepository.save(entity);

						log.debug("Updated row | year={} | project={} | rowId={} | corrId={}", request.getYear(),
								projectName, row.getRowId(), corrId);

					} else {
						// -------- CREATE --------
						PraptraMasterDataEntity entity = new PraptraMasterDataEntity();

						entity.setYear(request.getYear());
						entity.setProjectName(projectName);
						entity.setRowId(row.getRowId());
						entity.setDeleteId(row.getDeleteId());
						entity.setData(row.getData());
						entity.setFlag("C");
						entity.setCreatedAt(LocalDateTime.now());
						entity.setCreatedBy(currentUser);
						entity.setUpdatedAt(LocalDateTime.now());
						entity.setUpdatedBy(currentUser);

						praptraMasterDataRepository.save(entity);

						log.debug("Created row | year={} | project={} | rowId={} | corrId={}", request.getYear(),
								projectName, row.getRowId(), corrId);
					}
				}
			}

			// -------- SUCCESS RESPONSE --------
			error.setErrorCode("200");
			error.setErrorDescription("Praptra Master Data processed successfully");
			response.setErrorDetails(error);

			log.info("processPraptraMasterData SUCCESS | year={} | corrId={}", request.getYear(), corrId);

		} catch (Exception ex) {

			log.error("processPraptraMasterData FAILED | year={} | corrId={}", request.getYear(), corrId, ex);

			error.setErrorCode("500");
			error.setErrorDescription(ex.getMessage());
			response.setErrorDetails(error);
			response.setData(null);
		}

		return response;
	}

	@Override
	@Transactional(readOnly = true)
	public PraptraMasterDataResponse getPraptraMasterData(String year) {

		String corrId = MDC.get("correlationId");
		log.info("getPraptraMasterData START | year={} | projectName={} | corrId={}", year, corrId);

		PraptraMasterDataResponse response = new PraptraMasterDataResponse();
		ApplicationError error = new ApplicationError();

		try {

			if (year == null || year.isBlank()) {
				error.setErrorCode("400");
				error.setErrorDescription("Year is mandatory");
				response.setErrorDetails(error);
				response.setData(null);
				return response;
			}

			List<PraptraMasterDataEntity> entities = praptraMasterDataRepository.findAllByYear(year);

			if (entities.isEmpty()) {
				error.setErrorCode("404");
				error.setErrorDescription("No data found");
				response.setErrorDetails(error);
				response.setData(null);
				return response;
			}

			// ---------------- GROUP BY PROJECT NAME ----------------
			Map<String, List<PraptraMasterDataEntity>> grouped = entities.stream().collect(Collectors
					.groupingBy(PraptraMasterDataEntity::getProjectName, LinkedHashMap::new, Collectors.toList()));

			// ---------------- BUILD RESPONSE ----------------
			List<Map<String, Object>> projects = new ArrayList<>();

			for (Map.Entry<String, List<PraptraMasterDataEntity>> entry : grouped.entrySet()) {

				// 🔽 SORT BY data.srNo
				List<PraptraMasterDataEntity> sortedEntities = entry.getValue().stream()
						.sorted(Comparator.comparingInt(e -> {
							if (e.getData() != null && e.getData().has("srNo") && !e.getData().get("srNo").isNull()) {
								return e.getData().get("srNo").asInt();
							}
							return Integer.MAX_VALUE; // srNo missing → last
						})).toList();

				Map<String, Object> projectBlock = new LinkedHashMap<>();
				projectBlock.put("projectName", entry.getKey());

				List<Map<String, Object>> rows = new ArrayList<>();

				for (PraptraMasterDataEntity e : sortedEntities) {

					Map<String, Object> row = new LinkedHashMap<>();
					row.put("rowId", e.getRowId());
					row.put("deleteId", e.getDeleteId());
					row.put("flag", e.getFlag());
					row.put("data", e.getData());

					rows.add(row);
				}

				projectBlock.put("rows", rows);
				projects.add(projectBlock);
			}

			Map<String, Object> responseData = new LinkedHashMap<>();
			responseData.put("year", year);
			responseData.put("projects", projects);

			response.setData(responseData);
			response.setErrorDetails(new ApplicationError("200", "Success"));
			response.setMessage("Praptra Master Data fetched successfully");

			log.info("getPraptraMasterData SUCCESS | year={} | projects={} | corrId={}", year, projects.size(), corrId);

		} catch (Exception ex) {

			log.error("getPraptraMasterData FAILED | year={} | corrId={}", year, corrId, ex);

			error.setErrorCode("500");
			error.setErrorDescription(ex.getMessage());
			response.setErrorDetails(error);
			response.setData(null);
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
	public MasterDataResponse getPraptra1MasterData(String year, String projectName) {
		log.info("getPraptra1MasterData | year={} | corrId={}", year, MDC.get("correlationId"));
		return getGeneric(year, 1, projectName);
	}

	@Override
	@Transactional
	public PraptraMasterDataResponse savePraptra2MasterData(PraptraMasterDataRequest request) {
		log.info("savePraptra2MasterData START | year={} | corrId={}", request.getYear(), MDC.get("correlationId"));
		return saveGeneric(request, 2);
	}

	@Override
	@Transactional(readOnly = true)
	public MasterDataResponse getPraptra2MasterData(String year, String projectName) {
		log.info("getPraptra2MasterData | year={} | corrId={}", year, MDC.get("correlationId"));
		return getGeneric(year, 2, projectName);
	}

	@Override
	@Transactional
	public PraptraMasterDataResponse savePraptra3MasterData(PraptraMasterDataRequest request) {
		log.info("savePraptra3MasterData START | year={} | corrId={}", request.getYear(), MDC.get("correlationId"));
		return saveGeneric(request, 3);
	}

	@Override
	@Transactional(readOnly = true)
	public MasterDataResponse getPraptra3MasterData(String year, String projectName) {
		log.info("getPraptra3MasterData | year={} | corrId={}", year, MDC.get("correlationId"));
		return getGeneric(year, 3, projectName);
	}

	private PraptraMasterDataResponse saveGeneric(PraptraMasterDataRequest request, int type) {

		String currentUser = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
		String corrId = MDC.get("correlationId");

		log.info("saveGeneric START | praptra={} | year={} | corrId={}", type, request.getYear(), corrId);

		PraptraMasterDataResponse response = new PraptraMasterDataResponse();
		ApplicationError error = new ApplicationError();

		try {

			if (request.getYear() == null || request.getYear().isBlank()) {
				error.setErrorCode("400");
				error.setErrorDescription("year is mandatory");
				response.setErrorDetails(error);
				return response;
			}

			for (ProjectBlock project : request.getProjects()) {

				String projectName = project.getProjectName();
				if (projectName == null || projectName.isBlank())
					continue;

				for (PraptraMasterDataRowRequest row : project.getRows()) {

					/* ================= DELETE ================= */
					if ("D".equalsIgnoreCase(row.getFlag())) {

						if (row.getDeleteId() == null) {
							log.warn("deleteId missing | project={} | corrId={}", projectName, corrId);
							continue;
						}

						switch (type) {
						case 1 -> praptra1MasterDataRepository.deleteByYearAndProjectNameAndDeleteId(request.getYear(),
								projectName, row.getDeleteId());
						case 2 -> praptra2MasterDataRepository.deleteByYearProjectNameAndDeleteId(request.getYear(),
								projectName, row.getDeleteId());
						case 3 -> praptra3MasterDataRepository.deleteByYearProjectNameAndDeleteId(request.getYear(),
								projectName, row.getDeleteId());
						}

						log.debug("Deleted | praptra={} | project={} | deleteId={}", type, projectName,
								row.getDeleteId());
						continue;
					}

					/* ================= FIND ================= */
					Optional<?> existing = switch (type) {
					case 1 -> praptra1MasterDataRepository.findByYearAndRowIdAndProjectName(request.getYear(),
							row.getRowId(), projectName);
					case 2 -> praptra2MasterDataRepository.findByYearRowIdAndProjectName(request.getYear(),
							row.getRowId(), projectName);
					case 3 -> praptra3MasterDataRepository.findByYearRowIdAndProjectName(request.getYear(),
							row.getRowId(), projectName);
					default -> Optional.empty();
					};

					/* ================= UPDATE ================= */
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

						/* ================= CREATE ================= */
						if (type == 1) {
							Praptra1MasterDataEntity e = new Praptra1MasterDataEntity();
							e.setYear(request.getYear());
							e.setProjectName(projectName);
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
							e.setProjectName(projectName);
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
							e.setProjectName(projectName);
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
			}

			error.setErrorCode("200");
			error.setErrorDescription("Success");
			response.setErrorDetails(error);
			response.setMessage("Praptra-" + type + " Master Data processed successfully");

			log.info("saveGeneric SUCCESS | praptra={} | year={} | corrId={}", type, request.getYear(), corrId);

		} catch (Exception ex) {

			log.error("saveGeneric FAILED | praptra={} | year={} | corrId={}", type, request.getYear(), corrId, ex);

			error.setErrorCode("500");
			error.setErrorDescription(ex.getMessage());
			response.setErrorDetails(error);
			response.setData(null);
		}

		return response;
	}

	private MasterDataResponse getGeneric(String year, int type, String projectName) {

		String corrId = MDC.get("correlationId");
		log.info("getGeneric START | praptra={} | year={} | project={} | corrId={}", type, year, projectName, corrId);

		MasterDataResponse response = new MasterDataResponse();
		ApplicationError error = new ApplicationError();

		try {

			// -------- VALIDATION --------
			if (year == null || year.isBlank()) {
				error.setErrorCode("400");
				error.setErrorDescription("year is mandatory");
				response.setErrorDetails(error);
				return response;
			}

			if (projectName == null || projectName.isBlank()) {
				error.setErrorCode("400");
				error.setErrorDescription("projectName is mandatory");
				response.setErrorDetails(error);
				return response;
			}

			// -------- FETCH --------
			List<?> entities = switch (type) {
			case 1 -> praptra1MasterDataRepository.findAllByYearAndProjectName(year, projectName);
			case 2 -> praptra2MasterDataRepository.findAllByYearAndProjectName(year, projectName);
			case 3 -> praptra3MasterDataRepository.findAllByYearAndProjectName(year, projectName);
			default -> List.of();
			};

			// 🔽 SORT BY data.srNo
			entities = entities.stream().sorted(Comparator.comparingInt(obj -> {
				JsonNode data = null;

				if (obj instanceof Praptra1MasterDataEntity e)
					data = e.getData();
				else if (obj instanceof Praptra2MasterDataEntity e)
					data = e.getData();
				else if (obj instanceof Praptra3MasterDataEntity e)
					data = e.getData();

				if (data != null && data.has("srNo") && !data.get("srNo").isNull()) {
					return data.get("srNo").asInt();
				}
				return Integer.MAX_VALUE; // srNo missing → last
			})).toList();

			List<Map<String, Object>> dataList = new ArrayList<>();

			for (Object obj : entities) {

				Map<String, Object> map = new LinkedHashMap<>();

				if (obj instanceof Praptra1MasterDataEntity e) {
					map.put("id", e.getId());
					map.put("year", e.getYear());
					map.put("projectName", e.getProjectName());
					map.put("rowId", e.getRowId());
					map.put("deleteId", e.getDeleteId());
					map.put("flag", e.getFlag());
					map.put("data", e.getData());
					map.put("createdBy", e.getCreatedBy());
					map.put("createdAt", e.getCreatedAt());
					map.put("updatedBy", e.getUpdatedBy());
					map.put("updatedAt", e.getUpdatedAt());
				} else if (obj instanceof Praptra2MasterDataEntity e) {
					map.put("id", e.getId());
					map.put("year", e.getYear());
					map.put("projectName", e.getProjectName());
					map.put("rowId", e.getRowId());
					map.put("deleteId", e.getDeleteId());
					map.put("flag", e.getFlag());
					map.put("data", e.getData());
					map.put("createdBy", e.getCreatedBy());
					map.put("createdAt", e.getCreatedAt());
					map.put("updatedBy", e.getUpdatedBy());
					map.put("updatedAt", e.getUpdatedAt());
				} else if (obj instanceof Praptra3MasterDataEntity e) {
					map.put("id", e.getId());
					map.put("year", e.getYear());
					map.put("projectName", e.getProjectName());
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

			// -------- RESPONSE --------
			response.setData(dataList);
			response.setMessage("Praptra-" + type + " Master Data fetched successfully");

			error.setErrorCode("200");
			error.setErrorDescription("Success");
			response.setErrorDetails(error);

			log.info("getGeneric SUCCESS | praptra={} | year={} | project={} | records={} | corrId={}", type, year,
					projectName, dataList.size(), corrId);

		} catch (Exception ex) {

			log.error("getGeneric FAILED | praptra={} | year={} | project={} | corrId={}", type, year, projectName,
					corrId, ex);

			error.setErrorCode("500");
			error.setErrorDescription(ex.getMessage());
			response.setErrorDetails(error);
			response.setData(null);
		}

		return response;
	}

}
