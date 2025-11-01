package com.pipc.dashboard.drawing.request;

import java.util.Map;

import lombok.Data;

@Data
public class DamNalikaRequest {
    private String title;
    private String period;
    private String year;
    private String month;
    private String date;
    private Map<String, NalikaDepartmentData> departments;
}