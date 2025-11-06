package com.pipc.dashboard.pdn.repository;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "nrld_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NrldEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "row_id", nullable = false)
	private Integer rowId;

	@Column(name = "dam_name", nullable = false)
	private String damName;

	@Column(name = "year")
	private String year;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb", nullable = false)
	private JsonNode data;

	private Long deleteId;
	private String createdBy;
	private String updatedBy;
	private String recordFlag;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
