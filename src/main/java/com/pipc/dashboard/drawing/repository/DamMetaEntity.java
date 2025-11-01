package com.pipc.dashboard.drawing.repository;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "dam_safety_meta_table")
public class DamMetaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;
	private String period;
	private String unit;

	private String createdBy;
	private LocalDateTime createdAt;

	private String updatedBy;
	private LocalDateTime updatedAt;
}