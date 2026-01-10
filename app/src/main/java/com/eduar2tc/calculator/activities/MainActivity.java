package com.eduar2tc.calculator.activities;

import static com.eduar2tc.calculator.R.*;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.eduar2tc.calculator.DecimalTextWatcher;
import com.eduar2tc.calculator.utils.CustomDialog;
import com.eduar2tc.calculator.utils.InputFormat;
import com.eduar2tc.calculator.utils.PerformOperations;
import com.eduar2tc.calculator.R;

public class MainActivity extends AppCompatActivity {
    private EditText editText;
    private TextView textViewResult;
    private HorizontalScrollView horizontalScrollView;
    private float originalTextSize;

    private boolean validOperation = false;
    private static final int OPERATOR_BACK = id.operator0;
    private static final int OPERATOR_CLEAR = id.operator1;
    private static final int OPERATOR_DIVISION = id.operator3;
    private static final int OPERATOR_MULTIPLICATION = id.operator4;
    private static final int OPERATOR_SUBTRACTION = id.operator5;
    private static final int OPERATOR_ADDITION = id.operator6;
    private static final int OPERATOR_EQUAL = id.operator7;
    private static final int OPERATOR_DECIMAL_POINT = id.operator8;
    private static final int OPERATOR_PERCENTAGE = id.operator2;
    private static final int OPERATOR_MORE_MINUS = id.operator9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);
        initializeComponents();
        configureListeners();
        preventLockScreen();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        new CustomDialog(MainActivity.this).onOptionsItemSelected(item, MainActivity.this);
        return super.onOptionsItemSelected(item);
    }

    private void initializeComponents() {
        initializeTextViewAndEditText();
        initializeButtons();
        initializeAppbarStatusBar();
    }

    private void initializeTextViewAndEditText() {
        editText = findViewById(id.editText);
        editText.requestFocus();
        editText.setShowSoftInputOnFocus(false);
        textViewResult = findViewById(id.textViewResult);
        originalTextSize = editText.getTextSize();
        horizontalScrollView = findViewById(id.horizontalScrollView);
    }

    private void initializeButtons() {
        Button[] arrayListBtn = new Button[10];
        Button[] arrayListOperators = new Button[10];

        for (int i = 0; i < arrayListBtn.length; i++) {
            int id = getResources().getIdentifier("btn" + i, "id", getPackageName());
            arrayListBtn[i] = findViewById(id);
            arrayListBtn[i].setOnClickListener(this::onClick);
        }

        for (int i = 0; i < arrayListOperators.length; i++) {
            int id = getResources().getIdentifier("operator" + i, "id", getPackageName());
            arrayListOperators[i] = findViewById(id);
            arrayListOperators[i].setOnClickListener(this::onClick);
        }
    }

    private void initializeAppbarStatusBar() {
        //Toolbar
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.status_app_bar_background_color));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(drawable.baseline_history_24);
            getSupportActionBar().setTitle("");
        }
    }

    private void configureListeners() {
        configureEditTextListener();
        configureTextViewResultListener();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void configureEditTextListener() {
        // Restaurado: Formateador de miles en tiempo real
        editText.addTextChangedListener(new DecimalTextWatcher(editText));

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence editable, int start, int count, int after) {
                adjustTextSize((Editable) editable);
            }

            @Override
            public void onTextChanged(CharSequence editable, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                handleTextChange(editable);
            }
        });

        editText.setOnTouchListener((v, event) -> {
            int offset = editText.getOffsetForPosition(event.getX(), event.getY());
            Selection.setSelection(editText.getText(), offset);
            hideKeyboard(editText);
            return false; //false show handle
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void configureTextViewResultListener() {
        textViewResult.setOnTouchListener((v, event) -> {
            hideKeyboard(textViewResult);
            return true;
        });
    }

    private void handleTextChange(Editable editable) {
        if (containsInvalidOperators(editable)) {
            validOperation = false;
        } else {
            // Restaurado: Limpiar comas para el cálculo interno previo
            String expression = editable.toString().replace(",", "");
            if (expression.length() > 0 && PerformOperations.containsOperator(expression)) {
                String result = PerformOperations.performOperation(expression);
                textViewResult.setText(result);
                validOperation = !result.equals("Error");
            } else {
                textViewResult.setText("");
            }
        }
    }

    private boolean containsInvalidOperators(Editable editable) {
        String last2Chars = editable.length() >= 2 ? editable.subSequence(editable.length() - 2, editable.length()).toString() : "";
        if (last2Chars.matches("([+\\-×÷])\\1") || last2Chars.matches("([+\\-×÷])[+\\-×÷]")) {
            editable.delete(editable.length() - 1, editable.length());
            return true;
        } else {
            return isLastCharOperator(editable);
        }
    }

    private boolean isLastCharOperator(Editable editable) {
        String lastChar = editable.length() > 0 ? editable.subSequence(editable.length() - 1, editable.length()).toString() : "";
        return lastChar.matches("[+\\-×÷.]");
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View btn) {
        Button buttonClicked = (Button) btn;
        InputFormat.checkIsNumeric(editText, buttonClicked);
        performOperation(buttonClicked);
    }

    @SuppressLint("NonConstantResourceId")
    public void performOperation(Button buttonClicked) {
        switch (buttonClicked.getId()) {
            case OPERATOR_BACK:
                PerformOperations.deleteCharAtCursor(editText);
                adjustTextSizeWhenPressBack(editText.getText());
                break;
            case OPERATOR_CLEAR:
                PerformOperations.performClearOperation(editText, textViewResult, horizontalScrollView);
                break;
            case OPERATOR_DECIMAL_POINT: InputFormat.checkDecimalPoint(editText, buttonClicked);
                break;
            case OPERATOR_DIVISION:
            case OPERATOR_MULTIPLICATION:
            case OPERATOR_ADDITION:
            case OPERATOR_SUBTRACTION:
                 String operator = buttonClicked.getText().toString();
                PerformOperations.insertTextAtCursor(editText, operator);
                break;
            case OPERATOR_MORE_MINUS:
                PerformOperations.toggleSign(editText);
                break;
            case OPERATOR_EQUAL:
                if (validOperation && !textViewResult.getText().toString().isEmpty()) {
                    PerformOperations.performEqualOperation(editText, textViewResult);
                    resultAnimation();
                }
                break;
            default:
                break;
        }
    }

    private void resultAnimation() {
        // --- Preparación ---
        // Hacemos el resultado invisible al principio para que aparezca suavemente.
        textViewResult.setAlpha(0f);
        // Calculamos la distancia que el resultado debe "subir".
        float distance = editText.getY() - textViewResult.getY();

        // --- Animación de la Expresión (EditText) ---
        // Se desvanece y baja un poco para dar espacio al resultado.
        editText.animate()
                .alpha(0f)
                .translationYBy(50f) // Baja ligeramente
                .setDuration(250)
                .setInterpolator(new android.view.animation.AccelerateInterpolator())
                .start();

        // --- Animación del Resultado (TextViewResult) ---
        // Sube a la posición del EditText, aparece y recupera su tamaño.
        // Cuando la animación del resultado termina, reseteamos todo.
        textViewResult.animate()
                .translationY(distance) // Sube a la posición del EditText
                .alpha(1f)
                .setDuration(350)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .withEndAction(this::resetAfterAnimation)
                .start();
    }
    private void resetAfterAnimation() {
        editText.post(() -> {
            editText.setText(textViewResult.getText());

            editText.setAlpha(1f);
            editText.setTranslationY(0f);
            editText.setSelection(editText.getText().length());

            textViewResult.setAlpha(1f);
            textViewResult.setTranslationY(0f);
        });
    }



    private void adjustTextSize(Editable editable) {
        // 1. Definir el tamaño máximo (el del XML) y el mínimo (donde empieza el scroll)
        float maxTextSizeSP = 70f;
        float minTextSizeSP = 45f; // Cuando llegue a 45sp, dejará de achicarse y hará scroll

        Paint textPaint = editText.getPaint();
        int maxWidth = editText.getWidth(); // El ancho visible de la pantalla
        if (maxWidth <= 0) return;

        // Calcular cuánto mide el texto actualmente
        String text = editable.toString();
        float textWidth = textPaint.measureText(text);

        if (textWidth > maxWidth) {
            // Calcular el nuevo tamaño proporcional
            float newSize = textPaint.getTextSize() * (maxWidth / textWidth);

            // LIMITAR: Si el nuevo tamaño es menor al mínimo, usamos el mínimo
            if (newSize < TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, minTextSizeSP, getResources().getDisplayMetrics())) {
                editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, minTextSizeSP);
            } else {
                editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
            }
        } else {
            // Si el texto es corto, volver al tamaño original
            editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, maxTextSizeSP);
        }
    }

    private void adjustTextSizeWhenPressBack(Editable editable) {
        Paint textPaint = editText.getPaint();
        int maxWidth = editText.getWidth();
        if (maxWidth <= 0) return;
        int maxDigits = (int) (maxWidth / textPaint.measureText("0"));
        int currentLength = editable.length();

        if (currentLength <= maxDigits) {
            restoreOriginalTextSize();
        } else {
            float ratio = (float) currentLength / maxDigits;
            float newTextSize = editText.getTextSize() * ratio;
            editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);
        }
    }

    private void restoreOriginalTextSize() {
        float currentTextSize = editText.getTextSize();
        if (currentTextSize < originalTextSize) {
            float newSize = currentTextSize + (originalTextSize - currentTextSize) * 0.1f;
            editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
        }
    }

    private void preventLockScreen() {
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
