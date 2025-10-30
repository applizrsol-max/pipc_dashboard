package com.pipc.dashboard.pdn.request;

import java.util.List;

import lombok.Data;

@Data
public class AgendaPoint {
    private Integer srNo;
    private String pointOfAgenda;
    private List<AgendaDetail> details;
}