package com.pipc.dashboard.establishment.request;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
public class AgendaRow {
    private Long rowId;
    private Long deleteId;
    private String deleteFlag;
    private String upAdhikshakAbhiyantaName;
    private JsonNode columnData;   // NEW merged JSON comes directly
}
