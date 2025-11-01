package com.pipc.dashboard.drawing.request;

import java.util.List;

import lombok.Data;

@Data
public class DepartmentData {
    private String name;
    private List<InspectionRow> rows;
}