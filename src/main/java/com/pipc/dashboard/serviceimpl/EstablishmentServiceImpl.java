package com.pipc.dashboard.serviceimpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFAbstractNum;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFNumbering;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTAbstractNum;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTLvl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STNumberFormat;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pipc.dashboard.bhusmapadan.request.PraptraMasterDataRowRequest;
import com.pipc.dashboard.bhusmapadan.response.BhaniniResponse;
import com.pipc.dashboard.establishment.repository.AgendaOfficerEntity;
import com.pipc.dashboard.establishment.repository.AgendaOfficerRepository;
import com.pipc.dashboard.establishment.repository.AgendaSecBRepository;
import com.pipc.dashboard.establishment.repository.AgendaSecDRepository;
import com.pipc.dashboard.establishment.repository.AgendaSecEntityGATB;
import com.pipc.dashboard.establishment.repository.AgendaSecEntityGATD;
import com.pipc.dashboard.establishment.repository.AgendaThirteenEntity;
import com.pipc.dashboard.establishment.repository.AgendaThirteenRepository;
import com.pipc.dashboard.establishment.repository.AppealEntity;
import com.pipc.dashboard.establishment.repository.AppealRepository;
import com.pipc.dashboard.establishment.repository.AppealRequestEntity;
import com.pipc.dashboard.establishment.repository.AppealRequestRepository;
import com.pipc.dashboard.establishment.repository.BhaniniEntity;
import com.pipc.dashboard.establishment.repository.BhaniniRepo;
import com.pipc.dashboard.establishment.repository.CrFileListEntity;
import com.pipc.dashboard.establishment.repository.CrFileListRepository;
import com.pipc.dashboard.establishment.repository.CrFileListRtrEntity;
import com.pipc.dashboard.establishment.repository.CrFileListRtrRepository;
import com.pipc.dashboard.establishment.repository.DeputyReturnAEntity;
import com.pipc.dashboard.establishment.repository.DeputyReturnARepository;
import com.pipc.dashboard.establishment.repository.DeputyReturnBEntity;
import com.pipc.dashboard.establishment.repository.DeputyReturnBRepository;
import com.pipc.dashboard.establishment.repository.EmployeePostingEntity;
import com.pipc.dashboard.establishment.repository.EmployeePostingRepository;
import com.pipc.dashboard.establishment.repository.IncomeTaxDeductionEntity;
import com.pipc.dashboard.establishment.repository.IncomeTaxDeductionRepository;
import com.pipc.dashboard.establishment.repository.JeReturnEntity;
import com.pipc.dashboard.establishment.repository.JeReturnRepository;
import com.pipc.dashboard.establishment.repository.KaryaratGopniyaAhwalEntity;
import com.pipc.dashboard.establishment.repository.KaryaratGopniyaAhwalRepository;
import com.pipc.dashboard.establishment.repository.MahaparRegisterEntity;
import com.pipc.dashboard.establishment.repository.MahaparRegisterRepository;
import com.pipc.dashboard.establishment.repository.MasterDataEntity;
import com.pipc.dashboard.establishment.repository.MasterDataRepository;
import com.pipc.dashboard.establishment.repository.RtrGopniyaAhwal;
import com.pipc.dashboard.establishment.repository.RtrGopniyaAhwalRepository;
import com.pipc.dashboard.establishment.repository.VivranPatraAEntity;
import com.pipc.dashboard.establishment.repository.VivranPatraARepository;
import com.pipc.dashboard.establishment.request.AgendaRequest;
import com.pipc.dashboard.establishment.request.AgendaRow;
import com.pipc.dashboard.establishment.request.AgendaSecRequest;
import com.pipc.dashboard.establishment.request.AgendaSecRow;
import com.pipc.dashboard.establishment.request.AppealRequest;
import com.pipc.dashboard.establishment.request.AppealRequest2;
import com.pipc.dashboard.establishment.request.AppealWrapper;
import com.pipc.dashboard.establishment.request.AppealWrapper2;
import com.pipc.dashboard.establishment.request.BhaniniRequest;
import com.pipc.dashboard.establishment.request.DeputyReturnADivisionDto;
import com.pipc.dashboard.establishment.request.DeputyReturnARequest;
import com.pipc.dashboard.establishment.request.DeputyReturnARowDto;
import com.pipc.dashboard.establishment.request.EmployeePostingRequest;
import com.pipc.dashboard.establishment.request.IncomeTaxDeductionRequest;
import com.pipc.dashboard.establishment.request.JeReturnRequest;
import com.pipc.dashboard.establishment.request.MahaparRegisterRequest;
import com.pipc.dashboard.establishment.request.MahaparRegisterRowRequest;
import com.pipc.dashboard.establishment.request.MahaparRegisterSectionRequest;
import com.pipc.dashboard.establishment.request.MasterDataRequest;
import com.pipc.dashboard.establishment.request.ThirteenRequest;
import com.pipc.dashboard.establishment.request.ThirteenRow;
import com.pipc.dashboard.establishment.request.VivranPatraADivisionDto;
import com.pipc.dashboard.establishment.request.VivranPatraARequest;
import com.pipc.dashboard.establishment.request.VivranPatraARowDto;
import com.pipc.dashboard.establishment.request.VivranPatraASummaryDto;
import com.pipc.dashboard.establishment.response.AgendaResponse;
import com.pipc.dashboard.establishment.response.AgendaSecResponse;
import com.pipc.dashboard.establishment.response.AppealResponse;
import com.pipc.dashboard.establishment.response.DeputyReturnAResponse;
import com.pipc.dashboard.establishment.response.EmployeePostingResponse;
import com.pipc.dashboard.establishment.response.IncomeTaxDeductionResponse;
import com.pipc.dashboard.establishment.response.JeReturnResponse;
import com.pipc.dashboard.establishment.response.MasterDataResponse;
import com.pipc.dashboard.establishment.response.ThirteenResponse;
import com.pipc.dashboard.establishment.response.VivranPatraAResponse;
import com.pipc.dashboard.service.EstablishmentService;
import com.pipc.dashboard.utility.ApplicationError;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EstablishmentServiceImpl implements EstablishmentService {

	private final AppealRepository appealRepository;
	private final EmployeePostingRepository employeePostingRepository;
	private final IncomeTaxDeductionRepository incomeTaxDeductionRepository;
	private final AgendaOfficerRepository agendaOfficerRepository;
	private final AgendaSecBRepository agendaSecBRepository;
	private final AgendaSecDRepository agendaSecDRepository;
	private final AgendaThirteenRepository agendaThirteenRepository;
	private final AppealRequestRepository appealRequestRepository;
	private final MasterDataRepository masterDataRepository;
	private final CrFileListRepository crFileListRepository;
	private final CrFileListRtrRepository crFileListRtrRepository;
	private final MahaparRegisterRepository mahaparRegisterRepository;
	private final KaryaratGopniyaAhwalRepository karyaratGopniyaAhwalRepository;
	private final RtrGopniyaAhwalRepository rtrGopniyaAhwalRepository;
	private final JeReturnRepository jeReturnRepository;
	@Autowired
	private ObjectMapper objectMapper;
	private final BhaniniRepo bhaniniRepo;
	private final DeputyReturnARepository deputyReturnARepository;
	private final DeputyReturnBRepository deputyReturnBRepository;
	private final VivranPatraARepository vivranPatraARepository;

	@Transactional
	@Override
	public AppealResponse saveOrUpdateAppeal(AppealWrapper wrapper) {

		AppealResponse response = new AppealResponse();
		ApplicationError error = new ApplicationError();
		response.setData(new ArrayList<>());

		String currentUser = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
		LocalDateTime now = LocalDateTime.now();

		log.info("START saveOrUpdateAppeal | records={} | user={}",
				wrapper.getAppealData() != null ? wrapper.getAppealData().size() : 0, currentUser);

		try {

			for (AppealRequest dto : wrapper.getAppealData()) {

				Optional<AppealEntity> existingOpt = Optional.empty();

				// 🔍 Find existing record using deleteId + date
				if (dto.getDeleteId() != null && dto.getDate() != null) {
					existingOpt = appealRepository.findByDeleteIdAndDate(dto.getDeleteId(), dto.getDate());
				}

				String flag = Optional.ofNullable(dto.getFlag()).orElse("C").toUpperCase();

				// ---------------- DELETE ----------------
				if ("D".equals(flag)) {

					if (existingOpt.isPresent()) {
						appealRepository.delete(existingOpt.get());

						Map<String, Object> delMap = new LinkedHashMap<>();
						delMap.put("deleteId", dto.getDeleteId());
						delMap.put("date", dto.getDate());
						delMap.put("status", "DELETED");
						response.getData().add(delMap);

						log.info("Deleted Appeal | deleteId={} | date={}", dto.getDeleteId(), dto.getDate());
					} else {
						log.warn("Delete requested but record not found | deleteId={} | date={}", dto.getDeleteId(),
								dto.getDate());
					}
					continue;
				}

				// ---------------- CREATE / UPDATE ----------------
				boolean isUpdate = existingOpt.isPresent();
				AppealEntity entity = existingOpt.orElse(new AppealEntity());

				entity.setDeleteId(dto.getDeleteId());
				entity.setYear(dto.getYear());
				entity.setRowId(dto.getRowId());
				entity.setDate(dto.getDate());

				if (isUpdate) {
					entity.setFlag("U");
					entity.setUpdatedBy(currentUser);
					entity.setUpdatedDate(now);
				} else {
					entity.setFlag("C");
					entity.setCreatedBy(currentUser);
					entity.setCreatedDate(now);
					entity.setUpdatedBy(currentUser);
					entity.setUpdatedDate(now);
				}

				// 🔹 Business fields
				entity.setApeelArjachaNondaniKramank(dto.getApeelArjachaNondaniKramank());
				entity.setApeelkaracheNav(dto.getApeelkaracheNav());
				entity.setApeelArjKonakadeKelaAahe(dto.getApeelArjKonakadeKelaAahe());
				entity.setApeelArjPraptJhalyachaDinank(dto.getApeelArjPraptJhalyachaDinank());
				entity.setPratidariYancheNavPatta(dto.getPratidariYancheNavPatta());
				entity.setApeelarthiDurustReneSathiKaam(dto.getApeelarthiDurustReneSathiKaam());
				entity.setApeelchaThodkyaTathya(dto.getApeelchaThodkyaTathya());
				entity.setApeelArjacheShulk(dto.getApeelArjacheShulk());
				entity.setKonakadeApeelSwikarBharle(dto.getKonakadeApeelSwikarBharle());
				entity.setApeelArjachiVidhikShulkBharalDinank(dto.getApeelArjachiVidhikShulkBharalDinank());
				entity.setKonalyaJanmahitiAdhikariYanchikadeApeelKeleTathyaTapshil(
						dto.getKonalyaJanmahitiAdhikariYanchikadeApeelKeleTathyaTapshil());
				entity.setApeelvarNirnayDilyachaDinank(dto.getApeelvarNirnayDilyachaDinank());
				entity.setShera(dto.getShera());
				entity.setDynamicColumns(dto.getDynamicColumns());

				AppealEntity saved = appealRepository.save(entity);

				Map<String, Object> map = new LinkedHashMap<>();
				map.put("deleteId", saved.getDeleteId());
				map.put("year", saved.getYear());
				map.put("flag", saved.getFlag());
				map.put("status", isUpdate ? "UPDATED" : "CREATED");

				response.getData().add(map);

				log.info("{} Appeal | rowId={} | year={}", isUpdate ? "Updated" : "Created", saved.getRowId(),
						saved.getYear());
			}

			error.setErrorCode("200");
			error.setErrorDescription("Success");
			response.setErrorDetails(error);

			log.info("SUCCESS saveOrUpdateAppeal | processedRecords={}", response.getData().size());

		} catch (Exception e) {

			log.error("ERROR saveOrUpdateAppeal", e);

			error.setErrorCode("500");
			error.setErrorDescription(e.getMessage());
			response.setErrorDetails(error);
		}

		return response;
	}

	@Override
	public AppealResponse getAppealData(String year) {

		AppealResponse response = new AppealResponse();
		ApplicationError error = new ApplicationError();
		response.setData(new ArrayList<>());

		log.info("START getAppealData | year={}", year);

		try {

			List<AppealEntity> entities;

			// 🎯 Filter by year OR fetch all (pagination removed)
			if (year != null && !year.isBlank()) {
				entities = appealRepository.findByYear(year);
			} else {
				entities = appealRepository.findAll();
			}

			log.info("Fetched {} records from DB", entities.size());

			for (AppealEntity e : entities) {

				Map<String, Object> map = new LinkedHashMap<>();

				map.put("id", e.getId());
				map.put("rowId", e.getRowId());
				map.put("year", e.getYear());
				map.put("date", e.getDate());
				map.put("deleteId", e.getDeleteId());
				map.put("flag", e.getFlag());

				map.put("apeelArjachaNondaniKramank", e.getApeelArjachaNondaniKramank());
				map.put("apeelkaracheNav", e.getApeelkaracheNav());
				map.put("apeelArjKonakadeKelaAahe", e.getApeelArjKonakadeKelaAahe());
				map.put("apeelArjPraptJhalyachaDinank", e.getApeelArjPraptJhalyachaDinank());
				map.put("pratidariYancheNavPatta", e.getPratidariYancheNavPatta());
				map.put("apeelarthiDurustReneSathiKaam", e.getApeelarthiDurustReneSathiKaam());
				map.put("apeelchaThodkyaTathya", e.getApeelchaThodkyaTathya());
				map.put("apeelArjacheShulk", e.getApeelArjacheShulk());
				map.put("konakadeApeelSwikarBharle", e.getKonakadeApeelSwikarBharle());
				map.put("apeelArjachiVidhikShulkBharalDinank", e.getApeelArjachiVidhikShulkBharalDinank());
				map.put("konalyaJanmahitiAdhikariYanchikadeApeelKeleTathyaTapshil",
						e.getKonalyaJanmahitiAdhikariYanchikadeApeelKeleTathyaTapshil());
				map.put("apeelvarNirnayDilyachaDinank", e.getApeelvarNirnayDilyachaDinank());
				map.put("shera", e.getShera());

				// 🧩 Dynamic Columns
				if (e.getDynamicColumns() != null && !e.getDynamicColumns().isEmpty()) {
					Map<String, Object> dynMap = objectMapper.convertValue(e.getDynamicColumns(), Map.class);
					map.put("dynamicColumns", dynMap);
				} else {
					map.put("dynamicColumns", new LinkedHashMap<>());
				}

				// 🔍 Audit fields
				map.put("createdBy", e.getCreatedBy());
				map.put("createdDate", e.getCreatedDate());
				map.put("updatedBy", e.getUpdatedBy());
				map.put("updatedDate", e.getUpdatedDate());

				response.getData().add(map);
			}

			// ✅ SORT BY rowId ASC
			response.getData().sort(Comparator.comparingInt(m -> Integer.parseInt(m.get("rowId").toString())));

			response.setMessage("Appeal register data fetched successfully.");
			error.setErrorCode("200");
			error.setErrorDescription("Success");

			log.info("SUCCESS getAppealData | totalRecords={}", response.getData().size());

		} catch (Exception ex) {

			log.error("ERROR getAppealData | year={}", year, ex);

			response.setMessage("Error while fetching appeal data.");
			error.setErrorCode("500");
			error.setErrorDescription(ex.getMessage());
		}

		response.setErrorDetails(error);
		return response;
	}

	@Override
	@Transactional
	public EmployeePostingResponse saveOrUpdateEmployeePosting(EmployeePostingRequest dto) {

		EmployeePostingResponse response = new EmployeePostingResponse();
		ApplicationError error = new ApplicationError();
		response.setData(new ArrayList<>());

		final String currentUser = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
		final LocalDateTime now = LocalDateTime.now();

		log.info("START saveOrUpdateEmployeePosting | records={} | user={}",
				dto.getData() != null ? dto.getData().size() : 0, currentUser);

		try {

			for (Map<String, Object> row : dto.getData()) {

				// ---------------- EXTRACT BASIC FIELDS ----------------
				if (row.get("rowId") == null) {
					log.warn("Skipping record without rowId");
					continue;
				}

				Long rowId = ((Number) row.get("rowId")).longValue();
				String year = (String) row.get("year");
				String flag = String.valueOf(row.getOrDefault("flag", "")).toUpperCase();

				Optional<EmployeePostingEntity> existingOpt = employeePostingRepository.findByRowId(rowId);

				// ---------------- DELETE ----------------
				if ("D".equals(flag)) {

					if (existingOpt.isPresent()) {
						employeePostingRepository.delete(existingOpt.get());
						log.info("Deleted EmployeePosting | rowId={}", rowId);
					} else {
						log.warn("Delete requested but record not found | rowId={}", rowId);
					}
					continue;
				}

				// ---------------- CREATE / UPDATE ----------------
				boolean isUpdate = existingOpt.isPresent();
				EmployeePostingEntity entity = existingOpt.orElse(new EmployeePostingEntity());

				entity.setFlag(isUpdate ? "U" : "C");
				entity.setRowId(rowId);
				entity.setYear(year);

				entity.setKramank((Integer) row.get("kramank"));
				entity.setAdhikariKarmacharyacheNav((String) row.get("adhikariKarmacharyacheNav"));
				entity.setPadnaam((String) row.get("padnaam"));
				entity.setDharika((String) row.get("dharika"));
				entity.setKalavadhi((String) row.get("kalavadhi"));

				// ---------------- DYNAMIC COLUMNS ----------------
				ObjectNode dynamic = objectMapper.createObjectNode();

				List<String> fixedKeys = List.of("rowId", "year", "flag", "kramank", "adhikariKarmacharyacheNav",
						"padnaam", "dharika", "kalavadhi");

				for (Map.Entry<String, Object> entry : row.entrySet()) {
					if (!fixedKeys.contains(entry.getKey())) {
						dynamic.putPOJO(entry.getKey(), entry.getValue());
					}
				}
				entity.setDynamicColumns(dynamic);

				// ---------------- AUDIT FIELDS ----------------
				if (!isUpdate) {
					entity.setCreatedBy(currentUser);
					entity.setCreatedDate(now);
				} else {
					if (entity.getCreatedBy() == null)
						entity.setCreatedBy(currentUser);
					if (entity.getCreatedDate() == null)
						entity.setCreatedDate(now);
				}

				entity.setUpdatedBy(currentUser);
				entity.setUpdatedDate(now);

				employeePostingRepository.save(entity);

				log.info("{} EmployeePosting | rowId={} | year={}", isUpdate ? "Updated" : "Created", rowId, year);
			}

			response.setMessage("Records processed successfully.");
			error.setErrorCode("200");
			error.setErrorDescription("Success");

			log.info("SUCCESS saveOrUpdateEmployeePosting | processedRecords={}", dto.getData().size());

		} catch (Exception e) {

			log.error("ERROR saveOrUpdateEmployeePosting", e);

			response.setMessage("Error while saving data.");
			error.setErrorCode("500");
			error.setErrorDescription(e.getMessage());
		}

		response.setErrorDetails(error);
		return response;
	}

	@Override
	public EmployeePostingResponse getEmployeePostingData(String name, String year) {

		EmployeePostingResponse response = new EmployeePostingResponse();
		ApplicationError error = new ApplicationError();
		response.setData(new ArrayList<>());

		log.info("START getEmployeePostingData | name={} | year={}", name, year);

		try {

			// 🔹 Fetch all records (NO pagination)
			List<EmployeePostingEntity> entities = employeePostingRepository.findByNameAndYear(name, year);

			if (entities == null || entities.isEmpty()) {
				log.warn("No EmployeePosting records found | name={} | year={}", name, year);
			}

			// 🔹 Sort by rowId ASC (stable & predictable)
			entities.sort(Comparator.comparing(EmployeePostingEntity::getRowId));

			for (EmployeePostingEntity e : entities) {

				Map<String, Object> map = new LinkedHashMap<>();

				map.put("rowId", e.getRowId());
				map.put("year", e.getYear());
				map.put("kramank", e.getKramank());
				map.put("adhikariKarmacharyacheNav", e.getAdhikariKarmacharyacheNav());
				map.put("padnaam", e.getPadnaam());
				map.put("dharika", e.getDharika());
				map.put("kalavadhi", e.getKalavadhi());
				map.put("flag", e.getFlag());

				// Audit
				map.put("createdBy", e.getCreatedBy());
				map.put("createdDate", e.getCreatedDate());
				map.put("updatedBy", e.getUpdatedBy());
				map.put("updatedDate", e.getUpdatedDate());

				// Dynamic columns
				map.put("dynamicColumns",
						e.getDynamicColumns() != null ? e.getDynamicColumns() : objectMapper.createObjectNode());

				response.getData().add(map);
			}

			response.setMessage("Records fetched successfully.");
			error.setErrorCode("200");
			error.setErrorDescription("Success");

			log.info("SUCCESS getEmployeePostingData | records={}", response.getData().size());

		} catch (Exception ex) {

			log.error("ERROR getEmployeePostingData | name={} | year={}", name, year, ex);

			response.setMessage("Error while fetching records.");
			error.setErrorCode("500");
			error.setErrorDescription(ex.getMessage());
		}

		response.setErrorDetails(error);
		return response;
	}

	@Override
	public IncomeTaxDeductionResponse saveOrUpdateIncomeTaxDeduc(IncomeTaxDeductionRequest request) {

		IncomeTaxDeductionResponse response = new IncomeTaxDeductionResponse();
		ApplicationError error = new ApplicationError();
		response.setData(new ArrayList<>());

		final String user = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
		final LocalDateTime now = LocalDateTime.now();

		log.info("START saveOrUpdateIncomeTaxDeduc | year={} | month={} | user={}", request.getYear(),
				request.getMonth(), user);

		try {

			for (Map<String, Object> row : request.getData()) {

				if (row.get("rowId") == null)
					continue;

				Long rowId = ((Number) row.get("rowId")).longValue();
				String flag = String.valueOf(row.getOrDefault("flag", "")).trim();

				Optional<IncomeTaxDeductionEntity> existingOpt = incomeTaxDeductionRepository.findByRowId(rowId);

				// 🗑️ DELETE
				if ("D".equalsIgnoreCase(flag)) {
					existingOpt.ifPresent(entity -> {
						incomeTaxDeductionRepository.delete(entity);
						log.debug("Deleted IncomeTax row | rowId={}", rowId);
					});
					continue;
				}

				IncomeTaxDeductionEntity entity = existingOpt.orElse(new IncomeTaxDeductionEntity());

				// FLAG
				entity.setFlag(existingOpt.isPresent() ? "U" : "C");

				// STATIC FIELDS
				entity.setRowId(rowId);
				entity.setYear(request.getYear());
				entity.setMonth(request.getMonth());
				entity.setSrNo((Integer) row.get("srNo"));
				entity.setNameAndDesignation((String) row.get("nameAndDesignation"));

				if (row.get("amountOfIncomeTaxDeducted") != null) {
					entity.setAmountOfIncomeTaxDeducted(
							new java.math.BigDecimal(row.get("amountOfIncomeTaxDeducted").toString()));
				}

				entity.setRemarks((String) row.get("remarks"));

				// 🔹 DYNAMIC COLUMNS
				Map<String, Object> dynamic = new LinkedHashMap<>();
				for (Map.Entry<String, Object> entry : row.entrySet()) {
					String key = entry.getKey();
					if (!List.of("rowId", "srNo", "nameAndDesignation", "amountOfIncomeTaxDeducted", "remarks", "flag")
							.contains(key)) {
						dynamic.put(key, entry.getValue());
					}
				}

				entity.setDynamicColumns(objectMapper.valueToTree(dynamic));

				// AUDIT
				if (entity.getId() == null) {
					entity.setCreatedBy(user);
					entity.setCreatedDate(now);
				}
				entity.setUpdatedBy(user);
				entity.setUpdatedDate(now);

				incomeTaxDeductionRepository.save(entity);

				log.debug("{} IncomeTax row processed | rowId={}", entity.getFlag(), rowId);
			}

			response.setMessage("Income Tax data saved/updated successfully.");
			error.setErrorCode("200");
			error.setErrorDescription("Success");

			log.info("SUCCESS saveOrUpdateIncomeTaxDeduc");

		} catch (Exception e) {

			log.error("ERROR saveOrUpdateIncomeTaxDeduc", e);

			response.setMessage("Error while saving data.");
			error.setErrorCode("500");
			error.setErrorDescription(e.getMessage());
		}

		response.setErrorDetails(error);
		return response;
	}

	@Override
	public IncomeTaxDeductionResponse getIncomeTaxDeductionData(String year, String month) {

		IncomeTaxDeductionResponse response = new IncomeTaxDeductionResponse();
		ApplicationError error = new ApplicationError();
		List<Map<String, Object>> dataList = new ArrayList<>();

		final String user = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");

		log.info("START getIncomeTaxDeductionData | year={} | month={} | user={}", year, month, user);

		try {

			List<IncomeTaxDeductionEntity> entities;

			// 🧠 SAME filtering logic (pagination removed)
			if ((year == null || year.isBlank()) && (month == null || month.isBlank())) {
				entities = incomeTaxDeductionRepository.findAll();
			} else if (year != null && !year.isBlank() && (month == null || month.isBlank())) {
				entities = incomeTaxDeductionRepository.findByYear(year);
			} else if ((year == null || year.isBlank()) && month != null && !month.isBlank()) {
				entities = incomeTaxDeductionRepository.findByMonthContainingIgnoreCase(month);
			} else {
				entities = incomeTaxDeductionRepository.findByYearAndMonthContainingIgnoreCase(year, month);
			}

			// 🔥 Sort by rowId ASC
			entities.sort(Comparator.comparing(IncomeTaxDeductionEntity::getRowId));

			for (IncomeTaxDeductionEntity e : entities) {

				Map<String, Object> map = new LinkedHashMap<>();

				map.put("rowId", e.getRowId());
				map.put("year", e.getYear());
				map.put("month", e.getMonth());
				map.put("flag", e.getFlag());
				map.put("srNo", e.getSrNo());
				map.put("nameAndDesignation", e.getNameAndDesignation());
				map.put("amountOfIncomeTaxDeducted", e.getAmountOfIncomeTaxDeducted());
				map.put("remarks", e.getRemarks());

				// ✅ Dynamic columns
				if (e.getDynamicColumns() != null) {
					e.getDynamicColumns().fields()
							.forEachRemaining(entry -> map.put(entry.getKey(), entry.getValue().asText()));
				}

				// Audit
				map.put("createdBy", e.getCreatedBy());
				map.put("createdDate", e.getCreatedDate());
				map.put("updatedBy", e.getUpdatedBy());
				map.put("updatedDate", e.getUpdatedDate());

				dataList.add(map);
			}

			response.setData(dataList);
			response.setMessage("Fetched successfully");

			error.setErrorCode("200");
			error.setErrorDescription("SUCCESS");

			log.info("SUCCESS getIncomeTaxDeductionData | records={}", dataList.size());

		} catch (Exception ex) {

			log.error("ERROR getIncomeTaxDeductionData", ex);

			response.setData(Collections.emptyList());
			response.setMessage("Error occurred while fetching data");

			error.setErrorCode("500");
			error.setErrorDescription(ex.getMessage());
		}

		response.setErrorDetails(error);
		return response;
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadAppealArj(String year) throws IOException {

		List<AppealEntity> list = (year != null && !year.isBlank()) ? appealRepository.findByYear(year)
				: appealRepository.findAll();
		list.sort(Comparator.comparing(AppealEntity::getRowId));

		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sh = wb.createSheet("Appeal Register");

		// ---------- COLUMN WIDTHS ----------
		int[] colWidths = { 2000, 4500, 4500, 5500, 4500, 4500, 4500, 5500, 3000, 4500, 5500, 3500, 3000 };
		for (int i = 0; i < colWidths.length; i++) {
			sh.setColumnWidth(i, colWidths[i]);
		}

		// ---------- MARATHI FONT ----------
		XSSFFont marathiFont = wb.createFont();
		marathiFont.setFontName("Mangal");
		marathiFont.setFontHeightInPoints((short) 12);

		// ---------- BOLD HEADER FONT ----------
		XSSFFont headerFont = wb.createFont();
		headerFont.setFontName("Mangal");
		headerFont.setFontHeightInPoints((short) 12);
		headerFont.setBold(true);

		// ---------- BASE BORDER STYLE ----------
		CellStyle baseBorder = wb.createCellStyle();
		baseBorder.setBorderBottom(BorderStyle.MEDIUM);
		baseBorder.setBorderTop(BorderStyle.MEDIUM);
		baseBorder.setBorderLeft(BorderStyle.MEDIUM);
		baseBorder.setBorderRight(BorderStyle.MEDIUM);
		baseBorder.setAlignment(HorizontalAlignment.CENTER);
		baseBorder.setVerticalAlignment(VerticalAlignment.CENTER);
		baseBorder.setWrapText(true);

		// ---------- HEADER STYLE ----------
		CellStyle headerStyle = wb.createCellStyle();
		headerStyle.cloneStyleFrom(baseBorder);
		headerStyle.setFont(headerFont);

		// ---------- DATA STYLE ----------
		CellStyle dataStyle = wb.createCellStyle();
		dataStyle.cloneStyleFrom(baseBorder);
		dataStyle.setFont(marathiFont);

		// ---------- NUMBER STYLE ----------
		CellStyle numberStyle = wb.createCellStyle();
		numberStyle.cloneStyleFrom(baseBorder);
		numberStyle.setFont(marathiFont);

		// ---------- TITLE STYLE (NO BORDER) ----------
		CellStyle titleStyle = wb.createCellStyle();
		titleStyle.setFont(headerFont);
		titleStyle.setAlignment(HorizontalAlignment.CENTER);
		titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		titleStyle.setWrapText(true);
		// NOTE: NO BORDERS (clean look)

		// ---------- ROW 0 : EMPTY ----------
		sh.createRow(0);

		// ---------- ROW 1 : TITLE ----------
		Row titleRow = sh.createRow(1);
		Cell titleCell = titleRow.createCell(0);
		titleCell.setCellValue("माहिती अधिकारांतर्गत खूड मंडळ कार्यालयास प्राप्त झालेले अपील अर्ज");
		titleCell.setCellStyle(titleStyle);
		sh.addMergedRegion(new CellRangeAddress(1, 1, 0, 12));

		// ---------- HEADER TEXT ----------
		String[] headers = { "अ.क्र.", "अपील अर्जाचा नोंदणी क्रमांक", "अपीलकाचे नाव", "अपील अर्ज कोणाकडे केला आहे",
				"अपील अर्ज प्राप्त झाल्याचा दिनांक", "प्रतिवादी यांचे नाव व पत्ता",
				"अपीलार्थीदारिद्रय रेषेखालील आहे काय", "अपीलाचा थोडक्यात तपशील",
				"अपील अर्जाचे शुल्क कोणत्या स्वरुपात भरले", "अपील अर्जाचे विहीत शुल्क भरल्याचा दिनांक",
				"कोणत्या जनमाहिती अधिकारी यांचेविरुद्ध अपील केले त्याचा तपशील", "अपीलवर निर्णय दिल्याचा दिनांक",
				"शेरा" };

		// ---------- ROW 2 : HEADER ----------
		Row headerRow = sh.createRow(2);
		headerRow.setHeightInPoints(40);

		for (int i = 0; i < headers.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(headers[i]);
			cell.setCellStyle(headerStyle);
		}

		// ---------- ROW 3 : COLUMN NUMBERS ----------
		Row numberRow = sh.createRow(3);
		numberRow.setHeightInPoints(22);

		for (int i = 0; i < headers.length; i++) {
			Cell numCell = numberRow.createCell(i);
			numCell.setCellValue(i + 1);
			numCell.setCellStyle(numberStyle);
		}

		// ---------- ROW 4 : DATA START ----------
		int rowNum = 4;
		int sr = 1;

		for (AppealEntity e : list) {

			Row row = sh.createRow(rowNum++);
			row.setHeightInPoints(-1); // auto stretch

			int c = 0;

			addCell(row, c++, String.valueOf(sr++), dataStyle);
			addCell(row, c++, nvl(e.getApeelArjachaNondaniKramank()), dataStyle);
			addCell(row, c++, nvl(e.getApeelkaracheNav()), dataStyle);
			addCell(row, c++, nvl(e.getApeelArjKonakadeKelaAahe()), dataStyle);
			addCell(row, c++, nvl(e.getApeelArjPraptJhalyachaDinank()), dataStyle);
			addCell(row, c++, nvl(e.getPratidariYancheNavPatta()), dataStyle);
			addCell(row, c++, nvl(e.getApeelarthiDurustReneSathiKaam()), dataStyle);
			addCell(row, c++, nvl(e.getApeelchaThodkyaTathya()), dataStyle);
			addCell(row, c++, nvl(e.getApeelArjacheShulk()), dataStyle);
			addCell(row, c++, nvl(e.getApeelArjachiVidhikShulkBharalDinank()), dataStyle);
			addCell(row, c++, nvl(e.getKonalyaJanmahitiAdhikariYanchikadeApeelKeleTathyaTapshil()), dataStyle);
			addCell(row, c++, nvl(e.getApeelvarNirnayDilyachaDinank()), dataStyle);
			addCell(row, c++, nvl(e.getShera()), dataStyle);
		}

		// ---------- WRITE OUTPUT ----------
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		wb.write(out);
		wb.close();

		HttpHeaders headersHttp = new HttpHeaders();
		headersHttp.add("Content-Disposition", "attachment; filename=Appeal_Register.xlsx");

		return ResponseEntity.ok().headers(headersHttp)
				.contentType(
						MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())));
	}

	private void addCell(Row row, int col, String val, CellStyle style) {
		Cell cell = row.createCell(col);
		cell.setCellValue(val);
		cell.setCellStyle(style);
	}

	private String nvl(String v) {
		return v == null ? "" : v;
	}

	public BigInteger addBulletStyle(XWPFDocument doc) throws Exception {
		XWPFNumbering numbering = doc.createNumbering();

		CTAbstractNum ctAbstractNum = CTAbstractNum.Factory.newInstance();
		ctAbstractNum.setAbstractNumId(BigInteger.valueOf(1));

		CTLvl lvl = ctAbstractNum.addNewLvl();
		lvl.setIlvl(BigInteger.ZERO);
		lvl.addNewNumFmt().setVal(STNumberFormat.BULLET);
		lvl.addNewLvlText().setVal("•");

		XWPFAbstractNum abs = new XWPFAbstractNum(ctAbstractNum);

		BigInteger abstractNumId = numbering.addAbstractNum(abs);
		return numbering.addNum(abstractNumId);
	}

	@Override
	@Transactional
	public AgendaResponse saveOrUpdateAgenda(AgendaRequest dto) {

		AgendaResponse response = new AgendaResponse();
		ApplicationError error = new ApplicationError();

		final String user = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
		final String year = dto.getMeta().getYear();
		final String targetDate = dto.getMeta().getTargetDate();

		log.info("START saveOrUpdateAgenda | year={} | targetDate={} | user={}", year, targetDate, user);

		try {

			for (AgendaRow row : dto.getRows()) {

				long rowId = row.getRowId();
				String deleteFlag = Optional.ofNullable(row.getDeleteFlag()).orElse("");
				LocalDateTime now = LocalDateTime.now();

				/* -------------------- HARD DELETE -------------------- */
				if ("D".equalsIgnoreCase(deleteFlag)) {

					Long deleteId = row.getDeleteId();

					if (deleteId != null && deleteId > 0) {

						agendaOfficerRepository.findByDeleteIdAndYearAndTargetDate(deleteId, year, targetDate)
								.ifPresent(entity -> {
									agendaOfficerRepository.delete(entity);
									log.info("DELETED Agenda | rowId={} | deleteId={} | year={} | targetDate={}", rowId,
											deleteId, year, targetDate);
								});

						error.setErrorCode("200");
						error.setErrorDescription("Deleted successfully.");
					} else {
						log.warn("DELETE SKIPPED | rowId={} | reason=deleteId missing", rowId);
					}

					continue;
				}

				/* -------------------- CREATE / UPDATE -------------------- */
				Optional<AgendaOfficerEntity> existingOpt = agendaOfficerRepository
						.findByRowIdAndYearAndTargetDate(rowId, year, targetDate);

				AgendaOfficerEntity entity = existingOpt.orElse(new AgendaOfficerEntity());

				entity.setRowId(rowId);
				entity.setYear(year);
				entity.setTargetDate(targetDate);
				entity.setColumnData(row.getColumnData()); // DIRECT SAVE (unchanged)
				entity.setDeleteId(row.getDeleteId());
				entity.setUpAdhikshakAbhiyantaName(row.getUpAdhikshakAbhiyantaName());

				if (entity.getId() == null) {
					entity.setFlag("C");
					entity.setCreatedBy(user);
					entity.setCreatedAt(now);
					log.info("CREATED Agenda | rowId={} | year={} | targetDate={}", rowId, year, targetDate);
				} else {
					entity.setFlag("U");
					log.info("UPDATED Agenda | rowId={} | year={} | targetDate={}", rowId, year, targetDate);
				}

				entity.setUpdatedBy(user);
				entity.setUpdatedAt(now);

				agendaOfficerRepository.save(entity);
			}

			response.setMessage("Success");
			error.setErrorCode("200");
			error.setErrorDescription("Agenda saved successfully.");

			log.info("SUCCESS saveOrUpdateAgenda | year={} | targetDate={}", year, targetDate);

		} catch (Exception e) {

			log.error("ERROR saveOrUpdateAgenda | year={} | targetDate={} | user={}", year, targetDate, user, e);

			error.setErrorCode("500");
			error.setErrorDescription("Error: " + e.getMessage());
			response.setMessage("Failed");
		}

		response.setErrorDetails(error);
		return response;
	}

	@Override
	public AgendaResponse getAgendaByYearAndTargetDate(String year, String targetDate) {

		AgendaResponse response = new AgendaResponse();
		ApplicationError error = new ApplicationError();

		final String user = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
		final String corrId = MDC.get("correlationId");

		log.info("START getAgendaByYearAndTargetDate | year={} | targetDate={} | user={} | corrId={}", year, targetDate,
				user, corrId);

		try {

			// 🔹 Direct fetch from DB (same as before)
			List<AgendaOfficerEntity> list = agendaOfficerRepository.findByYearAndTargetDate(year, targetDate);

			// 🔹 Sort by rowId ASC (same logic)
			list.sort(Comparator.comparing(AgendaOfficerEntity::getRowId));

			// 🔹 No transformation – return exactly what was saved
			response.setData(list);
			response.setMessage("Success");

			error.setErrorCode("200");
			error.setErrorDescription("Agenda fetched successfully.");

			log.info("SUCCESS getAgendaByYearAndTargetDate | year={} | targetDate={} | records={} | corrId={}", year,
					targetDate, list.size(), corrId);

		} catch (Exception e) {

			log.error("ERROR getAgendaByYearAndTargetDate | year={} | targetDate={} | user={} | corrId={}", year,
					targetDate, user, corrId, e);

			response.setMessage("Failed");
			error.setErrorCode("500");
			error.setErrorDescription("Error: " + e.getMessage());
		}

		response.setErrorDetails(error);
		return response;
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadAgendaExcel(String year, String targetDate) throws Exception {

		List<AgendaOfficerEntity> list = agendaOfficerRepository.findByYearAndTargetDate(year, targetDate);
		// ===== SORT BY ROW ID =====
		list.sort(Comparator.comparing(AgendaOfficerEntity::getRowId));

		// ===== FOOTER NAME =====
		String footerName = "( नि.लि.हैमे )";
		if (!list.isEmpty() && list.get(0).getUpAdhikshakAbhiyantaName() != null)
			footerName = list.get(0).getUpAdhikshakAbhiyantaName();

		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("Agenda");

		// ===== SMALL FONT =====
		Font smallFont = workbook.createFont();
		smallFont.setFontHeightInPoints((short) 10);

		// ===== TITLE STYLE =====
		CellStyle noBorder = workbook.createCellStyle();
		noBorder.setAlignment(HorizontalAlignment.CENTER);
		noBorder.setVerticalAlignment(VerticalAlignment.CENTER);
		Font titleFont = workbook.createFont();
		titleFont.setBold(true);
		titleFont.setFontHeightInPoints((short) 14);
		noBorder.setFont(titleFont);
		noBorder.setWrapText(true);

		// ===== CENTER BORDER =====
		CellStyle center = workbook.createCellStyle();
		center.setAlignment(HorizontalAlignment.CENTER);
		center.setVerticalAlignment(VerticalAlignment.CENTER);
		center.setBorderBottom(BorderStyle.THIN);
		center.setBorderTop(BorderStyle.THIN);
		center.setBorderLeft(BorderStyle.THIN);
		center.setBorderRight(BorderStyle.THIN);
		center.setFont(smallFont);
		center.setWrapText(true);

		// ===== FULL BORDER FOR DATA =====
		CellStyle fullBorder = workbook.createCellStyle();
		fullBorder.cloneStyleFrom(center);
		fullBorder.setWrapText(false);

		// ===== HEADER BOLD BORDER =====
		CellStyle boldHeader = workbook.createCellStyle();
		boldHeader.cloneStyleFrom(center);
		Font boldHeaderFont = workbook.createFont();
		boldHeaderFont.setBold(true);
		boldHeaderFont.setFontHeightInPoints((short) 11);
		boldHeader.setFont(boldHeaderFont);

		// ===== LEFT ALIGN =====
		CellStyle leftAlign = workbook.createCellStyle();
		leftAlign.setAlignment(HorizontalAlignment.LEFT);
		leftAlign.setVerticalAlignment(VerticalAlignment.CENTER);
		leftAlign.setFont(smallFont);

		// ===== FOOTER ALIGN =====
		CellStyle footerStyle = workbook.createCellStyle();
		footerStyle.setAlignment(HorizontalAlignment.RIGHT);
		footerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		Font footFont = workbook.createFont();
		footFont.setBold(true);
		footFont.setFontHeightInPoints((short) 11);
		footerStyle.setFont(footFont);

		int rowIdx = 0;

		// =================== TITLE SECTION ===================
		rowIdx = merged(sheet, rowIdx, "परिशिष्ट-ब", noBorder, 10);
		rowIdx = merged(sheet, rowIdx, "1 ऑगस्ट रोजी वयाची 49/ 54 वर्षे पूर्ण झालेले गट-अ मधील शासकीय अधिकारी",
				noBorder, 10);
		rowIdx = merged(sheet, rowIdx, "विभागाचे नाव- जलसंपदा विभाग.", noBorder, 10);
		rowIdx = merged(sheet, rowIdx, "मंडळ कार्यालयाचे नाव- अधीक्षक अभियंता, पुणे पाटबंधारे प्रकल्प मंडळ,पुणे",
				leftAlign, 10);
		rowIdx = merged(sheet, rowIdx, "पदनाम- उपविभागीय अभियंता.", leftAlign, 10);
		rowIdx++;

		// =================== COMPACT COLUMN WIDTHS ===================
		int[] widths = { 1800, 5500, 4200, 4200, 5200, 3500, 5500, 3800, 3000, 3000, 3000 };
		for (int c = 0; c < widths.length; c++)
			sheet.setColumnWidth(c, widths[c]);

		// =================== HEADER ROWS ===================
		Row h1 = sheet.createRow(rowIdx++);
		Row h2 = sheet.createRow(rowIdx++);
		h1.setHeightInPoints(24);
		h2.setHeightInPoints(22);

		String[] mainHeaders = { "अ. क्र.", "अधिकारी नाव", "पदनाम", "जन्मतारीख व वय",
				" सेवेतील प्रथम नियुक्तीचा दिनांक", "झालेली एकुण सेवा", "विभागीय चौकशी सुरू / प्रस्तावित",
				"गोपनीय अहवालामधील बाबी." };

		String[] subHeaders = { "गोपनीय अहवाल वर्ष", "सचोटी", "प्रकृतीमान", "प्रतवारी" };

		// Merge left block (first 7 cols)
		for (int c = 0; c <= 6; c++)
			sheet.addMergedRegion(new CellRangeAddress(h1.getRowNum(), h2.getRowNum(), c, c));

		// Merge group header for confidential fields
		sheet.addMergedRegion(new CellRangeAddress(h1.getRowNum(), h1.getRowNum(), 7, 10));

		// Header row 1
		for (int i = 0; i < mainHeaders.length; i++) {
			Cell cell = h1.createCell(i);
			cell.setCellValue(mainHeaders[i]);
			cell.setCellStyle(boldHeader);
		}

		// Header row 2
		for (int i = 0; i < subHeaders.length; i++) {
			Cell cell = h2.createCell(i + 7);
			cell.setCellValue(subHeaders[i]);
			cell.setCellStyle(boldHeader);
		}

		// =================== COLUMN NUMBER ROW ===================
		Row colNumRow = sheet.createRow(rowIdx++);
		colNumRow.setHeightInPoints(16);

		String[] colNums = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11" };
		for (int c = 0; c < colNums.length; c++) {
			Cell cell = colNumRow.createCell(c);
			cell.setCellValue(colNums[c]);
			cell.setCellStyle(center);
		}

		// ===== Fix border of merged header cells =====
		for (int c = 0; c <= 10; c++) {
			Cell c1 = h1.getCell(c);
			if (c1 == null)
				c1 = h1.createCell(c);
			c1.setCellStyle(boldHeader);

			Cell c2 = h2.getCell(c);
			if (c2 == null)
				c2 = h2.createCell(c);
			c2.setCellStyle(boldHeader);
		}

		// =================== DATA ROWS ===================
		for (AgendaOfficerEntity entity : list) {

			JsonNode cd = entity.getColumnData();
			JsonNode g = cd.get("gopniyaAhawal");

			String[] years = g.get("varsh").asText().split(",");
			String[] sachoti = g.get("sachoti").asText().split(",");
			String[] prakrutiman = g.get("prakrutiman").asText().split(",");
			String[] pratavaari = g.get("pratavaari").asText().split(",");

			int n = years.length;

			// Merge left 7 columns for multi-row entries
			if (n > 1)
				for (int c = 0; c <= 6; c++)
					sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx + n - 1, c, c));

			// FIRST ROW
			Row r = sheet.createRow(rowIdx);
			r.setHeightInPoints(22);

			r.createCell(0).setCellValue(cd.get("kramank").asInt());
			r.createCell(1).setCellValue(cd.get("adhikariNav").asText());
			r.createCell(2).setCellValue(cd.get("padnaam").asText());
			r.createCell(3).setCellValue(cd.get("janmaTarikh").asText());
			r.createCell(4).setCellValue(cd.get("sevetIlPrathamNiyuktiDinank").asText());
			r.createCell(5).setCellValue(cd.get("jhaleliEkunaSeva").asText());
			r.createCell(6).setCellValue(cd.get("vibhagiyaChokashiStatus").asText());

			r.createCell(7).setCellValue(years[0].trim());
			r.createCell(8).setCellValue(sachoti[0].trim());
			r.createCell(9).setCellValue(prakrutiman[0].trim());
			r.createCell(10).setCellValue(pratavaari[0].trim());

			apply(fullBorder, r, 11);

			// CHILD ROW(S)
			for (int i = 1; i < n; i++) {
				Row rr = sheet.createRow(rowIdx + i);
				rr.setHeightInPoints(22);

				rr.createCell(7).setCellValue(years[i].trim());
				rr.createCell(8).setCellValue(sachoti[i].trim());
				rr.createCell(9).setCellValue(prakrutiman[i].trim());
				rr.createCell(10).setCellValue(pratavaari[i].trim());

				apply(fullBorder, rr, 11);
			}

			rowIdx += n;
		}

		// =================== FOOTER ===================
		rowIdx += 2;

		footerName = "(" + footerName + ")";
		rowIdx = merged(sheet, rowIdx, "स्थळ प्रत अ.अ.यांना मान्य असे.", leftAlign, 3);
		rowIdx++;
		rowIdx = merged(sheet, rowIdx, footerName, footerStyle, 10);
		rowIdx = merged(sheet, rowIdx, "उप अधीक्षक अभियंता", footerStyle, 10);
		rowIdx = merged(sheet, rowIdx, "पुणे पाटबंधारे प्रकल्प मंडळ", footerStyle, 10);
		rowIdx = merged(sheet, rowIdx, "पुणे.", footerStyle, 10);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		workbook.write(out);
		workbook.close();

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=agenda.xlsx");

		return ResponseEntity.ok().headers(headers).contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
				.body(new InputStreamResource(in));
	}

	private int merged(Sheet sheet, int rowIdx, String text, CellStyle style, int lastCol) {
		Row r = sheet.createRow(rowIdx);
		Cell c = r.createCell(0);
		c.setCellValue(text);
		c.setCellStyle(style);
		sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, lastCol));
		return rowIdx + 1;
	}

	private void apply(CellStyle st, Row r, int colCount) {
		for (int c = 0; c < colCount; c++) {
			Cell cl = r.getCell(c);
			if (cl == null)
				cl = r.createCell(c);
			cl.setCellStyle(st);
		}
	}

	@Override
	@Transactional
	public AgendaSecResponse saveOrUpdateAgendaSec(AgendaSecRequest dto) {

		AgendaSecResponse response = new AgendaSecResponse();
		ApplicationError error = new ApplicationError();

		final String username = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
		final String corrId = MDC.get("correlationId");

		log.info("START saveOrUpdateAgendaSec | section={} | user={} | corrId={}", dto.getSectionFlag(), username,
				corrId);

		try {

			String year = dto.getMeta().getYear();
			String targetDate = dto.getMeta().getTargetDate();

			// ====================== GATB ======================
			if ("GATB".equalsIgnoreCase(dto.getSectionFlag())) {

				for (AgendaSecRow row : dto.getRows()) {

					long rowId = row.getRowId();
					String deleteFlag = Optional.ofNullable(row.getDeleteFlag()).orElse("");

					// -------- HARD DELETE --------
					if ("D".equalsIgnoreCase(deleteFlag)) {
						Long deleteId = row.getDeleteId();
						if (deleteId != null && deleteId > 0) {
							agendaSecBRepository.findByDeleteIdAndYearAndTargetDate(deleteId, year, targetDate)
									.ifPresent(entity -> {
										agendaSecBRepository.delete(entity);
										log.info("DELETED GATB | deleteId={} | year={} | targetDate={} | corrId={}",
												deleteId, year, targetDate, corrId);
									});
						}
						continue;
					}

					// -------- CREATE / UPDATE --------
					Optional<AgendaSecEntityGATB> existing = agendaSecBRepository.findByRowIdAndYearAndTargetDate(rowId,
							year, targetDate);

					AgendaSecEntityGATB entity = existing.orElse(new AgendaSecEntityGATB());
					LocalDateTime now = LocalDateTime.now();

					entity.setRowId(rowId);
					entity.setYear(year);
					entity.setTargetDate(targetDate);
					entity.setColumnData(row.getColumnData());
					entity.setDeleteId(row.getDeleteId());

					if (entity.getId() == null) {
						entity.setFlag("C");
						entity.setCreatedBy(username);
						entity.setCreatedAt(now);
					} else {
						entity.setFlag("U");
					}

					entity.setUpdatedBy(username);
					entity.setUpdatedAt(now);

					agendaSecBRepository.save(entity);

					log.debug("SAVED GATB | rowId={} | flag={} | corrId={}", rowId, entity.getFlag(), corrId);
				}
			}

			// ====================== GATD ======================
			else {

				for (AgendaSecRow row : dto.getRows()) {

					long rowId = row.getRowId();
					String deleteFlag = Optional.ofNullable(row.getDeleteFlag()).orElse("");

					// -------- HARD DELETE --------
					if ("D".equalsIgnoreCase(deleteFlag)) {
						Long deleteId = row.getDeleteId();
						if (deleteId != null && deleteId > 0) {
							agendaSecDRepository.findByDeleteIdAndYearAndTargetDate(deleteId, year, targetDate)
									.ifPresent(entity -> {
										agendaSecDRepository.delete(entity);
										log.info("DELETED GATD | deleteId={} | year={} | targetDate={} | corrId={}",
												deleteId, year, targetDate, corrId);
									});
						}
						continue;
					}

					// -------- CREATE / UPDATE --------
					Optional<AgendaSecEntityGATD> existing = agendaSecDRepository.findByRowIdAndYearAndTargetDate(rowId,
							year, targetDate);

					AgendaSecEntityGATD entity = existing.orElse(new AgendaSecEntityGATD());
					LocalDateTime now = LocalDateTime.now();

					entity.setRowId(rowId);
					entity.setYear(year);
					entity.setTargetDate(targetDate);
					entity.setColumnData(row.getColumnData());
					entity.setDeleteId(row.getDeleteId());

					if (entity.getId() == null) {
						entity.setFlag("C");
						entity.setCreatedBy(username);
						entity.setCreatedAt(now);
					} else {
						entity.setFlag("U");
					}

					entity.setUpdatedBy(username);
					entity.setUpdatedAt(now);

					agendaSecDRepository.save(entity);

					log.debug("SAVED GATD | rowId={} | flag={} | corrId={}", rowId, entity.getFlag(), corrId);
				}
			}

			error.setErrorCode("200");
			error.setErrorDescription("AgendaSec saved successfully.");
			response.setMessage("Success");

			log.info("SUCCESS saveOrUpdateAgendaSec | section={} | year={} | targetDate={} | corrId={}",
					dto.getSectionFlag(), year, targetDate, corrId);

		} catch (Exception e) {

			log.error("ERROR saveOrUpdateAgendaSec | section={} | corrId={}", dto.getSectionFlag(), corrId, e);

			error.setErrorCode("500");
			error.setErrorDescription("Error: " + e.getMessage());
			response.setMessage("Failed");
		}

		response.setErrorDetails(error);
		return response;
	}

	@Override
	public AgendaSecResponse getAgendaSecByYearAndTargetDate(String year, String targetDate, String section) {

		AgendaSecResponse response = new AgendaSecResponse();
		ApplicationError error = new ApplicationError();

		final String corrId = MDC.get("correlationId");

		log.info("START getAgendaSec | section={} | year={} | targetDate={} | corrId={}", section, year, targetDate,
				corrId);

		try {

			// ====================== GATB ======================
			if ("GATB".equalsIgnoreCase(section)) {

				List<AgendaSecEntityGATB> list = agendaSecBRepository.findByYearAndTargetDate(year, targetDate);

				// sort by rowId ASC
				list.sort(Comparator.comparingLong(AgendaSecEntityGATB::getRowId));

				response.setDataGatB(list);
				response.setMessage("Success");

				log.info("FETCHED GATB | records={} | corrId={}", list.size(), corrId);

			}
			// ====================== GATD ======================
			else {

				List<AgendaSecEntityGATD> list = agendaSecDRepository.findByYearAndTargetDate(year, targetDate);

				// sort by rowId ASC
				list.sort(Comparator.comparingLong(AgendaSecEntityGATD::getRowId));

				response.setDataGatD(list);
				response.setMessage("Success");

				log.info("FETCHED GATD | records={} | corrId={}", list.size(), corrId);
			}

			error.setErrorCode("200");
			error.setErrorDescription("Fetched successfully.");

		} catch (Exception e) {

			log.error("ERROR getAgendaSec | section={} | year={} | targetDate={} | corrId={}", section, year,
					targetDate, corrId, e);

			error.setErrorCode("500");
			error.setErrorDescription(e.getMessage());
			response.setMessage("Failed");
		}

		response.setErrorDetails(error);
		return response;
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadAgendaSecExcel(String year, String targetDate, String section)
			throws Exception {

		if (section.equalsIgnoreCase("GATB")) {

			List<AgendaSecEntityGATB> list = agendaSecBRepository.findByYearAndTargetDate(year, targetDate);

			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("AgendaSec");

			// ==== FONTS ====
			Font smallFont = workbook.createFont();
			smallFont.setFontHeightInPoints((short) 10);

			Font titleFont = workbook.createFont();
			titleFont.setFontHeightInPoints((short) 11);
			titleFont.setBold(true);

			Font headerFont = workbook.createFont();
			headerFont.setFontHeightInPoints((short) 10);
			headerFont.setBold(true);

			// ==== TITLE STYLE ====
			CellStyle titleStyle = workbook.createCellStyle();
			titleStyle.setAlignment(HorizontalAlignment.CENTER);
			titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			titleStyle.setFont(titleFont);
			titleStyle.setWrapText(true);

			// ==== CELL CENTER STYLE ====
			CellStyle cellCenter = workbook.createCellStyle();
			cellCenter.setAlignment(HorizontalAlignment.CENTER);
			cellCenter.setVerticalAlignment(VerticalAlignment.CENTER);
			cellCenter.setFont(smallFont);
			cellCenter.setWrapText(true);
			cellCenter.setBorderTop(BorderStyle.THIN);
			cellCenter.setBorderBottom(BorderStyle.THIN);
			cellCenter.setBorderLeft(BorderStyle.THIN);
			cellCenter.setBorderRight(BorderStyle.THIN);

			// ==== CELL LEFT STYLE ====
			CellStyle cellLeft = workbook.createCellStyle();
			cellLeft.cloneStyleFrom(cellCenter);
			cellLeft.setAlignment(HorizontalAlignment.LEFT);

			// ==== HEADER STYLE ====
			CellStyle headerStyle = workbook.createCellStyle();
			headerStyle.cloneStyleFrom(cellCenter);
			headerStyle.setFont(headerFont);

			// ==== FOOTER STYLE (NO BORDER) ====
			CellStyle footerStyle = workbook.createCellStyle();
			footerStyle.setAlignment(HorizontalAlignment.CENTER);
			footerStyle.setVerticalAlignment(VerticalAlignment.TOP);
			footerStyle.setBorderTop(BorderStyle.NONE);
			footerStyle.setBorderBottom(BorderStyle.NONE);
			footerStyle.setBorderLeft(BorderStyle.NONE);
			footerStyle.setBorderRight(BorderStyle.NONE);
			footerStyle.setWrapText(true);
			footerStyle.setFont(headerFont);

			final int LAST_COL = 9;
			int rowIdx = 0;

			// ================== TITLES ==================
			rowIdx = mergedNoBorder(sheet, rowIdx, "परिशिष्ट-ब", titleStyle, LAST_COL);
			rowIdx = mergedNoBorder(sheet, rowIdx,
					"1 ऑगस्ट  रोजी वयाची 49/54 वर्षे पुर्ण झालेले गट-क मधील शासकीय अधिकारी/कर्मचारी", titleStyle,
					LAST_COL);

			rowIdx = mergedNoBorder(sheet, rowIdx,
					"मंडळ कार्यालयाचे नाव- अधीक्षक अभियंता, पुणे पाटबंधारे प्रकल्प मंडळ,पुणे", titleStyle, LAST_COL);

			rowIdx++;

			// ================== HEADER ROWS ==================
			Row h1 = sheet.createRow(rowIdx++);
			Row h2 = sheet.createRow(rowIdx++);
			int[] widths = { 1500, 3500, 3500, 3500, 4000, 4000, 4000, 4000, 4000, 5000 };

			String[] headers = { "अ. क्र.", "शासकीय कर्मचाऱ्यांचे पुर्ण नांव व त्याने धारण केलेले पद", "जन्म दिनांक",
					"सेवेतील ‍ नेमणुकीचा दिनांक",
					"वयाची 35 वर्ष पुर्ण होण्यापुर्वी शासकीय सेवेत (भारतातील कुठलीही शासकीय सेवा धरुन) प्रवेश केला काय?",
					"विभागीय  चौकशी / कार्यवाही चालु असल्यास त्या संबंधीचे आरोप व त्याची सद्य:स्थिती यांचा तपशील",
					"मागासवर्गीय असल्यास कोणत्या वर्गात मोडतात",
					"शारिरिक दृष्ट्या शासकीय सेवेत राहण्यास पात्र आहेत काय?", "इतर अभिप्राय असल्यास", "शेरा" };

			for (int c = 0; c <= LAST_COL; c++) {

				Cell ch = h1.createCell(c);
				ch.setCellValue(headers[c]);
				ch.setCellStyle(headerStyle);

				Cell ch2 = h2.createCell(c);
				ch2.setCellStyle(headerStyle);

				sheet.addMergedRegion(new CellRangeAddress(h1.getRowNum(), h2.getRowNum(), c, c));
				RegionUtil.setBorderTop(BorderStyle.THIN, new CellRangeAddress(h1.getRowNum(), h2.getRowNum(), c, c),
						sheet);
				RegionUtil.setBorderBottom(BorderStyle.THIN, new CellRangeAddress(h1.getRowNum(), h2.getRowNum(), c, c),
						sheet);
				RegionUtil.setBorderLeft(BorderStyle.THIN, new CellRangeAddress(h1.getRowNum(), h2.getRowNum(), c, c),
						sheet);
				RegionUtil.setBorderRight(BorderStyle.THIN, new CellRangeAddress(h1.getRowNum(), h2.getRowNum(), c, c),
						sheet);

			}
			float headerHeight = calculateDynamicHeight(h1, widths);
			h1.setHeightInPoints(Math.max(headerHeight, 25));
			h2.setHeightInPoints(5);

			// ================== COLUMN NUMBER ROW ==================
			Row colNumRow = sheet.createRow(rowIdx++);
			for (int c = 0; c <= LAST_COL; c++) {
				Cell cn = colNumRow.createCell(c);
				cn.setCellValue(c + 1);
				cn.setCellStyle(cellCenter);
			}

			// ================== FIXED COLUMN WIDTHS ==================

			for (int c = 0; c <= LAST_COL; c++)
				sheet.setColumnWidth(c, widths[c]);

			// ================== DATA ROWS ==================
			for (AgendaSecEntityGATB entity : list) {

				JsonNode cd = entity.getColumnData();

				Row r = sheet.createRow(rowIdx);

				// PRE-CREATE CELLS WITH BORDER
				for (int c = 0; c <= LAST_COL; c++) {
					Cell cl = r.createCell(c);
					cl.setCellStyle(cellCenter);
				}

				if (cd.has("kramank"))
					r.getCell(0).setCellValue(cd.get("kramank").asText());
				if (cd.has("purnNavPad"))
					r.getCell(1).setCellValue(cd.get("purnNavPad").asText());
				if (cd.has("janmaDinank"))
					r.getCell(2).setCellValue(cd.get("janmaDinank").asText());
				if (cd.has("seveNiyuktiDinank"))
					r.getCell(3).setCellValue(cd.get("seveNiyuktiDinank").asText());
				if (cd.has("vay35PurviPravesh"))
					r.getCell(4).setCellValue(cd.get("vay35PurviPravesh").asText());
				if (cd.has("vibhagiyaChaukashi"))
					r.getCell(5).setCellValue(cd.get("vibhagiyaChaukashi").asText());
				if (cd.has("magasVargiya"))
					r.getCell(6).setCellValue(cd.get("magasVargiya").asText());
				if (cd.has("shasakiyaHattavaAdhikar"))
					r.getCell(7).setCellValue(cd.get("shasakiyaHattavaAdhikar").asText());
				if (cd.has("itarAbhipray"))
					r.getCell(8).setCellValue(cd.get("itarAbhipray").asText());
				if (cd.has("shera"))
					r.getCell(9).setCellValue(cd.get("shera").asText());

				// DYNAMIC ROW HEIGHT FIX
				float maxHeight = calculateDynamicHeight(r, widths);
				r.setHeightInPoints(maxHeight);

				rowIdx++;
			}

			// ================== FOOTER ==================
			rowIdx += 2;

			Row fr = sheet.createRow(rowIdx);
			fr.setHeightInPoints(70);

			String t1 = "सदस्य\nकार्यकारी अभियंता\nभामा आसखेड धरण विभाग,\nपुणे";
			String t2 = "सदस्य\nकार्यकारी अभियंता\nपाटबंधारे प्रकल्प अन्वेषण विभाग,\n(भिमा उपखोरे) पुणे";
			String t3 = "सदस्य सचिव\nअधीक्षक\nपुणे पाटबंधारे प्रकल्प मंडळ\nपुणे";
			String t4 = "अध्यक्ष\nउपअधीक्षक अभियंता\nपुणे पाटबंधारे प्रकल्प मंडळ\nपुणे.";

			sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 2));
			sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 3, 5));
			sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 6, 7));
			sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 8, 9));

			Cell f1 = fr.createCell(0);
			f1.setCellValue(t1);
			f1.setCellStyle(footerStyle);
			Cell f2 = fr.createCell(3);
			f2.setCellValue(t2);
			f2.setCellStyle(footerStyle);
			Cell f3 = fr.createCell(6);
			f3.setCellValue(t3);
			f3.setCellStyle(footerStyle);
			Cell f4 = fr.createCell(8);
			f4.setCellValue(t4);
			f4.setCellStyle(footerStyle);

			clearMergedBorders(sheet, rowIdx, 0, 2);
			clearMergedBorders(sheet, rowIdx, 3, 5);
			clearMergedBorders(sheet, rowIdx, 6, 7);
			clearMergedBorders(sheet, rowIdx, 8, 9);

			// ================== SAVE FILE ==================
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			workbook.write(out);
			workbook.close();

			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			HttpHeaders headers1 = new HttpHeaders();
			headers1.add("Content-Disposition", "attachment; filename=agenda_sec.xlsx");

			return ResponseEntity.ok().headers(headers1)
					.contentType(MediaType
							.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
					.body(new InputStreamResource(in));
		} else {
			List<AgendaSecEntityGATD> list = agendaSecDRepository.findByYearAndTargetDate(year, targetDate);

			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("AgendaSec");

			// ==== FONTS ====
			Font smallFont = workbook.createFont();
			smallFont.setFontHeightInPoints((short) 10);

			Font titleFont = workbook.createFont();
			titleFont.setFontHeightInPoints((short) 11);
			titleFont.setBold(true);

			Font headerFont = workbook.createFont();
			headerFont.setFontHeightInPoints((short) 10);
			headerFont.setBold(true);

			// ==== TITLE STYLE ====
			CellStyle titleStyle = workbook.createCellStyle();
			titleStyle.setAlignment(HorizontalAlignment.CENTER);
			titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			titleStyle.setFont(titleFont);
			titleStyle.setWrapText(true);

			// ==== CELL CENTER STYLE ====
			CellStyle cellCenter = workbook.createCellStyle();
			cellCenter.setAlignment(HorizontalAlignment.CENTER);
			cellCenter.setVerticalAlignment(VerticalAlignment.CENTER);
			cellCenter.setFont(smallFont);
			cellCenter.setWrapText(true);
			cellCenter.setBorderTop(BorderStyle.THIN);
			cellCenter.setBorderBottom(BorderStyle.THIN);
			cellCenter.setBorderLeft(BorderStyle.THIN);
			cellCenter.setBorderRight(BorderStyle.THIN);

			// ==== CELL LEFT STYLE ====
			CellStyle cellLeft = workbook.createCellStyle();
			cellLeft.cloneStyleFrom(cellCenter);
			cellLeft.setAlignment(HorizontalAlignment.LEFT);

			// ==== HEADER STYLE ====
			CellStyle headerStyle = workbook.createCellStyle();
			headerStyle.cloneStyleFrom(cellCenter);
			headerStyle.setFont(headerFont);

			// ==== FOOTER STYLE (NO BORDER) ====
			CellStyle footerStyle = workbook.createCellStyle();
			footerStyle.setAlignment(HorizontalAlignment.CENTER);
			footerStyle.setVerticalAlignment(VerticalAlignment.TOP);
			footerStyle.setBorderTop(BorderStyle.NONE);
			footerStyle.setBorderBottom(BorderStyle.NONE);
			footerStyle.setBorderLeft(BorderStyle.NONE);
			footerStyle.setBorderRight(BorderStyle.NONE);
			footerStyle.setWrapText(true);
			footerStyle.setFont(headerFont);

			final int LAST_COL = 9;
			int rowIdx = 0;

			// ================== TITLES ==================
			rowIdx = mergedNoBorder(sheet, rowIdx, "परिशिष्ट-ब", titleStyle, LAST_COL);
			rowIdx = mergedNoBorder(sheet, rowIdx,
					"1 ऑगस्ट  रोजी वयाची 49/54 वर्षे पुर्ण झालेले गट-क मधील शासकीय अधिकारी/कर्मचारी", titleStyle,
					LAST_COL);

			rowIdx = mergedNoBorder(sheet, rowIdx,
					"मंडळ कार्यालयाचे नाव- अधीक्षक अभियंता, पुणे पाटबंधारे प्रकल्प मंडळ,पुणे", titleStyle, LAST_COL);

			rowIdx++;

			// ================== HEADER ROWS ==================
			Row h1 = sheet.createRow(rowIdx++);
			Row h2 = sheet.createRow(rowIdx++);
			int[] widths = { 1500, 3500, 3500, 3500, 4000, 4000, 4000, 4000, 4000, 5000 };

			String[] headers = { "अ. क्र.", "शासकीय कर्मचाऱ्यांचे पुर्ण नांव व त्याने धारण केलेले पद", "जन्म दिनांक",
					"सेवेतील ‍ नेमणुकीचा दिनांक",
					"वयाची 35 वर्ष पुर्ण होण्यापुर्वी शासकीय सेवेत (भारतातील कुठलीही शासकीय सेवा धरुन) प्रवेश केला काय?",
					"विभागीय  चौकशी / कार्यवाही चालु असल्यास त्या संबंधीचे आरोप व त्याची सद्य:स्थिती यांचा तपशील",
					"मागासवर्गीय असल्यास कोणत्या वर्गात मोडतात",
					"शारिरिक दृष्ट्या शासकीय सेवेत राहण्यास पात्र आहेत काय?", "इतर अभिप्राय असल्यास", "शेरा" };

			for (int c = 0; c <= LAST_COL; c++) {

				Cell ch = h1.createCell(c);
				ch.setCellValue(headers[c]);
				ch.setCellStyle(headerStyle);

				Cell ch2 = h2.createCell(c);
				ch2.setCellStyle(headerStyle);

				sheet.addMergedRegion(new CellRangeAddress(h1.getRowNum(), h2.getRowNum(), c, c));
				RegionUtil.setBorderTop(BorderStyle.THIN, new CellRangeAddress(h1.getRowNum(), h2.getRowNum(), c, c),
						sheet);
				RegionUtil.setBorderBottom(BorderStyle.THIN, new CellRangeAddress(h1.getRowNum(), h2.getRowNum(), c, c),
						sheet);
				RegionUtil.setBorderLeft(BorderStyle.THIN, new CellRangeAddress(h1.getRowNum(), h2.getRowNum(), c, c),
						sheet);
				RegionUtil.setBorderRight(BorderStyle.THIN, new CellRangeAddress(h1.getRowNum(), h2.getRowNum(), c, c),
						sheet);

			}
			float headerHeight = calculateDynamicHeight(h1, widths);
			h1.setHeightInPoints(Math.max(headerHeight, 25));
			h2.setHeightInPoints(5);

			// ================== COLUMN NUMBER ROW ==================
			Row colNumRow = sheet.createRow(rowIdx++);
			for (int c = 0; c <= LAST_COL; c++) {
				Cell cn = colNumRow.createCell(c);
				cn.setCellValue(c + 1);
				cn.setCellStyle(cellCenter);
			}

			// ================== FIXED COLUMN WIDTHS ==================

			for (int c = 0; c <= LAST_COL; c++)
				sheet.setColumnWidth(c, widths[c]);

			// ================== DATA ROWS ==================
			for (AgendaSecEntityGATD entity : list) {

				JsonNode cd = entity.getColumnData();

				Row r = sheet.createRow(rowIdx);

				// PRE-CREATE CELLS WITH BORDER
				for (int c = 0; c <= LAST_COL; c++) {
					Cell cl = r.createCell(c);
					cl.setCellStyle(cellCenter);
				}

				if (cd.has("kramank"))
					r.getCell(0).setCellValue(cd.get("kramank").asText());
				if (cd.has("purnNavPad"))
					r.getCell(1).setCellValue(cd.get("purnNavPad").asText());
				if (cd.has("janmaDinank"))
					r.getCell(2).setCellValue(cd.get("janmaDinank").asText());
				if (cd.has("seveNiyuktiDinank"))
					r.getCell(3).setCellValue(cd.get("seveNiyuktiDinank").asText());
				if (cd.has("vay35PurviPravesh"))
					r.getCell(4).setCellValue(cd.get("vay35PurviPravesh").asText());
				if (cd.has("vibhagiyaChaukashi"))
					r.getCell(5).setCellValue(cd.get("vibhagiyaChaukashi").asText());
				if (cd.has("magasVargiya"))
					r.getCell(6).setCellValue(cd.get("magasVargiya").asText());
				if (cd.has("shasakiyaHattavaAdhikar"))
					r.getCell(7).setCellValue(cd.get("shasakiyaHattavaAdhikar").asText());
				if (cd.has("itarAbhipray"))
					r.getCell(8).setCellValue(cd.get("itarAbhipray").asText());
				if (cd.has("shera"))
					r.getCell(9).setCellValue(cd.get("shera").asText());

				// DYNAMIC ROW HEIGHT FIX
				float maxHeight = calculateDynamicHeight(r, widths);
				r.setHeightInPoints(maxHeight);

				rowIdx++;
			}

			// ================== FOOTER ==================
			rowIdx += 2;

			Row fr = sheet.createRow(rowIdx);
			fr.setHeightInPoints(70);

			String t1 = "सदस्य\nकार्यकारी अभियंता\nभामा आसखेड धरण विभाग,\nपुणे";
			String t2 = "सदस्य\nकार्यकारी अभियंता\nपाटबंधारे प्रकल्प अन्वेषण विभाग,\n(भिमा उपखोरे) पुणे";
			String t3 = "सदस्य सचिव\nअधीक्षक\nपुणे पाटबंधारे प्रकल्प मंडळ\nपुणे";
			String t4 = "अध्यक्ष\nउपअधीक्षक अभियंता\nपुणे पाटबंधारे प्रकल्प मंडळ\nपुणे.";

			sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 2));
			sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 3, 5));
			sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 6, 7));
			sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 8, 9));

			Cell f1 = fr.createCell(0);
			f1.setCellValue(t1);
			f1.setCellStyle(footerStyle);
			Cell f2 = fr.createCell(3);
			f2.setCellValue(t2);
			f2.setCellStyle(footerStyle);
			Cell f3 = fr.createCell(6);
			f3.setCellValue(t3);
			f3.setCellStyle(footerStyle);
			Cell f4 = fr.createCell(8);
			f4.setCellValue(t4);
			f4.setCellStyle(footerStyle);

			clearMergedBorders(sheet, rowIdx, 0, 2);
			clearMergedBorders(sheet, rowIdx, 3, 5);
			clearMergedBorders(sheet, rowIdx, 6, 7);
			clearMergedBorders(sheet, rowIdx, 8, 9);

			// ================== SAVE FILE ==================
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			workbook.write(out);
			workbook.close();

			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			HttpHeaders headers1 = new HttpHeaders();
			headers1.add("Content-Disposition", "attachment; filename=agenda_sec.xlsx");

			return ResponseEntity.ok().headers(headers1)
					.contentType(MediaType
							.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
					.body(new InputStreamResource(in));
		}
	}

	// ---------------- HELPERS ----------------
	private int mergedNoBorder(Sheet sheet, int rowIdx, String text, CellStyle style, int lastCol) {
		Row r = sheet.createRow(rowIdx);
		Cell c = r.createCell(0);
		c.setCellValue(text);
		c.setCellStyle(style);
		sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, lastCol));
		return rowIdx + 1;
	}

	private void clearMergedBorders(Sheet sheet, int row, int startCol, int endCol) {
		CellRangeAddress range = new CellRangeAddress(row, row, startCol, endCol);
		RegionUtil.setBorderTop(BorderStyle.NONE, range, sheet);
		RegionUtil.setBorderBottom(BorderStyle.NONE, range, sheet);
		RegionUtil.setBorderLeft(BorderStyle.NONE, range, sheet);
		RegionUtil.setBorderRight(BorderStyle.NONE, range, sheet);
	}

	private float calculateDynamicHeight(Row row, int[] columnWidths) {
		float maxHeight = 22f; // default

		for (int c = 0; c < row.getLastCellNum(); c++) {
			Cell cell = row.getCell(c);
			if (cell == null || cell.getCellType() != CellType.STRING)
				continue;

			String text = cell.getStringCellValue();
			int colWidthPx = columnWidths[c] / 40; // rough width

			int approxCharsPerLine = Math.max(1, colWidthPx / 7);
			int lines = (int) Math.ceil((double) text.length() / approxCharsPerLine);

			float height = Math.max(22, lines * 15);
			maxHeight = Math.max(maxHeight, height);
		}
		return maxHeight;
	}

	@Override
	public ThirteenResponse saveOrUpdateAnukampa(ThirteenRequest dto) {

		ThirteenResponse response = new ThirteenResponse();
		ApplicationError error = new ApplicationError();

		final String currentUser = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
		final String corrId = MDC.get("correlationId");

		try {
			String year = dto.getMeta().getYear();
			String targetDate = dto.getMeta().getTargetDate();

			log.info("START saveOrUpdateAnukampa | year={} | targetDate={} | user={} | corrId={}", year, targetDate,
					currentUser, corrId);

			for (ThirteenRow row : dto.getRows()) {

				long rowId = row.getRowId();
				String deleteFlag = Optional.ofNullable(row.getDeleteFlag()).orElse("");

				// ---------------- HARD DELETE ----------------
				if ("D".equalsIgnoreCase(deleteFlag)) {

					if (row.getDeleteId() != null) {
						agendaThirteenRepository.findByDeleteIdAndYearAndTargetDate(row.getDeleteId(), year, targetDate)
								.ifPresent(entity -> {
									agendaThirteenRepository.delete(entity);
									log.info("DELETED Anukampa | rowId={} | deleteId={} | corrId={}", rowId,
											row.getDeleteId(), corrId);
								});
					}
					continue;
				}

				// ---------------- CREATE / UPDATE ----------------
				Optional<AgendaThirteenEntity> existingOpt = agendaThirteenRepository
						.findByRowIdAndYearAndTargetDate(rowId, year, targetDate);

				AgendaThirteenEntity entity = existingOpt.orElse(new AgendaThirteenEntity());
				LocalDateTime now = LocalDateTime.now();

				entity.setRowId(rowId);
				entity.setYear(year);
				entity.setTargetDate(targetDate);
				entity.setColumnData(row.getColumnData());
				entity.setDeleteId(row.getDeleteId());

				if (entity.getId() == null) {
					entity.setFlag("C");
					entity.setCreatedBy(currentUser);
					entity.setCreatedAt(now);
					log.debug("CREATING Anukampa | rowId={} | corrId={}", rowId, corrId);
				} else {
					entity.setFlag("U");
					log.debug("UPDATING Anukampa | rowId={} | corrId={}", rowId, corrId);
				}

				entity.setUpdatedBy(currentUser);
				entity.setUpdatedAt(now);

				agendaThirteenRepository.save(entity);
			}

			response.setMessage("Success");
			error.setErrorCode("200");
			error.setErrorDescription("Saved successfully");

			log.info("SUCCESS saveOrUpdateAnukampa | year={} | targetDate={} | corrId={}", year, targetDate, corrId);

		} catch (Exception e) {

			log.error("ERROR saveOrUpdateAnukampa | corrId={}", corrId, e);

			error.setErrorCode("500");
			error.setErrorDescription(e.getMessage());
			response.setMessage("Failed");
		}

		response.setErrorDetails(error);
		return response;
	}

	@Override
	public ThirteenResponse getAnukampaData(String year, String targetDate) {

		ThirteenResponse response = new ThirteenResponse();
		ApplicationError error = new ApplicationError();
		final String corrId = MDC.get("correlationId");

		try {

			log.info("START getAnukampaData | year={} | targetDate={} | corrId={}", year, targetDate, corrId);

			List<AgendaThirteenEntity> list = agendaThirteenRepository.findByYearAndTargetDate(year, targetDate)
					.stream().sorted(Comparator.comparingLong(AgendaThirteenEntity::getRowId)).toList();

			response.setData(list);
			response.setMessage("Success");

			error.setErrorCode("200");
			error.setErrorDescription("Fetched successfully");

			log.info("FETCHED Anukampa | records={} | corrId={}", list.size(), corrId);

		} catch (Exception e) {

			log.error("ERROR getAnukampaData | year={} | targetDate={} | corrId={}", year, targetDate, corrId, e);

			error.setErrorCode("500");
			error.setErrorDescription(e.getMessage());
			response.setMessage("Failed");
		}

		response.setErrorDetails(error);
		return response;
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadAnukampaExcel(String year, String targetDate) throws Exception {

		List<AgendaThirteenEntity> list = agendaThirteenRepository.findByYearAndTargetDate(year, targetDate);

		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("Anukampa");
		sheet.setZoom(70);

		// ----------------------------------------------------------------
		// FONT & STYLES
		// ----------------------------------------------------------------
		XSSFFont boldFont = workbook.createFont();
		boldFont.setBold(true);
		boldFont.setFontHeight(11);

		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setWrapText(true);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);
		headerStyle.setFont(boldFont);

		CellStyle centerStyle = workbook.createCellStyle();
		centerStyle.setWrapText(true);
		centerStyle.setAlignment(HorizontalAlignment.CENTER);
		centerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		centerStyle.setBorderBottom(BorderStyle.THIN);
		centerStyle.setBorderTop(BorderStyle.THIN);
		centerStyle.setBorderLeft(BorderStyle.THIN);
		centerStyle.setBorderRight(BorderStyle.THIN);

		CellStyle titleStyle = workbook.createCellStyle();
		XSSFFont titleFont = workbook.createFont();
		titleFont.setBold(true);
		titleFont.setFontHeight(16);
		titleStyle.setFont(titleFont);
		titleStyle.setAlignment(HorizontalAlignment.CENTER);
		titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

		// ----------------------------------------------------------------
		// TITLE ROW
		// ----------------------------------------------------------------
		Row titleRow = sheet.createRow(0);
		Cell titleCell = titleRow.createCell(0);
		titleCell.setCellValue("अधीक्षक अभियंता,पुणे पाटबंधारे प्रकल्प मंडळ,पुणे");
		titleCell.setCellStyle(titleStyle);

		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 12));

		// ----------------------------------------------------------------
		// MAIN HEADER ROW (ROW 1)
		// ----------------------------------------------------------------
		Row mainHeader = sheet.createRow(1);
		mainHeader.setHeight((short) 900);

		String[] mainHeaders = { "अ.क्र.", "अनुकंपा सामाईक जेष्ठता क्रमांक", "कार्यालयाचे नांव",
				"दिवंगत कर्मचा-यांचे नांव", "दिवंगत झालेला दिनांक",
				"अनुकंपा कारणास्तव नोकरीसाठी अर्ज केलेल्या नातेवाईकाचे नाव", "नोकरीसाठी अर्ज केल्याचा दिनांक",
				"दिवंगत कर्मचारी यांचेशी नाते", "जन्मतारीख", "शैक्षणिक अर्हता", "जातीचा प्रवर्ग", "शेरा" // merged
																											// header
																											// for 11 &
																											// 12
		};

		for (int i = 0; i < mainHeaders.length; i++) {
			Cell c = mainHeader.createCell(i);
			c.setCellValue(mainHeaders[i]);
			c.setCellStyle(headerStyle);
		}

		// IMPORTANT: Create cell(12) because loop created only 0–11
		Cell sheraExtra = mainHeader.createCell(12);
		sheraExtra.setCellStyle(headerStyle);

		// APPLY BORDER STYLE to both merged shera cells
		Cell sheraH1 = mainHeader.getCell(11);
		Cell sheraH2 = mainHeader.getCell(12);
		sheraH1.setCellStyle(headerStyle);
		sheraH2.setCellStyle(headerStyle);

		// MERGE SHERA HEADER
		sheet.addMergedRegion(new CellRangeAddress(1, 1, 11, 12));

		// ----------------------------------------------------------------
		// SUB HEADER ROW (ROW 2)
		// ----------------------------------------------------------------
		Row subHeader = sheet.createRow(2);
		subHeader.setHeight((short) 900);

		// Merge vertically rows 1–2 for col 0 to col 10
		for (int col = 0; col <= 10; col++) {
			sheet.addMergedRegion(new CellRangeAddress(1, 2, col, col));
			Cell mergedCell = subHeader.createCell(col);
			mergedCell.setCellStyle(headerStyle);
		}

		// Create shera sub-columns
		Cell sh1 = subHeader.createCell(11);
		sh1.setCellValue("नियुक्ती दिली असल्यास");
		sh1.setCellStyle(headerStyle);

		Cell sh2 = subHeader.createCell(12);
		sh2.setCellValue("नियुक्ती आदेश दिनांक");
		sh2.setCellStyle(headerStyle);

		// ----------------------------------------------------------------
		// COLUMN WIDTHS
		// ----------------------------------------------------------------
		int[] columnWidths = { 2000, 5000, 6000, 8000, 5000, 9000, 5000, 6000, 5000, 5000, 4000, 5000, 6000 };
		for (int i = 0; i < columnWidths.length; i++) {
			sheet.setColumnWidth(i, columnWidths[i]);
		}

		// ----------------------------------------------------------------
		// NUMBERING ROW (ROW 3)
		// ----------------------------------------------------------------
		Row numberingRow = sheet.createRow(3);
		for (int i = 0; i < 13; i++) {
			Cell c = numberingRow.createCell(i);
			c.setCellValue(i + 1);
			c.setCellStyle(centerStyle);
		}

		// ----------------------------------------------------------------
		// DATA ROWS START AT ROW 4
		// ----------------------------------------------------------------
		int rowIndex = 4;

		for (AgendaThirteenEntity entity : list) {
			JsonNode json = entity.getColumnData();
			Row row = sheet.createRow(rowIndex++);

			fill(row, 0, json.get("kramank"), centerStyle);
			fill(row, 1, json.get("anukramank"), centerStyle);
			fill(row, 2, json.get("karyalayacheNav"), centerStyle);
			fill(row, 3, json.get("divangatKarmachariNav"), centerStyle);
			fill(row, 4, json.get("divangatDinank"), centerStyle);
			fill(row, 5, json.get("navteekKaranyasathiNoKS"), centerStyle);
			fill(row, 6, json.get("narkhasathiArjiDinank"), centerStyle);
			fill(row, 7, json.get("divangatKarmachariYogyaNate"), centerStyle);
			fill(row, 8, json.get("janmTarikh"), centerStyle);
			fill(row, 9, json.get("shaikshanikArhata"), centerStyle);
			fill(row, 10, json.get("jatPramanpatra"), centerStyle);

			if (json.has("shera")) {
				fill(row, 11, json.get("shera").get("nirNay"), centerStyle);
				fill(row, 12, json.get("shera").get("nikalDinank"), centerStyle);
			}
		}

		// ----------------------------------------------------------------
		// EXPORT FILE
		// ----------------------------------------------------------------
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		workbook.write(out);
		workbook.close();

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=Anukampa.xlsx");

		return ResponseEntity.ok().headers(headers)
				.contentType(
						MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(new InputStreamResource(in));
	}

	// ----------------------------------------------------------------
	// HELPER
	// ----------------------------------------------------------------
	private void fill(Row row, int col, JsonNode node, CellStyle style) {
		Cell cell = row.createCell(col);
		cell.setCellStyle(style);
		cell.setCellValue(node != null ? node.asText() : "");
	}

	@Override
	public AppealResponse saveOrUpdateAppeal2(AppealWrapper2 request) {

		AppealResponse response = new AppealResponse();
		ApplicationError error = new ApplicationError();
		response.setData(new ArrayList<>());

		final String currentUser = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
		final String corrId = MDC.get("correlationId");
		final LocalDateTime now = LocalDateTime.now();

		try {

			log.info("START saveOrUpdateAppeal2 | records={} | user={} | corrId={}", request.getAppealData().size(),
					currentUser, corrId);

			for (AppealRequest2 dto : request.getAppealData()) {

				Optional<AppealRequestEntity> existingOpt = Optional.empty();

				if (dto.getDeleteId() != null && dto.getDate() != null) {
					existingOpt = appealRequestRepository.findByDeleteIdAndDate(dto.getDeleteId(), dto.getDate());
				}

				String flag = Optional.ofNullable(dto.getFlag()).orElse("C").toUpperCase();

				// ---------------- DELETE ----------------
				if ("D".equalsIgnoreCase(flag)) {

					if (existingOpt.isPresent()) {
						appealRequestRepository.delete(existingOpt.get());

						log.info("DELETED Appeal2 | deleteId={} | date={} | corrId={}", dto.getDeleteId(),
								dto.getDate(), corrId);

						Map<String, Object> delMap = new LinkedHashMap<>();
						delMap.put("deleteId", dto.getDeleteId());
						delMap.put("date", dto.getDate());
						delMap.put("status", "DELETED");
						response.getData().add(delMap);
					}
					continue;
				}

				// ---------------- CREATE / UPDATE ----------------
				AppealRequestEntity entity = existingOpt.orElse(new AppealRequestEntity());
				boolean isUpdate = existingOpt.isPresent();

				entity.setDeleteId(dto.getDeleteId());
				entity.setYear(dto.getYear());
				entity.setRowId(dto.getRowId());
				entity.setDate(dto.getDate());

				if (isUpdate) {
					entity.setFlag("U");
					entity.setUpdatedBy(currentUser);
					entity.setUpdatedDate(now);
					log.debug("UPDATING Appeal2 | rowId={} | corrId={}", dto.getRowId(), corrId);
				} else {
					entity.setFlag("C");
					entity.setCreatedBy(currentUser);
					entity.setCreatedDate(now);
					entity.setUpdatedBy(currentUser);
					entity.setUpdatedDate(now);
					log.debug("CREATING Appeal2 | rowId={} | corrId={}", dto.getRowId(), corrId);
				}

				// -------- SET ALL FIELDS --------
				entity.setArjachaNondaniKramank(dto.getArjachaNondaniKramank());
				entity.setArjdarNavPatta(dto.getArjdarNavPatta());
				entity.setArjPraptDinank(dto.getArjPraptDinank());
				entity.setDarikhvReshBabli(dto.getDarikhvReshBabli());
				entity.setMahitiChaPrakar(dto.getMahitiChaPrakar());
				entity.setMaangItlelyaMahitichiRuprekha(dto.getMaangItlelyaMahitichiRuprekha());
				entity.setYogyMahitiAaheKa(dto.getYogyMahitiAaheKa());
				entity.setArjShulk(dto.getArjShulk());
				entity.setShulkPramanitDinank(dto.getShulkPramanitDinank());
				entity.setMahitiDilyaDinank(dto.getMahitiDilyaDinank());
				entity.setMahitiNghitAaheKa(dto.getMahitiNghitAaheKa());
				entity.setKonKshaAwarNokri(dto.getKonKshaAwarNokri());
				entity.setPrathamAppeal(dto.getPrathamAppeal());
				entity.setShera(dto.getShera());
				entity.setDynamicColumns(dto.getDynamicColumns());

				AppealRequestEntity saved = appealRequestRepository.save(entity);

				Map<String, Object> map = new LinkedHashMap<>();
				map.put("deleteId", saved.getDeleteId());
				map.put("year", saved.getYear());
				map.put("flag", saved.getFlag());
				map.put("status", isUpdate ? "UPDATED" : "CREATED");
				response.getData().add(map);
			}

			error.setErrorCode("200");
			error.setErrorDescription("Success");

			log.info("SUCCESS saveOrUpdateAppeal2 | processed={} | corrId={}", response.getData().size(), corrId);

		} catch (Exception e) {

			log.error("ERROR saveOrUpdateAppeal2 | corrId={}", corrId, e);

			error.setErrorCode("500");
			error.setErrorDescription(e.getMessage());
		}

		response.setErrorDetails(error);
		return response;
	}

	@Override
	public AppealResponse getAppealData2(String year) {

		AppealResponse response = new AppealResponse();
		ApplicationError error = new ApplicationError();
		response.setData(new ArrayList<>());

		final String corrId = MDC.get("correlationId");

		try {

			log.info("START getAppealData2 | year={} | corrId={}", year, corrId);

			List<AppealRequestEntity> appealList = (year != null && !year.isBlank())
					? appealRequestRepository.findByYear(year)
					: appealRequestRepository.findAll();

			for (AppealRequestEntity e : appealList) {

				Map<String, Object> map = new LinkedHashMap<>();

				map.put("id", e.getId());
				map.put("rowId", e.getRowId());
				map.put("year", e.getYear());
				map.put("date", e.getDate());
				map.put("deleteId", e.getDeleteId());
				map.put("flag", e.getFlag());

				map.put("arjachaNondaniKramank", e.getArjachaNondaniKramank());
				map.put("arjdarNavPatta", e.getArjdarNavPatta());
				map.put("arjPraptDinank", e.getArjPraptDinank());
				map.put("darikhvReshBabli", e.getDarikhvReshBabli());
				map.put("mahitiChaPrakar", e.getMahitiChaPrakar());
				map.put("maangItlelyaMahitichiRuprekha", e.getMaangItlelyaMahitichiRuprekha());
				map.put("yogyMahitiAaheKa", e.getYogyMahitiAaheKa());
				map.put("arjShulk", e.getArjShulk());
				map.put("shulkPramanitDinank", e.getShulkPramanitDinank());
				map.put("mahitiDilyaDinank", e.getMahitiDilyaDinank());
				map.put("mahitiNghitAaheKa", e.getMahitiNghitAaheKa());
				map.put("konKshaAwarNokri", e.getKonKshaAwarNokri());
				map.put("prathamAppeal", e.getPrathamAppeal());
				map.put("shera", e.getShera());

				if (e.getDynamicColumns() != null && !e.getDynamicColumns().isEmpty()) {
					map.put("dynamicColumns", objectMapper.convertValue(e.getDynamicColumns(), Map.class));
				} else {
					map.put("dynamicColumns", new LinkedHashMap<>());
				}

				map.put("createdBy", e.getCreatedBy());
				map.put("createdDate", e.getCreatedDate());
				map.put("updatedBy", e.getUpdatedBy());
				map.put("updatedDate", e.getUpdatedDate());

				response.getData().add(map);
			}

			// SORT BY rowId
			response.getData().sort(Comparator.comparing(m -> Integer.parseInt(m.get("rowId").toString())));

			Map<String, Object> meta = new LinkedHashMap<>();
			meta.put("totalRecords", response.getData().size());
			meta.put("filterYear", year);
			response.setMeta(meta);

			response.setMessage("Appeal register data fetched successfully.");
			error.setErrorCode("200");
			error.setErrorDescription("Success");

			log.info("FETCHED Appeal2 | records={} | corrId={}", response.getData().size(), corrId);

		} catch (Exception ex) {

			log.error("ERROR getAppealData2 | year={} | corrId={}", year, corrId, ex);

			response.setMessage("Error while fetching appeal data.");
			error.setErrorCode("500");
			error.setErrorDescription(ex.getMessage());
		}

		response.setErrorDetails(error);
		return response;
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadAppealArj2(String year) throws IOException {
		List<AppealRequestEntity> list = (year != null && !year.isBlank()) ? appealRequestRepository.findByYear(year)
				: appealRequestRepository.findAll();

		list.sort(Comparator.comparing(AppealRequestEntity::getRowId));

		XSSFWorkbook wb = new XSSFWorkbook();

		XSSFSheet sh = wb.createSheet("Appeal Register");
		sh.setZoom(80);

		// ---------- COLUMN WIDTH ----------
		int[] colWidths = { 2000, 4500, 4500, 4500, 4500, 4500, 5500, 4500, 3500, 4500, 4500, 4000, 4000, 4000, 4500 };
		for (int i = 0; i < colWidths.length; i++)
			sh.setColumnWidth(i, colWidths[i]);

		// ---------- FONTS ----------
		XSSFFont marathiFont = wb.createFont();
		marathiFont.setFontName("Mangal");
		marathiFont.setFontHeightInPoints((short) 12);

		XSSFFont headerFont = wb.createFont();
		headerFont.setFontName("Mangal");
		headerFont.setFontHeightInPoints((short) 12);
		headerFont.setBold(true);

		// ---------- BORDER STYLE ----------
		CellStyle baseBorder = wb.createCellStyle();
		baseBorder.setBorderBottom(BorderStyle.MEDIUM);
		baseBorder.setBorderTop(BorderStyle.MEDIUM);
		baseBorder.setBorderLeft(BorderStyle.MEDIUM);
		baseBorder.setBorderRight(BorderStyle.MEDIUM);
		baseBorder.setAlignment(HorizontalAlignment.CENTER);
		baseBorder.setVerticalAlignment(VerticalAlignment.CENTER);
		baseBorder.setWrapText(true);

		CellStyle headerStyle = wb.createCellStyle();
		headerStyle.cloneStyleFrom(baseBorder);
		headerStyle.setFont(headerFont);

		CellStyle dataStyle = wb.createCellStyle();
		dataStyle.cloneStyleFrom(baseBorder);
		dataStyle.setFont(marathiFont);

		CellStyle numberStyle = wb.createCellStyle();
		numberStyle.cloneStyleFrom(baseBorder);
		numberStyle.setFont(marathiFont);

		// ---------- TITLE ----------
		sh.createRow(0);

		Row titleRow = sh.createRow(1);
		Cell titleCell = titleRow.createCell(0);
		titleCell.setCellValue("माहिती अधिकारांतर्गत  मंडळ कार्यालयास प्राप्त झालेले अर्ज");
		titleCell.setCellStyle(headerStyle);
		sh.addMergedRegion(new CellRangeAddress(1, 1, 0, 14));

		// ---------- HEADERS ----------
		String[] headers = { "अ.क्र.", "अर्जाचा नोंदणी क्रमांक", "अर्जदाराचे नाव व पत्ता",
				"अर्ज प्राप्त झाल्याचा दिनांक", "अर्जदार दारिद्रय रेषेखालील आहे काय", "माहितीचा थोडक्यात तपशील",
				"मागितलेल्या माहितीचे स्वरुप", "त्रयस्थ पक्षाची माहिती आहे काय?", "अर्जाचे शुल्क रु.",
				"माहिती शुल्क कळविल्याचा दिनांक", "माहिती दिल्याचा दिनांक", "माहिती नाकारली आहे का",
				"कोणत्या कलमा आधारे नाकारली", "प्रथम अपील झाले आहे काय?", "शेरा" };

		Row headerRow = sh.createRow(2);
		headerRow.setHeightInPoints(40);

		for (int i = 0; i < headers.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(headers[i]);
			cell.setCellStyle(headerStyle);
		}

		// ---------- COLUMN NUMBERS ----------
		Row numRow = sh.createRow(3);
		for (int i = 0; i < headers.length; i++) {
			Cell num = numRow.createCell(i);
			num.setCellValue(i + 1);
			num.setCellStyle(numberStyle);
		}

		// ---------- DATA ----------
		int rowNum = 4;
		int sr = 1;

		for (AppealRequestEntity e : list) {

			Row r = sh.createRow(rowNum++);

			int c = 0;

			addCell(r, c++, String.valueOf(sr++), dataStyle);
			addCell(r, c++, nvl(e.getArjachaNondaniKramank()), dataStyle);
			addCell(r, c++, nvl(e.getArjdarNavPatta()), dataStyle);
			addCell(r, c++, nvl(e.getArjPraptDinank()), dataStyle);
			addCell(r, c++, nvl(e.getDarikhvReshBabli()), dataStyle);
			addCell(r, c++, nvl(e.getMahitiChaPrakar()), dataStyle);
			addCell(r, c++, nvl(e.getMaangItlelyaMahitichiRuprekha()), dataStyle);
			addCell(r, c++, nvl(e.getYogyMahitiAaheKa()), dataStyle);
			addCell(r, c++, nvl(e.getArjShulk()), dataStyle);
			addCell(r, c++, nvl(e.getShulkPramanitDinank()), dataStyle);
			addCell(r, c++, nvl(e.getMahitiDilyaDinank()), dataStyle);
			addCell(r, c++, nvl(e.getMahitiNghitAaheKa()), dataStyle);
			addCell(r, c++, nvl(e.getKonKshaAwarNokri()), dataStyle);
			addCell(r, c++, nvl(e.getPrathamAppeal()), dataStyle);
			addCell(r, c++, nvl(e.getShera()), dataStyle);
		}

		// WRITE STREAM
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		wb.write(out);
		wb.close();

		HttpHeaders hh = new HttpHeaders();
		hh.add("Content-Disposition", "attachment; filename=Appeal_Register.xlsx");

		return ResponseEntity.ok().headers(hh)
				.contentType(
						MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())));
	}

	@Override
	@Transactional
	public MasterDataResponse saveMasterData(MasterDataRequest request) {

		MasterDataResponse response = new MasterDataResponse();
		ApplicationError error = new ApplicationError();

		final String username = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
		final String corrId = MDC.get("correlationId");
		final LocalDateTime now = LocalDateTime.now();

		try {

			log.info("START saveMasterData | year={} | rows={} | user={} | corrId={}", request.getYear(),
					request.getRows().size(), username, corrId);

			for (PraptraMasterDataRowRequest row : request.getRows()) {

				// ---------------- DELETE ----------------
				if ("D".equalsIgnoreCase(row.getFlag())) {

					if (row.getDeleteId() == null) {
						throw new RuntimeException("deleteId is mandatory");
					}

					masterDataRepository.deleteByYearAndDeleteId(request.getYear(), row.getDeleteId());

					log.info("DELETED MasterData | year={} | deleteId={} | corrId={}", request.getYear(),
							row.getDeleteId(), corrId);
					continue;
				}

				// ---------------- UPDATE / INSERT ----------------
				Optional<MasterDataEntity> existing = masterDataRepository.findByYearAndRowId(request.getYear(),
						row.getRowId());

				if (existing.isPresent()) {

					MasterDataEntity e = existing.get();
					e.setData(row.getData());
					e.setFlag("U");
					e.setUpdatedAt(now);
					e.setUpdatedBy(username);

					masterDataRepository.save(e);

					log.debug("UPDATED MasterData | year={} | rowId={} | corrId={}", request.getYear(), row.getRowId(),
							corrId);

				} else {

					MasterDataEntity e = new MasterDataEntity();
					e.setYear(request.getYear());
					e.setRowId(row.getRowId());
					e.setDeleteId(row.getDeleteId());
					e.setData(row.getData());
					e.setFlag("C");
					e.setCreatedAt(now);
					e.setUpdatedAt(now);
					e.setCreatedBy(username);
					e.setUpdatedBy(username);

					masterDataRepository.save(e);

					log.debug("CREATED MasterData | year={} | rowId={} | corrId={}", request.getYear(), row.getRowId(),
							corrId);
				}
			}

			response.setMessage("Master Data saved successfully");
			response.setData(List.of());

			error.setErrorCode("200");
			error.setErrorDescription("Success");
			response.setErrorDetails(error);

			log.info("SUCCESS saveMasterData | year={} | corrId={}", request.getYear(), corrId);

			return response;

		} catch (Exception e) {

			log.error("ERROR saveMasterData | year={} | corrId={}", request.getYear(), corrId, e);

			response.setMessage("Failed to save Master Data");
			response.setData(null);

			error.setErrorCode("500");
			error.setErrorDescription(e.getMessage());
			response.setErrorDetails(error);

			return response;
		}
	}

	@Override
	public MasterDataResponse getMasterData(String year) {

		MasterDataResponse response = new MasterDataResponse();
		ApplicationError error = new ApplicationError();
		List<Map<String, Object>> list = new ArrayList<>();

		final String corrId = MDC.get("correlationId");

		try {

			log.info("START getMasterData | year={} | corrId={}", year, corrId);

			for (MasterDataEntity e : masterDataRepository.findAllByYearOrderByRowId(year)) {

				Map<String, Object> m = new LinkedHashMap<>();
				m.put("rowId", e.getRowId());
				m.put("deleteId", e.getDeleteId());
				m.put("data", e.getData());
				list.add(m);
			}

			response.setMessage("Master Data fetched");
			response.setData(list);

			error.setErrorCode("200");
			error.setErrorDescription("Success");
			response.setErrorDetails(error);

			log.info("FETCHED MasterData | year={} | records={} | corrId={}", year, list.size(), corrId);

			return response;

		} catch (Exception e) {

			log.error("ERROR getMasterData | year={} | corrId={}", year, corrId, e);

			response.setMessage("Failed to fetch Master Data");
			response.setData(null);

			error.setErrorCode("500");
			error.setErrorDescription(e.getMessage());
			response.setErrorDetails(error);

			return response;
		}
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadMasterData(String year) throws IOException {

		List<MasterDataEntity> list = masterDataRepository.findAllByYearOrderByRowId(year);

		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = wb.createSheet("विवरणपत्र");
		sheet.setZoom(70);

		// ======================= FONT =======================
		XSSFFont boldFont = wb.createFont();
		boldFont.setBold(true);

		// ======================= STYLES =====================
		XSSFCellStyle titleStyle = wb.createCellStyle();
		titleStyle.setFont(boldFont);
		titleStyle.setAlignment(HorizontalAlignment.CENTER);
		titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

		XSSFCellStyle centerStyle = wb.createCellStyle();
		centerStyle.setAlignment(HorizontalAlignment.CENTER);
		centerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

		XSSFCellStyle headerTop = wb.createCellStyle();
		headerTop.setFont(boldFont);
		headerTop.setWrapText(true);
		headerTop.setAlignment(HorizontalAlignment.CENTER);
		headerTop.setVerticalAlignment(VerticalAlignment.CENTER);
		headerTop.setBorderTop(BorderStyle.THIN);
		headerTop.setBorderLeft(BorderStyle.THIN);
		headerTop.setBorderRight(BorderStyle.THIN);

		XSSFCellStyle headerBottom = wb.createCellStyle();
		headerBottom.cloneStyleFrom(headerTop);
		headerBottom.setBorderBottom(BorderStyle.THIN);

		XSSFCellStyle headerFull = wb.createCellStyle();
		headerFull.cloneStyleFrom(headerBottom);
		headerFull.setBorderTop(BorderStyle.THIN);

		XSSFCellStyle dataStyle = wb.createCellStyle();
		dataStyle.setWrapText(true);
		dataStyle.setAlignment(HorizontalAlignment.CENTER);
		dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		dataStyle.setBorderTop(BorderStyle.THIN);
		dataStyle.setBorderBottom(BorderStyle.THIN);
		dataStyle.setBorderLeft(BorderStyle.THIN);
		dataStyle.setBorderRight(BorderStyle.THIN);

		// ======================= BORDER HELPER ======================
		BiConsumer<CellRangeAddress, CellStyle> applyBorder = (region, style) -> {
			for (int r = region.getFirstRow(); r <= region.getLastRow(); r++) {
				Row row = sheet.getRow(r);
				if (row == null)
					row = sheet.createRow(r);
				for (int c = region.getFirstColumn(); c <= region.getLastColumn(); c++) {
					Cell cell = row.getCell(c);
					if (cell == null)
						cell = row.createCell(c);
					cell.setCellStyle(style);
				}
			}
		};

		// ======================= ROW 1–2 ======================
		Row r1 = sheet.createRow(0);
		r1.createCell(0).setCellValue("पुणे पाटबंधारे प्रकल्प मंडळ, पुणे 01");
		r1.getCell(0).setCellStyle(titleStyle);

		CellRangeAddress regTitle = new CellRangeAddress(0, 1, 0, 27);
		sheet.addMergedRegion(regTitle);
		applyBorder.accept(regTitle, titleStyle);

		// ======================= ROW 3 ======================
		Row r3 = sheet.createRow(2);
		r3.createCell(0).setCellValue("विवरणपत्र");
		r3.getCell(0).setCellStyle(centerStyle);

		CellRangeAddress regSubTitle = new CellRangeAddress(2, 2, 0, 27);
		sheet.addMergedRegion(regSubTitle);
		applyBorder.accept(regSubTitle, centerStyle);

		// ======================= ROW 4 ======================
		Row r4 = sheet.createRow(3);

		// LEFT aligned style
		XSSFCellStyle leftStyle = wb.createCellStyle();
		leftStyle.setAlignment(HorizontalAlignment.LEFT);
		leftStyle.setVerticalAlignment(VerticalAlignment.CENTER);

		r4.createCell(1).setCellValue(
				"विभागाचे/उपविभागाचे नाव - कार्यकारी अभियंता, पाटबंधारे प्रकल्प अन्वेष्ण विभाग (भिमा उपखोरे) पुणे 11");
		r4.getCell(1).setCellStyle(leftStyle);

		// 🔥 Merge from B to AC (column 1 to 27)
		CellRangeAddress regDept = new CellRangeAddress(3, 3, 1, 27);
		sheet.addMergedRegion(regDept);
		applyBorder.accept(regDept, leftStyle);

		// ======================= HEADER ROWS ======================
		Row h1 = sheet.createRow(5);
		h1.setHeightInPoints(90);

		Row h2 = sheet.createRow(6);
		h2.setHeightInPoints(60);

		String[] mainHeaders = { "अ. क्र.", "कर्मचाऱ्‍याचे/अधिकाऱ्याचे  नाव", "पदनाम",
				"सेवेत नियुक्ती सरळसेवा/दिव्यांग/खेळाडू /प्रकल्पग्रस्त/अनुकंपा", "जन्मदिनांक", "सेवानिवृत्ती दिनांक",
				"जातीचा प्रवर्ग (खुला/इमाव/अजा/अज) व जात वैधता झाली आहे काय",
				"सेवेत मूळ नेमणुकीचा दिनांक व मूळ नियुक्तीचे पदनाम (RT/CRT नमूद करावे)",
				"सध्याचे पदावर कार्यरत असलेला दिनांक व पदनाम",
				"कर्मचाऱ्‍याचे/अधिकाऱ्यांची मूळ नियुक्तीपासून बदली झाली असल्यास त्याचा तपशील", "", "कार्यालयाचे नाव",
				"स्थायीत्व प्रमाणपत्र", "खात्याची परीक्षा उत्तीर्ण आहे किंवा कसे?",
				"हिंदी/मराठी भाषा परीक्षा उत्तीर्ण/सूट आहे काय?", "",
				"विभागीय चौकशी प्रस्तावित / चालू असल्यास त्याचा तपशिल", "यापूर्वी नियमित पदोन्नती नाकारली किंवा कसे?",
				"संगणक परिक्षा उत्तीर्ण", "50/55 पुनर्विलोकन झाले आहे किंवा कसे ?",
				"मत्ता व दायित्व सादर केले किंवा कसे ?",
				"सेवांतर्गत आश्वासित प्रगती योजनेचा पहिला लाभ अनुज्ञेय केलेचा दिनांक व पद",
				"सुधारित सेवांतर्गत आश्वासित प्रगती योजनेचा दुसरा लाभ अनुज्ञेय केलेचा दिनांक व पदनाम",
				"सेवांतर्गत आश्वासित प्रगती योजनेंतर्गत पहिला /दुसरा लाभ मंजूरीनंतर पदोन्नती मिळाली असल्यास पदोन्नतीचे पदनाम व दिनांक",
				"सुधारित सेवांतर्गत आश्वासित प्रगती योजना /  तीन लाभांची सुधारित सेवांतर्गत आश्वासित प्रगती योजनेचा लाभास पात्र होत असल्याचा दिनांक",
				"DCPS ची नोंद", "वेतन पडताळणी झाली आहे का ? असल्यास दिनांक", "शेरा" };

		for (int i = 0; i < mainHeaders.length; i++) {
			Cell cell = h1.createCell(i);
			cell.setCellValue(mainHeaders[i]);
			cell.setCellStyle(headerTop);

			if (i == 9 || i == 10 || i == 14 || i == 15)
				continue;

			CellRangeAddress region = new CellRangeAddress(5, 6, i, i);
			sheet.addMergedRegion(region);
			applyBorder.accept(region, headerTop);
		}

		// parent header colspan
		CellRangeAddress badli = new CellRangeAddress(5, 5, 9, 10);
		sheet.addMergedRegion(badli);
		applyBorder.accept(badli, headerTop);

		CellRangeAddress lang = new CellRangeAddress(5, 5, 14, 15);
		sheet.addMergedRegion(lang);
		applyBorder.accept(lang, headerTop);

		// sub headers
		h2.createCell(9).setCellValue("पासून");
		h2.createCell(10).setCellValue("पर्यंत");
		h2.createCell(14).setCellValue("हिंदी");
		h2.createCell(15).setCellValue("मराठी");

		h2.getCell(9).setCellStyle(headerBottom);
		h2.getCell(10).setCellStyle(headerBottom);
		h2.getCell(14).setCellStyle(headerBottom);
		h2.getCell(15).setCellStyle(headerBottom);

		// ======================= COLUMN NUMBERS ======================
		Row r8 = sheet.createRow(7);
		for (int i = 0; i < 28; i++) {
			Cell c = r8.createCell(i);
			c.setCellValue(i + 1);
			c.setCellStyle(headerFull);
		}

		// ======================= DATA ======================
		int rowIdx = 8;

		for (MasterDataEntity e : list) {

			JsonNode d = e.getData();

			String[] pasunArr = d.path("sevaTapshilPasun").asText("").split("\\s+");
			String[] paryantArr = d.path("sevaTapshilParyant").asText("").split("\\s+");
			String[] karyalayArr = d.path("karyalayNav").asText("").trim().split("\\s{3,}");

			int blockSize = Math.max(Math.max(pasunArr.length, paryantArr.length), karyalayArr.length);

			int startRow = rowIdx;

			for (int i = 0; i < blockSize; i++) {
				Row row = sheet.createRow(rowIdx++);

				row.createCell(9).setCellValue(i < pasunArr.length ? pasunArr[i] : "");
				row.createCell(10).setCellValue(i < paryantArr.length ? paryantArr[i] : "");
				row.createCell(11).setCellValue(i < karyalayArr.length ? karyalayArr[i] : "");

				for (int c = 0; c < 28; c++) {
					Cell cell = row.getCell(c);
					if (cell == null)
						cell = row.createCell(c);
					cell.setCellStyle(dataStyle);
				}
			}

			int endRow = startRow + blockSize - 1;
			Row fr = sheet.getRow(startRow);

			fr.getCell(0).setCellValue(d.path("srNo").asText(""));
			fr.getCell(1).setCellValue(d.path("adhikariNav").asText(""));
			fr.getCell(2).setCellValue(d.path("padnaam").asText(""));
			fr.getCell(3).setCellValue(d.path("sevetNiyukti").asText(""));
			fr.getCell(4).setCellValue(d.path("janmTarikh").asText(""));
			fr.getCell(5).setCellValue(d.path("sevaNivruttiTarikh").asText(""));
			fr.getCell(6).setCellValue(d.path("jatichaPravarg").asText(""));
			fr.getCell(7).setCellValue(d.path("sevetMulNemnukichaDinank").asText(""));
			fr.getCell(8).setCellValue(d.path("sadyacheyPadavarDinank").asText(""));
			fr.getCell(12).setCellValue(d.path("statyitvaPraanPatra").asText(""));
			fr.getCell(13).setCellValue(d.path("khtyachiParikshaUtrinAheKa").asText(""));
			fr.getCell(14).setCellValue(d.path("hindiPariksha").asText(""));
			fr.getCell(15).setCellValue(d.path("marathiPariksha").asText(""));
			fr.getCell(16).setCellValue(d.path("vibhagiyaChouksi").asText(""));
			fr.getCell(17).setCellValue(d.path("yapurviNiyamitPadonnatiKasey").asText(""));
			fr.getCell(18).setCellValue(d.path("sangnakParikshaUtrin").asText(""));
			fr.getCell(19).setCellValue(d.path("purnavilokan5050Jhaley").asText(""));
			fr.getCell(20).setCellValue(d.path("mattaVaDaitvaSadarKasey").asText(""));
			fr.getCell(21).setCellValue(d.path("sevaantrgatAshvashitPragatiYojnachaLabh").asText(""));
			fr.getCell(22).setCellValue(d.path("sudhariSevaAntargatKelechaDinank").asText(""));
			fr.getCell(23).setCellValue(d.path("sevaAntargatAshvashitPrgatiYojnaLabh").asText(""));
			fr.getCell(24).setCellValue(d.path("sudharitSevantargatAshvashitPrgatiYojna").asText(""));
			fr.getCell(25).setCellValue(d.path("dcpsChiNond").asText(""));
			fr.getCell(26).setCellValue(d.path("vetanPadtaliniJhali").asText(""));
			fr.getCell(27).setCellValue(d.path("shera").asText(""));

			for (int c = 0; c < 28; c++) {

				// ❌ DO NOT MERGE split columns
				if (c == 9 || c == 10 || c == 11)
					continue;

				// ❌ DO NOT MERGE if only one row
				if (startRow == endRow)
					continue;

				CellRangeAddress region = new CellRangeAddress(startRow, endRow, c, c);

				sheet.addMergedRegion(region);
				applyBorder.accept(region, dataStyle);
			}

		}

		for (int i = 0; i < 28; i++) {
			sheet.setColumnWidth(i, 5000);
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		wb.write(out);
		wb.close();

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=MasterData_" + year + ".xlsx")
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())));
	}

	@Override
	@Transactional
	public MasterDataResponse saveCrFileList(MasterDataRequest request) {

		MasterDataResponse response = new MasterDataResponse();
		ApplicationError error = new ApplicationError();

		final String username = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
		final String corrId = MDC.get("correlationId");
		final LocalDateTime now = LocalDateTime.now();

		try {

			log.info("START saveCrFileList | year={} | rows={} | user={} | corrId={}", request.getYear(),
					request.getRows().size(), username, corrId);

			for (PraptraMasterDataRowRequest r : request.getRows()) {

				// ---------------- HARD DELETE ----------------
				if ("D".equalsIgnoreCase(r.getFlag())) {

					crFileListRepository.deleteByYearAndDeleteId(request.getYear(), r.getDeleteId());

					log.info("DELETED CrFileList | year={} | deleteId={} | corrId={}", request.getYear(),
							r.getDeleteId(), corrId);
					continue;
				}

				// ---------------- SAVE / UPDATE ----------------
				Optional<CrFileListEntity> opt = crFileListRepository.findByYearAndRowId(request.getYear(),
						r.getRowId());

				CrFileListEntity e;

				if (opt.isPresent()) {
					// UPDATE
					e = opt.get();
					e.setFlag("U");
					e.setUpdatedAt(now);
					e.setUpdatedBy(username);

					log.debug("UPDATED CrFileList | year={} | rowId={} | corrId={}", request.getYear(), r.getRowId(),
							corrId);

				} else {
					// CREATE
					e = new CrFileListEntity();
					e.setYear(request.getYear());
					e.setRowId(r.getRowId());
					e.setDeleteId(r.getDeleteId());
					e.setFlag("C");
					e.setCreatedAt(now);
					e.setCreatedBy(username);
					e.setUpdatedAt(now);
					e.setUpdatedBy(username);

					log.debug("CREATED CrFileList | year={} | rowId={} | corrId={}", request.getYear(), r.getRowId(),
							corrId);
				}

				e.setData(r.getData());
				crFileListRepository.save(e);
			}

			response.setMessage("CrFileList processed successfully");
			response.setData(List.of());

			error.setErrorCode("200");
			error.setErrorDescription("Success");
			response.setErrorDetails(error);

			log.info("SUCCESS saveCrFileList | year={} | corrId={}", request.getYear(), corrId);

			return response;

		} catch (Exception ex) {

			log.error("ERROR saveCrFileList | year={} | corrId={}", request.getYear(), corrId, ex);

			response.setMessage("Failed to process CrFileList");
			response.setData(null);

			error.setErrorCode("500");
			error.setErrorDescription(ex.getMessage());
			response.setErrorDetails(error);

			return response;
		}
	}

	@Override
	public MasterDataResponse getCrFileList(String year) {

		MasterDataResponse response = new MasterDataResponse();
		ApplicationError error = new ApplicationError();
		final String corrId = MDC.get("correlationId");

		try {

			log.info("START getCrFileList | year={} | corrId={}", year, corrId);

			List<CrFileListEntity> list = crFileListRepository.findAllByYearOrderByRowId(year);

			List<Map<String, Object>> data = list.stream()
					.map(e -> Map.of("rowId", e.getRowId(), "deleteId", e.getDeleteId(), "data", e.getData())).toList();

			response.setMessage("Success");
			response.setData(data);

			error.setErrorCode("200");
			error.setErrorDescription("Fetched successfully");
			response.setErrorDetails(error);

			log.info("FETCHED CrFileList | year={} | records={} | corrId={}", year, data.size(), corrId);

			return response;

		} catch (Exception ex) {

			log.error("ERROR getCrFileList | year={} | corrId={}", year, corrId, ex);

			response.setMessage("Failed to fetch CrFileList");
			response.setData(null);

			error.setErrorCode("500");
			error.setErrorDescription(ex.getMessage());
			response.setErrorDetails(error);

			return response;
		}
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadCrFileList(String year) throws IOException {

		List<CrFileListEntity> list = crFileListRepository.findAllByYearOrderByRowId(year);

		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = wb.createSheet("CR File List");

		// ================= FONT =================
		XSSFFont boldFont = wb.createFont();
		boldFont.setBold(true);

		// ================= STYLES =================
		XSSFCellStyle headerStyle = wb.createCellStyle();
		headerStyle.setFont(boldFont);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);

		XSSFCellStyle dataStyle = wb.createCellStyle();
		dataStyle.setAlignment(HorizontalAlignment.CENTER);
		dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		dataStyle.setBorderTop(BorderStyle.THIN);
		dataStyle.setBorderBottom(BorderStyle.THIN);
		dataStyle.setBorderLeft(BorderStyle.THIN);
		dataStyle.setBorderRight(BorderStyle.THIN);
		dataStyle.setWrapText(true);
		// ---------- ROW 1 : TITLE ----------
		Row titleRow = sheet.createRow(0);

		Cell titleCell = titleRow.createCell(0);
		titleCell.setCellValue("कार्यरत अधिकारी व कर्मचारी यांची धारिका यादी");
		titleCell.setCellStyle(headerStyle);

		// merge A1 to E1 (0–4)
		CellRangeAddress titleRegion = new CellRangeAddress(0, 0, 0, 4);
		sheet.addMergedRegion(titleRegion);

		// apply border + style to merged region
		for (int c = 0; c <= 4; c++) {
			Cell cell = titleRow.getCell(c);
			if (cell == null)
				cell = titleRow.createCell(c);
			cell.setCellStyle(headerStyle);
		}

		// ================= HEADER ROW =================
		Row header = sheet.createRow(2);

		String[] headers = { "अ. क्र.", "अधिकारी / कर्मचाऱ्याचे नाव", "पदनाम", "धारिका", "कालावधी" };

		for (int i = 0; i < headers.length; i++) {
			Cell c = header.createCell(i);
			c.setCellValue(headers[i]);
			c.setCellStyle(headerStyle);
			sheet.setColumnWidth(i, 6000);
		}

		// ================= DATA ROWS =================
		int rowIdx = 3;

		for (CrFileListEntity e : list) {

			JsonNode d = e.getData();
			Row row = sheet.createRow(rowIdx++);

			row.createCell(0).setCellValue(d.path("srNo").asText(""));
			row.createCell(1).setCellValue(d.path("adhikariNav").asText(""));
			row.createCell(2).setCellValue(d.path("padnaam").asText(""));
			row.createCell(3).setCellValue(d.path("dharika").asText(""));
			row.createCell(4).setCellValue(d.path("kalavdhi").asText(""));

			for (int c = 0; c < 5; c++) {
				row.getCell(c).setCellStyle(dataStyle);
			}
		}

		// ================= RESPONSE =================
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		wb.write(out);
		wb.close();

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=CrFileList_" + year + ".xlsx")
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())));
	}

	@Transactional
	@Override
	public MasterDataResponse saveCrFileRtrList(MasterDataRequest request) {

		MasterDataResponse response = new MasterDataResponse();
		ApplicationError error = new ApplicationError();

		final String username = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
		final String corrId = MDC.get("correlationId");
		final LocalDateTime now = LocalDateTime.now();

		try {

			log.info("START saveCrFileRtrList | year={} | rows={} | user={} | corrId={}", request.getYear(),
					request.getRows().size(), username, corrId);

			for (PraptraMasterDataRowRequest r : request.getRows()) {

				// ---------------- HARD DELETE ----------------
				if ("D".equalsIgnoreCase(r.getFlag())) {

					crFileListRtrRepository.deleteByYearAndDeleteId(request.getYear(), r.getDeleteId());

					log.info("DELETED CrFileRtr | year={} | deleteId={} | corrId={}", request.getYear(),
							r.getDeleteId(), corrId);
					continue;
				}

				// ---------------- SAVE / UPDATE ----------------
				Optional<CrFileListRtrEntity> opt = crFileListRtrRepository.findByYearAndRowId(request.getYear(),
						r.getRowId());

				CrFileListRtrEntity e;

				if (opt.isPresent()) {
					// UPDATE
					e = opt.get();
					e.setFlag("U");
					e.setUpdatedAt(now);
					e.setUpdatedBy(username);

					log.debug("UPDATED CrFileRtr | year={} | rowId={} | corrId={}", request.getYear(), r.getRowId(),
							corrId);

				} else {
					// CREATE
					e = new CrFileListRtrEntity();
					e.setYear(request.getYear());
					e.setRowId(r.getRowId());
					e.setDeleteId(r.getDeleteId());
					e.setFlag("C");
					e.setCreatedAt(now);
					e.setCreatedBy(username);
					e.setUpdatedAt(now);
					e.setUpdatedBy(username);

					log.debug("CREATED CrFileRtr | year={} | rowId={} | corrId={}", request.getYear(), r.getRowId(),
							corrId);
				}

				e.setData(r.getData());
				crFileListRtrRepository.save(e);
			}

			response.setMessage("CrFileList processed successfully");
			response.setData(List.of());

			error.setErrorCode("200");
			error.setErrorDescription("Success");
			response.setErrorDetails(error);

			log.info("SUCCESS saveCrFileRtrList | year={} | corrId={}", request.getYear(), corrId);

			return response;

		} catch (Exception ex) {

			log.error("ERROR saveCrFileRtrList | year={} | corrId={}", request.getYear(), corrId, ex);

			response.setMessage("Failed to process CrFileList");
			response.setData(null);

			error.setErrorCode("500");
			error.setErrorDescription(ex.getMessage());
			response.setErrorDetails(error);

			return response;
		}
	}

	@Override
	public MasterDataResponse getCrFileRtrList(String year) {

		MasterDataResponse response = new MasterDataResponse();
		ApplicationError error = new ApplicationError();
		final String corrId = MDC.get("correlationId");

		try {

			log.info("START getCrFileRtrList | year={} | corrId={}", year, corrId);

			List<CrFileListRtrEntity> list = crFileListRtrRepository.findAllByYearOrderByRowId(year);

			List<Map<String, Object>> data = list.stream()
					.map(e -> Map.of("rowId", e.getRowId(), "deleteId", e.getDeleteId(), "data", e.getData())).toList();

			response.setMessage("Success");
			response.setData(data);

			error.setErrorCode("200");
			error.setErrorDescription("Fetched successfully");
			response.setErrorDetails(error);

			log.info("FETCHED CrFileRtr | year={} | records={} | corrId={}", year, data.size(), corrId);

			return response;

		} catch (Exception ex) {

			log.error("ERROR getCrFileRtrList | year={} | corrId={}", year, corrId, ex);

			response.setMessage("Failed to fetch CrFileList");
			response.setData(null);

			error.setErrorCode("500");
			error.setErrorDescription(ex.getMessage());
			response.setErrorDetails(error);

			return response;
		}
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadCrFileRtrList(String year) throws IOException {
		List<CrFileListRtrEntity> list = crFileListRtrRepository.findAllByYearOrderByRowId(year);

		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = wb.createSheet("CR File Rtr List");

		// ================= FONT =================
		XSSFFont boldFont = wb.createFont();
		boldFont.setBold(true);

		// ================= STYLES =================
		XSSFCellStyle headerStyle = wb.createCellStyle();
		headerStyle.setFont(boldFont);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);

		XSSFCellStyle dataStyle = wb.createCellStyle();
		dataStyle.setAlignment(HorizontalAlignment.CENTER);
		dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		dataStyle.setBorderTop(BorderStyle.THIN);
		dataStyle.setBorderBottom(BorderStyle.THIN);
		dataStyle.setBorderLeft(BorderStyle.THIN);
		dataStyle.setBorderRight(BorderStyle.THIN);
		dataStyle.setWrapText(true);
		// ---------- ROW 1 : TITLE ----------
		Row titleRow = sheet.createRow(0);

		Cell titleCell = titleRow.createCell(0);
		titleCell.setCellValue("सेवानिवृत्त अधिकारी व कर्मचारी यांची धारिका यादी");
		titleCell.setCellStyle(headerStyle);

		// merge A1 to E1 (0–4)
		CellRangeAddress titleRegion = new CellRangeAddress(0, 0, 0, 4);
		sheet.addMergedRegion(titleRegion);

		// apply border + style to merged region
		for (int c = 0; c <= 4; c++) {
			Cell cell = titleRow.getCell(c);
			if (cell == null)
				cell = titleRow.createCell(c);
			cell.setCellStyle(headerStyle);
		}

		// ================= HEADER ROW =================
		Row header = sheet.createRow(2);

		String[] headers = { "अ. क्र.", "अधिकारी / कर्मचाऱ्याचे नाव", "पदनाम", "धारिका", "कालावधी" };

		for (int i = 0; i < headers.length; i++) {
			Cell c = header.createCell(i);
			c.setCellValue(headers[i]);
			c.setCellStyle(headerStyle);
			sheet.setColumnWidth(i, 6000);
		}

		// ================= DATA ROWS =================
		int rowIdx = 3;

		for (CrFileListRtrEntity e : list) {

			JsonNode d = e.getData();
			Row row = sheet.createRow(rowIdx++);

			row.createCell(0).setCellValue(d.path("srNo").asText(""));
			row.createCell(1).setCellValue(d.path("adhikariNav").asText(""));
			row.createCell(2).setCellValue(d.path("padnaam").asText(""));
			row.createCell(3).setCellValue(d.path("dharika").asText(""));
			row.createCell(4).setCellValue(d.path("kalavdhi").asText(""));

			for (int c = 0; c < 5; c++) {
				row.getCell(c).setCellStyle(dataStyle);
			}
		}

		// ================= RESPONSE =================
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		wb.write(out);
		wb.close();

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=CrFileRtrList_" + year + ".xlsx")
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())));
	}

	@Transactional
	@Override
	public MasterDataResponse saveMahaparRegister(MahaparRegisterRequest request) {

		MasterDataResponse response = new MasterDataResponse();
		ApplicationError error = new ApplicationError();

		final String username = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
		final String corrId = MDC.get("correlationId");
		final String year = request.getYear();
		final LocalDateTime now = LocalDateTime.now();

		try {

			log.info("START saveMahaparRegister | year={} | sections={} | user={} | corrId={}", year,
					request.getSections() == null ? 0 : request.getSections().size(), username, corrId);

			if (request.getSections() == null || request.getSections().isEmpty()) {
				throw new IllegalArgumentException("sections cannot be empty");
			}

			for (MahaparRegisterSectionRequest section : request.getSections()) {

				Long sectionId = section.getSectionId(); // 0 allowed
				String sectionName = section.getSectionName();

				if (section.getRows() == null || section.getRows().isEmpty()) {
					log.debug("Skipping empty section | sectionId={} | corrId={}", sectionId, corrId);
					continue;
				}

				for (MahaparRegisterRowRequest row : section.getRows()) {

					/* ------------ HARD DELETE ------------ */
					if ("D".equalsIgnoreCase(row.getFlag())) {

						if (row.getDeleteId() == null) {
							throw new IllegalArgumentException("deleteId is mandatory for deletion");
						}

						mahaparRegisterRepository.deleteByYearAndDeleteId(year, row.getDeleteId());

						log.info("DELETED MahaparRegister | year={} | sectionId={} | deleteId={} | corrId={}", year,
								sectionId, row.getDeleteId(), corrId);
						continue;
					}

					upsertMahaparEntity(year, sectionId, sectionName, row, username, now, corrId);
				}
			}

			response.setMessage("Mahapar Register saved successfully");
			response.setData(List.of());

			error.setErrorCode("200");
			error.setErrorDescription("Success");
			response.setErrorDetails(error);

			log.info("SUCCESS saveMahaparRegister | year={} | corrId={}", year, corrId);
			return response;

		} catch (Exception e) {

			log.error("ERROR saveMahaparRegister | year={} | corrId={}", year, corrId, e);

			response.setMessage("Failed to save Mahapar Register");
			response.setData(null);

			error.setErrorCode("500");
			error.setErrorDescription(e.getMessage());
			response.setErrorDetails(error);

			return response;
		}
	}

	private void upsertMahaparEntity(String year, Long sectionId, String sectionName, MahaparRegisterRowRequest row,
			String username, LocalDateTime now, String corrId) {

		Optional<MahaparRegisterEntity> opt = mahaparRegisterRepository.findByYearAndSectionIdAndRowId(year, sectionId,
				row.getRowId());

		MahaparRegisterEntity entity;

		if (opt.isPresent()) {
			/* -------- UPDATE -------- */
			entity = opt.get();
			entity.setFlag("U");
			entity.setUpdatedAt(now);
			entity.setUpdatedBy(username);

			log.debug("UPDATED MahaparRegister | year={} | sectionId={} | rowId={} | corrId={}", year, sectionId,
					row.getRowId(), corrId);

		} else {
			/* -------- CREATE -------- */
			entity = new MahaparRegisterEntity();
			entity.setYear(year);
			entity.setSectionId(sectionId); // 0 allowed
			entity.setSectionName(sectionName);
			entity.setRowId(row.getRowId());
			entity.setDeleteId(row.getDeleteId());
			entity.setFlag("C");
			entity.setCreatedAt(now);
			entity.setCreatedBy(username);
			entity.setUpdatedAt(now);
			entity.setUpdatedBy(username);

			log.debug("CREATED MahaparRegister | year={} | sectionId={} | rowId={} | corrId={}", year, sectionId,
					row.getRowId(), corrId);
		}

		entity.setData(row.getData());
		mahaparRegisterRepository.save(entity);
	}

	@Override
	public MasterDataResponse getMahaparRegister(String year) {

		MasterDataResponse response = new MasterDataResponse();
		ApplicationError error = new ApplicationError();
		final String corrId = MDC.get("correlationId");

		try {

			log.info("START getMahaparRegister | year={} | corrId={}", year, corrId);

			List<MahaparRegisterEntity> entities = mahaparRegisterRepository
					.findAllByYearOrderBySectionIdAscRowIdAsc(year);

			MahaparRegisterRequest result = new MahaparRegisterRequest();
			result.setYear(year);

			Map<Long, MahaparRegisterSectionRequest> sectionMap = new LinkedHashMap<>();

			for (MahaparRegisterEntity e : entities) {

				MahaparRegisterRowRequest row = new MahaparRegisterRowRequest();
				row.setRowId(e.getRowId());
				row.setDeleteId(e.getDeleteId());
				row.setFlag(e.getFlag());
				row.setData(e.getData());

				MahaparRegisterSectionRequest section = sectionMap.computeIfAbsent(e.getSectionId(), k -> {
					MahaparRegisterSectionRequest s = new MahaparRegisterSectionRequest();
					s.setSectionId(e.getSectionId());
					s.setSectionName(e.getSectionName());
					s.setRows(new ArrayList<>());
					return s;
				});

				section.getRows().add(row);
			}

			result.setSections(new ArrayList<>(sectionMap.values()));

			response.setMessage("Fetched successfully");
			response.setData(List.of(Map.of("request", result)));

			error.setErrorCode("200");
			error.setErrorDescription("Success");
			response.setErrorDetails(error);

			log.info("FETCHED MahaparRegister | year={} | sections={} | corrId={}", year, sectionMap.size(), corrId);

			return response;

		} catch (Exception e) {

			log.error("ERROR getMahaparRegister | year={} | corrId={}", year, corrId, e);

			response.setMessage("Failed to fetch Mahapar Register");
			response.setData(null);

			error.setErrorCode("500");
			error.setErrorDescription(e.getMessage());
			response.setErrorDetails(error);

			return response;
		}
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadMahaparRegister(String year) throws IOException {

		List<MahaparRegisterEntity> list = mahaparRegisterRepository.findAllByYearOrderBySectionIdAscRowIdAsc(year);

		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = wb.createSheet("कार्य मूल्यमापन");

		/* ===================== STYLES ===================== */

		XSSFFont boldFont = wb.createFont();
		boldFont.setBold(true);

		XSSFCellStyle centerBold = wb.createCellStyle();
		centerBold.setFont(boldFont);
		centerBold.setAlignment(HorizontalAlignment.CENTER);
		centerBold.setVerticalAlignment(VerticalAlignment.CENTER);

		XSSFCellStyle headerStyle = wb.createCellStyle();
		headerStyle.setFont(boldFont);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);

		XSSFCellStyle dataStyle = wb.createCellStyle();
		dataStyle.setAlignment(HorizontalAlignment.CENTER);
		dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		dataStyle.setBorderTop(BorderStyle.THIN);
		dataStyle.setBorderBottom(BorderStyle.THIN);
		dataStyle.setBorderLeft(BorderStyle.THIN);
		dataStyle.setBorderRight(BorderStyle.THIN);
		dataStyle.setWrapText(true);
		XSSFCellStyle leftBold = wb.createCellStyle();
		leftBold.setFont(boldFont);
		leftBold.setAlignment(HorizontalAlignment.LEFT);
		leftBold.setVerticalAlignment(VerticalAlignment.CENTER);

		int rowIdx = 0;

		/*
		 * ================================================== FIRST TABLE HEADER
		 * (INDEPENDENT) ==================================================
		 */

		rowIdx = writeMainHeader(sheet, rowIdx, "पुणे पाटबंधारे प्रकल्प मंडळ, पुणे-01",
				"मंडळ कार्यालय महापार प्रणालीमध्ये भरण्यात आलेल्या कार्यमुल्यमापन अहवालाची यादी", "सन " + year,
				centerBold);

		rowIdx = writeColumnHeader(sheet, rowIdx, headerStyle);

		/*
		 * ================================================== DATA PROCESSING
		 * ==================================================
		 */

		Long currentSectionId = null;
		boolean sectionHeaderPrinted = false;
		boolean firstSectionStarted = false;

		for (MahaparRegisterEntity e : list) {

			/* ---------- SWITCH FROM INDEPENDENT → SECTION TABLE ---------- */
			if (currentSectionId == null && e.getSectionId() != 0) {

				rowIdx += 2; // spacing

				rowIdx = writeMainHeader(sheet, rowIdx, "पुणे पाटबंधारे प्रकल्प मंडळ, पुणे-01",
						"मंडळांतर्गत विभागीय कार्यालयात महापार प्रणालीमध्ये भरण्यात आलेल्या कार्यमुल्यमापन अहवालाची यादी",
						"सन " + year, centerBold);
				rowIdx++;
				sectionHeaderPrinted = true;
				currentSectionId = -1L; // force section detection
			}

			// ---------- NEW SECTION ----------
			if (e.getSectionId() != 0 && !Objects.equals(currentSectionId, e.getSectionId())) {

				// ✅ if this is NOT the very first section → leave one empty row
				if (firstSectionStarted) {
					rowIdx++; // blank row between sections
				}

				firstSectionStarted = true;

				Row secRow = sheet.createRow(rowIdx++);

				// Column A → sectionId (CENTER)
				secRow.createCell(0).setCellValue(e.getSectionId());
				secRow.getCell(0).setCellStyle(centerBold);

				// Column B → sectionName (LEFT)
				secRow.createCell(1).setCellValue(e.getSectionName());
				secRow.getCell(1).setCellStyle(leftBold);

				// Merge B–F
				sheet.addMergedRegion(new CellRangeAddress(secRow.getRowNum(), secRow.getRowNum(), 1, 5));

				// Column header after section title
				rowIdx = writeColumnHeader(sheet, rowIdx, headerStyle);

				currentSectionId = e.getSectionId();
			}

			/* ---------- DATA ROW ---------- */

			JsonNode d = e.getData();
			Row r = sheet.createRow(rowIdx++);

			r.createCell(0).setCellValue(d.path("srNo").asText(""));
			r.createCell(1).setCellValue(d.path("adhikariNav").asText(""));
			r.createCell(2).setCellValue(d.path("padnaam").asText(""));
			r.createCell(3).setCellValue(d.path("kalavadhi").asText(""));
			r.createCell(4).setCellValue(d.path("keleliKaryavahi").asText(""));
			r.createCell(5).setCellValue(d.path("shera").asText(""));

			for (int i = 0; i < 6; i++) {
				r.getCell(i).setCellStyle(dataStyle);
			}
		}

		/* ===================== WIDTH ===================== */

		sheet.setColumnWidth(0, 2500);
		sheet.setColumnWidth(1, 8000);
		sheet.setColumnWidth(2, 6000);
		sheet.setColumnWidth(3, 6000);
		sheet.setColumnWidth(4, 9000);
		sheet.setColumnWidth(5, 5000);

		/* ===================== RESPONSE ===================== */

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		wb.write(out);
		wb.close();

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=MahaparRegister_" + year + ".xlsx")
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())));
	}

	private int writeMainHeader(XSSFSheet sheet, int rowIdx, String l1, String l2, String l3, CellStyle style) {
		Row r1 = sheet.createRow(rowIdx++);
		r1.createCell(0).setCellValue(l1);
		r1.getCell(0).setCellStyle(style);
		sheet.addMergedRegion(new CellRangeAddress(r1.getRowNum(), r1.getRowNum(), 0, 5));

		Row r2 = sheet.createRow(rowIdx++);
		r2.createCell(0).setCellValue(l2);
		r2.getCell(0).setCellStyle(style);
		sheet.addMergedRegion(new CellRangeAddress(r2.getRowNum(), r2.getRowNum(), 0, 5));

		Row r3 = sheet.createRow(rowIdx++);
		r3.createCell(0).setCellValue(l3);
		r3.getCell(0).setCellStyle(style);
		sheet.addMergedRegion(new CellRangeAddress(r3.getRowNum(), r3.getRowNum(), 0, 5));

		return rowIdx;
	}

	private int writeColumnHeader(XSSFSheet sheet, int rowIdx, CellStyle headerStyle) {
		Row h = sheet.createRow(rowIdx++);
		String[] cols = { "अ.क्र.", "अधिकाऱ्याचे नांव", "पदनाम", "कालावधी", "केलेली कार्यवाही", "शेरा" };

		for (int i = 0; i < cols.length; i++) {
			Cell c = h.createCell(i);
			c.setCellValue(cols[i]);
			c.setCellStyle(headerStyle);
		}
		return rowIdx;
	}

	@Transactional
	@Override
	public MasterDataResponse saveKaryaratGopniyaAhwal(MahaparRegisterRequest request) {

		MasterDataResponse response = new MasterDataResponse();
		ApplicationError error = new ApplicationError();

		final String user = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
		final String corrId = MDC.get("correlationId");
		final String year = request.getYear();
		final boolean isRetired = "Retired".equalsIgnoreCase(request.getType());
		final LocalDateTime now = LocalDateTime.now();

		try {

			log.info("START saveKaryaratGopniyaAhwal | year={} | type={} | user={} | corrId={}", year,
					request.getType(), user, corrId);

			if (request.getSections() == null || request.getSections().isEmpty()) {
				response.setMessage("No sections found");
				response.setData(List.of());
				response.setErrorDetails(new ApplicationError("400", "Sections are empty"));
				return response;
			}

			for (MahaparRegisterSectionRequest div : request.getSections()) {

				if (div.getRows() == null || div.getRows().isEmpty()) {
					continue;
				}

				for (MahaparRegisterRowRequest row : div.getRows()) {

					/* ---------- HARD DELETE ---------- */
					if ("D".equalsIgnoreCase(row.getFlag())) {

						if (isRetired) {
							rtrGopniyaAhwalRepository.deleteByYearAndDivisionIdAndDeleteId(year, div.getSectionId(),
									row.getDeleteId());
						} else {
							karyaratGopniyaAhwalRepository.deleteByYearAndDivisionIdAndDeleteId(year,
									div.getSectionId(), row.getDeleteId());
						}

						log.info(
								"DELETED GopniyaAhwal | year={} | divisionId={} | deleteId={} | retired={} | corrId={}",
								year, div.getSectionId(), row.getDeleteId(), isRetired, corrId);
						continue;
					}

					/* ---------- UPSERT ---------- */
					if (isRetired) {
						upsertRetiredGopniyaAhwal(year, div, row, user, now, corrId);
					} else {
						upsertKaryaratGopniyaAhwal(year, div, row, user, now, corrId);
					}
				}
			}

			response.setMessage(isRetired ? "Retired Gopniya Ahwal saved successfully"
					: "Karyarat Gopniya Ahwal saved successfully");
			response.setData(List.of());
			error.setErrorCode("200");
			error.setErrorDescription("Success");
			response.setErrorDetails(error);

			log.info("SUCCESS saveKaryaratGopniyaAhwal | year={} | type={} | corrId={}", year, request.getType(),
					corrId);

			return response;

		} catch (Exception e) {

			log.error("ERROR saveKaryaratGopniyaAhwal | year={} | type={} | corrId={}", year, request.getType(), corrId,
					e);

			response.setMessage(
					isRetired ? "Failed to save Retired Gopniya Ahwal" : "Failed to save Karyarat Gopniya Ahwal");
			response.setData(null);
			response.setErrorDetails(new ApplicationError("500", e.getMessage()));
			return response;
		}
	}

	private void upsertRetiredGopniyaAhwal(String year, MahaparRegisterSectionRequest div,
			MahaparRegisterRowRequest row, String user, LocalDateTime now, String corrId) {

		Optional<RtrGopniyaAhwal> opt = rtrGopniyaAhwalRepository.findByYearAndDivisionIdAndRowId(year,
				div.getSectionId(), row.getRowId());

		RtrGopniyaAhwal entity;

		if (opt.isPresent()) {
			entity = opt.get();
			entity.setFlag("U");
			entity.setUpdatedAt(now);
			entity.setUpdatedBy(user);

			log.debug("UPDATED RetiredGopniyaAhwal | year={} | divisionId={} | rowId={} | corrId={}", year,
					div.getSectionId(), row.getRowId(), corrId);

		} else {
			entity = new RtrGopniyaAhwal();
			entity.setYear(year);
			entity.setDivisionId(div.getSectionId());
			entity.setDivisionName(div.getSectionName());
			entity.setRowId(row.getRowId());
			entity.setDeleteId(row.getDeleteId());
			entity.setFlag("C");
			entity.setCreatedAt(now);
			entity.setCreatedBy(user);
			entity.setUpdatedAt(now);
			entity.setUpdatedBy(user);

			log.debug("CREATED RetiredGopniyaAhwal | year={} | divisionId={} | rowId={} | corrId={}", year,
					div.getSectionId(), row.getRowId(), corrId);
		}

		entity.setData(row.getData());
		rtrGopniyaAhwalRepository.save(entity);
	}

	private void upsertKaryaratGopniyaAhwal(String year, MahaparRegisterSectionRequest div,
			MahaparRegisterRowRequest row, String user, LocalDateTime now, String corrId) {

		Optional<KaryaratGopniyaAhwalEntity> opt = karyaratGopniyaAhwalRepository.findByYearAndDivisionIdAndRowId(year,
				div.getSectionId(), row.getRowId());

		KaryaratGopniyaAhwalEntity entity;

		if (opt.isPresent()) {
			entity = opt.get();
			entity.setFlag("U");
			entity.setUpdatedAt(now);
			entity.setUpdatedBy(user);

			log.debug("UPDATED KaryaratGopniyaAhwal | year={} | divisionId={} | rowId={} | corrId={}", year,
					div.getSectionId(), row.getRowId(), corrId);

		} else {
			entity = new KaryaratGopniyaAhwalEntity();
			entity.setYear(year);
			entity.setDivisionId(div.getSectionId());
			entity.setDivisionName(div.getSectionName());
			entity.setRowId(row.getRowId());
			entity.setDeleteId(row.getDeleteId());
			entity.setFlag("C");
			entity.setCreatedAt(now);
			entity.setCreatedBy(user);
			entity.setUpdatedAt(now);
			entity.setUpdatedBy(user);

			log.debug("CREATED KaryaratGopniyaAhwal | year={} | divisionId={} | rowId={} | corrId={}", year,
					div.getSectionId(), row.getRowId(), corrId);
		}

		entity.setData(row.getData());
		karyaratGopniyaAhwalRepository.save(entity);
	}

	@Override
	public MasterDataResponse getKaryaratGopniyaAhwal(String year, String type) {

		MasterDataResponse response = new MasterDataResponse();
		final String corrId = MDC.get("correlationId");
		final boolean isRetired = "Retired".equalsIgnoreCase(type);

		try {
			log.info("START getKaryaratGopniyaAhwal | year={} | type={} | corrId={}", year, type, corrId);

			// 🔹 Fetch data (DB level sorted)
			List<?> entities = isRetired ? rtrGopniyaAhwalRepository.findAllByYearOrderByDivisionIdAscRowIdAsc(year)
					: karyaratGopniyaAhwalRepository.findAllByYearOrderByDivisionIdAscRowIdAsc(year);

			MahaparRegisterRequest result = new MahaparRegisterRequest();
			result.setYear(year);

			Map<Long, MahaparRegisterSectionRequest> divisionMap = new LinkedHashMap<>();

			for (Object obj : entities) {

				Long divisionId;
				String divisionName;
				Long rowId;
				Long deleteId;
				String flag;
				Object data;

				if (isRetired) {
					RtrGopniyaAhwal e = (RtrGopniyaAhwal) obj;
					divisionId = e.getDivisionId();
					divisionName = e.getDivisionName();
					rowId = e.getRowId();
					deleteId = e.getDeleteId();
					flag = e.getFlag();
					data = e.getData();
				} else {
					KaryaratGopniyaAhwalEntity e = (KaryaratGopniyaAhwalEntity) obj;
					divisionId = e.getDivisionId();
					divisionName = e.getDivisionName();
					rowId = e.getRowId();
					deleteId = e.getDeleteId();
					flag = e.getFlag();
					data = e.getData();
				}

				// 🔹 Build row
				MahaparRegisterRowRequest row = new MahaparRegisterRowRequest();
				row.setRowId(rowId);
				row.setDeleteId(deleteId);
				row.setFlag(flag);
				row.setData(objectMapper.valueToTree(data)); // ✅ FIXED

				// 🔹 Group by division
				MahaparRegisterSectionRequest section = divisionMap.computeIfAbsent(divisionId, k -> {
					MahaparRegisterSectionRequest s = new MahaparRegisterSectionRequest();
					s.setSectionId(divisionId);
					s.setSectionName(divisionName);
					s.setRows(new ArrayList<>());
					return s;
				});

				section.getRows().add(row);
			}

			result.setSections(new ArrayList<>(divisionMap.values()));

			response.setMessage("Fetched successfully");
			response.setData(List.of(Map.of("request", result)));
			response.setErrorDetails(new ApplicationError("200", "Success"));

			log.info("END getKaryaratGopniyaAhwal | year={} | type={} | divisions={} | corrId={}", year, type,
					divisionMap.size(), corrId);

			return response;

		} catch (Exception e) {

			log.error("ERROR getKaryaratGopniyaAhwal | year={} | type={} | corrId={}", year, type, corrId, e);

			response.setMessage(
					isRetired ? "Failed to fetch Retired Gopniya Ahwal" : "Failed to fetch Karyarat Gopniya Ahwal");
			response.setData(null);
			response.setErrorDetails(new ApplicationError("500", e.getMessage()));
			return response;
		}
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadKaryaratGopniyaAhwal(String year, String type)
			throws IOException {

		if (!type.equalsIgnoreCase("Retired")) {

			List<KaryaratGopniyaAhwalEntity> list = karyaratGopniyaAhwalRepository
					.findAllByYearOrderByDivisionIdAscRowIdAsc(year);

			XSSFWorkbook wb = new XSSFWorkbook();
			XSSFSheet sheet = wb.createSheet("कार्यरत गोपनीय अहवाल");

			/* ===================== STYLES ===================== */

			XSSFFont boldFont = wb.createFont();
			boldFont.setBold(true);

			XSSFCellStyle centerBold = wb.createCellStyle();
			centerBold.setFont(boldFont);
			centerBold.setAlignment(HorizontalAlignment.CENTER);
			centerBold.setVerticalAlignment(VerticalAlignment.CENTER);

			XSSFCellStyle headerStyle = wb.createCellStyle();
			headerStyle.setFont(boldFont);
			headerStyle.setAlignment(HorizontalAlignment.CENTER);
			headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			headerStyle.setBorderTop(BorderStyle.THIN);
			headerStyle.setBorderBottom(BorderStyle.THIN);
			headerStyle.setBorderLeft(BorderStyle.THIN);
			headerStyle.setBorderRight(BorderStyle.THIN);
			headerStyle.setWrapText(true);

			XSSFCellStyle dataStyle = wb.createCellStyle();
			dataStyle.setAlignment(HorizontalAlignment.CENTER);
			dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			dataStyle.setBorderTop(BorderStyle.THIN);
			dataStyle.setBorderBottom(BorderStyle.THIN);
			dataStyle.setBorderLeft(BorderStyle.THIN);
			dataStyle.setBorderRight(BorderStyle.THIN);
			dataStyle.setWrapText(true);

			int rowIdx = 0;
			Long currentDivisionId = null;
			boolean firstDivision = true;

			/* ===================== DATA ===================== */

			for (KaryaratGopniyaAhwalEntity e : list) {

				// ---------- NEW DIVISION ----------
				if (!Objects.equals(currentDivisionId, e.getDivisionId())) {

					if (!firstDivision) {
						rowIdx++; // 🔥 blank row after each division
					}
					firstDivision = false;

					// ---------- DIVISION NAME (CENTER, MERGED) ----------
					Row divRow = sheet.createRow(rowIdx++);
					Cell divCell = divRow.createCell(0);
					divCell.setCellValue(e.getDivisionName());
					divCell.setCellStyle(centerBold);

					// Merge A–F
					sheet.addMergedRegion(new CellRangeAddress(divRow.getRowNum(), divRow.getRowNum(), 0, 5));

					// ---------- GAP ROW ----------
					rowIdx++;

					// ---------- COLUMN HEADER ----------
					rowIdx = writeColumnHeaderForKaryarat(sheet, rowIdx, headerStyle);

					currentDivisionId = e.getDivisionId();
				}

				// ---------- DATA ROW ----------
				JsonNode d = e.getData();
				Row r = sheet.createRow(rowIdx++);

				r.createCell(0).setCellValue(d.path("srNo").asText(""));
				r.createCell(1).setCellValue(d.path("nav").asText(""));
				r.createCell(2).setCellValue(d.path("padnaam").asText(""));
				r.createCell(3).setCellValue(d.path("mulGopniyaAhval").asText(""));
				r.createCell(4).setCellValue(d.path("dusraGopniyaAhval").asText(""));
				r.createCell(5).setCellValue(d.path("sthiti").asText(""));

				for (int i = 0; i < 6; i++) {
					r.getCell(i).setCellStyle(dataStyle);
				}
			}
			/* ===================== FOOTER ===================== */

			// Blank row
			rowIdx++;

			// ---------- ORDER TEXT ----------
			/* ===================== FOOTER ===================== */

			// Blank row
			rowIdx++;

			// ---------- ORDER TEXT ----------
			Row orderRow = sheet.createRow(rowIdx++);

			/* 🔥 IMPORTANT: increase row height */
			orderRow.setHeightInPoints(80); // <-- THIS FIXES CUTTING (60–100 adjust if needed)

			Cell orderCell = orderRow.createCell(1); // Column B

			orderCell.setCellValue("मंडळ कार्यालयाचे कार्यालयीन आदेश क्र. 40 जा.क्र. पुपाप्रमं/आ 1/872 दि. 29/02/2024 "
					+ "अन्वये अनु. क्र. 1 ते 35 सौ. माधवी राहुल ढेंबरे, प्रथम लिपीक यांचेकडे "
					+ "हस्तांतरित करण्यात येत आहे.");

			XSSFCellStyle orderStyle = wb.createCellStyle();
			orderStyle.setAlignment(HorizontalAlignment.CENTER);
			orderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			orderStyle.setWrapText(true);

			orderCell.setCellStyle(orderStyle);

			// Merge B to F
			sheet.addMergedRegion(new CellRangeAddress(orderRow.getRowNum(), orderRow.getRowNum(), 1, 5));

			// Blank row
			rowIdx++;

			/* ---------- SIGNATURE ROW ---------- */
			Row signRow = sheet.createRow(rowIdx++);

			// LEFT SIGNATURE (Column B)
			Cell leftSign = signRow.createCell(1);
			leftSign.setCellValue("कार्यभार देणार\n" + "(संदीप चं. अमृतकर)\n" + "व.लि.");

			XSSFCellStyle signLeftStyle = wb.createCellStyle();
			signLeftStyle.setAlignment(HorizontalAlignment.CENTER);
			signLeftStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			signLeftStyle.setWrapText(true);

			leftSign.setCellStyle(signLeftStyle);

			// RIGHT SIGNATURE (Column E)
			Cell rightSign = signRow.createCell(4);
			rightSign.setCellValue("कार्यभार घेणार\n" + "(माधवी रा. ढेंबरे)\n" + "प्रथम लिपीक");

			XSSFCellStyle signRightStyle = wb.createCellStyle();
			signRightStyle.setAlignment(HorizontalAlignment.CENTER);
			signRightStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			signRightStyle.setWrapText(true);

			rightSign.setCellStyle(signRightStyle);

			/* ===================== COLUMN WIDTH ===================== */

			sheet.setColumnWidth(0, 2500);
			sheet.setColumnWidth(1, 7000);
			sheet.setColumnWidth(2, 6000);
			sheet.setColumnWidth(3, 9000);
			sheet.setColumnWidth(4, 9000);
			sheet.setColumnWidth(5, 6000);

			/* ===================== RESPONSE ===================== */

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			wb.write(out);
			wb.close();

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION,
							"attachment; filename=KaryaratGopniyaAhwal_" + year + ".xlsx")
					.contentType(MediaType.APPLICATION_OCTET_STREAM)
					.body(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())));
		} else {
			List<RtrGopniyaAhwal> list = rtrGopniyaAhwalRepository.findAllByYearOrderByDivisionIdAscRowIdAsc(year);

			XSSFWorkbook wb = new XSSFWorkbook();
			XSSFSheet sheet = wb.createSheet("कार्यरत गोपनीय अहवाल");

			/* ===================== STYLES ===================== */

			XSSFFont boldFont = wb.createFont();
			boldFont.setBold(true);

			XSSFCellStyle centerBold = wb.createCellStyle();
			centerBold.setFont(boldFont);
			centerBold.setAlignment(HorizontalAlignment.CENTER);
			centerBold.setVerticalAlignment(VerticalAlignment.CENTER);

			XSSFCellStyle headerStyle = wb.createCellStyle();
			headerStyle.setFont(boldFont);
			headerStyle.setAlignment(HorizontalAlignment.CENTER);
			headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			headerStyle.setBorderTop(BorderStyle.THIN);
			headerStyle.setBorderBottom(BorderStyle.THIN);
			headerStyle.setBorderLeft(BorderStyle.THIN);
			headerStyle.setBorderRight(BorderStyle.THIN);
			headerStyle.setWrapText(true);

			XSSFCellStyle dataStyle = wb.createCellStyle();
			dataStyle.setAlignment(HorizontalAlignment.CENTER);
			dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			dataStyle.setBorderTop(BorderStyle.THIN);
			dataStyle.setBorderBottom(BorderStyle.THIN);
			dataStyle.setBorderLeft(BorderStyle.THIN);
			dataStyle.setBorderRight(BorderStyle.THIN);
			dataStyle.setWrapText(true);

			int rowIdx = 0;
			Long currentDivisionId = null;
			boolean firstDivision = true;

			/* ===================== DATA ===================== */

			for (RtrGopniyaAhwal e : list) {

				// ---------- NEW DIVISION ----------
				if (!Objects.equals(currentDivisionId, e.getDivisionId())) {

					if (!firstDivision) {
						rowIdx++; // 🔥 blank row after each division
					}
					firstDivision = false;

					// ---------- DIVISION NAME (CENTER, MERGED) ----------
					Row divRow = sheet.createRow(rowIdx++);
					Cell divCell = divRow.createCell(0);
					divCell.setCellValue(e.getDivisionName());
					divCell.setCellStyle(centerBold);

					// Merge A–F
					sheet.addMergedRegion(new CellRangeAddress(divRow.getRowNum(), divRow.getRowNum(), 0, 5));

					// ---------- GAP ROW ----------
					rowIdx++;

					// ---------- COLUMN HEADER ----------
					rowIdx = writeColumnHeaderForKaryarat(sheet, rowIdx, headerStyle);

					currentDivisionId = e.getDivisionId();
				}

				// ---------- DATA ROW ----------
				JsonNode d = e.getData();
				Row r = sheet.createRow(rowIdx++);

				r.createCell(0).setCellValue(d.path("srNo").asText(""));
				r.createCell(1).setCellValue(d.path("nav").asText(""));
				r.createCell(2).setCellValue(d.path("padnaam").asText(""));
				r.createCell(3).setCellValue(d.path("mulGopniyaAhval").asText(""));
				r.createCell(4).setCellValue(d.path("dusraGopniyaAhval").asText(""));
				r.createCell(5).setCellValue(d.path("sthiti").asText(""));

				for (int i = 0; i < 6; i++) {
					r.getCell(i).setCellStyle(dataStyle);
				}
			}
			/* ===================== FOOTER ===================== */

			// Blank row
			rowIdx++;

			// ---------- ORDER TEXT ----------
			/* ===================== FOOTER ===================== */

			// Blank row
			rowIdx++;

			// ---------- ORDER TEXT ----------
			Row orderRow = sheet.createRow(rowIdx++);

			/* 🔥 IMPORTANT: increase row height */
			orderRow.setHeightInPoints(80); // <-- THIS FIXES CUTTING (60–100 adjust if needed)

			Cell orderCell = orderRow.createCell(1); // Column B

			orderCell.setCellValue("मंडळ कार्यालयाचे कार्यालयीन आदेश क्र. 40 जा.क्र. पुपाप्रमं/आ 1/872 दि. 29/02/2024 "
					+ "अन्वये अनु. क्र. 1 ते 35 सौ. माधवी राहुल ढेंबरे, प्रथम लिपीक यांचेकडे "
					+ "हस्तांतरित करण्यात येत आहे.");

			XSSFCellStyle orderStyle = wb.createCellStyle();
			orderStyle.setAlignment(HorizontalAlignment.CENTER);
			orderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			orderStyle.setWrapText(true);

			orderCell.setCellStyle(orderStyle);

			// Merge B to F
			sheet.addMergedRegion(new CellRangeAddress(orderRow.getRowNum(), orderRow.getRowNum(), 1, 5));

			// Blank row
			rowIdx++;

			/* ---------- SIGNATURE ROW ---------- */
			Row signRow = sheet.createRow(rowIdx++);

			// LEFT SIGNATURE (Column B)
			Cell leftSign = signRow.createCell(1);
			leftSign.setCellValue("कार्यभार देणार\n" + "(संदीप चं. अमृतकर)\n" + "व.लि.");

			XSSFCellStyle signLeftStyle = wb.createCellStyle();
			signLeftStyle.setAlignment(HorizontalAlignment.CENTER);
			signLeftStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			signLeftStyle.setWrapText(true);

			leftSign.setCellStyle(signLeftStyle);

			// RIGHT SIGNATURE (Column E)
			Cell rightSign = signRow.createCell(4);
			rightSign.setCellValue("कार्यभार घेणार\n" + "(माधवी रा. ढेंबरे)\n" + "प्रथम लिपीक");

			XSSFCellStyle signRightStyle = wb.createCellStyle();
			signRightStyle.setAlignment(HorizontalAlignment.CENTER);
			signRightStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			signRightStyle.setWrapText(true);

			rightSign.setCellStyle(signRightStyle);

			/* ===================== COLUMN WIDTH ===================== */

			sheet.setColumnWidth(0, 2500);
			sheet.setColumnWidth(1, 7000);
			sheet.setColumnWidth(2, 6000);
			sheet.setColumnWidth(3, 9000);
			sheet.setColumnWidth(4, 9000);
			sheet.setColumnWidth(5, 6000);

			/* ===================== RESPONSE ===================== */

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			wb.write(out);
			wb.close();

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION,
							"attachment; filename=RetiredGopniyaAhwal" + year + ".xlsx")
					.contentType(MediaType.APPLICATION_OCTET_STREAM)
					.body(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())));

		}
	}

	private int writeColumnHeaderForKaryarat(XSSFSheet sheet, int rowIdx, CellStyle headerStyle) {
		Row h = sheet.createRow(rowIdx++);
		String[] cols = { "अनु क्र", "नाव", "पदनाम", "मूळ गोपनीय अहवाल", "दुय्यम गोपनीय अहवाल",
				"कार्यरत/बदली/सेवानिवृत्त" };

		for (int i = 0; i < cols.length; i++) {
			Cell c = h.createCell(i);
			c.setCellValue(cols[i]);
			c.setCellStyle(headerStyle);
		}
		return rowIdx;
	}

	@Transactional
	@Override
	public BhaniniResponse saveOrUpdateBhaniniData(BhaniniRequest request) {
		String currentUser = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
		String corrId = MDC.get("correlationId");

		log.info("saveOrUpdateSalaryArrears START | year={} | employee={} | user={} | corrId={}", request.getYear(),
				request.getEmployee() != null ? request.getEmployee().getName() : "NA", currentUser, corrId);

		BhaniniResponse response = new BhaniniResponse();
		ApplicationError error = new ApplicationError();

		try {

			// ---------------- VALIDATION ----------------
			if (request.getEmployee() == null || request.getEmployee().getName() == null
					|| request.getEmployee().getName().isBlank()) {

				error.setErrorCode("400");
				error.setErrorDescription("Employee name is mandatory");
				response.setErrorDetails(error);
				return response;
			}

			if (request.getYear() == null || request.getYear().isBlank()) {
				error.setErrorCode("400");
				error.setErrorDescription("Year is mandatory");
				response.setErrorDetails(error);
				return response;
			}

			String employeeName = request.getEmployee().getName();
			String year = request.getYear();
			String incomingFlag = request.getFlag(); // may be null

			ObjectMapper mapper = new ObjectMapper();

			// ---------------- DELETE (explicit only) ----------------
			if ("D".equalsIgnoreCase(incomingFlag)) {

				bhaniniRepo.deleteByEmployeeNameAndYear(employeeName, year);

				error.setErrorCode("200");
				error.setErrorDescription("Deleted Successfully");
				response.setErrorDetails(error);

				log.info("saveOrUpdateSalaryArrears DELETE SUCCESS | year={} | employee={} | corrId={}", year,
						employeeName, corrId);

				return response;
			}

			// ---------------- EXIST CHECK ----------------
			Optional<BhaniniEntity> existingOpt = bhaniniRepo.findByEmployeeNameAndYear(employeeName, year);

			BhaniniEntity entity;

			if (existingOpt.isPresent()) {
				// ---------------- UPDATE ----------------
				entity = existingOpt.get();
				entity.setData(mapper.valueToTree(request));
				entity.setFlag("U");
				entity.setUpdatedAt(LocalDateTime.now());
				entity.setUpdatedBy(currentUser);

				log.debug("Updating Salary Arrears | year={} | employee={} | corrId={}", year, employeeName, corrId);

			} else {
				// ---------------- CREATE ----------------
				entity = new BhaniniEntity();
				entity.setYear(year);
				entity.setAaPrupam(request.getAaPrupam());
				entity.setEmployeeName(employeeName);
				entity.setData(mapper.valueToTree(request));
				entity.setFlag("C");
				entity.setCreatedAt(LocalDateTime.now());
				entity.setCreatedBy(currentUser);
				entity.setUpdatedAt(LocalDateTime.now());
				entity.setUpdatedBy(currentUser);

				log.debug("Creating Salary Arrears | year={} | employee={} | corrId={}", year, employeeName, corrId);
			}

			bhaniniRepo.save(entity);

			// ---------------- SUCCESS RESPONSE ----------------
			error.setErrorCode("200");
			error.setErrorDescription("Success");
			response.setErrorDetails(error);

			log.info("saveOrUpdateSalaryArrears SUCCESS | year={} | employee={} | flag={} | corrId={}", year,
					employeeName, entity.getFlag(), corrId);

		} catch (Exception e) {

			log.error("saveOrUpdateSalaryArrears FAILED | year={} | employee={} | corrId={}", request.getYear(),
					request.getEmployee() != null ? request.getEmployee().getName() : "NA", corrId, e);

			error.setErrorCode("500");
			error.setErrorDescription(e.getMessage());
			response.setErrorDetails(error);
		}

		return response;
	}

	@Override
	public BhaniniResponse getBhaniniData(String employeeName, String year) {

		String currentUser = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
		String corrId = MDC.get("correlationId");

		log.info("getBhaniniData START | year={} | employee={} | user={} | corrId={}", year, employeeName, currentUser,
				corrId);

		BhaniniResponse response = new BhaniniResponse();
		ApplicationError error = new ApplicationError();

		try {
			// -------- BASIC VALIDATION --------
			if (year == null || year.isBlank()) {
				error.setErrorCode("400");
				error.setErrorDescription("Year is mandatory");
				response.setErrorDetails(error);

				log.warn("getBhaniniData VALIDATION FAILED | reason=year missing | corrId={}", corrId);
				return response;
			}

			if (employeeName == null || employeeName.isBlank()) {
				error.setErrorCode("400");
				error.setErrorDescription("Employee name is mandatory");
				response.setErrorDetails(error);

				log.warn("getBhaniniData VALIDATION FAILED | reason=employeeName missing | corrId={}", corrId);
				return response;
			}

			// -------- DB FETCH (SINGLE HIT) --------
			Optional<BhaniniEntity> entityOpt = bhaniniRepo.findByEmployeeNameAndYear(employeeName, year);

			if (entityOpt.isEmpty()) {
				error.setErrorCode("404");
				error.setErrorDescription("No data found");
				response.setErrorDetails(error);

				log.info("getBhaniniData NO DATA | year={} | employee={} | corrId={}", year, employeeName, corrId);
				return response;
			}

			// -------- SUCCESS --------
			response.setData(entityOpt.get().getData());
			response.setAaPupram(entityOpt.get().getAaPrupam());
			response.setYear(entityOpt.get().getYear());
			error.setErrorCode("200");
			error.setErrorDescription("Success");
			response.setErrorDetails(error);

			log.info("getBhaniniData SUCCESS | year={} | employee={} | corrId={}", year, employeeName, corrId);

		} catch (Exception e) {

			log.error("getBhaniniData FAILED | year={} | employee={} | corrId={}", year, employeeName, corrId, e);

			error.setErrorCode("500");
			error.setErrorDescription(e.getMessage() != null ? e.getMessage() : "Unexpected error occurred");
			response.setErrorDetails(error);
		}

		return response;
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadBhaniniData(String year, String employeeName)
			throws IOException {
		Optional<BhaniniEntity> entityOpt = bhaniniRepo.findByEmployeeNameAndYear(employeeName, year);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = entityOpt.get().getData();

		JsonNode employee = root.get("employee");
		JsonNode previous = root.get("previousYearPending");
		JsonNode khata = root.get("khatyavarJamaRakam");
		JsonNode months = root.get("monthlyDetails");

		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = wb.createSheet("Bhanini");

		sheet.setColumnWidth(0, 6000); // Month
		sheet.setColumnWidth(1, 4500);
		sheet.setColumnWidth(2, 3000);
		sheet.setColumnWidth(3, 4500);
		sheet.setColumnWidth(4, 3000);
		sheet.setColumnWidth(5, 4500);
		sheet.setColumnWidth(6, 3000);
		sheet.setColumnWidth(7, 4500);
		sheet.setColumnWidth(8, 3000);
		sheet.setColumnWidth(9, 7000); // Interest calculation
		sheet.setColumnWidth(10, 3000);
		sheet.setColumnWidth(11, 8000); // Shera / Arrears

		// ================= STYLES =================
		XSSFFont bold = wb.createFont();
		bold.setBold(true);
		XSSFFont normal = wb.createFont();

		XSSFCellStyle centerBold = wb.createCellStyle();
		centerBold.setFont(bold);
		centerBold.setAlignment(HorizontalAlignment.CENTER);
		centerBold.setVerticalAlignment(VerticalAlignment.CENTER);

		XSSFCellStyle leftBold = wb.createCellStyle();
		leftBold.setFont(bold);
		leftBold.setAlignment(HorizontalAlignment.LEFT);

		XSSFCellStyle center = wb.createCellStyle();
		center.setAlignment(HorizontalAlignment.CENTER);

		XSSFCellStyle border = wb.createCellStyle();
		border.setBorderBottom(BorderStyle.THIN);
		border.setBorderTop(BorderStyle.THIN);
		border.setBorderLeft(BorderStyle.THIN);
		border.setBorderRight(BorderStyle.THIN);

		// ---------- BORDERED STYLES ----------
		XSSFCellStyle borderCenter = wb.createCellStyle();
		borderCenter.setBorderBottom(BorderStyle.THIN);
		borderCenter.setBorderTop(BorderStyle.THIN);
		borderCenter.setBorderLeft(BorderStyle.THIN);
		borderCenter.setBorderRight(BorderStyle.THIN);
		borderCenter.setAlignment(HorizontalAlignment.CENTER);
		borderCenter.setVerticalAlignment(VerticalAlignment.CENTER);

		XSSFCellStyle borderLeft = wb.createCellStyle();
		borderLeft.cloneStyleFrom(borderCenter);
		borderLeft.setAlignment(HorizontalAlignment.LEFT);

		XSSFCellStyle borderBold = wb.createCellStyle();
		borderBold.cloneStyleFrom(borderCenter);
		borderBold.setFont(bold);
		// -------- CENTER TEXT (NO BORDER) --------
		XSSFCellStyle centerText = wb.createCellStyle();
		centerText.setAlignment(HorizontalAlignment.CENTER);
		centerText.setVerticalAlignment(VerticalAlignment.CENTER);
		centerText.setFont(normal); // या boldFont अगर signature bold चाहिए

		XSSFCellStyle borderBoldLeft = wb.createCellStyle();
		borderBoldLeft.cloneStyleFrom(borderLeft);
		borderBoldLeft.setFont(bold);

		XSSFCellStyle headerStyle = wb.createCellStyle();
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerStyle.setWrapText(true); // 🔴 ये MUST है
		headerStyle.setFont(bold);
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);

		// Utility
		BiFunction<Row, Integer, Cell> c = (r, i) -> r.getCell(i) == null ? r.createCell(i) : r.getCell(i);

		int r = 0;

		// ================= ROW 1 =================
		Row r1 = sheet.createRow(r++);
		c.apply(r1, 0).setCellValue("चतुर्थश्रेणी कर्मचारी सर्वसाधारण भविष्यनिर्वाह निधी लेखा - नमुना एक");
		c.apply(r1, 0).setCellStyle(centerBold);
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 11));

		// ================= ROW 2 =================
		Row r2 = sheet.createRow(r++);
		c.apply(r2, 0).setCellValue("सन " + year);
		c.apply(r2, 7).setCellValue("अअपुपाप्रमं/" + entityOpt.get().getAaPrupam());
		sheet.addMergedRegion(new CellRangeAddress(1, 1, 7, 8));

		// ================= ROW 3 =================
		Row r3 = sheet.createRow(r++);
		c.apply(r3, 0).setCellValue("नांव " + employee.get("name").asText());
		c.apply(r3, 6).setCellValue("अधिकृत पदनाम : " + employee.get("designation").asText());
		sheet.addMergedRegion(new CellRangeAddress(2, 2, 6, 8));

		// ================= ROW 4 HEADER =================
		Row h1 = sheet.createRow(r++);
		h1.setHeightInPoints(42);
		c.apply(h1, 0).setCellValue("मागील वर्षाच्या 31 मार्चचे वेतन");
		c.apply(h1, 0).setCellStyle(headerStyle);
		sheet.addMergedRegion(new CellRangeAddress(3, 3, 1, 2));
		c.apply(h1, 1).setCellValue("पूर्ण रुपया मधील वर्गणी");

		sheet.addMergedRegion(new CellRangeAddress(3, 3, 3, 4));
		c.apply(h1, 3).setCellValue("काढलेल्या रकमांची परतफेड");

		sheet.addMergedRegion(new CellRangeAddress(3, 3, 5, 6));
		c.apply(h1, 5).setCellValue("एकूण");

		sheet.addMergedRegion(new CellRangeAddress(3, 3, 7, 8));
		c.apply(h1, 7).setCellValue("काढलेल्या रक्कमा");

		sheet.addMergedRegion(new CellRangeAddress(3, 3, 9, 10));
		c.apply(h1, 9).setCellValue("ज्यावरील व्याजाची गणना होते अशी मासिक शिल्लक");
		c.apply(h1, 9).setCellStyle(headerStyle);

		c.apply(h1, 11).setCellValue("शेरा Arrears");
		c.apply(h1, 11).setCellStyle(headerStyle);

		// ================= ROW 5 SUB HEADER =================
		Row h2 = sheet.createRow(r++);
		c.apply(h2, 0).setCellValue(year);
		c.apply(h2, 1).setCellValue("रु.");
		c.apply(h2, 2).setCellValue("पै.");
		c.apply(h2, 3).setCellValue("रु.");
		c.apply(h2, 4).setCellValue("पै.");
		c.apply(h2, 5).setCellValue("रु.");
		c.apply(h2, 6).setCellValue("पै.");
		c.apply(h2, 7).setCellValue("रु.");
		c.apply(h2, 8).setCellValue("पै.");
		c.apply(h2, 9).setCellValue("रु.");
		c.apply(h2, 10).setCellValue("पै.");
		for (int i = 0; i <= 11; i++) {
			c.apply(h1, i).setCellStyle(borderBold);
			c.apply(h2, i).setCellStyle(borderBold);
		}
		// ================= MONTH DATA =================
		int startMonthRow = r;
		for (JsonNode m : months) {

			Row dr = sheet.createRow(r++);

			c.apply(dr, 0).setCellValue(safeText(m, "month"));

			c.apply(dr, 1).setCellValue(safeDouble(m, "purnaRupyaMadhilVargni"));
			c.apply(dr, 3).setCellValue(safeDouble(m, "kadhlelyaRakmanchiParatFed"));
			c.apply(dr, 5).setCellValue(safeDouble(m, "ekun"));
			c.apply(dr, 7).setCellValue(safeDouble(m, "kadhlelyaRakam"));

			c.apply(dr, 11).setCellValue(safeText(m, "sheraOrArrears"));

			for (int i = 0; i <= 11; i++) {
				if (i == 0 || i == 11) {
					c.apply(dr, i).setCellStyle(borderLeft);
				} else {
					c.apply(dr, i).setCellStyle(borderCenter);
				}
			}
		}

		int endMonthRow = r - 1;

		// ================= J COLUMN RUNNING BALANCE =================
		// Excel rows: startMonthRow+1 = Row 6, endMonthRow+1 = Row 19
		// Columns: F = 5, H = 7, J = 9

		double pasunchiShillak = previous.get("pasunchiShillak").asDouble();

		// ---- J6 = pasunchiShillak + F6 ----
		Row firstMonthRow = sheet.getRow(startMonthRow);
		Cell jFirst = c.apply(firstMonthRow, 9);
		jFirst.setCellFormula(pasunchiShillak + "+F" + (startMonthRow + 1));
		jFirst.setCellStyle(borderCenter);

		// ---- J7 to J19 : J(n) = J(n-1) + F(n) - H(n) ----
		for (int excelRow = startMonthRow + 2; excelRow <= endMonthRow + 1; excelRow++) {

			Row rr = sheet.getRow(excelRow - 1);
			Cell jCell = c.apply(rr, 9);

			jCell.setCellFormula("J" + (excelRow - 1) + "+F" + excelRow + "-H" + excelRow);
			jCell.setCellStyle(borderCenter);
		}

		// ================= TOTAL ROW =================
		Row total = sheet.createRow(r++);
		c.apply(total, 0).setCellValue("एकूण रू.");
		c.apply(total, 1).setCellFormula("SUM(B" + (startMonthRow + 1) + ":B" + (endMonthRow + 1) + ")");
		c.apply(total, 3).setCellFormula("SUM(D" + (startMonthRow + 1) + ":D" + (endMonthRow + 1) + ")");
		c.apply(total, 5).setCellFormula("SUM(F" + (startMonthRow + 1) + ":F" + (endMonthRow + 1) + ")");
		c.apply(total, 7).setCellFormula("SUM(H" + (startMonthRow + 1) + ":H" + (endMonthRow + 1) + ")");

		// ---- J20 = SUM(J6:J19) ----
		Row totalRowRef = sheet.getRow(endMonthRow + 1); // this will be TOTAL row
		Cell jTotal = c.apply(totalRowRef, 9);
		jTotal.setCellFormula("SUM(J" + (startMonthRow + 1) + ":J" + (endMonthRow + 1) + ")");
		jTotal.setCellStyle(borderBold);
		// ================= PREVIOUS YEAR BLOCK =================
		Row py1 = sheet.createRow(r++);
		sheet.addMergedRegion(new CellRangeAddress(py1.getRowNum(), py1.getRowNum(), 1, 8));
		c.apply(py1, 1).setCellValue(year + " पासूनची शिल्लक");
		c.apply(py1, 9).setCellValue(previous.get("pasunchiShillak").asDouble());

		Row py2 = sheet.createRow(r++);
		sheet.addMergedRegion(new CellRangeAddress(py2.getRowNum(), py2.getRowNum(), 1, 8));
		c.apply(py2, 1).setCellValue("वरीलप्रमाणे ठेवी व परतफेडीच्या रक्कमा");
		c.apply(py2, 9).setCellValue(previous.get("varilPramaneThevi").asDouble());

		Row py3 = sheet.createRow(r++);
		sheet.addMergedRegion(new CellRangeAddress(py3.getRowNum(), py3.getRowNum(), 1, 8));
		c.apply(py3, 1).setCellValue(year + " व्याज");
		c.apply(py3, 9).setCellValue(previous.get("vyaj").asDouble());

		Row py4 = sheet.createRow(r++);
		sheet.addMergedRegion(new CellRangeAddress(py4.getRowNum(), py4.getRowNum(), 1, 8));
		c.apply(py4, 1).setCellValue("सातवा वेतन आयोगाच्या फरकाची चौथ्या  हप्त्याची फरकाची व्याजासह जमा रक्कम");
		c.apply(py4, 9).setCellValue(previous.get("sattvaVetanAyogyaFarkachi").asDouble());

		// ================= GRAND TOTAL =================
		Row gt = sheet.createRow(r++);
		c.apply(gt, 8).setCellValue("एकूण");
		c.apply(gt, 9).setCellFormula("J" + (py1.getRowNum() + 1) + "+J" + (py2.getRowNum() + 1) + "+J"
				+ (py3.getRowNum() + 1) + "+J" + (py4.getRowNum() + 1));
		for (int i = 0; i <= 11; i++) {
			c.apply(gt, i).setCellStyle(borderBold);
		}
		// TOTAL ROW (Row 20)
		for (int i = 0; i <= 11; i++) {
			if (i == 0) {
				c.apply(total, i).setCellStyle(borderBoldLeft);
			} else {
				c.apply(total, i).setCellStyle(borderBold);
			}
		}

		for (Row rr : List.of(py1, py2, py3, py4)) {
			for (int i = 0; i <= 11; i++) {
				if (i <= 8) {
					c.apply(rr, i).setCellStyle(borderBoldLeft);
				} else {
					c.apply(rr, i).setCellStyle(borderCenter);
				}
			}
		}

		// ================= ROW 26 =================
		Row r26 = sheet.createRow(r++);

		c.apply(r26, 0).setCellValue("नोंदवणारा");
		c.apply(r26, 0).setCellStyle(borderBoldLeft);

		mergeWithBorder(sheet, r26.getRowNum(), r26.getRowNum(), 4, 8);
		c.apply(r26, 4).setCellValue("सातव्या वेतन आयोगाच्या फरकाची चौथ्या हप्त्याची व्याजासह वजा रक्कम");
		c.apply(r26, 4).setCellStyle(borderLeft);

		c.apply(r26, 9).setCellValue("------>");
		c.apply(r26, 9).setCellStyle(borderCenter);

		mergeWithBorder(sheet, r26.getRowNum(), r26.getRowNum(), 10, 11);
		c.apply(r26, 10).setCellValue(previous.get("sattvaVetanAyogyaFarkachi").asDouble());
		c.apply(r26, 10).setCellStyle(borderCenter);

		applyRowBorder(r26, 11, borderBoldLeft, borderCenter);

		// ================= ROW 27 =================
		// ================= ROW 27 =================
		Row r27 = sheet.createRow(r++);

		// A col
		c.apply(r27, 0).setCellValue("पडताळणारा");
		c.apply(r27, 0).setCellStyle(borderBoldLeft);

		// B–I merged blank
		mergeWithBorder(sheet, r27.getRowNum(), r27.getRowNum(), 1, 8);

		// ✅ J27 = SAME EKUN (933391)
		c.apply(r27, 9).setCellFormula("J21+J22+J23+J24");
		c.apply(r27, 9).setCellStyle(borderBold);

		// L col = padtalnara (DB)
		c.apply(r27, 11).setCellValue(previous.get("padtalnara").asDouble());
		c.apply(r27, 11).setCellStyle(borderCenter);

		// ================= ROW 28 =================
		Row r28 = sheet.createRow(r++);

		c.apply(r28, 0).setCellValue("तपासणारा");
		c.apply(r28, 0).setCellStyle(borderBoldLeft);

		mergeWithBorder(sheet, r28.getRowNum(), r28.getRowNum(), 1, 8);
		c.apply(r28, 1).setCellValue("वजा वरील प्रमाणे काढलेल्या रकमा");
		c.apply(r28, 1).setCellStyle(borderLeft);

		c.apply(r28, 9).setCellFormula("H20");
		c.apply(r28, 9).setCellStyle(borderCenter);

		applyRowBorder(r28, 11, borderBoldLeft, borderCenter);

		// ================= ROW 29 =================
		Row r29 = sheet.createRow(r++);

		mergeWithBorder(sheet, r29.getRowNum(), r29.getRowNum(), 1, 8);
		c.apply(r29, 1).setCellValue(employee.get("payScaleAsOn") + " रोजी असलेली शिल्लक");
		c.apply(r29, 1).setCellStyle(borderBoldLeft);

		c.apply(r29, 9).setCellFormula("J27-J28");
		c.apply(r29, 9).setCellStyle(borderCenter);

		c.apply(r29, 11).setCellFormula("L27-" + previous.get("sattvaVetanAyogyaFarkachi").asDouble());
		c.apply(r29, 11).setCellStyle(borderCenter);

		applyRowBorder(r29, 11, borderBoldLeft, borderCenter);

		r += 2;
		// ================= FOOTER (NO BORDER) =================

		Row f32 = sheet.createRow(r++);
		sheet.addMergedRegion(new CellRangeAddress(f32.getRowNum(), f32.getRowNum(), 9, 11));
		c.apply(f32, 9).setCellValue("उप अधीक्षक अभियंता,");
		c.apply(f32, 9).setCellStyle(centerText); // ❌ no border style

		Row f33 = sheet.createRow(r++);
		sheet.addMergedRegion(new CellRangeAddress(f33.getRowNum(), f33.getRowNum(), 9, 11));
		c.apply(f33, 9).setCellValue("पुणे पाटबंधारे प्रकल्प मंडळ,");
		c.apply(f33, 9).setCellStyle(centerText);

		Row f34 = sheet.createRow(r++);
		sheet.addMergedRegion(new CellRangeAddress(f34.getRowNum(), f34.getRowNum(), 9, 11));
		c.apply(f34, 9).setCellValue("पुणे");
		c.apply(f34, 9).setCellStyle(centerText);

		r++; // spacer

		// ================= ROW 36 HEADER =================
		// ================= ROW 36 HEADER =================
		Row h36 = sheet.createRow(r++);

		// B–D merged
		mergeWithBorderForBhanini(sheet, h36.getRowNum(), h36.getRowNum(), 1, 3, borderBold);
		c.apply(h36, 1).setCellValue("मागील हप्ता शिल्लक");

		// E–F merged
		mergeWithBorderForBhanini(sheet, h36.getRowNum(), h36.getRowNum(), 4, 5, borderBold);
		c.apply(h36, 4).setCellValue("पाचवा हप्ता जमा");

		// G–I merged
		mergeWithBorderForBhanini(sheet, h36.getRowNum(), h36.getRowNum(), 6, 8, borderBold);
		c.apply(h36, 6).setCellValue("पाचवा हप्ता 7/2023 पासून व्याज");

		// A, J, K, L columns – normal border
		for (int i : new int[] { 0, 9, 10, 11 }) {
			c.apply(h36, i).setCellStyle(borderBold);
		}

		Row r37 = sheet.createRow(r++);

		c.apply(r37, 0).setCellValue("खात्यावर जमा रक्कम");
		c.apply(r37, 0).setCellStyle(borderLeft);

		// B col = L29
		c.apply(r37, 1).setCellFormula("L29");

		// F col
		c.apply(r37, 5).setCellValue(khata.get("panchwaHaftaJama").asDouble());

		// H col
		c.apply(r37, 7).setCellValue(khata.get("panchwaHaftaJuly2023PasunVyaj").asDouble());
		applyRowBorder(r37, 11, borderLeft, borderCenter);
		// Row 38
		Row r38 = sheet.createRow(r++);
		c.apply(r38, 5).setCellFormula("F37*7.1%");
		c.apply(r38, 7).setCellFormula("H37*7.1%");
		applyRowBorder(r38, 11, borderLeft, borderCenter);
		// Row 39
		Row r39 = sheet.createRow(r++);
		c.apply(r39, 5).setCellFormula("F38*12");
		c.apply(r39, 7).setCellFormula("H38*12");
		applyRowBorder(r39, 11, borderLeft, borderCenter);
		// Row 40
		Row r40 = sheet.createRow(r++);
		c.apply(r40, 5).setCellFormula("F39/12");
		c.apply(r40, 7).setCellFormula("H39/12");
		applyRowBorder(r40, 11, borderLeft, borderCenter);
		// Row 41
		Row r41 = sheet.createRow(r++);
		c.apply(r41, 5).setCellFormula("F40/12");
		c.apply(r41, 7).setCellFormula("H40/12");
		applyRowBorder(r41, 11, borderLeft, borderCenter);
		// Row 42
		Row r42 = sheet.createRow(r++);
		c.apply(r42, 5).setCellFormula("F41*9");
		c.apply(r42, 7).setCellFormula("H41*13");
		applyRowBorder(r42, 11, borderLeft, borderCenter);
		// Row 43
		Row r43 = sheet.createRow(r++);
		c.apply(r43, 5).setCellFormula("D42+F42");
		c.apply(r43, 7).setCellFormula("F43+H42");
		applyRowBorder(r43, 11, borderLeft, borderCenter);

		// ================= WRITE =================
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		wb.write(out);
		wb.close();

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Bhanini.xlsx\"")
				.contentType(
						MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())));
	}

	private double safeDouble(JsonNode n, String key) {
		return n.path(key).asDouble(0.0);
	}

	private String safeText(JsonNode n, String key) {
		return n.path(key).asText("");
	}

	private void mergeWithBorder(Sheet sheet, int row1, int row2, int col1, int col2) {

		CellRangeAddress region = new CellRangeAddress(row1, row2, col1, col2);

		sheet.addMergedRegion(region);

		RegionUtil.setBorderTop(BorderStyle.THIN, region, sheet);
		RegionUtil.setBorderBottom(BorderStyle.THIN, region, sheet);
		RegionUtil.setBorderLeft(BorderStyle.THIN, region, sheet);
		RegionUtil.setBorderRight(BorderStyle.THIN, region, sheet);
	}

	private void mergeWithBorderForBhanini(XSSFSheet sheet, int firstRow, int lastRow, int firstCol, int lastCol,
			XSSFCellStyle style) {
		CellRangeAddress region = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);

		sheet.addMergedRegion(region);

		RegionUtil.setBorderTop(BorderStyle.THIN, region, sheet);
		RegionUtil.setBorderBottom(BorderStyle.THIN, region, sheet);
		RegionUtil.setBorderLeft(BorderStyle.THIN, region, sheet);
		RegionUtil.setBorderRight(BorderStyle.THIN, region, sheet);

		// apply style to top-left cell
		Row row = sheet.getRow(firstRow);
		if (row == null)
			row = sheet.createRow(firstRow);
		Cell cell = row.getCell(firstCol);
		if (cell == null)
			cell = row.createCell(firstCol);
		cell.setCellStyle(style);
	}

	private void applyRowBorder(Row row, int lastCol, CellStyle left, CellStyle center) {
		for (int i = 0; i <= lastCol; i++) {
			Cell cell = row.getCell(i);
			if (cell == null)
				cell = row.createCell(i);
			if (i == 0)
				cell.setCellStyle(left);
			else
				cell.setCellStyle(center);
		}
	}

	@Override
	@Transactional
	public JeReturnResponse saveOrUpdateJeReturn(JeReturnRequest req) {

		JeReturnResponse response = new JeReturnResponse();
		ApplicationError error = new ApplicationError();

		String year = req.getYear();
		String upAdhikshak = req.getUpAdhikshakAbhiyanta();
		String user = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");

		log.info("JeReturn SAVE START | year={} | divisions={}", year,
				req.getDivision() != null ? req.getDivision().size() : 0);

		try {
			for (JeReturnRequest.DivisionDto division : req.getDivision()) {

				String office = division.getKaryalayacheNav();
				log.debug("Processing division | office={}", office);

				for (JeReturnRequest.RowDto row : division.getRows()) {

					Long rowId = row.getRowId();
					Long deleteId = row.getDeleteId();
					String flag = Optional.ofNullable(row.getFlag()).orElse("");

					// ---------------- DELETE ----------------
					if ("D".equalsIgnoreCase(flag) && deleteId != null) {

						log.info("DELETE requested | year={} | office={} | deleteId={}", year, office, deleteId);

						jeReturnRepository.findByDeleteIdAndYearAndKaryalayacheNav(deleteId, year, office)
								.ifPresent(jeReturnRepository::delete);

						continue;
					}

					// ---------------- CREATE / UPDATE ----------------
					Optional<JeReturnEntity> existingOpt = jeReturnRepository
							.findByRowIdAndYearAndKaryalayacheNav(rowId, year, office);

					JeReturnEntity entity = existingOpt.orElseGet(JeReturnEntity::new);

					boolean isCreate = (entity.getId() == null);

					entity.setRowId(rowId);
					entity.setDeleteId(deleteId);
					entity.setYear(year);
					entity.setKaryalayacheNav(office);
					entity.setUpAdhikshakAbhiyanta(upAdhikshak);
					entity.setData(row.getData());

					if (isCreate) {
						entity.setFlag("C");
						entity.setCreatedBy(user);
						log.debug("CREATE entity | rowId={} | office={}", rowId, office);
					} else {
						entity.setFlag("U");
						log.debug("UPDATE entity | rowId={} | office={}", rowId, office);
					}

					entity.setUpdatedBy(user);
					jeReturnRepository.save(entity);
				}
			}

			response.setMessage("Success");
			error.setErrorCode("200");
			error.setErrorDescription("JE Return saved successfully");

			log.info("JeReturn SAVE SUCCESS | year={}", year);

		} catch (Exception ex) {

			log.error("JeReturn SAVE FAILED | year={} | error={}", year, ex.getMessage(), ex);

			response.setMessage("Failed");
			error.setErrorCode("500");
			error.setErrorDescription(ex.getMessage());
		}

		response.setErrorDetails(error);
		return response;
	}

	@Override
	public JeReturnResponse getJeReturnData(String year) {

		JeReturnResponse response = new JeReturnResponse();
		ApplicationError error = new ApplicationError();

		log.info("JeReturn GET START | year={}", year);

		try {
			List<JeReturnEntity> list = jeReturnRepository.findByYear(year);

			// ---------- GROUP BY karyalayacheNav ----------
			Map<String, List<JeReturnEntity>> grouped = list.stream()
					.sorted(Comparator.comparingLong(JeReturnEntity::getRowId)).collect(Collectors
							.groupingBy(JeReturnEntity::getKaryalayacheNav, LinkedHashMap::new, Collectors.toList()));

			// ---------- BUILD RESPONSE ----------
			List<Map<String, Object>> divisions = new ArrayList<>();

			for (Map.Entry<String, List<JeReturnEntity>> entry : grouped.entrySet()) {

				Map<String, Object> div = new LinkedHashMap<>();
				div.put("karyalayacheNav", entry.getKey());
				div.put("rows", entry.getValue());

				divisions.add(div);
			}

			Map<String, Object> finalData = new LinkedHashMap<>();
			finalData.put("year", year);
			finalData.put("division", divisions);

			response.setData(finalData);
			response.setMessage("Success");

			error.setErrorCode("200");
			error.setErrorDescription("JE Return fetched successfully");

			log.info("JeReturn GET SUCCESS | year={} | divisions={}", year, divisions.size());

		} catch (Exception ex) {

			log.error("JeReturn GET FAILED | year={} | error={}", year, ex.getMessage(), ex);

			response.setMessage("Failed");
			error.setErrorCode("500");
			error.setErrorDescription(ex.getMessage());
		}

		response.setErrorDetails(error);
		return response;
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadJeReturnData(String year) throws IOException {

		List<JeReturnEntity> list = jeReturnRepository.findByYearOrderByRowIdAsc(year);
		if (list.isEmpty())
			throw new RuntimeException("No data found");
		String footerName = Optional.ofNullable(list.get(0).getUpAdhikshakAbhiyanta()).orElse("");
		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = wb.createSheet("JE Return");
		sheet.setZoom(85);

		// ================= FONTS =================
		Font normal = wb.createFont();
		normal.setFontHeightInPoints((short) 10);

		Font bold = wb.createFont();
		bold.setFontHeightInPoints((short) 10);
		bold.setBold(true);

		Font titleFont = wb.createFont();
		titleFont.setBold(true);
		titleFont.setFontHeightInPoints((short) 12);

		// ================= STYLES =================
		CellStyle border = style(wb, normal);
		CellStyle borderBold = style(wb, bold);

		CellStyle titleStyle = wb.createCellStyle();
		titleStyle.setAlignment(HorizontalAlignment.CENTER);
		titleStyle.setFont(titleFont);
		titleStyle.setIndention((short) 2); // left padding
		titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

		CellStyle footerStyle = wb.createCellStyle();
		footerStyle.setAlignment(HorizontalAlignment.CENTER);
		footerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		footerStyle.setFont(bold);
		footerStyle.setWrapText(true);

		int r = 0;

		// ================= TITLE =================
		r = merge(sheet, r, "   (संघटन तक्ता) कनिष्ठ अभियंता / शाखा अभियंता / सहाय्यक अभियंता श्रेणी-2 "
				+ "या संवर्गातील कार्यालय व कर्मचारी यांची नावानिशी माहिती.  ऑगस्ट " + year, titleStyle, 17);

		// increase row height for padding effect
		sheet.getRow(r - 1).setHeightInPoints(35);

		// ================= HEADERS =================
		Row h1 = sheet.createRow(r++);
		Row h2 = sheet.createRow(r++);

		String[] main = { "अ.क्र.", "कार्यालयाचे नाव", "मंजूर पदे", "", "", "कार्यरत पदे", "", "", "रिक्त पदे", "", "",
				"अभियंता यांची नावे", "अभियंता यांची नावे", "अभियंता यांची नावे", "अभियंता यांची नावे",
				"अभियंता यांची नावे", "अभियंता यांची नावे", "अभियंता यांची नावे" };

		String[] sub = { "", "", "क.अ./ शा.अ", "स.अ.श्रे-2", "एकूण", "क.अ./ शा.अ", "स.अ.श्रे-2", "एकूण", "क.अ./ शा.अ",
				"स.अ.श्रे-2", "एकूण" };

		// merges
		mergeCol(sheet, h1, h2, 0);
		mergeCol(sheet, h1, h2, 1);

		mergeCol(sheet, h1.getRowNum(), h1.getRowNum(), 2, 4);
		mergeCol(sheet, h1.getRowNum(), h1.getRowNum(), 5, 7);
		mergeCol(sheet, h1.getRowNum(), h1.getRowNum(), 8, 10);

		for (int i = 11; i <= 17; i++)
			mergeCol(sheet, h1, h2, i);

		// fill headers
		for (int i = 0; i < main.length; i++)
			set(h1, i, main[i], borderBold);

		for (int i = 0; i < sub.length; i++)
			set(h2, i, sub[i], borderBold);

		// ================= DATA =================

		int sumC = 0, sumD = 0, sumE = 0;
		int sumF = 0, sumG = 0, sumH = 0;
		int sumI = 0, sumJ = 0, sumK = 0;

		for (JeReturnEntity e : list) {

			JsonNode d = e.getData();

			int c = safeInt(d, "manjurPad", "kanishthaAbhiyanta");
			int d1 = safeInt(d, "manjurPad", "shakhaAbhiyanta");

			int f = safeInt(d, "karyaratPad", "kanishthaAbhiyanta");
			int g = safeInt(d, "karyaratPad", "shakhaAbhiyanta");

			int i1 = safeInt(d, "riktaPad", "kanishthaAbhiyanta");
			int j = safeInt(d, "riktaPad", "shakhaAbhiyanta");

			Row row = sheet.createRow(r++);

			// create all cells for borders
			for (int i = 0; i <= 17; i++)
				set(row, i, "", border);

			set(row, 0, e.getRowId(), border);
			set(row, 1, e.getKaryalayacheNav(), border);

			set(row, 2, c, borderBold);
			set(row, 3, d1, borderBold);
			set(row, 4, c + d1, borderBold);

			set(row, 5, f, borderBold);
			set(row, 6, g, borderBold);
			set(row, 7, f + g, borderBold);

			set(row, 8, i1, borderBold);
			set(row, 9, j, borderBold);
			set(row, 10, i1 + j, borderBold);

			// ================= ENGINEERS =================

			int col = 11;
			JsonNode arr = d.path("abhiyantaYanchiYadi");

			if (arr.isArray()) {

				String firstSection = arr.get(0).path("section").asText("").trim();

				// ---- BLANK SECTION ----
				if (firstSection.isEmpty()) {

					StringBuilder sb = new StringBuilder();
					for (JsonNode s : arr) {
						for (JsonNode eng : s.path("engineers"))
							sb.append(eng.asText()).append("\n");
					}

					mergeCol(sheet, row.getRowNum(), row.getRowNum(), 11, 17);
					set(row, 11, sb.toString().trim(), border);
				}

				// ---- SECTION PRESENT ----
				else {
					for (JsonNode sec : arr) {

						if (col > 17)
							break;

						String section = sec.path("section").asText("");
						StringBuilder eng = new StringBuilder();

						for (JsonNode e1n : sec.path("engineers"))
							eng.append(e1n.asText()).append("\n");

						String finalTxt = section + "\n----------------------------------------\n"
								+ eng.toString().trim();

						set(row, col, finalTxt, border);
						col++;
					}
				}
			}

			// auto height
			autoHeight(row);

			// sums
			sumC += c;
			sumD += d1;
			sumE += (c + d1);

			sumF += f;
			sumG += g;
			sumH += (f + g);

			sumI += i1;
			sumJ += j;
			sumK += (i1 + j);
		}

		// ================= TOTAL =================
		Row t = sheet.createRow(r++);

		for (int i = 0; i <= 17; i++)
			set(t, i, "", borderBold);

		mergeCol(sheet, t.getRowNum(), t.getRowNum(), 0, 1);
		set(t, 0, "एकुण", borderBold);

		set(t, 2, sumC, borderBold);
		set(t, 3, sumD, borderBold);
		set(t, 4, sumE, borderBold);

		set(t, 5, sumF, borderBold);
		set(t, 6, sumG, borderBold);
		set(t, 7, sumH, borderBold);

		set(t, 8, sumI, borderBold);
		set(t, 9, sumJ, borderBold);
		set(t, 10, sumK, borderBold);

		// ================= WIDTH =================
		sheet.setColumnWidth(0, 3000);
		sheet.setColumnWidth(1, 9000);

		for (int i = 2; i <= 10; i++)
			sheet.setColumnWidth(i, 4000);

		for (int i = 11; i <= 17; i++)
			sheet.setColumnWidth(i, 6000);
		// ================= FOOTER =================
		r += 2;

		// Left side text
		sheet.createRow(r++).createCell(5).setCellValue("स्थळ प्रत अ.अ.यांना मान्य असे.");
		// Right side block
		Row f1 = sheet.createRow(r++);
		set(f1, 16, "(" + footerName + ")", footerStyle);

		Row f2 = sheet.createRow(r++);
		set(f2, 16, "उपअधीक्षक अभियंता", footerStyle);

		Row f3 = sheet.createRow(r++);
		set(f3, 16, "पुणे पाटबंधारे प्रकल्प मंडळ", footerStyle);

		Row f4 = sheet.createRow(r++);
		set(f4, 16, "पुणे 01", footerStyle);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		wb.write(out);
		wb.close();

		return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=je_return.xlsx")
				.contentType(
						MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())));
	}

	private CellStyle style(XSSFWorkbook wb, Font f) {
		CellStyle st = wb.createCellStyle();
		st.setAlignment(HorizontalAlignment.CENTER);
		st.setVerticalAlignment(VerticalAlignment.CENTER);
		st.setBorderBottom(BorderStyle.THIN);
		st.setBorderTop(BorderStyle.THIN);
		st.setBorderLeft(BorderStyle.THIN);
		st.setBorderRight(BorderStyle.THIN);
		st.setWrapText(true);
		st.setFont(f);
		return st;
	}

	private void put(Row r, int c, Object v, CellStyle st) {
		Cell cell = r.createCell(c);
		cell.setCellValue(String.valueOf(v));
		cell.setCellStyle(st);
	}

	private int safeInt(JsonNode n, String o, String k) {
		return n.has(o) && n.get(o).has(k) ? n.get(o).get(k).asInt() : 0;
	}

	private void set(Row r, int c, Object v, CellStyle st) {
		Cell cl = r.getCell(c);
		if (cl == null)
			cl = r.createCell(c);
		if (v instanceof Number)
			cl.setCellValue(((Number) v).doubleValue());
		else
			cl.setCellValue(v == null ? "" : v.toString());
		cl.setCellStyle(st);
	}

	private int merge(Sheet sh, int r, String txt, CellStyle st, int lc) {
		Row row = sh.createRow(r);
		set(row, 0, txt, st);
		sh.addMergedRegion(new CellRangeAddress(r, r, 0, lc));
		return r + 1;
	}

	private void mergeCol(Sheet sh, int fr, int tr, int fc, int tc) {
		CellRangeAddress region = new CellRangeAddress(fr, tr, fc, tc);

		sh.addMergedRegion(region);

		applyBorderToMergedRegion(sh, region); // 🔥 important
	}

	private void mergeCol(Sheet sh, Row r1, Row r2, int c) {
		CellRangeAddress region = new CellRangeAddress(r1.getRowNum(), r2.getRowNum(), c, c);

		sh.addMergedRegion(region);

		applyBorderToMergedRegion(sh, region);
	}

	private void applyBorderToMergedRegion(Sheet sheet, CellRangeAddress region) {

		RegionUtil.setBorderTop(BorderStyle.THIN, region, sheet);
		RegionUtil.setBorderBottom(BorderStyle.THIN, region, sheet);
		RegionUtil.setBorderLeft(BorderStyle.THIN, region, sheet);
		RegionUtil.setBorderRight(BorderStyle.THIN, region, sheet);
	}

	private void autoHeight(Row row) {

		float defaultHeight = row.getSheet().getDefaultRowHeightInPoints();
		float maxHeight = defaultHeight;

		for (Cell cell : row) {

			if (cell == null)
				continue;

			if (cell.getCellType() == CellType.STRING) {

				String text = cell.getStringCellValue();
				if (text == null)
					continue;

				int lines = text.split("\n").length;

				// Excel approx: 1 line ≈ default row height
				float needed = lines * defaultHeight;

				if (needed > maxHeight)
					maxHeight = needed;
			}
		}

		// Extra padding so last line never hides
		row.setHeightInPoints(maxHeight + 30);
	}

	@Transactional
	@Override
	public DeputyReturnAResponse saveOrUpdateDeputyReturnA(DeputyReturnARequest request) {

		DeputyReturnAResponse response = new DeputyReturnAResponse();
		ApplicationError error;

		final String username = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
		final String corrId = MDC.get("correlationId");
		final LocalDateTime now = LocalDateTime.now();

		try {

			log.info("START saveOrUpdateDeputyReturnA | year={} | divisions={} | user={} | corrId={}",
					request.getYear(), request.getDivision().size(), username, corrId);

			for (DeputyReturnADivisionDto div : request.getDivision()) {

				String office = div.getKaryalayacheNav();

				for (DeputyReturnARowDto r : div.getRows()) {

					// ================= HARD DELETE =================
					if ("D".equalsIgnoreCase(r.getFlag())) {

						deputyReturnARepository
								.findByDeleteIdAndYearAndKaryalayacheNav(r.getDeleteId(), request.getYear(), office)
								.ifPresent(deputyReturnARepository::delete);

						log.info("DELETED DeputyReturnA | year={} | office={} | deleteId={} | corrId={}",
								request.getYear(), office, r.getDeleteId(), corrId);
						continue;
					}

					// ================= SAVE / UPDATE =================
					Optional<DeputyReturnAEntity> opt = deputyReturnARepository
							.findByRowIdAndYearAndKaryalayacheNav(r.getRowId(), request.getYear(), office);

					DeputyReturnAEntity e;

					if (opt.isPresent()) {

						// -------- UPDATE --------
						e = opt.get();
						e.setFlag("U");
						e.setUpdatedAt(now);
						e.setUpdatedBy(username);

						log.debug("UPDATED DeputyReturnA | year={} | office={} | rowId={} | corrId={}",
								request.getYear(), office, r.getRowId(), corrId);

					} else {

						// -------- CREATE --------
						e = new DeputyReturnAEntity();
						e.setYear(request.getYear());
						e.setRowId(r.getRowId());
						e.setDeleteId(r.getDeleteId());
						e.setKaryalayacheNav(office);
						e.setUpAdhikshakAbhiyanta(request.getUpAdhikshakAbhiyanta());
						e.setFlag("C");
						e.setCreatedAt(now);
						e.setCreatedBy(username);
						e.setUpdatedAt(now);
						e.setUpdatedBy(username);

						log.debug("CREATED DeputyReturnA | year={} | office={} | rowId={} | corrId={}",
								request.getYear(), office, r.getRowId(), corrId);
					}

					// common fields
					e.setData(r.getData());

					deputyReturnARepository.save(e);
				}
			}

			response.setYear(request.getYear());
			response.setUpAdhikshakAbhiyanta(request.getUpAdhikshakAbhiyanta());

			error = new ApplicationError("200", "SUCCESS");
			response.setErrorDetails(error);

			log.info("SUCCESS saveOrUpdateDeputyReturnA | year={} | corrId={}", request.getYear(), corrId);

			return response;

		} catch (Exception ex) {

			log.error("ERROR saveOrUpdateDeputyReturnA | year={} | corrId={}", request.getYear(), corrId, ex);

			error = new ApplicationError("500", ex.getMessage());
			response.setErrorDetails(error);

			return response;
		}
	}

	@Transactional(readOnly = true)
	@Override
	public DeputyReturnAResponse getDeputyReturnAData(String year) {

		DeputyReturnAResponse response = new DeputyReturnAResponse();
		ApplicationError error;

		String corrId = MDC.get("correlationId");

		try {

			log.info("START getDeputyReturnAData | year={} | corrId={}", year, corrId);

			List<DeputyReturnAEntity> list = deputyReturnARepository.findByYearOrderByKaryalayacheNavAscRowIdAsc(year);

			if (list.isEmpty()) {

				log.warn("NO DATA | year={} | corrId={}", year, corrId);

				response.setYear(year);
				response.setData(null);
				response.setMessage("No records found");

				error = new ApplicationError("204", "No data available");
				response.setErrorDetails(error);
				return response;
			}

			// ================= EXTRA SAFETY SORT =================
			list.sort(Comparator.comparing(DeputyReturnAEntity::getKaryalayacheNav)
					.thenComparing(DeputyReturnAEntity::getRowId));

			// ================= GROUP BY OFFICE =================
			Map<String, List<DeputyReturnARowDto>> groupedMap = list.stream()
					.collect(Collectors.groupingBy(DeputyReturnAEntity::getKaryalayacheNav, LinkedHashMap::new, // preserve
																												// order
							Collectors.mapping(this::mapToRowDto, Collectors.toList())));

			List<DeputyReturnADivisionDto> divisions = new ArrayList<>();

			for (Map.Entry<String, List<DeputyReturnARowDto>> entry : groupedMap.entrySet()) {

				// sort rows inside each office
				entry.getValue().sort(Comparator.comparing(DeputyReturnARowDto::getRowId));

				divisions.add(DeputyReturnADivisionDto.builder().karyalayacheNav(entry.getKey()).rows(entry.getValue())
						.build());
			}

			DeputyReturnARequest data = DeputyReturnARequest.builder().year(year).division(divisions).build();

			response.setYear(year);
			response.setData(data);
			response.setMessage("Data fetched successfully");

			error = new ApplicationError("200", "SUCCESS");
			response.setErrorDetails(error);

			log.info("SUCCESS getDeputyReturnAData | year={} | groups={} | corrId={}", year, divisions.size(), corrId);

			return response;

		} catch (Exception ex) {

			log.error("ERROR getDeputyReturnAData | year={} | corrId={}", year, corrId, ex);

			response.setYear(year);
			response.setData(null);
			response.setMessage("Failed to fetch data");

			error = new ApplicationError("500", ex.getMessage());
			response.setErrorDetails(error);

			return response;
		}
	}

	private DeputyReturnARowDto mapToRowDto(DeputyReturnAEntity e) {

		return DeputyReturnARowDto.builder().rowId(e.getRowId()).deleteId(e.getDeleteId()).flag(e.getFlag())
				.data(e.getData()).build();
	}

	@Transactional
	@Override
	public DeputyReturnAResponse saveOrUpdateDeputyReturnB(DeputyReturnARequest request) {

		DeputyReturnAResponse response = new DeputyReturnAResponse();

		String user = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");

		String corrId = MDC.get("correlationId");
		LocalDateTime now = LocalDateTime.now();

		try {

			log.info("START saveOrUpdateDeputyReturnB | year={} | divisions={} | user={} | corrId={}",
					request.getYear(), request.getDivision().size(), user, corrId);

			for (DeputyReturnADivisionDto div : request.getDivision()) {

				String office = div.getKaryalayacheNav();

				for (DeputyReturnARowDto r : div.getRows()) {

					// ========= DELETE =========
					if ("D".equalsIgnoreCase(r.getFlag())) {

						deputyReturnBRepository
								.findByDeleteIdAndYearAndKaryalayacheNav(r.getDeleteId(), request.getYear(), office)
								.ifPresent(deputyReturnBRepository::delete);

						log.info("DELETED DeputyReturnB | year={} | office={} | deleteId={} | corrId={}",
								request.getYear(), office, r.getDeleteId(), corrId);
						continue;
					}

					// ========= SAVE / UPDATE =========
					Optional<DeputyReturnBEntity> opt = deputyReturnBRepository
							.findByRowIdAndYearAndKaryalayacheNav(r.getRowId(), request.getYear(), office);

					DeputyReturnBEntity e;

					if (opt.isPresent()) {

						// UPDATE
						e = opt.get();
						e.setFlag("U");
						e.setUpdatedAt(now);
						e.setUpdatedBy(user);

						log.debug("UPDATED DeputyReturnB | year={} | office={} | rowId={} | corrId={}",
								request.getYear(), office, r.getRowId(), corrId);

					} else {

						// CREATE
						e = new DeputyReturnBEntity();
						e.setYear(request.getYear());
						e.setRowId(r.getRowId());
						e.setDeleteId(r.getDeleteId());
						e.setKaryalayacheNav(office);
						e.setUpAdhikshakAbhiyanta(request.getUpAdhikshakAbhiyanta());

						e.setFlag("C");
						e.setCreatedAt(now);
						e.setCreatedBy(user);
						e.setUpdatedAt(now);
						e.setUpdatedBy(user);

						log.debug("CREATED DeputyReturnB | year={} | office={} | rowId={} | corrId={}",
								request.getYear(), office, r.getRowId(), corrId);
					}

					e.setData(r.getData());
					deputyReturnBRepository.save(e);
				}
			}

			// -------- RESPONSE --------
			response.setYear(request.getYear());
			response.setUpAdhikshakAbhiyanta(request.getUpAdhikshakAbhiyanta());
			response.setMessage("Saved successfully");

			response.setErrorDetails(new ApplicationError("200", "SUCCESS"));

			log.info("SUCCESS saveOrUpdateDeputyReturnB | year={} | corrId={}", request.getYear(), corrId);

			return response;

		} catch (Exception ex) {

			log.error("ERROR saveOrUpdateDeputyReturnB | year={} | corrId={}", request.getYear(), corrId, ex);

			response.setErrorDetails(new ApplicationError("500", ex.getMessage()));

			return response;
		}
	}

	@Transactional(readOnly = true)
	@Override
	public DeputyReturnAResponse getDeputyReturnBData(String year) {

		DeputyReturnAResponse response = new DeputyReturnAResponse();
		String corrId = MDC.get("correlationId");

		try {

			log.info("START getDeputyReturnBData | year={} | corrId={}", year, corrId);

			// already ordered by repo, still safe sort
			List<DeputyReturnBEntity> list = deputyReturnBRepository.findByYearOrderByKaryalayacheNavAscRowIdAsc(year);

			if (list.isEmpty()) {

				response.setMessage("No records found");
				response.setErrorDetails(new ApplicationError("204", "No data"));
				return response;
			}

			// ================= SORT SAFETY =================
			list.sort(Comparator.comparing(DeputyReturnBEntity::getKaryalayacheNav)
					.thenComparing(DeputyReturnBEntity::getRowId));

			// ================= GROUP BY OFFICE =================
			Map<String, List<DeputyReturnARowDto>> map = list.stream()
					.collect(Collectors.groupingBy(DeputyReturnBEntity::getKaryalayacheNav, LinkedHashMap::new, // preserve
																												// order
							Collectors.mapping(this::mapToRowDtoB, Collectors.toList())));

			List<DeputyReturnADivisionDto> divisions = new ArrayList<>();

			for (Map.Entry<String, List<DeputyReturnARowDto>> e : map.entrySet()) {

				// sort rows inside office
				e.getValue().sort(Comparator.comparing(DeputyReturnARowDto::getRowId));

				DeputyReturnADivisionDto d = new DeputyReturnADivisionDto();
				d.setKaryalayacheNav(e.getKey());
				d.setRows(e.getValue());

				divisions.add(d);
			}

			// ---- SAME STRUCTURE AS REQUEST ----
			DeputyReturnARequest data = new DeputyReturnARequest();
			data.setYear(year);
			data.setDivision(divisions);

			response.setYear(year);
			response.setUpAdhikshakAbhiyanta(list.get(0).getUpAdhikshakAbhiyanta());
			response.setData(data);
			response.setMessage("Data fetched");

			response.setErrorDetails(new ApplicationError("200", "SUCCESS"));

			log.info("SUCCESS getDeputyReturnBData | year={} | groups={} | corrId={}", year, divisions.size(), corrId);

			return response;

		} catch (Exception ex) {

			log.error("ERROR getDeputyReturnBData | year={} | corrId={}", year, corrId, ex);

			response.setErrorDetails(new ApplicationError("500", ex.getMessage()));
			return response;
		}
	}

	private DeputyReturnARowDto mapToRowDtoB(DeputyReturnBEntity e) {

		return DeputyReturnARowDto.builder().rowId(e.getRowId()).deleteId(e.getDeleteId()).flag(e.getFlag())
				.data(e.getData()).build();
	}

	@Transactional
	@Override
	public VivranPatraAResponse saveOrUpdateDeputyVivranA(VivranPatraARequest request) {

		VivranPatraAResponse response = new VivranPatraAResponse();
		ApplicationError error;

		final String user = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");
		final String corrId = MDC.get("correlationId");
		final LocalDateTime now = LocalDateTime.now();

		try {

			log.info("START saveVivranPatraA | year={} | divisions={} | user={} | corrId={}", request.getYear(),
					request.getDivision().size(), user, corrId);

			for (VivranPatraADivisionDto div : request.getDivision()) {

				String office = div.getKaryalayacheNav();

				for (VivranPatraARowDto r : div.getRows()) {

					// ===== DELETE =====
					if ("D".equalsIgnoreCase(r.getFlag())) {

						vivranPatraARepository
								.findByDeleteIdAndYearAndKaryalayacheNav(r.getDeleteId(), request.getYear(), office)
								.ifPresent(vivranPatraARepository::delete);

						log.info("DELETED | year={} | office={} | deleteId={}", request.getYear(), office,
								r.getDeleteId());
						continue;
					}

					// ===== SAVE / UPDATE =====
					Optional<VivranPatraAEntity> opt = vivranPatraARepository
							.findByRowIdAndYearAndKaryalayacheNav(r.getRowId(), request.getYear(), office);

					VivranPatraAEntity e;

					if (opt.isPresent()) {

						e = opt.get();
						e.setFlag("U");
						e.setUpdatedBy(user);
						e.setUpdatedAt(now);

					} else {

						e = new VivranPatraAEntity();
						e.setYear(request.getYear());
						e.setRowId(r.getRowId());
						e.setDeleteId(r.getDeleteId());
						e.setKaryalayacheNav(office);
						e.setUpAdhikshakAbhiyanta(request.getUpAdhikshakAbhiyanta());

						e.setFlag("C");
						e.setCreatedBy(user);
						e.setCreatedAt(now);
						e.setUpdatedBy(user);
						e.setUpdatedAt(now);
					}

					e.setData(r.getData());
					vivranPatraARepository.save(e);
				}
			}

			response.setYear(request.getYear());
			response.setUpAdhikshakAbhiyanta(request.getUpAdhikshakAbhiyanta());
			response.setMessage("Saved successfully");

			error = new ApplicationError("200", "SUCCESS");
			response.setErrorDetails(error);

			log.info("SUCCESS saveVivranPatraA | year={} | corrId={}", request.getYear(), corrId);

			return response;

		} catch (Exception ex) {

			log.error("ERROR saveVivranPatraA | year={} | corrId={}", request.getYear(), corrId, ex);

			error = new ApplicationError("500", ex.getMessage());
			response.setErrorDetails(error);
			return response;
		}
	}

	@Override
	public VivranPatraAResponse getDeputyVivranA(String year) {

		VivranPatraAResponse response = new VivranPatraAResponse();
		ApplicationError error;

		String corrId = MDC.get("correlationId");

		try {

			log.info("START getVivranPatraA | year={} | corrId={}", year, corrId);

			List<VivranPatraAEntity> list = vivranPatraARepository.findByYearOrderByKaryalayacheNavAscRowIdAsc(year);

			if (list.isEmpty()) {

				response.setMessage("No data found");
				response.setData(List.of());

				error = new ApplicationError("204", "NO DATA");
				response.setErrorDetails(error);
				return response;
			}

			// ================= GROUP BY OFFICE =================
			Map<String, List<VivranPatraARowDto>> map = list.stream()
					.collect(Collectors.groupingBy(VivranPatraAEntity::getKaryalayacheNav, LinkedHashMap::new,
							Collectors.mapping(this::mapToRowDto, Collectors.toList())));

			List<VivranPatraADivisionDto> divisions = new ArrayList<>();

			// ===== SUMMARY MAP =====
			Map<String, VivranPatraASummaryDto> summaryMap = new LinkedHashMap<>();

			for (Map.Entry<String, List<VivranPatraARowDto>> entry : map.entrySet()) {

				String office = entry.getKey();
				List<VivranPatraARowDto> rows = entry.getValue();

				// ---------- SORT ROWS ----------
				rows.sort(Comparator.comparing(VivranPatraARowDto::getRowId));

				divisions.add(VivranPatraADivisionDto.builder().karyalayacheNav(office).rows(rows).build());

				// ---------- SUMMARY CALC ----------
				int sanction = 0;
				int working = 0;
				int vacant = 0;
				int futureVacant = 0;

				for (VivranPatraARowDto r : rows) {

					JsonNode d = r.getData();

					sanction += d.path("sanctionPost").asInt(0);
					working += d.path("workingPost").asInt(0);
					vacant += d.path("vacantPost").asInt(0);
					futureVacant += d.path("futureVacancy").asInt(0);
				}

				summaryMap.put(office, VivranPatraASummaryDto.builder().district(office).sanctionPost(sanction)
						.workingPost(working).vacantPost(vacant).futureVacancy(futureVacant).build());
			}

			// ---------- GRAND TOTAL ----------
			int tSanction = 0, tWorking = 0, tVacant = 0, tFuture = 0;

			for (VivranPatraASummaryDto s : summaryMap.values()) {
				tSanction += s.getSanctionPost();
				tWorking += s.getWorkingPost();
				tVacant += s.getVacantPost();
				tFuture += s.getFutureVacancy();
			}

			summaryMap.put("एकुण", VivranPatraASummaryDto.builder().district("एकुण").sanctionPost(tSanction)
					.workingPost(tWorking).vacantPost(tVacant).futureVacancy(tFuture).build());

			// ================= FINAL RESPONSE =================
			Map<String, Object> finalData = new HashMap<>();
			finalData.put("division", divisions);
			finalData.put("summary", new ArrayList<>(summaryMap.values()));

			response.setYear(year);
			response.setData(finalData);
			response.setMessage("Success");

			error = new ApplicationError("200", "SUCCESS");
			response.setErrorDetails(error);

			log.info("SUCCESS getVivranPatraA | year={} | groups={} | corrId={}", year, divisions.size(), corrId);

			return response;

		} catch (Exception ex) {

			log.error("ERROR getVivranPatraA | year={} | corrId={}", year, corrId, ex);

			error = new ApplicationError("500", ex.getMessage());
			response.setErrorDetails(error);
			return response;
		}
	}

	private VivranPatraARowDto mapToRowDto(VivranPatraAEntity e) {

		return VivranPatraARowDto.builder().rowId(e.getRowId()).deleteId(e.getDeleteId()).flag(e.getFlag())
				.data(e.getData()).build();
	}

}