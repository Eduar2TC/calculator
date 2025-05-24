package com.eduar2tc.calculator.utils;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import com.itis.libs.parserng.android.expressParser.MathExpression;

public class PerformOperations {
    public static String performOperation(String expression) {
        String result = "";
        try {
            MathExpression mathExpression = new MathExpression(expression);
            result = mathExpression.solve();
        } catch (Exception e) {
            result = "Error in expression";
        }
        return InputFormat.formatResult(result);
    }
    public static void performBackOperation(EditText editText, TextView textView) {
        if (!editText.getText().toString().isEmpty()) {
            editText.getText().delete(editText.getText().length() - 1, editText.getText().length());
            //adjustTextSizeWhenPressBack(editText.getText());
        }
    }

    public static void performClearOperation(EditText editText, TextView result, HorizontalScrollView horizontalScrollView) {
        InputFormat.clear( editText, result);
        //reset font size editText
        editText.setTextSize(70f);
        //reset position
        horizontalScrollView.post(() -> {
            horizontalScrollView.fullScroll(View.FOCUS_LEFT);
            editText.clearFocus();
            editText.requestFocus();
            editText.setSelection(0);
        });
    }
    public static void performEqualOperation(EditText editText, TextView textViewResult) {
        String result = PerformOperations.performOperation(editText.getText().toString());
        textViewResult.setText(result);
        editText.setTextSize(70f); //reset font size
    }
    public static void appendOperation(EditText editText, String operation) {
        editText.append(operation);
    }
    public static void toggleSign(EditText editText) {
        String currentText = editText.getText().toString();
         if (currentText.startsWith("-")) {
            editText.setText(currentText.substring(1));
             editText.setSelection(editText.getText().length());
        }else{
             editText.getText().insert(0, "-");
         }
    }
}
