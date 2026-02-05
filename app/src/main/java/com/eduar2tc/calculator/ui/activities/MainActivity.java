package com.eduar2tc.calculator.ui.activities;

import static com.eduar2tc.calculator.R.*;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
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
import androidx.recyclerview.widget.RecyclerView;

import com.eduar2tc.calculator.R;
import com.eduar2tc.calculator.ui.behavior.TopSheetBehavior;
import com.eduar2tc.calculator.ui.controllers.AnimationController;
import com.eduar2tc.calculator.ui.controllers.HistoryUIController;
import com.eduar2tc.calculator.ui.controllers.TextSizeController;
import com.eduar2tc.calculator.ui.controllers.TopSheetController;
import com.eduar2tc.calculator.ui.validators.OperationValidator;
import com.eduar2tc.calculator.ui.dialogs.CustomDialog;
import com.eduar2tc.calculator.utils.DecimalTextWatcher;
import com.eduar2tc.calculator.utils.InputFormat;
import com.eduar2tc.calculator.utils.PerformOperations;
import com.eduar2tc.calculator.viewmodel.HistoryViewModel;

public class MainActivity extends AppCompatActivity implements TopSheetBehavior.TopSheetCallback {

    // UI Components
    private EditText editText;
    private TextView textViewResult;
    private HorizontalScrollView horizontalScrollView;
    private ConstraintLayout topSheet;
    private ConstraintLayout constraintLayout;

    // Controllers
    private TopSheetController topSheetController;
    private AnimationController animationController;
    private TextSizeController textSizeController;
    private HistoryUIController historyUIController;

    // ViewModel
    private HistoryViewModel historyViewModel;

    // State
    private boolean validOperation = false;
    private String lastValidResult = "";

