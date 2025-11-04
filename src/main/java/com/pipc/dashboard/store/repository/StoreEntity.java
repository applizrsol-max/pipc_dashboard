package com.pipc.dashboard.store.repository;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
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
@Table(name = "store_data")
@Data
public class StoreEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "department_name", nullable = false)
	private String departmentName;

	// ✅ Store JSON safely in PostgreSQL jsonb column
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "rows_data", columnDefinition = "jsonb", nullable = false)
	private JsonNode rowsData;

	@Column(name = "ekun")
	private Integer ekun;

	@Column(name = "ekun_ekandar")
	private Integer ekunEkandar;

	@Column(name = "row_id")
	private Integer rowId;

	private Long deleteId;

	@Column(name = "created_by")
	private String createdBy;

	@Column(name = "updated_by")
	private String updatedBy;

	@Column(name = "flag")
	private String flag; // "C" = Created, "U" = Updated

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
}
