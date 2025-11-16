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
@Table(name = "appeal_register")
public class AppealEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long deleteId;
	private String date;
	private Long rowId;
	private String year;
	private String flag;

	private String apeelArjachaNondaniKramank;
	private String apeelkaracheNav;
	private String apeelArjKonakadeKelaAahe;
	private String apeelArjPraptJhalyachaDinank;
	private String pratidariYancheNavPatta;
	private String apeelarthiDurustReneSathiKaam;
	private String apeelchaThodkyaTathya;
	private String apeelArjacheShulk;
	private String konakadeApeelSwikarBharle;
	private String apeelArjachiVidhikShulkBharalDinank;
	private String konalyaJanmahitiAdhikariYanchikadeApeelKeleTathyaTapshil;
	private String apeelvarNirnayDilyachaDinank;
	private String shera;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private JsonNode dynamicColumns;

	private String createdBy;

	private LocalDateTime createdDate;
	private String updatedBy;

	private LocalDateTime updatedDate;
}