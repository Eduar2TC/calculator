package com.eduar2tc.calculator.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class DecimalTextWatcher implements TextWatcher {
    private final EditText editText;
    private final DecimalFormat decimalFormat;

    public DecimalTextWatcher(EditText editText) {
        this.editText = editText;
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        decimalFormat = new DecimalFormat("#,###.########", symbols);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable editable) {
        String originalString = editable.toString();
        if (originalString.isEmpty()) return;

        editText.removeTextChangedListener(this);

        try {
            int selectionStart = editText.getSelectionStart();
            String cleanString = originalString.replace(",", "");

            // Split numbers and operators using regex
            String[] tokens = cleanString.split("(?<=[+\\-×÷%])|(?=[+\\-×÷%])");
            StringBuilder formattedString = new StringBuilder();

            for (String token : tokens) {
                if (token.matches("[+\\-×÷%]")) {
                    formattedString.append(token);
                } else if (!token.isEmpty()) {
                    formattedString.append(formatNumber(token));
                }
            }

            String finalString = formattedString.toString();
            editText.setText(finalString);

            // Restore cursor adjusting for length changes
            int diff = finalString.length() - originalString.length();
            int newCursor = Math.max(0, Math.min(finalString.length(), selectionStart + diff));
            editText.setSelection(newCursor);

        } catch (Exception e) {
            e.printStackTrace();
        }

        editText.addTextChangedListener(this);
    }


    private String formatNumber(String number) {
        if (number.isEmpty() || number.equals("-")) return number;
        // If it ends with . or .0 or .00 etc, don't fully format - return as is
        if (number.endsWith(".") || number.matches(".*\\.0+$")) {
            return number;
        }
        // If it ends with a dot and has only one dot at end, keep it and format the rest
        if (number.endsWith(".") && number.indexOf('.') == number.length() - 1) {
            try {
                double parsed = Double.parseDouble(number.substring(0, number.length() - 1));
                return decimalFormat.format(parsed) + ".";
            } catch (Exception e) {
                return number;
            }
        }
        try {
            double parsed = Double.parseDouble(number);
            return decimalFormat.format(parsed);
        } catch (Exception e) {
            return number;
        }
    }

}
