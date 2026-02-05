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
                    .replace("\u00d7", "*")
                    .replace("\u00f7", "/");

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
        return expression.matches(".*[+\\-\u00d7\u00f7%*].*");
    }

    // THIS METHOD WAS PROBLEMATIC - FIXED
    public static void insertTextAtCursor(EditText editText, String textToInsert) {
        Editable editable = editText.getText();
        if (editable == null) return;

        int start = Math.max(editText.getSelectionStart(), 0);
        int end = Math.max(editText.getSelectionEnd(), 0);

        // Insert the text
        editable.replace(Math.min(start, end), Math.max(start, end), textToInsert);

        // DO NOT set the selection here - let the TextWatcher handle it
        // The TextWatcher will correctly compute the cursor position after formatting
    }

    public static void deleteCharAtCursor(EditText editText) {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        Editable editable = editText.getText();
        if (start > 0 && start == end) {
            editable.delete(start - 1, start);
            // DO NOT set selection - the TextWatcher will handle it
        } else if (start != end) {
            editable.delete(start, end);
            // DO NOT set selection - the TextWatcher will handle it
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
        return s.matches("[+\\-\u00d7\u00f7%]");
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