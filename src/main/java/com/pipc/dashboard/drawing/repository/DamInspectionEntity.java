package com.pipc.dashboard.drawing.repository;

import java.time.LocalDateTime;

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
@Table(name = "dam_inspection_data")
@Data
public class DamInspectionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // ✅ Should always be Long (not String)

	// ---------- META FIELDS ----------
	@Column(name = "title", length = 2000)
	private String title;

	@Column(name = "department_name", length = 500)
	private String departmentName;

	private String period;

	// ---------- ROW FIELDS ----------
	private String departmentKey; // e.g. mamukhyaAbhiyanta

	private Integer rowId;
	private Long deleteId;
	private String year;
	private String month;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private JsonNode data;

	private String flag; // C, U, D

	// ---------- AUDIT ----------
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String createdBy;
	private String updatedBy;
}