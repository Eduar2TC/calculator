package com.eduar2tc.calculator.ui.validators;

import android.text.Editable;
import com.eduar2tc.calculator.utils.PerformOperations;

public class OperationValidator {

    /**
     * Valida una expresión matemática
     * @param editable La expresión a validar
     * @param lastValidResult El último resultado válido calculado (para mantenerlo cuando se añade un operador)
     * @return ValidationResult con el estado y resultado
     */
    public static ValidationResult validate(Editable editable, String lastValidResult) {
        // Primero verificar operadores inválidos (dobles)
        if (hasInvalidDoubleOperators(editable)) {
            return new ValidationResult(false, lastValidResult);
        }

        String expression = editable.toString().replace(",", "");

        // Si está vacío, limpiar resultado
        if (expression.isEmpty()) {
            return new ValidationResult(false, "");
        }

        // Si NO contiene operadores, NO mostrar nada (es solo un número)
        if (!PerformOperations.containsOperator(expression)) {
            return new ValidationResult(false, "");
        }

        // Si termina en operador, mantener el último resultado válido
        if (endsWithOperator(expression)) {
            return new ValidationResult(false, lastValidResult);
        }

        // Tiene operadores y NO termina en operador -> calcular
        String result = PerformOperations.performOperation(expression);
        boolean isValid = !result.equals("Error");

        if (isValid) {
            return new ValidationResult(true, result);
        }

        // Si hay error, mantener el último resultado válido
        return new ValidationResult(false, lastValidResult);
    }

    /**
     * Verifica y elimina operadores dobles inválidos
     */
    private static boolean hasInvalidDoubleOperators(Editable editable) {
        if (editable.length() < 2) return false;

        String last2Chars = editable.subSequence(editable.length() - 2, editable.length()).toString();

        // Permitir "+-" para números negativos (ej: 5+-3)
        if (last2Chars.equals("+-")) {
            return false;
        }

        // Detectar operadores dobles inválidos: ++, --, ××, ÷÷, +×, etc.
        if (last2Chars.matches("([+\\-×÷])[+×÷]") || last2Chars.matches("([×÷])\\-")) {
            editable.delete(editable.length() - 1, editable.length());
            return true;
        }

        return false;
    }

    /**
     * Verifica si la expresión termina con un operador
     */
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