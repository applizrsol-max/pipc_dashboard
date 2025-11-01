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
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Data
@Entity
@Table(name = "bhumipadan_data", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "period", "kramank", "sub_id", "star" }) })
public class BhumipadanEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // DB PK - LONG

	private String title;
	private String period;
	private Integer kramank; // अ.क्र.
	@Column(name = "sub_id")
	private Integer subId; // inner subject id
	private String star; // स्तर (e.g. विभागीय कार्यालय)

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private JsonNode data; // stores the subject object (vishay etc)

	private String flag; // C / U / D

	private String createdBy;
	private String updatedBy;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}