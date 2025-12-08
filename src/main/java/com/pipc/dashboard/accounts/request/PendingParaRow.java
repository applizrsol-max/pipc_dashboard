package com.pipc.dashboard.accounts.request;

import java.util.Map;

import lombok.Data;

@Data
public class PendingParaRow {
    private Long rowId;
    private Long deleteId;
    private String flag;  // C, U, D
    private Map<String, Object> data;  // dynamic JSON
}
