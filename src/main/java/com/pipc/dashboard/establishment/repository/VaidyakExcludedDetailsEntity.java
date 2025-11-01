package com.pipc.dashboard.establishment.repository;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "medical_bill_vaidyak_excluded_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaidyakExcludedDetailsEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long subId;
	private String type;
	private Double amount;
	private String description;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vaidyak_pariganana_id")
	private VaidyakKharchaParigananaEntity vaidyakKharchaPariganana;
}
