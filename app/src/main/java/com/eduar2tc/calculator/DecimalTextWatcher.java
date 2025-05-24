package com.eduar2tc.calculator;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class DecimalTextWatcher implements TextWatcher {
    private final EditText editText;
    private final DecimalFormat decimalFormat;

    public DecimalTextWatcher(EditText editText) {
        this.editText = editText;
        decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(Locale.getDefault());
        decimalFormat.applyPattern("#,###.##");
        decimalFormat.setGroupingUsed(true);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable editable) {
        editText.removeTextChangedListener(this);

        String originalText = editable.toString();

        if (!originalText.isEmpty()) {
            double parsed;
            try {
                Number parsedValue = decimalFormat.parse(originalText);
                parsed = parsedValue != null ? parsedValue.doubleValue() : 0.0;
            } catch (Exception e) {
                parsed = 0.0;
            }

            String formattedText = decimalFormat.format(parsed);
            editText.setText(formattedText);
            editText.setSelection(formattedText.length());
        }

        editText.addTextChangedListener(this);
    }
}

