package com.pipc.dashboard.establishment.repository;

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
import lombok.Data;

@Entity
@Table(name = "employee_leave_data")
@Data
public class LeaveEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "row_id")
	private Long rowId;

	@Column(name = "year")
	private String year;

	@Column(name = "month")
	private String month;

	@Column(name = "date")
	private String date;

	@Column(name = "employee_name")
	private String employeeName;

	@Column(name = "designation")
	private String designation;

	@Column(name = "leave_from_date")
	private String leaveFromDate;

	@Column(name = "leave_to_date")
	private String leaveToDate;

	@Column(name = "flag")
	private String flag;

	@CreationTimestamp
	@Column(name = "created_date", updatable = false)
	private LocalDateTime createdDate;

	@Column(name = "created_by")
	private String createdBy;

	@UpdateTimestamp
	@Column(name = "updated_date")
	private LocalDateTime updatedDate;

	@Column(name = "updated_by")
	private String updatedBy;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private JsonNode data;
}