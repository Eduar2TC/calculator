package com.eduar2tc.calculator.utils;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Helper to centralize thresholds and smoothing for gesture "forwarding" toward TopSheetBehavior.
 * Extracts constants and state (last mapped coordinate) out of MainActivity for modularity.
 */
public class ForwardingHelper {
    // Default values (previously constants); now configurable fields.
    private static final float DEFAULT_FORWARD_SLOP_FACTOR = 0.5f; // adjust between 0.1 - 2.0 depending on desired sensitivity
    private static final float DEFAULT_FORWARD_MIN_DP = 8f; // value in dp
    private static final float DEFAULT_FORWARD_SMOOTHING = 0.85f; // adjust between 0.5 - 0.95

    private final Context context;
    // Configurable fields that MainActivity/Settings can modify
    private float forwardSlopFactor = DEFAULT_FORWARD_SLOP_FACTOR;
    private float forwardMinDp = DEFAULT_FORWARD_MIN_DP;
    private float forwardSmoothing = DEFAULT_FORWARD_SMOOTHING;

    // Field to remember the last mapped coordinate and apply smoothing
    private float lastForwardMappedY = Float.NaN;

    public ForwardingHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    /**
     * Decide whether the raw movement (dyRaw) exceeds the combined thresholds (slop * factor) and an absolute minimum.
     */
    public boolean shouldStartForwarding(View v, float dyRaw) {
        int slop = ViewConfiguration.get(v.getContext()).getScaledTouchSlop();
        float forwardThreshold = Math.max(1f, slop * forwardSlopFactor);
        float rawThresholdPx = dpToPx(forwardMinDp);
        return Math.abs(dyRaw) > forwardThreshold && Math.abs(dyRaw) > rawThresholdPx;
    }

    /**
     * Initialize smoothing with the mapped coordinate where forwarding starts.
     */
    public void initForwarding(float mappedY) {
        lastForwardMappedY = mappedY;
    }

    /**
     * Apply simple exponential smoothing and return the smoothed value.
     */
    public float smoothMappedY(float mappedY) {
        if (Float.isNaN(lastForwardMappedY)) lastForwardMappedY = mappedY;
        float smoothed = lastForwardMappedY * (1 - forwardSmoothing) + mappedY * forwardSmoothing;
        lastForwardMappedY = smoothed;
        return smoothed;
    }

    /**
     * Finalize forwarding by applying a last smoothing step and resetting the state.
     */
    public float finalizeAndReset(float mappedY) {
        if (Float.isNaN(lastForwardMappedY)) lastForwardMappedY = mappedY;
        float finalSmoothed = lastForwardMappedY * (1 - forwardSmoothing) + mappedY * forwardSmoothing;
        lastForwardMappedY = Float.NaN;
        return finalSmoothed;
    }

    public void reset() {
        lastForwardMappedY = Float.NaN;
    }

    // --- Getters / Setters to allow configuration from Settings/MainActivity ---
    public void setForwardSlopFactor(float forwardSlopFactor) {
        if (forwardSlopFactor > 0f) this.forwardSlopFactor = forwardSlopFactor;
    }

    public float getForwardSlopFactor() {
        return forwardSlopFactor;
    }

    public void setForwardMinDp(float forwardMinDp) {
        if (forwardMinDp >= 0f) this.forwardMinDp = forwardMinDp;
    }

    public float getForwardMinDp() {
        return forwardMinDp;
    }

    public void setForwardSmoothing(float forwardSmoothing) {
        if (forwardSmoothing >= 0f && forwardSmoothing <= 1f) this.forwardSmoothing = forwardSmoothing;
    }

    public float getForwardSmoothing() {
        return forwardSmoothing;
    }
}
