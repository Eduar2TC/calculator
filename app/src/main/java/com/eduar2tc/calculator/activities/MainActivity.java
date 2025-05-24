package com.eduar2tc.calculator.activities;

import static com.eduar2tc.calculator.R.*;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
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
    }

    private void initializeTextViewAndEditText() {
        editText = findViewById(id.editText);
        editText.requestFocus();
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

    private void configureListeners() {
        configureEditTextListener();
        configureTextViewResultListener();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void configureEditTextListener() {
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
            return true;
        });
    }


    @SuppressLint("ClickableViewAccessibility")
    private void configureTextViewResultListener() {
        textViewResult.setOnTouchListener((v, event) -> {
            hideKeyboard(textViewResult);
            return true;
        });
    }

    //TODO: Refactor this method . Restringir la animacion solo cuando la operacion sea mas de dos operandos y textViewResult no este vacio,
    // si textViewResult es vacio no se debe realizar la animacion
    private void handleTextChange(Editable editable) {
        if (containsInvalidOperators(editable)) {
            validOperation = false;
        } else {
            if (editable.length() > 1 ) {
                String result = PerformOperations.performOperation(editable.toString());
                textViewResult.setText(result);
                validOperation = true;
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
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
                PerformOperations.performBackOperation(editText, textViewResult);
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
                PerformOperations.appendOperation(editText, buttonClicked.getText().toString());
                break;
            case OPERATOR_MORE_MINUS:
                PerformOperations.toggleSign(editText);
                break;
            case OPERATOR_EQUAL:
                if (validOperation) {
                    PerformOperations.performEqualOperation(editText, textViewResult);
                    resultAnimation();
                }
                break;
            default:
                break;
        }
    }

    private void resultAnimation() {
        AnimatorSet set = new AnimatorSet();
        set.setDuration(350);
        set.playTogether(
                createFadeOutAnimator(editText),
                createFadeOutAnimator(textViewResult),
                createMoveAnimator(textViewResult),
                createMoveAnimator(editText),
                createTextSizeAnimator(textViewResult),
                createTextColorAnimator(textViewResult)
        );

        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {}

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                resetAfterAnimation();
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {}

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {}
        });

        set.start();
    }

    private ObjectAnimator createFadeOutAnimator(View view) {
        return ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
    }

    private ObjectAnimator createMoveAnimator(View view) {
        float finalY = editText.getY() - textViewResult.getY() + editText.getHeight() - textViewResult.getHeight();
        return ObjectAnimator.ofFloat(view, "translationY", 0f, finalY);
    }

    private ObjectAnimator createTextSizeAnimator(TextView textView) {
        return ObjectAnimator.ofFloat(textView, "textSize", textView.getTextSize(), 70f);
    }

    private ObjectAnimator createTextColorAnimator(TextView textView) {
        return ObjectAnimator.ofArgb(textView, "textColor",
                ContextCompat.getColor(this, R.color.text_view_color),
                ContextCompat.getColor(this, R.color.edit_text_color));
    }

    private void resetAfterAnimation() {
        editText.setText(textViewResult.getText());
        editText.setAlpha(1f);
        textViewResult.setAlpha(1f);
        textViewResult.setTextSize(38f);
        textViewResult.setTranslationY(0f);
        textViewResult.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.text_view_color));
        editText.setTranslationY(0f);
        editText.setSelection(editText.getText().length());
        textViewResult.setText("");
    }

    private void adjustTextSize(Editable editable) {
        Paint textPaint = editText.getPaint();
        int maxWidth = editText.getWidth();
        int maxDigits = (int) (maxWidth / textPaint.measureText("0"));
        int currentLength = editable.length();
        if (currentLength > maxDigits) {
            float newTextSize = textPaint.getTextSize() * ((float) maxDigits / currentLength);
            editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, Math.max(newTextSize, 12f));
            editText.setMovementMethod(new ScrollingMovementMethod());
        }
    }

    private void adjustTextSizeWhenPressBack(Editable editable) {
        Paint textPaint = editText.getPaint();
        int maxWidth = editText.getWidth();
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