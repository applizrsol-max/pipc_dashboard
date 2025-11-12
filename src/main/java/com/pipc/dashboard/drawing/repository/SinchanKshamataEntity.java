package com.pipc.dashboard.drawing.repository;

import java.time.LocalDate;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sinchan_kshamata_data", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "period", "section_title", "row_id", "delete_id" }) })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SinchanKshamataEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;
	private String period;
	private String month;
	private String year;
	private LocalDate date;

	@Column(name = "section_title")
	private String sectionTitle;

	@Column(name = "row_id")
	private Integer rowId; // 🔹 Each row under section

	private Long deleteId;
	private String flag; // S, U, D

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb", name = "section_data")
	private JsonNode sectionData; // dynamic JSON per row

	private String createdBy;
	private String updatedBy;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@UpdateTimestamp
	private LocalDateTime updatedAt;
}
