package com.pipc.dashboard.accounts.request;

import java.util.List;

import lombok.Data;

@Data
public class PendingParaRequest {
    private Integer year;
    private List<PendingParaRow> rows;
}