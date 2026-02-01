package com.eduar2tc.calculator.ui.validators;

import android.text.Editable;
import com.eduar2tc.calculator.utils.PerformOperations;

public class OperationValidator {

    private static String lastValidResult = "";

    public static ValidationResult validate(Editable editable) {

        if (hasInvalidDoubleOperators(editable)) {
            return new ValidationResult(false, lastValidResult); // Mantener último resultado
        }

        String expression = editable.toString().replace(",", "");

        if (expression.isEmpty()) {
            lastValidResult = "";
            return new ValidationResult(false, "");
        }

        if (!PerformOperations.containsOperator(expression)) {
            return new ValidationResult(false, "");
        }

        if (endsWithOperator(expression)) {
            return new ValidationResult(false, lastValidResult);
        }

        String result = PerformOperations.performOperation(expression);
        boolean isValid = !result.equals("Error");

        if (isValid) {
            lastValidResult = result;
            return new ValidationResult(true, result);
        }

        return new ValidationResult(false, lastValidResult);
    }

    public static void resetLastValidResult() {
        lastValidResult = "";
    }

    private static boolean hasInvalidDoubleOperators(Editable editable) {
        if (editable.length() < 2) return false;

        String last2Chars = editable.subSequence(editable.length() - 2, editable.length()).toString();

        if (last2Chars.equals("+-")) {
            return false;
        }

        if (last2Chars.matches("([+\\-×÷])[+×÷]") || last2Chars.matches("([×÷])\\-")) {
            editable.delete(editable.length() - 1, editable.length());
            return true;
        }

        return false;
    }

    private static boolean endsWithOperator(String expression) {
        if (expression.isEmpty()) return false;
        String lastChar = expression.substring(expression.length() - 1);
        return lastChar.matches("[+\\-×÷]");
    }

    public static class ValidationResult {
        public final boolean isValid;
        public final String result;

        public ValidationResult(boolean isValid, String result) {
            this.isValid = isValid;
            this.result = result;
        }
    }
}