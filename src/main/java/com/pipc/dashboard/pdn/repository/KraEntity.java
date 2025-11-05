package com.pipc.dashboard.pdn.repository;

import java.time.LocalDateTime;

// *** REMOVE ORG.HIBERNATE.ANNOTATIONS ***
// import org.hibernate.annotations.CreationTimestamp; 
// import org.hibernate.annotations.UpdateTimestamp; 
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "kra_data")
@Data
public class KraEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;
	private String kraPeriod;
	private String reference;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private JsonNode kraRow;

	@Column(name = "row_id", nullable = false)
	private Integer rowId;

	private Long deleteId;

	private String createdBy;
	private String updatedBy;
	private String flag;

	// *** REMOVED @CreationTimestamp ***
	private LocalDateTime createdAt;

	// *** REMOVED @UpdateTimestamp ***
	private LocalDateTime updatedAt;
}