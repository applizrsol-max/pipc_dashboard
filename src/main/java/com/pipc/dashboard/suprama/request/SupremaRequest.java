package com.pipc.dashboard.suprama.request;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupremaRequest {
    private String projectYear;
    private List<JsonNode> rows; // dynamic rows, flexible structure
}
