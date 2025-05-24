package com.eduar2tc.calculator.utils;

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
            // rounded result to  2 decimal places if it is a decimal number, if contains a "5" in the third decimal place, it will not be rounded to the next decimal place
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
            //TODO: Fix this bug
            // if result only contains a zero and decimal point and right side is empty
            if (result.equals(ZERO + DECIMAL_POINT)) {
                result = ZERO;
            }
            // if result contains a decimal point and two zeros or more in the right side (e.g. 1.0000) it will be displayed as an integer
            if (result.contains(DECIMAL_POINT) && result.split("\\.")[1].matches("0{1,}")) {
                result = result.split("\\.")[0];
            }
            // if result is too long, it will be displayed in scientific notation
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

    public static void checkIsNumeric(TextView editText, Button button) {
        if (TextUtils.isDigitsOnly(button.getText())) {
            editText.append(button.getText().toString());
            isInt = true;
        }
        if (editText.getText().toString().isEmpty() && button.getText().toString().equals(DECIMAL_POINT)) {
            editText.append(ZERO + DECIMAL_POINT);
            isInt = false;
            foundPoint = true;
        }
    }

    public static void checkDecimalPoint(EditText editText, Button buttonClicked) {
        String currentText = editText.getText().toString();

        // If it is empty or ends with an operator, add "0."
        if (currentText.isEmpty() || currentText.matches(".*[+\\-×÷]$")) {
            editText.append(ZERO + DECIMAL_POINT);
            return;
        }

        String[] operands = currentText.split("[+\\-×÷]");
        String lastOperand = operands[operands.length - 1];

        // If the last operand does not contain a decimal point, add the point
        if (!lastOperand.contains(DECIMAL_POINT)) {
            editText.append(buttonClicked.getText());
        }
    }

    // Unary operator (-, +)
    public boolean checkIsUnary(TextView textView, String operator) {
        return !textView.getText().toString().split(operator)[0].isEmpty();
    }
}
