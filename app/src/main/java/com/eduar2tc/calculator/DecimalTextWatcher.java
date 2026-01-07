package com.eduar2tc.calculator;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.StringTokenizer;

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

            // Separar números y operadores con regex
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

            // Restaurar cursor ajustando por cambios en longitud
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
        // Si termina en . o .0 o .00 etc, no formatear - devolver tal cual
        if (number.endsWith(".") || number.matches(".*\\.0+$")) {
            return number;
        }
        // Si termina en punto y no tiene más de uno, conservarlo
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
