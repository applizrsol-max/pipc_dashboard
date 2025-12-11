package com.pipc.dashboard.establishment.request;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
public class ThirteenRow {
    private long rowId;
    private String deleteFlag;      
    private Long deleteId;
    private JsonNode columnData;
}
