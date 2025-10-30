package com.pipc.dashboard.serviceimpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipc.dashboard.pdn.repository.NrldEntity;
import com.pipc.dashboard.pdn.repository.NrldRepository;
import com.pipc.dashboard.pdn.repository.PdnAgendaEntity;
import com.pipc.dashboard.pdn.repository.PdnAgendaRepository;
import com.pipc.dashboard.pdn.request.AgendaDetail;
import com.pipc.dashboard.pdn.request.AgendaPoint;
import com.pipc.dashboard.pdn.request.NrldRequest;
import com.pipc.dashboard.pdn.request.PdnAgendaRequest;
import com.pipc.dashboard.pdn.response.NrldResponse;
import com.pipc.dashboard.pdn.response.PdnAgendaResponse;
import com.pipc.dashboard.service.PdnAgendaService;
import com.pipc.dashboard.utility.ApplicationError;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class PdnAgendaServiceImpl implements PdnAgendaService {

	private final PdnAgendaRepository pdnAgnedaRepo;
	private final ObjectMapper objectMapper;
	private final NrldRepository nrldRepo;

	public PdnAgendaServiceImpl(PdnAgendaRepository pdnAgnedaRepo, ObjectMapper objectMapper, NrldRepository nrldRepo) {
		this.pdnAgnedaRepo = pdnAgnedaRepo;
		this.objectMapper = objectMapper;
		this.nrldRepo = nrldRepo;
	}

	// ----------------------------------------------------
	// 🔹 Save or Update PDN Agenda
	// ----------------------------------------------------
	@Override
	public PdnAgendaResponse saveOrUpdatePdnAgenda(PdnAgendaRequest pdnAgendaRequest) {

		PdnAgendaResponse response = new PdnAgendaResponse();
		ApplicationError error = new ApplicationError();
		StringBuilder statusMsg = new StringBuilder();

		try {
			String currentUser = Optional.ofNullable(MDC.get("userId")).orElse("SYSTEM");

			for (AgendaPoint point : pdnAgendaRequest.getAgendaPoints()) {
				for (AgendaDetail detail : point.getDetails()) {

					JsonNode incomingData = detail.getColumnData();

					// Dynamic extraction of dam name & year
					String damName = extractFieldValue(incomingData, "dam");
					String year = extractFieldValue(incomingData, "year");

					if (damName == null || damName.isEmpty()) {
						statusMsg.append("[Skipped: Missing Dam Name for RowId ").append(detail.getRowId())
								.append("], ");
						continue;
					}

					// 🔍 Find existing entity
					Optional<PdnAgendaEntity> existingOpt = pdnAgnedaRepo
							.findBySubmissionYearAndPointOfAgendaAndRecordIdAndNameOfDam(
									pdnAgendaRequest.getSubmissionYear(), point.getPointOfAgenda(), detail.getRowId(),
									damName);

					PdnAgendaEntity entity;

					if (existingOpt.isPresent()) {
						entity = existingOpt.get();
						JsonNode existingData = entity.getColumnData();

						if (!existingData.equals(incomingData)) {
							entity.setColumnData(incomingData);
							entity.setUpdatedBy(currentUser);
							entity.setUpdatedAt(LocalDateTime.now());
							entity.setRecordFlag("U");
							pdnAgnedaRepo.save(entity);
							statusMsg.append("Updated: ").append(damName).append(", ");
						} else {
							statusMsg.append("No change for: ").append(damName).append(", ");
						}

					} else {
						entity = new PdnAgendaEntity();
						entity.setSubmissionTitle(pdnAgendaRequest.getSubmissionTitle());
						entity.setSubmissionYear(pdnAgendaRequest.getSubmissionYear());
						entity.setSrNo(point.getSrNo());
						entity.setPointOfAgenda(point.getPointOfAgenda());
						entity.setRecordId(detail.getRowId());
						entity.setNameOfDam(damName);
						entity.setColumnData(incomingData);
						entity.setRecordFlag("C");
						entity.setCreatedBy(currentUser);
						entity.setUpdatedBy(currentUser);
						entity.setCreatedAt(LocalDateTime.now());
						entity.setUpdatedAt(LocalDateTime.now());

						pdnAgnedaRepo.save(entity);
						statusMsg.append("Created: ").append(damName).append(", ");
					}
				}
			}

			error.setErrorCode("0");
			error.setErrorDescription("Agenda processed: " + statusMsg);
			response.setMessage("Success");
			response.setErrorDetails(error);

		} catch (Exception e) {
			error.setErrorCode("1");
			error.setErrorDescription("Error while saving agenda: " + e.getMessage());
			response.setMessage("Failed");
			response.setErrorDetails(error);
		}

		return response;
	}

	// ----------------------------------------------------
	// 🔹 Save or Update NRLD (Dam Records)
	// ----------------------------------------------------
	@Override
	public NrldResponse saveOrUpdateNrld(NrldRequest nrldRequest) {
		NrldResponse response = new NrldResponse();
		ApplicationError error = new ApplicationError();

		List<String> createdRecords = new ArrayList<>();
		List<String> updatedRecords = new ArrayList<>();

		String currentUser = Optional.ofNullable(MDC.get("user")).orElse("SYSTEM");

		try {
			for (JsonNode record : nrldRequest.getRecords()) {

				JsonNode recordData = record.get("data");
				if (recordData == null || recordData.isNull())
					continue;

				String rowId = recordData.has("rowId") ? recordData.get("rowId").asText() : null;
				String damName = extractFieldValue(recordData, "dam");
				String year = extractFieldValue(recordData, "year");

				if (rowId == null || damName == null) {
					continue; // skip invalid records
				}

				Optional<NrldEntity> existingOpt = nrldRepo.findByRowIdAndDamNameAndYear(rowId, damName, year);
				NrldEntity entity;

				if (existingOpt.isPresent()) {
					entity = existingOpt.get();
					if (!entity.getData().equals(recordData)) {
						entity.setData(recordData);
						entity.setUpdatedBy(currentUser);
						entity.setUpdatedAt(LocalDateTime.now());
						entity.setRecordFlag("U");
						nrldRepo.save(entity);
						updatedRecords.add(rowId + " (" + damName + ")");
					}
				} else {
					entity = NrldEntity.builder().rowId(rowId).damName(damName).year(year).data(recordData)
							.createdBy(currentUser).updatedBy(currentUser).createdAt(LocalDateTime.now())
							.updatedAt(LocalDateTime.now()).recordFlag("C").build();

					nrldRepo.save(entity);
					createdRecords.add(rowId + " (" + damName + ")");
				}
			}

			StringBuilder desc = new StringBuilder();
			if (!createdRecords.isEmpty()) {
				desc.append("Created: ").append(String.join(", ", createdRecords)).append(". ");
			}
			if (!updatedRecords.isEmpty()) {
				desc.append("Updated: ").append(String.join(", ", updatedRecords)).append(". ");
			}
			if (createdRecords.isEmpty() && updatedRecords.isEmpty()) {
				desc.append("No changes detected.");
			}

			error.setErrorCode("0");
			error.setErrorDescription(desc.toString());
			response.setErrorDetails(error);

		} catch (Exception e) {
			error.setErrorCode("1");
			error.setErrorDescription("Error while saving dam data: " + e.getMessage());
			response.setErrorDetails(error);
		}

		return response;
	}

	// ----------------------------------------------------
	// 🔍 Helper for dynamic key extraction (case-insensitive)
	// ----------------------------------------------------
	private String extractFieldValue(JsonNode data, String keyword) {
		if (data == null || !data.isObject())
			return null;

		for (Iterator<String> it = data.fieldNames(); it.hasNext();) {
			String field = it.next();
			if (field.toLowerCase().contains(keyword.toLowerCase())) {
				JsonNode value = data.get(field);
				if (value != null && !value.isNull())
					return value.asText();
			}
		}
		return null;
	}

	@Override
	public Page<PdnAgendaEntity> getPDNAgenda(String projectYear, String projectName, int page, int size) {

		Pageable pageable = PageRequest.of(page, size, Sort.by("recordId").ascending());

		return pdnAgnedaRepo.findBySubmissionYear(projectYear, pageable);

	}

	@Override
	public Page<NrldEntity> getNrldByYear(String year, String damName, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("rowId").ascending());

		if (damName != null && !damName.isEmpty()) {
			return nrldRepo.findByYearAndDamNameContainingIgnoreCase(year, damName, pageable);
		} else {
			return nrldRepo.findByYear(year, pageable);
		}
	}

}
