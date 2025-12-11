package com.pipc.dashboard.establishment.request;

import java.util.List;

import lombok.Data;

@Data
public class ThirteenRequest {
    private Meta meta;
    private List<ThirteenRow> rows;
}