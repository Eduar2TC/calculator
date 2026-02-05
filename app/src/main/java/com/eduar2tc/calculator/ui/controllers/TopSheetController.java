package com.eduar2tc.calculator.ui.controllers;

import android.app.Activity;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.eduar2tc.calculator.R;
import com.eduar2tc.calculator.ui.behavior.TopSheetBehavior;
import com.eduar2tc.calculator.utils.ForwardingHelper;

/**
 * Encapsulates TopSheet init and forwarding behavior (drag forwarding from an EditText to the TopSheet).
 */
public class TopSheetController {
    private final Activity activity;
    private final ConstraintLayout parent;
    private final TopSheetBehavior.TopSheetCallback callback;

    private ConstraintLayout topSheet;
    private TopSheetBehavior<ConstraintLayout> topSheetBehavior;
    private final ForwardingHelper forwardingHelper;

    public TopSheetController(Activity activity, ConstraintLayout parent, TopSheetBehavior.TopSheetCallback callback) {
        this.activity = activity;
        this.parent = parent;
        this.callback = callback;
        this.forwardingHelper = new ForwardingHelper(activity);
    }

    public void attachTopSheet(ConstraintLayout topSheet) {
        this.topSheet = topSheet;
        this.topSheetBehavior = TopSheetBehavior.from(topSheet);
        if (this.topSheetBehavior != null && callback != null) {
            this.topSheetBehavior.setTopSheetCallback(callback);
        }
        topSheet.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                topSheet.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (topSheetBehavior != null) topSheetBehavior.setState(TopSheetBehavior.STATE_COLLAPSED);
                // Expand handle touch area (invisible hit) to ease dragging
                View handle = topSheet.findViewById(R.id.topSheetHandle);
                if (handle != null) {
                    final View parentView = (View) handle.getParent();
                    parentView.post(() -> {
                        Rect rect = new Rect();
                        handle.getHitRect(rect);
                        int extraDp = 20; // configurable if needed
                        int extraPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, extraDp, activity.getResources().getDisplayMetrics());
                        rect.top -= extraPx;
                        rect.bottom += extraPx;
                        rect.left -= extraPx;
                        rect.right += extraPx;
                        parentView.setTouchDelegate(new android.view.TouchDelegate(rect, handle));
                    });
                }
            }
        });
    }

    public void attachEditText(EditText editText) {
        if (topSheet == null || topSheetBehavior == null) return;

        editText.setOnTouchListener(new View.OnTouchListener() {
            float downX = 0f, downY = 0f, downRawY = 0f;
            boolean forwarding = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getActionMasked();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getX();
                        downY = event.getY();
                        downRawY = event.getRawY();
                        forwarding = false;
                        return false; // allow taps/selection initially

                    case MotionEvent.ACTION_MOVE: {
                        float dyRaw = event.getRawY() - downRawY;
                        int state = topSheetBehavior.getState();
                        if (state == TopSheetBehavior.STATE_COLLAPSED && Math.abs(dyRaw) > 24f) {
                            if (dyRaw > 0) topSheetBehavior.setState(TopSheetBehavior.STATE_PEEKED);
                            else topSheetBehavior.setState(TopSheetBehavior.STATE_COLLAPSED);
                            return true;
                        }

                        if (state == TopSheetBehavior.STATE_PEEKED || state == TopSheetBehavior.STATE_EXPANDED) {
                            if (!forwarding && forwardingHelper.shouldStartForwarding(v, dyRaw)) {
                                int[] topLoc = new int[2];
                                topSheet.getLocationOnScreen(topLoc);
                                float mappedDownY = event.getRawY() - topLoc[1];
                                topSheetBehavior.beginForwardDrag(mappedDownY);
                                MotionEvent cancel = MotionEvent.obtain(event.getDownTime(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, event.getX(), event.getY(), event.getMetaState());
                                v.dispatchTouchEvent(cancel);
                                cancel.recycle();
                                forwarding = true;
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                                forwardingHelper.initForwarding(mappedDownY);
                            }

                            if (forwarding) {
                                int[] topLoc = new int[2];
                                topSheet.getLocationOnScreen(topLoc);
                                float mappedY = event.getRawY() - topLoc[1];
                                float smoothed = forwardingHelper.smoothMappedY(mappedY);
                                topSheetBehavior.updateForwardDrag(smoothed);
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
                            float finalSmoothed = forwardingHelper.finalizeAndReset(mappedY);
                            topSheetBehavior.updateForwardDrag(finalSmoothed);
                            topSheetBehavior.endForwardDrag();
                            forwarding = false;
                            forwardingHelper.reset();
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            return true;
                        }
                        break;
                }
                return false;
            }
        });
    }

    public void applySettings(float slopFactor, float minDp, float smoothing) {
        forwardingHelper.setForwardSlopFactor(slopFactor);
        forwardingHelper.setForwardMinDp(minDp);
        forwardingHelper.setForwardSmoothing(smoothing);
    }

    public TopSheetBehavior<ConstraintLayout> getTopSheetBehavior() {
        return topSheetBehavior;
    }
}
