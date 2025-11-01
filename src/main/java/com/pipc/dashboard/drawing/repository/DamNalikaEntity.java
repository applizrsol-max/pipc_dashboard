package com.pipc.dashboard.drawing.repository;

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
@Table(name = "dam_nalika_data")
@Data
public class DamNalikaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // ✅ Should always be Long (not String)
	private String title;
	private String period;
	private String departmentKey;
	private String departmentName;
	private Integer rowId;
	private String year;
	private String month;
	private String flag;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private JsonNode data;

	@CreationTimestamp
	private LocalDateTime createdAt;
	private String createdBy;

	@UpdateTimestamp
	private LocalDateTime updatedAt;
	private String updatedBy;
}