package com.eduar2tc.calculator.ui.controllers;

import android.content.res.Resources;
import android.graphics.Paint;
import android.text.Editable;
import android.util.TypedValue;
import android.widget.EditText;

public class TextSizeController {
    private final EditText editText;
    private final float originalTextSize;
    private final Resources resources;

    private static final float MAX_TEXT_SIZE_SP = 70f;
    private static final float MIN_TEXT_SIZE_SP = 45f;

    public TextSizeController(EditText editText) {
        this.editText = editText;
        this.originalTextSize = editText.getTextSize();
        this.resources = editText.getResources();
    }

    public void adjustTextSize(Editable editable) {
        Paint textPaint = editText.getPaint();
        int maxWidth = editText.getWidth();
        if (maxWidth <= 0) return;

        float textWidth = textPaint.measureText(editable.toString());

        if (textWidth > maxWidth) {
            float newSize = textPaint.getTextSize() * (maxWidth / textWidth);
            float minSizePx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP,
                    MIN_TEXT_SIZE_SP,
                    resources.getDisplayMetrics()
            );

            if (newSize < minSizePx) {
                editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, MIN_TEXT_SIZE_SP);
            } else {
                editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
            }
        } else {
            editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, MAX_TEXT_SIZE_SP);
        }
    }

    public void adjustTextSizeWhenPressBack(Editable editable) {
        Paint textPaint = editText.getPaint();
        int maxWidth = editText.getWidth();
        if (maxWidth <= 0) return;

        int maxDigits = (int) (maxWidth / textPaint.measureText("0"));
        if (editable.length() <= maxDigits) {
            restoreOriginalTextSize();
        }
    }

    private void restoreOriginalTextSize() {
        float currentTextSize = editText.getTextSize();
        if (currentTextSize < originalTextSize) {
            float newSize = currentTextSize + (originalTextSize - currentTextSize) * 0.1f;
            editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
        }
    }
}