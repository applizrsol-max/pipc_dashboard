package com.pipc.dashboard.drawing.request;

import java.util.List;

import lombok.Data;

@Data
public class PralambitBhusampadanRequest {
    private String title;
    private String period;
    private String month;
    private String year;
    private String date;
    private List<PralambitBhusampadanRow> data;
}