package com.pipc.dashboard.accounts.repository;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "accounts_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountsEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String categoryName;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb", nullable = false, name = "accounts_data")
	private JsonNode accountsData;

	private String createdBy;
	@Column(name = "row_id")
	private int rowId;

	private Long deleteId;

	@CreationTimestamp
	private LocalDateTime createdDatetime;

	private String updatedBy;
	@Column(name = "record_type")
	private String recordType; // "R" = Regular, "E" = Ekun

	@UpdateTimestamp
	private LocalDateTime updatedDatetime;

	@Column(length = 1)
	private String recordFlag; // C or U

	private String projectYear;

}