    // Operator IDs
    private static final int OPERATOR_BACK = id.operator0;
    private static final int OPERATOR_CLEAR = id.operator1;
    private static final int OPERATOR_PERCENTAGE = id.operator2; //TODO: implement
    private static final int OPERATOR_DIVISION = id.operator3;
    private static final int OPERATOR_MULTIPLICATION = id.operator4;
    private static final int OPERATOR_SUBTRACTION = id.operator5;
    private static final int OPERATOR_ADDITION = id.operator6;
    private static final int OPERATOR_EQUAL = id.operator7;
    private static final int OPERATOR_DECIMAL_POINT = id.operator8;
    private static final int OPERATOR_MORE_MINUS = id.operator9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);

        setupSystemUI();
        initializeViews();
        initializeControllers();
        initializeHistory();
        configureListeners();
        preventLockScreen();
    }

    private void setupSystemUI() {
        if (Build.VERSION.SDK_INT >= 26) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.status_app_bar_background_color));
        setupActionBar();
    }

    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");

            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_history_24);
            if (drawable != null) {
                int color = ContextCompat.getColor(this, R.color.overflow_menu_item_text_color);
                drawable.setTint(color);
                getSupportActionBar().setHomeAsUpIndicator(drawable);
            }
        }
    }

    private void initializeViews() {
        constraintLayout = findViewById(id.constraintLayout);
        topSheet = findViewById(id.topSheet);
        editText = findViewById(id.editText);
        textViewResult = findViewById(id.textViewResult);
        horizontalScrollView = findViewById(id.horizontalScrollView);

        editText.requestFocus();
        editText.setShowSoftInputOnFocus(false);
        editText.setText("");
        textViewResult.setText("");
    }

    private void initializeControllers() {
        // Animation Controller
        animationController = new AnimationController(editText, textViewResult);

        // Text Size Controller
        textSizeController = new TextSizeController(editText);

        // TopSheet Controller
        topSheetController = new TopSheetController(this, constraintLayout, this);
        topSheetController.attachTopSheet(topSheet);
        topSheetController.attachEditText(editText);
    }

    private void initializeHistory() {
        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewHistory);
        TextView emptyMessage = findViewById(R.id.emptyHistoryMessage);

        historyUIController = new HistoryUIController(
                this,
                recyclerView,
                emptyMessage,
                constraintLayout,
                topSheet
        );

        historyViewModel.getHistory().observe(this, calculations -> {
            historyUIController.updateHistory(calculations);
        });
    }

    private void configureListeners() {
        // EditText listeners
        editText.addTextChangedListener(new DecimalTextWatcher(editText));
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                textSizeController.adjustTextSize((Editable) s);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                handleTextChange(s);
            }
        });

        // TextView Result listener
        textViewResult.setClickable(true);
        textViewResult.setOnClickListener(v -> hideKeyboard(textViewResult));

        // Button listeners
        initializeButtons();
    }

    private void initializeButtons() {
        int[] btnIds = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9};
        int[] operatorIds = {R.id.operator0, R.id.operator1, R.id.operator2, R.id.operator3,
                R.id.operator4, R.id.operator5, R.id.operator6, R.id.operator7,
                R.id.operator8, R.id.operator9};

        for (int id : btnIds) findViewById(id).setOnClickListener(this::onClick);
        for (int id : operatorIds) findViewById(id).setOnClickListener(this::onClick);
    }

    private void handleTextChange(Editable editable) {
        OperationValidator.ValidationResult result = OperationValidator.validate(editable, lastValidResult);
        validOperation = result.isValid;
        if (result.isValid && result.result != null && !result.result.isEmpty()) {
            lastValidResult = result.result;
        }
        textViewResult.setText(result.result != null ? result.result : "");
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View btn) {
        Button buttonClicked = (Button) btn;
        InputFormat.checkIsNumeric(editText, buttonClicked);
        performOperation(buttonClicked);
    }

    @SuppressLint("NonConstantResourceId")
    public void performOperation(Button buttonClicked) {
        int id = buttonClicked.getId();

        if (id == OPERATOR_BACK) {
            PerformOperations.deleteCharAtCursor(editText);
            textSizeController.adjustTextSizeWhenPressBack(editText.getText());
        } else if (id == OPERATOR_CLEAR) {
            PerformOperations.performClearOperation(editText, textViewResult, horizontalScrollView);
        } else if (id == OPERATOR_DECIMAL_POINT) {
            InputFormat.checkDecimalPoint(editText, buttonClicked);
        } else if (id == OPERATOR_DIVISION || id == OPERATOR_MULTIPLICATION ||
                id == OPERATOR_ADDITION || id == OPERATOR_SUBTRACTION) {
            PerformOperations.insertTextAtCursor(editText, buttonClicked.getText().toString());
        } else if (id == OPERATOR_MORE_MINUS) {
            PerformOperations.toggleSign(editText);
        } else if (id == OPERATOR_EQUAL) {
            handleEqualOperation();
        }
    }

    private void handleEqualOperation() {
        if (animationController.isAnimating()) return;

        if (validOperation && !textViewResult.getText().toString().isEmpty()) {
            String expression = editText.getText().toString();
            String result = textViewResult.getText().toString();

            historyViewModel.addCalculation(expression, result);
            PerformOperations.performEqualOperation(editText, textViewResult);
            animationController.performResultAnimation();
            lastValidResult = ""; //reset last valid result
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void preventLockScreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
        new CustomDialog(this).onOptionsItemSelected(item, this);
        return super.onOptionsItemSelected(item);
    }

    private void toggleTopSheet() {
        TopSheetBehavior<ConstraintLayout> behavior = topSheetController.getTopSheetBehavior();
        if (behavior != null) {
            int newState = behavior.getState() == TopSheetBehavior.STATE_COLLAPSED
                    ? TopSheetBehavior.STATE_PEEKED
                    : TopSheetBehavior.STATE_COLLAPSED;
            behavior.setState(newState);
        }
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
        loadSettings();
    }

    private void loadSettings() {
        try {
            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            float slop = prefs.getFloat(SettingsActivity.PREF_FORWARD_SLOP, 0.5f);
            float minDp = prefs.getFloat(SettingsActivity.PREF_FORWARD_MIN_DP, 8f);
            float smoothing = prefs.getFloat(SettingsActivity.PREF_FORWARD_SMOOTHING, 0.85f);

            if (topSheetController != null) {
                topSheetController.applySettings(slop, minDp, smoothing);
            }
        } catch (Exception e) {
            Log.w("MainActivity", "Failed to load settings: " + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (animationController != null) {
            animationController.cancelAnimations();
        }
    }
}