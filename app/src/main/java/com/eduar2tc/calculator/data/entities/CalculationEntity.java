package com.eduar2tc.calculator.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "calculations")
public class CalculationEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String expression;
    public String result;
    public long timestamp;

    public CalculationEntity() {}
    public CalculationEntity(String expression, String result, long timestamp) {
        this.expression = expression;
        this.result = result;
        this.timestamp = timestamp;
    }
}
