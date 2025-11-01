package com.pipc.dashboard.drawing.request;

import java.util.List;

import lombok.Data;

@Data
public class PralambitBhusampadanRow {
    private Integer kramank;
    private String star;
    private List<PralambitVishay> pralambitVishay;
}