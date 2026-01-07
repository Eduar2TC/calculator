package com.eduar2tc.calculator.utils;

import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import com.itis.libs.parserng.android.expressParser.MathExpression;

public class PerformOperations {

    public static String performOperation(String expression) {
        if (expression == null || expression.isEmpty()) return "";

        try {
            String sanitized = expression.replace(",", "")
                    .replace("×", "*")
                    .replace("÷", "/");

            MathExpression mathExpression = new MathExpression(sanitized);
            String result = mathExpression.solve();

            if (result == null || result.toLowerCase().contains("nan") || result.toLowerCase().contains("infinity")) {
                return "Error";
            }
            return InputFormat.formatResult(result);
        } catch (Exception e) {
            return "Error";
        }
    }

    public static void performBackOperation(EditText editText, TextView textView) {
        int length = editText.getText().length();
        if (length > 0) {
            editText.getText().delete(length - 1, length);
        }
    }

    public static void performClearOperation(EditText editText, TextView result, HorizontalScrollView horizontalScrollView) {
        InputFormat.clear(editText, result);
        editText.setTextSize(70f);
        horizontalScrollView.post(() -> {
            horizontalScrollView.fullScroll(View.FOCUS_LEFT);
            editText.clearFocus();
            editText.requestFocus();
            editText.setSelection(0);
        });
    }

    public static void performEqualOperation(EditText editText, TextView textViewResult) {
        String expression = editText.getText().toString();
        if (!containsOperator(expression)) {
            textViewResult.setText(expression);
            return;
        }
        String result = performOperation(expression);
        textViewResult.setText(result);
        editText.setTextSize(70f);
    }

    public static boolean containsOperator(String expression) {
        return expression.matches(".*[+\\-×÷%*].*");
    }

    // ESTE ES EL MÉTODO PROBLEMÁTICO - CORREGIDO
    public static void insertTextAtCursor(EditText editText, String textToInsert) {
        Editable editable = editText.getText();
        if (editable == null) return;

        int start = Math.max(editText.getSelectionStart(), 0);
        int end = Math.max(editText.getSelectionEnd(), 0);

        // Insertar el texto
        editable.replace(Math.min(start, end), Math.max(start, end), textToInsert);

        // NO establecer la selección aquí - dejar que el TextWatcher lo maneje
        // El TextWatcher calculará correctamente la posición después del formateo
    }

    public static void deleteCharAtCursor(EditText editText) {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        Editable editable = editText.getText();
        if (start > 0 && start == end) {
            editable.delete(start - 1, start);
            // NO establecer selección - el TextWatcher lo manejará
        } else if (start != end) {
            editable.delete(start, end);
            // NO establecer selección - el TextWatcher lo manejará
        }
    }

    public static void appendOperation(EditText editText, String operation) {
        String currentText = editText.getText().toString();
        if (!currentText.isEmpty() && isOperator(currentText.substring(currentText.length() - 1)) && isOperator(operation)) {
            editText.getText().replace(currentText.length() - 1, currentText.length(), operation);
        } else {
            editText.append(operation);
        }
    }

    private static boolean isOperator(String s) {
        return s.matches("[+\\-×÷%]");
    }

    public static void toggleSign(EditText editText) {
        String currentText = editText.getText().toString();
        if (currentText.isEmpty()) return;
        if (currentText.startsWith("-")) {
            editText.setText(currentText.substring(1));
        } else {
            editText.setText("-" + currentText);
        }
        editText.setSelection(editText.getText().length());
    }
}