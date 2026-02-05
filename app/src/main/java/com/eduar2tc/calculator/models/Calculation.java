package com.eduar2tc.calculator.models;

public class Calculation {
    private String expression;
    private String result;
    private long timestamp;

    public Calculation(String expression, String result, long timestamp) {
        this.expression = expression;
        this.result = result;
        this.timestamp = timestamp;
    }

    // Compat constructor para seguridad (no usado pero mantiene retrocompatibilidad)
    public Calculation(String expression, String result) {
        this(expression, result, System.currentTimeMillis());
    }

    public String getExpression() {
        return expression;
    }

    public String getResult() {
        return result;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
