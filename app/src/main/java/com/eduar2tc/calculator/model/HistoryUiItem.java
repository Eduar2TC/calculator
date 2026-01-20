package com.eduar2tc.calculator.model;

public class HistoryUiItem {
    private boolean isHeader;
    private String headerTitle;
    private Calculation calculation;

    private HistoryUiItem() {}

    public static HistoryUiItem createHeader(String title) {
        HistoryUiItem i = new HistoryUiItem();
        i.isHeader = true;
        i.headerTitle = title;
        return i;
    }

    public static HistoryUiItem createRow(Calculation calc) {
        HistoryUiItem i = new HistoryUiItem();
        i.isHeader = false;
        i.calculation = calc;
        return i;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public String getHeaderTitle() {
        return headerTitle;
    }

    public Calculation getCalculation() {
        return calculation;
    }
}
