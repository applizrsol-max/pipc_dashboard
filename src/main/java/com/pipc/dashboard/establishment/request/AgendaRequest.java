package com.pipc.dashboard.establishment.request;

import java.util.List;

import lombok.Data;

@Data
public class AgendaRequest {
    private Meta meta;
    private List<AgendaRow> rows;
}
