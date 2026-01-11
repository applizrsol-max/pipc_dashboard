package com.pipc.dashboard.establishment.repository;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "vivran_patra_a_summary")
public class VivranPatraASummaryEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "year", nullable = false)
	private String year;

	@Column(name = "district", nullable = false)
	private String district;

	@Column(name = "manjur_pad")
	private Integer manjurPad;

	@Column(name = "karyarat_pad")
	private Integer karyaratPad;

	@Column(name = "rikta_pad")
	private Integer riktaPad;

	@Column(name = "bhavishya_rikta_pad")
	private Integer bhavishyaRiktaPad;

	// ===== AUDIT FIELDS =====

	@Column(name = "created_by")
	private String createdBy;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_by")
	private String updatedBy;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
}