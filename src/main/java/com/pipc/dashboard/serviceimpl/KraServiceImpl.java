package com.pipc.dashboard.serviceimpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pipc.dashboard.pdn.repository.KraEntity;
import com.pipc.dashboard.pdn.repository.KraRepository;
import com.pipc.dashboard.pdn.request.KraRequest;
import com.pipc.dashboard.pdn.response.KraResponse;
import com.pipc.dashboard.service.KraService;
import com.pipc.dashboard.utility.ApplicationError;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Service
public class KraServiceImpl implements KraService {

	@Autowired
	private KraRepository kraRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private EntityManager entityManager; // Necessary for detach()

	@Override
	@Transactional
	public KraResponse saveOrUpdateKra(KraRequest request) {
		KraResponse kraResponse = new KraResponse();
		ApplicationError error = new ApplicationError();
		StringBuilder msg = new StringBuilder();

		try {
			String userFromMDC = MDC.get("user");
			if (userFromMDC == null)
				userFromMDC = "SYSTEM";
			final String currentUser = userFromMDC;
			ObjectMapper mapper = this.objectMapper;
			// Use a single timestamp for consistency within the transaction
			LocalDateTime now = LocalDateTime.now();

			for (Map<String, Object> rowData : request.getKraData()) {
				Integer rowId = (Integer) rowData.get("rowId");
				if (rowId == null)
					continue;

				JsonNode incomingNode = mapper.valueToTree(rowData);

				// Find the entity based on the unique combination (kraPeriod + rowId)
				Optional<KraEntity> existingOpt = kraRepository.findByKraPeriodAndRowId(request.getKraPeriod(), rowId);

				if (existingOpt.isPresent()) {
					KraEntity existing = existingOpt.get();
					boolean entityChanged = false; // Flag to track if ANY change occurred

					// --- 1. Check for KRA Row Data change (JSON column) ---
					JsonNode existingNode = existing.getKraRow() == null ? mapper.createObjectNode()
							: existing.getKraRow();

					// Compare JSON string representations for change detection
					if (!existingNode.toString().equals(incomingNode.toString())) {
						existing.setKraRow(incomingNode);
						entityChanged = true;
					}

					// --- 2. Check for Header field changes (title) ---
					// Use null-safe comparison logic
					if (existing.getTitle() == null ? request.getTitle() != null
							: !existing.getTitle().equals(request.getTitle())) {
						existing.setTitle(request.getTitle());
						entityChanged = true;
					}

					// --- 3. Check for Header field changes (reference) ---
					if (existing.getReference() == null ? request.getReference() != null
							: !existing.getReference().equals(request.getReference())) {
						existing.setReference(request.getReference());
						entityChanged = true;
					}

					// --- 4. Final Save/Detach decision ---
					if (entityChanged) {
						// Update audit fields MANUALLY (since @UpdateTimestamp was removed)
						existing.setUpdatedBy(currentUser);
						existing.setUpdatedAt(now);
						existing.setFlag("U");
						kraRepository.save(existing);
						msg.append("Updated rowId: ").append(rowId).append(" | ");
					} else {
						// If no change, detach to break the link with the persistence context
						// This is VITAL to stop phantom updates.
						entityManager.detach(existing);
						msg.append("No change for rowId: ").append(rowId).append(" | ");
					}

				} else {
					// --- Creation Logic ---
					KraEntity entity = new KraEntity();
					entity.setTitle(request.getTitle());
					entity.setKraPeriod(request.getKraPeriod());
					entity.setReference(request.getReference());
					entity.setKraRow(incomingNode);
					entity.setRowId(rowId);

					// Manually set all audit fields for creation
					entity.setCreatedBy(currentUser);
					entity.setCreatedAt(now);
					entity.setUpdatedBy(currentUser);
					entity.setUpdatedAt(now);
					entity.setFlag("C");
					kraRepository.save(entity);
					msg.append("Created rowId: ").append(rowId).append(" | ");
				}
			}

			error.setErrorCode("0");
			error.setErrorDescription("Success");
			kraResponse.setErrorDetails(error);
			kraResponse.setMessage(msg.toString());

		} catch (Exception e) {
			error.setErrorCode("1");
			error.setErrorDescription("Error while saving: " + e.getMessage());
			kraResponse.setErrorDetails(error);
			kraResponse.setMessage("Operation failed.");
			// Re-throw to ensure the transaction rolls back
			throw new RuntimeException("KRA save failed due to an exception.", e);
		}

		return kraResponse;
	}

	@Override
	public KraResponse getKraByPeriod(String kraPeriod, int page, int size) {
		KraResponse kraResponse = new KraResponse();
		ApplicationError error = new ApplicationError();

		try {
			Pageable pageable = PageRequest.of(page, size);
			Page<KraEntity> kraPage = kraRepository.findByKraPeriod(kraPeriod, pageable);

			List<JsonNode> kraData = kraPage.getContent().stream().map(KraEntity::getKraRow)
					.collect(Collectors.toList());

			ObjectMapper mapper = this.objectMapper;
			ObjectNode responseNode = mapper.createObjectNode();
			responseNode.put("kraPeriod", kraPeriod);
			responseNode.put("page", kraPage.getNumber());
			responseNode.put("size", kraPage.getSize());
			responseNode.put("totalPages", kraPage.getTotalPages());
			responseNode.put("totalElements", kraPage.getTotalElements());
			responseNode.set("kraData", mapper.valueToTree(kraData));

			kraResponse.setResponseData(responseNode);
			error.setErrorCode("0");
			error.setErrorDescription("Success");
			kraResponse.setErrorDetails(error);
			kraResponse.setMessage("Fetched successfully");

		} catch (Exception e) {
			error.setErrorCode("1");
			error.setErrorDescription("Error fetching KRA: " + e.getMessage());
			kraResponse.setErrorDetails(error);
			kraResponse.setMessage("Operation failed.");
		}

		return kraResponse;
	}
}