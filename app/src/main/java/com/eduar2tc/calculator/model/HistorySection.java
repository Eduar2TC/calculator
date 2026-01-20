package com.eduar2tc.calculator.model;

import java.time.LocalDate;
import java.util.List;

public class HistorySection {
    private LocalDate date;
    private List<Calculation> items;

    public HistorySection(LocalDate date, List<Calculation> items) {
        this.date = date;
        this.items = items;
    }

    public LocalDate getDate() {
        return date;
    }

    public List<Calculation> getItems() {
        return items;
    }
}
