package com.eduar2tc.calculator.ui.activities;

import static com.eduar2tc.calculator.R.*;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eduar2tc.calculator.utils.DecimalTextWatcher;
import com.eduar2tc.calculator.R;
import com.eduar2tc.calculator.adapters.HistoryAdapter;
import com.eduar2tc.calculator.ui.behavior.TopSheetBehavior;
import com.eduar2tc.calculator.ui.controllers.TopSheetController;
import com.eduar2tc.calculator.models.Calculation;
import com.eduar2tc.calculator.utils.CustomDialog;
import com.eduar2tc.calculator.utils.HistoryUtils;
import com.eduar2tc.calculator.utils.InputFormat;
import com.eduar2tc.calculator.utils.PerformOperations;
import com.eduar2tc.calculator.viewmodel.HistoryViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    private ConstraintLayout topSheet;
    private ConstraintLayout constraintLayout;

    private RecyclerView recyclerViewHistory;
    private HistoryAdapter historyAdapter;
    private List<Calculation> calculationHistory;
    private TextView emptyHistoryMessage;

    // Must match EXPANDED_HEIGHT_PERCENTAGE in TopSheetBehavior
    private static final float TOP_SHEET_EXPANDED_PERCENT = 0.6f;

    // Forwarding helper moved inside TopSheetController
    private TopSheetController topSheetController;
    private HistoryViewModel historyVIewModel;

    private boolean isResultAnimating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);
        if (Build.VERSION.SDK_INT >= 26) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
        constraintLayout = findViewById(id.constraintLayout);
        initializeComponents();
        configureListeners();
        preventLockScreen();

        // Initialize TopSheetController and attach the sheet + the EditText
        topSheetController = new TopSheetController(this, constraintLayout, this);
        topSheet = findViewById(id.topSheet);
        topSheetController.attachTopSheet(topSheet);
        // attach editText forwarding (controller handles forwarding)
        editText = findViewById(id.editText);
        topSheetController.attachEditText(editText);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            TopSheetBehavior<ConstraintLayout> b = topSheetController.getTopSheetBehavior();
            if (b != null) {
                if (b.getState() == TopSheetBehavior.STATE_COLLAPSED) b.setState(TopSheetBehavior.STATE_PEEKED);
                else b.setState(TopSheetBehavior.STATE_COLLAPSED);
            }
            return true;
        }
        new CustomDialog(MainActivity.this).onOptionsItemSelected(item, MainActivity.this);
        return super.onOptionsItemSelected(item);
    }

    private void initializeComponents() {
        initializeTextViewAndEditText();
        initializeButtons();
        initializeAppbarStatusBar();
        initializeHistory();
    }

    private void initializeHistory(){
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        emptyHistoryMessage = findViewById(R.id.emptyHistoryMessage);
        calculationHistory = new ArrayList<>();
        historyAdapter = new HistoryAdapter(calculationHistory);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHistory.setAdapter(historyAdapter);
        recyclerViewHistory.setHasFixedSize(false);
        recyclerViewHistory.setNestedScrollingEnabled(false);
        recyclerViewHistory.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        historyVIewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        historyVIewModel.getHistory().observe(this, calculations -> {
            calculationHistory.clear();
            if (calculations != null) calculationHistory.addAll(calculations);
            refreshHistoryUi();
        });
        // removed custom OnTouchListener to avoid accessibility/lint warnings
        refreshHistoryUi();
    }

    private void refreshHistoryUi() {
        if (HistoryUtils.isHistoryEmpty(calculationHistory)) {
            emptyHistoryMessage.setVisibility(View.VISIBLE);
            recyclerViewHistory.setVisibility(View.GONE);
        } else {
            emptyHistoryMessage.setVisibility(View.GONE);
            recyclerViewHistory.setVisibility(View.VISIBLE);
            List<com.eduar2tc.calculator.models.HistoryUiItem> uiItems = HistoryUtils.flattenFromCalculations(calculationHistory, Locale.getDefault());
            historyAdapter.setUiItems(uiItems);
            recyclerViewHistory.post(() -> {
                ViewGroup.LayoutParams lp = recyclerViewHistory.getLayoutParams();
                int parentHeight = constraintLayout.getHeight();
                int expandedVisible = (int) (parentHeight * TOP_SHEET_EXPANDED_PERCENT);
                int handleHeight = 0;
                if (topSheet != null) {
                    View handle = topSheet.findViewById(R.id.topSheetHandle);
                    if (handle != null) handleHeight = handle.getHeight();
                }
                int perItemPx = 0;
                if (recyclerViewHistory.getChildCount() > 0) {
                    perItemPx = recyclerViewHistory.getChildAt(0).getHeight();
                }
                if (perItemPx <= 0) {
                    perItemPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());
                }
                int contentHeight = perItemPx * uiItems.size() + recyclerViewHistory.getPaddingTop() + recyclerViewHistory.getPaddingBottom();
                int maxListHeight = Math.max(0, expandedVisible - handleHeight - (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()));
                if (contentHeight > maxListHeight) {
                    lp.height = maxListHeight;
                } else {
                    lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                }
                recyclerViewHistory.setLayoutParams(lp);
                recyclerViewHistory.requestLayout();
                recyclerViewHistory.invalidate();
            });
        }

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
        // explicit id arrays instead of getIdentifier to avoid reflection warnings
        int[] btnIds = new int[]{R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9};
        for (int resId : btnIds) {
            findViewById(resId).setOnClickListener(this::onClick);
        }
        int[] operatorIds = new int[]{R.id.operator0, R.id.operator1, R.id.operator2, R.id.operator3, R.id.operator4, R.id.operator5, R.id.operator6, R.id.operator7, R.id.operator8, R.id.operator9};
        for (int resId : operatorIds) {
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
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_history_24);
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

    private void configureEditTextListener() {
        editText.addTextChangedListener(new DecimalTextWatcher(editText));
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { adjustTextSize((Editable) s); }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { handleTextChange(s); }
        });
        // Touch forwarding and sheet behaviour handled by TopSheetController
    }

    private void configureTextViewResultListener() {
        textViewResult.setClickable(true);
        textViewResult.setOnClickListener(v -> {
            hideKeyboard(textViewResult);
        });
    }

    private void handleTextChange(Editable editable) {
        if (containsInvalidOperators(editable)) {
            validOperation = false;
        } else {
            String expression = editable.toString().replace(",", "");
            if (!expression.isEmpty() && PerformOperations.containsOperator(expression)) {
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
                if( isResultAnimating ) return;
                if (validOperation && !textViewResult.getText().toString().isEmpty()) {
                    String expression = editText.getText().toString();
                    String result = textViewResult.getText().toString();
                    historyVIewModel.addCalculation(expression, result);
                    PerformOperations.performEqualOperation(editText, textViewResult);
                    resultAnimation();
                }
                break;
        }
    }

    private void resultAnimation() {
        isResultAnimating = true;
        textViewResult.setAlpha(0f);
        float distance = editText.getY() - textViewResult.getY();

        // Animación del editText saliendo
        editText.animate()
                .alpha(0f)
                .translationYBy(50f)
                .setDuration(200)
                .setInterpolator(new AccelerateInterpolator(1.5f))
                .start();

        // Animación del resultado entrando
        textViewResult.animate()
                .translationY(distance)
                .alpha(1f)
                .setDuration(250)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(this::resetAfterAnimation)
                .start();
    }

    private void resetAfterAnimation() {
        editText.post(() -> {
            // Cancelar animaciones
            editText.animate().cancel();
            textViewResult.animate().cancel();

            // Mover el resultado al editText
            editText.setText(textViewResult.getText());
            editText.setAlpha(1f);
            editText.setTranslationY(0f);
            editText.setSelection(editText.getText().length());

            // Limpiar el textViewResult
            textViewResult.setText("");
            textViewResult.setAlpha(1f);
            textViewResult.setTranslationY(0f);

            isResultAnimating = false;
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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onStateChanged(@NonNull View topSheet, int newState) {}

    @Override
    public void onSlide(@NonNull View topSheet, float slideOffset) {
        constraintLayout.setTranslationY(topSheet.getTranslationY() + topSheet.getHeight());
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
           SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            float slop = prefs.getFloat(SettingsActivity.PREF_FORWARD_SLOP, 0.5f);
            float minDp = prefs.getFloat(SettingsActivity.PREF_FORWARD_MIN_DP, 8f);
            float smoothing = prefs.getFloat(SettingsActivity.PREF_FORWARD_SMOOTHING, 0.85f);
            if (topSheetController != null) {
                topSheetController.applySettings(slop, minDp, smoothing);
            }
        } catch (Exception e) {
           Log.w("MainActivity", "Failed to apply settings prefs: " + e.getMessage());
        }
    }
}
