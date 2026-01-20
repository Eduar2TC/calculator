package com.eduar2tc.calculator.activities;

import static com.eduar2tc.calculator.R.*;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.graphics.Rect;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
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
import com.eduar2tc.calculator.utils.HistoryUtils;
import com.eduar2tc.calculator.utils.InputFormat;
import com.eduar2tc.calculator.utils.PerformOperations;

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

    private TopSheetBehavior<ConstraintLayout> topSheetBehavior;
    private ConstraintLayout topSheet;
    private ConstraintLayout constraintLayout;

    private RecyclerView recyclerViewHistory;
    private HistoryAdapter historyAdapter;
    private List<Calculation> calculationHistory;
    private TextView emptyHistoryMessage;

    private float initialTouchY = 0;
    private boolean isDraggingTopSheet = false;

    // Debe coincidir con EXPANDED_HEIGHT_PERCENTAGE en TopSheetBehavior
    private static final float TOP_SHEET_EXPANDED_PERCENT = 0.6f;

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
        emptyHistoryMessage = findViewById(R.id.emptyHistoryMessage);
        calculationHistory = new ArrayList<>();
        historyAdapter = new HistoryAdapter(calculationHistory);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        // Permitir que RecyclerView mida su contenido cuando tenga pocos items
        recyclerViewHistory.setHasFixedSize(false);
        // Desactivar nested scrolling y usar requestDisallowInterceptTouchEvent desde el propio RecyclerView
        recyclerViewHistory.setNestedScrollingEnabled(false);
        recyclerViewHistory.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        recyclerViewHistory.setAdapter(historyAdapter);
        // OnTouchListener que evita que el parent intercepte el gesto cuando la lista puede desplazarse
        recyclerViewHistory.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    initialTouchY = event.getY();
                    // dejar pasar el evento al RecyclerView
                    break;
                case android.view.MotionEvent.ACTION_MOVE:
                    float dy = event.getY() - initialTouchY;
                    if ((dy < 0 && recyclerViewHistory.canScrollVertically(1)) || (dy > 0 && recyclerViewHistory.canScrollVertically(-1))) {
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                    } else {
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                    }
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return false;
        });
        refreshHistoryUi();
    }

    private void refreshHistoryUi() {
        if (HistoryUtils.isHistoryEmpty(calculationHistory)) {
            emptyHistoryMessage.setVisibility(View.VISIBLE);
            recyclerViewHistory.setVisibility(View.GONE);
        } else {
            emptyHistoryMessage.setVisibility(View.GONE);
            recyclerViewHistory.setVisibility(View.VISIBLE);
            // agrupar y preparar UI items
            List<com.eduar2tc.calculator.model.HistoryUiItem> uiItems = HistoryUtils.flattenFromCalculations(calculationHistory, Locale.getDefault());
            historyAdapter.setUiItems(uiItems);
            recyclerViewHistory.post(() -> {
                ViewGroup.LayoutParams lp = recyclerViewHistory.getLayoutParams();
                // calcular altura máxima disponible para la lista basada en el porcentaje de expansión del TopSheet
                int parentHeight = constraintLayout.getHeight();
                int expandedVisible = (int) (parentHeight * TOP_SHEET_EXPANDED_PERCENT);

                // restar alturas de elementos no listables dentro del sheet (handle, margins, paddings)
                int handleHeight = 0;
                if (topSheet != null) {
                    View handle = topSheet.findViewById(R.id.topSheetHandle);
                    if (handle != null) handleHeight = handle.getHeight();
                }

                // intentar obtener la altura real de un item (si ya fue medido)
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
                // Asegurar que el RecyclerView mida/porte los cambios para que sea scrollable
                recyclerViewHistory.requestLayout();
                recyclerViewHistory.invalidate();
                historyAdapter.notifyDataSetChanged();
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

    @SuppressLint("ClickableViewAccessibility")
    private void initializeTopSheet() {
        topSheet = findViewById(id.topSheet);
        topSheetBehavior = TopSheetBehavior.from(topSheet);
        topSheetBehavior.setTopSheetCallback(this);
        topSheet.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                topSheet.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                topSheetBehavior.setState(TopSheetBehavior.STATE_COLLAPSED);
                // Ampliar área táctil del handle (hit invisible) para facilitar el drag
                View handle = topSheet.findViewById(R.id.topSheetHandle);
                if (handle != null) {
                    final View parent = (View) handle.getParent();
                    parent.post(() -> {
                        Rect rect = new Rect();
                        handle.getHitRect(rect);
                        int extraDp = 20; // ajustar si hace falta
                        int extraPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, extraDp, getResources().getDisplayMetrics());
                        rect.top -= extraPx;
                        rect.bottom += extraPx;
                        rect.left -= extraPx;
                        rect.right += extraPx;
                        parent.setTouchDelegate(new android.view.TouchDelegate(rect, handle));
                    });
                }
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
        // Mantener gestos existentes y permitir arrastre continuo desde EditText tanto en PEEKED como EXPANDED
        editText.setOnTouchListener(new View.OnTouchListener() {
            float downX = 0f, downY = 0f, downRawY = 0f;
            boolean forwarding = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (topSheetBehavior == null || topSheet == null) return false;
                int action = event.getActionMasked();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getX();
                        downY = event.getY();
                        downRawY = event.getRawY();
                        forwarding = false;
                        return false; // permitir taps/selección inicialmente

                    case MotionEvent.ACTION_MOVE: {
                        float dyRaw = event.getRawY() - downRawY;
                        int state = topSheetBehavior.getState();
                        // comportamiento histórico: umbral reducido (24px) -> setState pero SOLO si estamos COLLAPSED
                        if (state == TopSheetBehavior.STATE_COLLAPSED && Math.abs(dyRaw) > 24f) {
                            if (dyRaw > 0) topSheetBehavior.setState(TopSheetBehavior.STATE_PEEKED);
                            else topSheetBehavior.setState(TopSheetBehavior.STATE_COLLAPSED);
                            return true;
                        }

                        // Forward continuous drag when sheet is PEEKED or EXPANDED
                        if (state == TopSheetBehavior.STATE_PEEKED || state == TopSheetBehavior.STATE_EXPANDED) {
                            float dx = event.getX() - downX;
                            float dy = event.getY() - downY;
                            float slop = ViewConfiguration.get(v.getContext()).getScaledTouchSlop();
                            // reducir el umbral para iniciar forwarding: movimientos más cortos empiezan a arrastrar
                            float forwardThreshold = Math.max(1f, slop * 0.15f);

                            if (!forwarding && Math.abs(dyRaw) > forwardThreshold) {
                                // iniciar forwarding usando la API de coordenadas (más segura)
                                int[] topLoc = new int[2];
                                topSheet.getLocationOnScreen(topLoc);
                                float mappedDownY = event.getRawY() - topLoc[1];
                                topSheetBehavior.beginForwardDrag(mappedDownY);
                                // cancelar el touch original del EditText para evitar que procese el gesto
                                MotionEvent cancel = MotionEvent.obtain(event.getDownTime(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, event.getX(), event.getY(), event.getMetaState());
                                v.dispatchTouchEvent(cancel);
                                cancel.recycle();
                                forwarding = true;
                                // evitar que el parent intercepte mientras reenviamos
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                             }

                            if (forwarding) {
                                int[] topLoc = new int[2];
                                topSheet.getLocationOnScreen(topLoc);
                                float mappedY = event.getRawY() - topLoc[1];
                                topSheetBehavior.updateForwardDrag(mappedY);
                                return true; // consume while forwarding
                            }
                         }
                         break;
                     }

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (forwarding) {
                            int[] topLoc = new int[2];
                            topSheet.getLocationOnScreen(topLoc);
                            float mappedY = event.getRawY() - topLoc[1];
                            topSheetBehavior.updateForwardDrag(mappedY);
                            topSheetBehavior.endForwardDrag();
                            forwarding = false;
                            // permitir que el parent vuelva a interceptar eventos
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            return true;
                        }
                         break;
                }
                return false; // default: let EditText handle taps/selection
            }
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
                    calculationHistory.add(0, new Calculation(expression, result, System.currentTimeMillis()));
                    // actualizar UI agrupada
                    refreshHistoryUi();
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