package com.eduar2tc.calculator.ui.controllers;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.EditText;
import android.widget.TextView;

public class AnimationController {
    private final EditText editText;
    private final TextView textViewResult;
    private boolean isAnimating = false;
    private Runnable onAnimationComplete;

    public AnimationController(EditText editText, TextView textViewResult) {
        this.editText = editText;
        this.textViewResult = textViewResult;
    }

    public boolean isAnimating() {
        return isAnimating;
    }

    public void setOnAnimationComplete(Runnable callback) {
        this.onAnimationComplete = callback;
    }

    public void performResultAnimation() {
        if (isAnimating) return;

        // Cancelar animaciones previas
        editText.animate().cancel();
        textViewResult.animate().cancel();

        // Resetear estados
        editText.setAlpha(1f);
        editText.setTranslationY(0f);
        textViewResult.setAlpha(0f);
        textViewResult.setTranslationY(0f);

        isAnimating = true;

        float distance = editText.getY() - textViewResult.getY();

        editText.animate()
                .alpha(0f)
                .translationYBy(50f)
                .setDuration(200)
                .setInterpolator(new AccelerateInterpolator(1.5f))
                .start();

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
            editText.animate().cancel();
            textViewResult.animate().cancel();

            editText.setText(textViewResult.getText());
            editText.setAlpha(1f);
            editText.setTranslationY(0f);
            editText.setSelection(editText.getText().length());

            textViewResult.setText("");
            textViewResult.setAlpha(1f);
            textViewResult.setTranslationY(0f);

            isAnimating = false;

            if (onAnimationComplete != null) {
                onAnimationComplete.run();
            }
        });
    }


    public void cancelAnimations() {
        editText.animate().cancel();
        textViewResult.animate().cancel();
        isAnimating = false;
    }
}