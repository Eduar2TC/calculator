package com.eduar2tc.calculator;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.EditText;

class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
    float startX = 0;
    private final EditText editText;
    MyGestureListener(EditText editText) {
        this.editText = editText;
    }
    @Override
    public boolean onDown(MotionEvent e) {
        startX = e.getX();  // Guardar la posición de inicio del desplazamiento
        return super.onDown(e);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float deltaX = e2.getX() - startX;

        // Establecer un umbral de desplazamiento para activar el desplazamiento horizontal
        if (Math.abs(deltaX) > 50) {
            editText.setHorizontalScrollBarEnabled(true);
        }

        return super.onFling(e1, e2, velocityX, velocityY);
    }
}

