package com.eduar2tc.calculator.utils;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;

public class InputFormat {

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
        // rounded result to  2 decimal places if it is a decimal number, if contains a "5" in the third decimal place, it will not be rounded to the next decimal place
        if (result.contains(".")) {
            String[] parts = result.split("\\.");
            if (parts.length > 1 && parts[1].length() > 2) {
                if (parts[1].charAt(2) == '5') {
                    result = String.format(Locale.getDefault(), "%.3f", Double.parseDouble(result));
                } else {
                    result = String.format(Locale.getDefault(), "%.2f", Double.parseDouble(result));
                }
            }
        }
        //TODO: Fix this bug
        // if result only contains a zero and decimal point and right side is empty
        if (result.equals("0.")) {
            result = "0";
        }
        // if result contains a decimal point and two zeros or more in the right side (e.g. 1.0000) it will be displayed as an integer
        if (result.contains(".") && result.split("\\.")[1].matches("0{1,}")) {
            result = result.split("\\.")[0];
        }
        // if result is too long, it will be displayed in scientific notation
        if (result.length() > 10) {
            result = String.format(Locale.getDefault(), "%.2E", Double.parseDouble(result));
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
        if (editText.getText().toString().isEmpty() && button.getText().toString().equals(".")) {
            editText.append("0.");
            isInt = false;
            foundPoint = true;
        }
    }

    public static void checkDecimalPoint(TextView textView, Button button) {
        if (!foundPoint && isInt) {
            textView.append(".");
            isInt = false;
            foundPoint = true;
        }
    }

    // Unary operator (-, +)
    public boolean checkIsUnary(TextView textView, String operator) {
        return !textView.getText().toString().split(operator)[0].isEmpty();
    }
}
