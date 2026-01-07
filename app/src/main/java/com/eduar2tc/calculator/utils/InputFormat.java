package com.eduar2tc.calculator.utils;

import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;

public class InputFormat {

    private static final String TAG = "InputFormat";
    private static final String DECIMAL_POINT = ".";
    private static final String ZERO = "0";
    private static final String SCIENTIFIC_NOTATION_FORMAT = "%.2E";
    private static final String DECIMAL_FORMAT = "%.2f";
    private static final String EXTENDED_DECIMAL_FORMAT = "%.3f";
    private static final int MAX_RESULT_LENGTH = 10;

    private static boolean isInt = false;
    private static boolean foundPoint = false;

    public static void clear(EditText editText, TextView result) {
        editText.setText("");
        result.setText("");
        isInt = false;
        foundPoint = false;
    }

    public static String formatResult(String result) {
        Log.d("InputFormat", "formatResult: " + result);
        try{
            if (result.contains(DECIMAL_POINT)) {
                String[] parts = result.split("\\.");
                if (parts.length > 1 && parts[1].length() > 2) {
                    if (parts[1].charAt(2) == '5') {
                        result = String.format(Locale.getDefault(), EXTENDED_DECIMAL_FORMAT, Double.parseDouble(result));
                    } else {
                        result = String.format(Locale.getDefault(), DECIMAL_FORMAT, Double.parseDouble(result));
                    }
                }
            }

            if (result.equals(ZERO + DECIMAL_POINT)) {
                result = ZERO;
            }

            if (result.contains(DECIMAL_POINT) && result.split("\\.")[1].matches("0{1,}")) {
                result = result.split("\\.")[0];
            }

            if (result.length() > MAX_RESULT_LENGTH) {
                result = String.format(Locale.getDefault(), SCIENTIFIC_NOTATION_FORMAT, Double.parseDouble(result));
            }
        }catch(NumberFormatException e){
            Log.e(TAG, "Error formatting result: " + e.getMessage());
            result = "Error in format";
        }
        return result;
    }

    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static void checkIsNumeric(EditText editText, Button button) {
        if (TextUtils.isDigitsOnly(button.getText())) {
            int start = editText.getSelectionStart();
            int end = editText.getSelectionEnd();
            editText.getText().replace(Math.min(start, end), Math.max(start, end), button.getText());
            isInt = true;
        }

        String currentText = editText.getText().toString();
        if (currentText.isEmpty() && button.getText().toString().equals(DECIMAL_POINT)) {
            editText.setText(ZERO + DECIMAL_POINT);
            editText.setSelection(editText.getText().length());
            isInt = false;
            foundPoint = true;
        }
    }

    public static void checkDecimalPoint(EditText editText, Button buttonClicked) {
        String currentText = editText.getText().toString();

        if (currentText.isEmpty() || currentText.matches(".*[+\\-×÷]$")) {
            int start = editText.getSelectionStart();
            int end = editText.getSelectionEnd();
            editText.getText().replace(Math.min(start, end), Math.max(start, end), ZERO + DECIMAL_POINT);
            return;
        }

        String[] operands = currentText.split("[+\\-×÷]");
        String lastOperand = operands[operands.length - 1];

        if (!lastOperand.contains(DECIMAL_POINT)) {
            int start = editText.getSelectionStart();
            int end = editText.getSelectionEnd();
            editText.getText().replace(Math.min(start, end), Math.max(start, end), buttonClicked.getText());
        }
    }

    public boolean checkIsUnary(TextView textView, String operator) {
        return !textView.getText().toString().split(operator)[0].isEmpty();
    }
}