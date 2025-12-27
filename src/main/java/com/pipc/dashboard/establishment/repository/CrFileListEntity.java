package com.pipc.dashboard.establishment.repository;

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
@Table(name = "cr_file_list")
@Data
public class CrFileListEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String year;

	private Long rowId;
	private Long deleteId;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private JsonNode data;

	private String flag; // C / U

	private String createdBy;
	private LocalDateTime createdAt;
	private String updatedBy;
	private LocalDateTime updatedAt;
}
