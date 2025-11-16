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
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "medical_bill_vastavya_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "vaidyakKharchaPariganana")

public class VastavyaDetailsEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long subId;
	private String vastavyaPrakar;
	private Double pratyakshaKharch;
	private String anugya_rakkam;
	private Double deyaRakkam;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vaidyak_pariganana_id")
	private VaidyakKharchaParigananaEntity vaidyakKharchaPariganana;
}
