package com.eduar2tc.calculator;

import android.content.Context;
import android.content.res.Resources;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.widget.TextViewCompat;

import org.jetbrains.annotations.NotNull;

import kotlin.jvm.internal.Intrinsics;
import kotlin.math.MathKt;
import kotlin.text.StringsKt;

public final class EditTextAutoSizeUtility2 {
    private static final float AUTOSIZE_EDITTEXT_MINTEXTSIZE_SP = 30.0F;
    private static final float AUTOSIZE_EDITTEXT_STEPSIZE_GRANULARITY_SP = 1.0F;
    @NotNull
    public static final EditTextAutoSizeUtility2 INSTANCE;

    @NotNull
    public final FrameLayout setupAutoResize(@NotNull final EditText editText, @NotNull Context context) {
        Intrinsics.checkNotNullParameter(editText, "editText");
        Intrinsics.checkNotNullParameter(context, "context");
        FrameLayout container = new FrameLayout(context);
        ViewGroup.LayoutParams orgLayoutParams = editText.getLayoutParams();
        ViewParent viewParent = editText.getParent();
        if (!(viewParent instanceof ViewGroup)) {
            viewParent = null;
        }

        ViewGroup viewGroup = (ViewGroup)viewParent;
        if (viewGroup != null) {
            ViewGroup notNullviewGroup = viewGroup;
            boolean var7 = false;
            int var8 = notNullviewGroup.indexOfChild((View)editText);
            boolean var10 = false;
            notNullviewGroup.removeViewAt(var8);
            container.addView((View)editText, (ViewGroup.LayoutParams)(new android.widget.FrameLayout.LayoutParams(-1, -1)));
            notNullviewGroup.addView((View)container, var8, orgLayoutParams);
        }

        final TextView textView = this.createAutoSizeHelperTextView(editText, context);
        container.addView((View)textView, 0, editText.getLayoutParams());
        editText.addTextChangedListener((TextWatcher)(new TextWatcher() {
            private final float originalTextSize = editText.getTextSize();

            public final float getOriginalTextSize() {
                return this.originalTextSize;
            }

            public void onTextChanged(@Nullable final CharSequence s, int start, int before, int count) {
                textView.setText((CharSequence)(s != null ? s.toString() : null), TextView.BufferType.EDITABLE);
                editText.post((Runnable)(new Runnable() {
                    public final void run() {
                        CharSequence charSequence = s;
                        float originalSize;
                        if (charSequence == null || StringsKt.isBlank(charSequence)) {
                            originalSize = getOriginalTextSize();
                        } else {
                            float autosize = textView.getTextSize();
                            originalSize = autosize;
                            Log.i("--->", String.valueOf(textView.getTextSize()));
                        }

                        float optimalSize = originalSize;
                        editText.setTextSize(0, optimalSize);
                    }
                }));
            }
            public void beforeTextChanged(@Nullable CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(@Nullable Editable editable) {
            }
        }));
        return container;
    }

    private final TextView createAutoSizeHelperTextView(EditText editText, Context context) {
        TextView textView = new TextView(context);
        boolean var5 = false;
        textView.setMaxLines(1);
        textView.setVisibility(View.INVISIBLE);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textView, INSTANCE.spToPx(context, AUTOSIZE_EDITTEXT_MINTEXTSIZE_SP ), MathKt.roundToInt(editText.getTextSize()), INSTANCE.spToPx(context, AUTOSIZE_EDITTEXT_STEPSIZE_GRANULARITY_SP), 0);
        textView.setPadding(editText.getPaddingLeft(), editText.getPaddingTop(), editText.getPaddingRight(), editText.getPaddingBottom());
        return textView;
    }

    public final int spToPx(@NotNull Context context, float sp) {
        Intrinsics.checkNotNullParameter(context, "context");
        Resources resources = context.getResources();
        Intrinsics.checkNotNullExpressionValue(resources, "context.resources");
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.getDisplayMetrics());
    }

    private EditTextAutoSizeUtility2() {
    }

    static {
        INSTANCE = new EditTextAutoSizeUtility2();
    }
}