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

import com.eduar2tc.calculator.utils.CustomThemeDialog;
import com.eduar2tc.calculator.utils.InputFormat;
import com.eduar2tc.calculator.utils.PerformOperations;
import com.eduar2tc.calculator.R;

public class MainActivity extends AppCompatActivity {
    private EditText editText; //result view
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



    public static int getMaxLengthForTextView(EditText textView) {
        int maxLength = 0;

        for (InputFilter filter : textView.getFilters()) {
            if (filter instanceof InputFilter.LengthFilter) {
               maxLength =  ((InputFilter.LengthFilter) filter).getMax();
            }
        }
        return maxLength;
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);
        initializeComponents();
        configureListeners();
    }
    private void initializeComponents() {
        editText = findViewById(id.editText);
        editText.requestFocus();
        textViewResult = findViewById(id.textViewResult);
        originalTextSize = editText.getTextSize();
        horizontalScrollView = findViewById(id.horizontalScrollView);
        MenuItem item = findViewById(id.theme_item);

        //btn list of numbers
        Button[] arrayListBtn = new Button[10];
        //Operators
        //btn list of numbers
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        new CustomThemeDialog(MainActivity.this).onOptionsItemSelected(item, MainActivity.this);
        return super.onOptionsItemSelected(item);
    }
    @SuppressLint("ClickableViewAccessibility")
    private void configureListeners() {
        //handle scroll content
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence editable, int start, int count, int after) {
                //works fine in scroll
                adjustTextSize((Editable) editable);
            }

            @Override
            public void onTextChanged(CharSequence editable, int start, int before, int count) {
                //adjust text all width not works with scroll
            }
            //handle input format
            @Override
            public void afterTextChanged(Editable editable) {
                if(containsInvalidOperators(editable)) {
                    // remove invalid operators
                    validOperation = false;
                }
                else {
                    // valid operation
                    //TODO: if exist an operation with minimal one operator then show it in result
                    //listen dynamic valid operation
                    if(editable.length() > 1){
                        String result = PerformOperations.performOperation(editable.toString());
                        textViewResult.setText(result);
                        validOperation = true;
                    }
                    else{
                        textViewResult.setText("");
                    }
                }
            }
            //count operators repeated
            boolean containsInvalidOperators(Editable editable) {
                String last2Chars = "";

                if (editable.length() >= 2) {
                    last2Chars = editable.subSequence(editable.length() - 2, editable.length()).toString();
                }

                if (last2Chars.matches("([+\\-×÷])\\1") || last2Chars.matches("([+\\-×÷])[+\\-×÷]")) {
                    // Remover repetido o consecutivo
                    editable.delete(editable.length() - 1, editable.length());
                    return true;
                } else if (isLastCharOperator(editable)) {
                    return true;
                }

                return false;
            }

            private boolean isLastCharOperator(Editable editable) {
                String lastChar = editable.length() > 0 ? editable.subSequence(editable.length() - 1, editable.length()).toString() : "";
                return lastChar.matches("[+\\-×÷.]");
            }

        });

        //handle keyboard visualization
        editText.setOnTouchListener((v, event) -> {
            int offset = editText.getOffsetForPosition(event.getX(), event.getY());
            Selection.setSelection(editText.getText(), offset);
            hideKeyboard(editText);
            return true;
        });
        textViewResult.setOnTouchListener((v, event) -> {
            hideKeyboard(textViewResult);
            return true;
        });
    }
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    @SuppressLint("NonConstantResourceId")
    public void onClick(View btn){

        Button buttonClicked = (Button) btn;
        InputFormat.checkIsNumeric( editText, buttonClicked );
        performOperation(buttonClicked, editText, textViewResult);

    }
    @SuppressLint("NonConstantResourceId")
    public void performOperation(Button buttonClicked, EditText editText, TextView textViewResult){

        switch ( buttonClicked.getId() ){
            case OPERATOR_BACK: { //Back
                PerformOperations.performBackOperation(editText, textViewResult);
                adjustTextSizeWhenPressBack(editText.getText());
                break;
            }
            case OPERATOR_CLEAR: { //Clear
                PerformOperations.performClearOperation(editText, textViewResult, horizontalScrollView);
                break;
            }
            case OPERATOR_DECIMAL_POINT: { // .
                InputFormat.checkDecimalPoint( this.editText, buttonClicked );
                break;
            }
            case OPERATOR_DIVISION:
            case OPERATOR_MULTIPLICATION:
            case OPERATOR_ADDITION:
            case OPERATOR_SUBTRACTION: {
                PerformOperations.appendOperation( this.editText, buttonClicked.getText().toString());
                break;
            }// /,x,-,+
            case OPERATOR_MORE_MINUS: { // +/-
                PerformOperations.toggleSign( this.editText );
                break;
            }
            case OPERATOR_EQUAL:{ // =
                if(validOperation){
                    PerformOperations.performEqualOperation(this.editText, this.textViewResult);
                    resultAnimation(editText, textViewResult);
                }
                break;
            }
            default:{
                //this.textViewResult.setText( expr.solve().toString() );
                break;
            }
        }
    }

    private void resultAnimation(EditText editText, TextView textViewResult) {
        // Animation to move the EditText upwards
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(editText, "translationY", 0f, -500f);

        // Calculate the final Y position relative to textViewResult's base
        float finalY = editText.getY() - textViewResult.getY() + editText.getHeight() - textViewResult.getHeight();

        // Fade-out animation for the EditText
        ObjectAnimator fadeOutEditText = ObjectAnimator.ofFloat(editText, "alpha", 1f, 0f);

        // Fade-out animation for the TextView
        ObjectAnimator fadeOutTextView = ObjectAnimator.ofFloat(textViewResult, "alpha", 1f, 1f);

        // Move TextView to the final position animation
        ObjectAnimator moveToFinalY = ObjectAnimator.ofFloat(textViewResult, "translationY", 0f, finalY);

        // Text size growth animation for the TextView
        ObjectAnimator textSizeGrow = ObjectAnimator.ofFloat(textViewResult, "textSize", textViewResult.getTextSize(), 70f);

        // Color animation from gray to white for the TextView
        ObjectAnimator textColorAnimation = ObjectAnimator.ofArgb(textViewResult, "textColor",
                ContextCompat.getColor(this, R.color.text_view_color),
                ContextCompat.getColor(this, R.color.edit_text_color));

        AnimatorSet set = new AnimatorSet();
        set.setDuration(350);

        // Play all animations simultaneously
        set.playTogether(fadeOutEditText, fadeOutTextView, moveToFinalY, anim1, textSizeGrow, textColorAnimation);

        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animator) {
                // Replace the editText's text with textViewResult's text
                editText.setText(textViewResult.getText());
                // Restore original properties
                editText.setAlpha(1f);
                textViewResult.setAlpha(1f);
                textViewResult.setTextSize(38f); // Set text size to 34sp
                textViewResult.setTranslationY(0f);
                textViewResult.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.text_view_color));
                editText.setTranslationY(0f);
                editText.setSelection(editText.getText().length());
                textViewResult.setText("");
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        set.start();
    }

    //dynamic adjust text size when resize editText
    private void adjustTextSize(Editable editable) {
        Paint textPaint = editText.getPaint();
        int maxWidth = editText.getWidth();
        int maxDigits = (int) (maxWidth / textPaint.measureText("0"));
        int currentLength = editable.length();

        if (currentLength > maxDigits) {
            float newTextSize = textPaint.getTextSize() * ((float) maxDigits / currentLength);
            editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, Math.max(newTextSize, 12f));
            editText.setMovementMethod(new ScrollingMovementMethod());  // Ability scroll displacement
        }
    }

    //dynamic adjust text size when press back adjust Text Size When Press Back
    private void adjustTextSizeWhenPressBack(Editable editable) {
        Paint textPaint = editText.getPaint();
        int maxWidth = editText.getWidth();
        int maxDigits = (int) (maxWidth / textPaint.measureText("0"));
        int currentLength = editable.length();

        float newTextSize;
        if (currentLength <= maxDigits) {
            restoreOriginalTextSize();  // Restore the original size when the length is less than or equal to the maximum
        } else {
            float ratio = (float) currentLength / maxDigits;
            newTextSize = editText.getTextSize() * ratio;
            editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);
        }
    }

    private void restoreOriginalTextSize() {
        float currentTextSize = editText.getTextSize();
        if (currentTextSize < originalTextSize) {
            float newSize = currentTextSize + (originalTextSize - currentTextSize) * 0.1f;  // (0.1f) Adapts the speed of restoration
            editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
        }
    }


}