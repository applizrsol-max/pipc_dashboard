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

@Data
@Entity
@Table(name = "appeal_mahiti_register")
public class AppealRequestEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long deleteId;
	private String date;
	private Long rowId;
	private String year;
	private String flag;

	private String arjachaNondaniKramank;
	private String arjdarNavPatta;
	private String arjPraptDinank;
	private String darikhvReshBabli;
	private String mahitiChaPrakar;
	private String maangItlelyaMahitichiRuprekha;
	private String yogyMahitiAaheKa;
	private String arjShulk;
	private String shulkPramanitDinank;
	private String mahitiDilyaDinank;
	private String mahitiNghitAaheKa;
	private String konKshaAwarNokri;
	private String prathamAppeal;
	private String shera;
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private JsonNode dynamicColumns;

	private String createdBy;

	private LocalDateTime createdDate;
	private String updatedBy;

	private LocalDateTime updatedDate;
}