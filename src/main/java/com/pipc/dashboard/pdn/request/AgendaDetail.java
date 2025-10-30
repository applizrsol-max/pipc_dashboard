package com.pipc.dashboard.pdn.request;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
public class AgendaDetail {
    private String rowId;
    private JsonNode columnData;
}