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

	@Column(name = "department_name")
	private String deptName; // departmentName (e.g. "Adhishak Abhiyanta Pune...")

	// ✅ Correct JSONB mapping for Hibernate 6.6+
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb", nullable = false)
	private JsonNode data;

	private Integer ekun; // department total
	private Integer ekunEkandar; // overall total
	private String createdBy;
	private String updatedBy;
	private String flag; // "C" (Create) or "U" (Update)
	@Column(name = "row_id")
	private Integer rowId;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@UpdateTimestamp
	private LocalDateTime updatedAt;
}
