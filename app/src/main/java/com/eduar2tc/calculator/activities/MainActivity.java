package com.eduar2tc.calculator.activities;

import static com.eduar2tc.calculator.R.*;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eduar2tc.calculator.DecimalTextWatcher;
import com.eduar2tc.calculator.R;
import com.eduar2tc.calculator.adapter.HistoryAdapter;
import com.eduar2tc.calculator.behavior.TopSheetBehavior;
import com.eduar2tc.calculator.model.Calculation;
import com.eduar2tc.calculator.utils.CustomDialog;
import com.eduar2tc.calculator.utils.InputFormat;
import com.eduar2tc.calculator.utils.PerformOperations;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TopSheetBehavior.TopSheetCallback {
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

    private TopSheetBehavior<ConstraintLayout> topSheetBehavior;
    private ConstraintLayout topSheet;
    private ConstraintLayout constraintLayout;

    private RecyclerView recyclerViewHistory;
    private HistoryAdapter historyAdapter;
    private List<Calculation> calculationHistory;

    private float initialTouchY = 0;
    private boolean isDraggingTopSheet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
        constraintLayout = findViewById(id.constraintLayout);
        initializeComponents();
        configureListeners();
        preventLockScreen();
        initializeTopSheet();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            toggleTopSheet();
            return true;
        }
        new CustomDialog(MainActivity.this).onOptionsItemSelected(item, MainActivity.this);
        return super.onOptionsItemSelected(item);
    }

    private void toggleTopSheet() {
        if (topSheetBehavior.getState() == TopSheetBehavior.STATE_COLLAPSED) {
            topSheetBehavior.setState(TopSheetBehavior.STATE_PEEKED);
        } else {
            topSheetBehavior.setState(TopSheetBehavior.STATE_COLLAPSED);
        }
    }

    private void initializeComponents() {
        initializeTextViewAndEditText();
        initializeButtons();
        initializeAppbarStatusBar();
        initializeHistory();
    }

    private void initializeHistory(){
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        calculationHistory = new ArrayList<>();
        historyAdapter = new HistoryAdapter(calculationHistory);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHistory.setAdapter(historyAdapter);
    }

    private void initializeTextViewAndEditText() {
        editText = findViewById(id.editText);
        editText.requestFocus();
        editText.setShowSoftInputOnFocus(false);
        textViewResult = findViewById(id.textViewResult);
        originalTextSize = editText.getTextSize();
        horizontalScrollView = findViewById(id.horizontalScrollView);
        editText.setText("");
        textViewResult.setText("");
        editText.setHint("");
        textViewResult.setHint("");
    }

    private void initializeButtons() {
        for (int i = 0; i <= 9; i++) {
            int resId = getResources().getIdentifier("btn" + i, "id", getPackageName());
            findViewById(resId).setOnClickListener(this::onClick);
        }
        for (int i = 0; i <= 9; i++) {
            int resId = getResources().getIdentifier("operator" + i, "id", getPackageName());
            findViewById(resId).setOnClickListener(this::onClick);
        }
    }
    private void initializeAppbarStatusBar() {
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.status_app_bar_background_color));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        initializeHomeAsUpIndicator();
    }
    private void initializeHomeAsUpIndicator() {
        android.graphics.drawable.Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_history_24);
        if (drawable != null) {
            int color = ContextCompat.getColor(this, R.color.overflow_menu_item_text_color);
            drawable.setTint(color);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setHomeAsUpIndicator(drawable);
            }
        }
    }
    private void configureListeners() {
        configureEditTextListener();
        configureTextViewResultListener();
    }

    private void initializeTopSheet() {
        topSheet = findViewById(id.topSheet);
        topSheetBehavior = TopSheetBehavior.from(topSheet);
        topSheetBehavior.setTopSheetCallback(this);
        topSheet.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                topSheet.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                topSheetBehavior.setState(TopSheetBehavior.STATE_COLLAPSED);
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void configureEditTextListener() {
        editText.addTextChangedListener(new DecimalTextWatcher(editText));
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { adjustTextSize((Editable) s); }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { handleTextChange(s); }
        });
        // Permitir drag desde cualquier parte del EditText para controlar el TopSheet
        editText.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    initialTouchY = event.getRawY();
                    return false; // Permitir selección de texto
                case android.view.MotionEvent.ACTION_MOVE:
                    float dy = event.getRawY() - initialTouchY;
                    if (Math.abs(dy) > 40) { // Umbral para detectar drag
                        if (topSheetBehavior != null) {
                            if (dy > 0) {
                                // Drag hacia abajo: expandir TopSheet
                                topSheetBehavior.setState(TopSheetBehavior.STATE_PEEKED);
                            } else {
                                // Drag hacia arriba: colapsar TopSheet
                                topSheetBehavior.setState(TopSheetBehavior.STATE_COLLAPSED);
                            }
                            return true;
                        }
                    }
                    break;
            }
            return false;
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
            case OPERATOR_BACK: PerformOperations.deleteCharAtCursor(editText); adjustTextSizeWhenPressBack(editText.getText()); break;
            case OPERATOR_CLEAR: PerformOperations.performClearOperation(editText, textViewResult, horizontalScrollView); break;
            case OPERATOR_DECIMAL_POINT: InputFormat.checkDecimalPoint(editText, buttonClicked); break;
            case OPERATOR_DIVISION: case OPERATOR_MULTIPLICATION: case OPERATOR_ADDITION: case OPERATOR_SUBTRACTION:
                PerformOperations.insertTextAtCursor(editText, buttonClicked.getText().toString()); break;
            case OPERATOR_MORE_MINUS: PerformOperations.toggleSign(editText); break;
            case OPERATOR_EQUAL:
                if (validOperation && !textViewResult.getText().toString().isEmpty()) {
                    String expression = editText.getText().toString();
                    String result = textViewResult.getText().toString();
                    calculationHistory.add(0, new Calculation(expression, result));
                    historyAdapter.notifyDataSetChanged();
                    PerformOperations.performEqualOperation(editText, textViewResult);
                    resultAnimation();
                }
                break;
        }
    }

    private void resultAnimation() {
        textViewResult.setAlpha(0f);
        float distance = editText.getY() - textViewResult.getY();
        editText.animate().alpha(0f).translationYBy(50f).setDuration(250).setInterpolator(new android.view.animation.AccelerateInterpolator()).start();
        textViewResult.animate().translationY(distance).alpha(1f).setDuration(350).setInterpolator(new android.view.animation.DecelerateInterpolator()).withEndAction(this::resetAfterAnimation).start();
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
        float maxTextSizeSP = 70f, minTextSizeSP = 45f;
        Paint textPaint = editText.getPaint();
        int maxWidth = editText.getWidth();
        if (maxWidth <= 0) return;
        float textWidth = textPaint.measureText(editable.toString());
        if (textWidth > maxWidth) {
            float newSize = textPaint.getTextSize() * (maxWidth / textWidth);
            if (newSize < TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, minTextSizeSP, getResources().getDisplayMetrics())) {
                editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, minTextSizeSP);
            } else {
                editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
            }
        } else {
            editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, maxTextSizeSP);
        }
    }

    private void adjustTextSizeWhenPressBack(Editable editable) {
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

    private void preventLockScreen() {
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onStateChanged(@NonNull View topSheet, int newState) {}

    @Override
    public void onSlide(@NonNull View topSheet, float slideOffset) {
        constraintLayout.setTranslationY(topSheet.getTranslationY() + topSheet.getHeight());
    }
}
