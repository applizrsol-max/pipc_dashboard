package com.pipc.dashboard.suprama.repository;

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
@Table(name = "suprema_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupremaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String projectName; // ✅ Unique per project
    private String projectYear;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private JsonNode supremaData;

    private String createdBy;

    @CreationTimestamp
    private LocalDateTime createdDatetime;

    private String updatedBy;

    @UpdateTimestamp
    private LocalDateTime updatedDatetime;

    @Column(length = 1)
    private String recordFlag; // 'C' or 'U'
}
