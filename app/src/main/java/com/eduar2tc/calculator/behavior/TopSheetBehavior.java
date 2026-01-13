package com.eduar2tc.calculator.behavior;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.eduar2tc.calculator.R;
import com.google.android.material.R.styleable;

import java.lang.ref.WeakReference;

public class TopSheetBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

    public static final int STATE_COLLAPSED = 1;
    public static final int STATE_EXPANDED = 2;
    public static final int STATE_PEEKED = 3;

    private static final float EXPANDED_HEIGHT_PERCENTAGE = 0.6f;

    private int mState = STATE_COLLAPSED;

    private float mInitialY;
    private int mTouchSlop;
    private boolean mIsDragging;

    private int mMinOffset;
    private int mMaxOffset;
    private int mPeekOffset;
    private int mPeekHeight;

    private WeakReference<V> mViewRef;
    private View mHandle;
    private TopSheetCallback mCallback;

    public interface TopSheetCallback {
        void onStateChanged(@NonNull View topSheet, int newState);
        void onSlide(@NonNull View topSheet, float slideOffset);
    }

    public TopSheetBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, styleable.BottomSheetBehavior_Layout);
            mPeekHeight = a.getDimensionPixelSize(styleable.BottomSheetBehavior_Layout_behavior_peekHeight, 0);
            a.recycle();
        }
    }

    @Override
    public boolean onLayoutChild(@NonNull CoordinatorLayout parent, @NonNull V child, int layoutDirection) {
        if (mViewRef == null) {
            mViewRef = new WeakReference<>(child);
            mHandle = child.findViewById(R.id.topSheetHandle);
        }
        parent.onLayoutChild(child, layoutDirection);

        int expandedHeight = (int) (parent.getHeight() * EXPANDED_HEIGHT_PERCENTAGE);
        mMinOffset = -child.getHeight();
        mMaxOffset = -child.getHeight() + expandedHeight;
        mPeekOffset = -child.getHeight() + mPeekHeight;

        if (mState == STATE_EXPANDED) {
            child.setTranslationY(mMaxOffset);
        } else if (mState == STATE_PEEKED) {
            child.setTranslationY(mPeekOffset);
        } else {
            child.setTranslationY(mMinOffset);
        }
        dispatchOnSlide(child);
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull MotionEvent ev) {
        if (ev.getActionMasked() != MotionEvent.ACTION_DOWN) {
            return false;
        }
        // Permitir drag en todo el top sheet, pero priorizar el handle si existe
        boolean isTouchOnHandle = isPointInChildBounds(mHandle, ev);
        boolean isTouchOnSheet = isPointInChildBounds(child, ev);
        if (isTouchOnHandle || isTouchOnSheet) {
            mInitialY = ev.getY();
            mIsDragging = false;
        }
        return isTouchOnHandle || isTouchOnSheet;
    }

    @Override
    public boolean onTouchEvent(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                float dy = ev.getY() - mInitialY;
                if (!mIsDragging && Math.abs(dy) > mTouchSlop) {
                    mIsDragging = true;
                }
                if (mIsDragging) {
                    float newTranslationY = child.getTranslationY() + dy;
                    child.setTranslationY(Math.max(mMinOffset, Math.min(newTranslationY, mMaxOffset)));
                    dispatchOnSlide(child);
                }
                mInitialY = ev.getY();
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mIsDragging) {
                    float currentY = child.getTranslationY();
                    float distToCollapsed = Math.abs(currentY - mMinOffset);
                    float distToPeek = Math.abs(currentY - mPeekOffset);
                    float distToExpanded = Math.abs(currentY - mMaxOffset);

                    if (distToCollapsed < distToPeek && distToCollapsed < distToExpanded) {
                        animateToState(child, STATE_COLLAPSED);
                    } else if (distToPeek < distToExpanded) {
                        animateToState(child, STATE_PEEKED);
                    } else {
                        animateToState(child, STATE_EXPANDED);
                    }
                }
                mIsDragging = false;
                break;
        }
        return true;
    }

    private boolean isPointInChildBounds(View view, MotionEvent ev) {
        if (view == null) return false;
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return ev.getRawX() >= location[0] && ev.getRawX() <= (location[0] + view.getWidth()) &&
               ev.getRawY() >= location[1] && ev.getRawY() <= (location[1] + view.getHeight());
    }

    public void setState(int state) {
        V child = mViewRef != null ? mViewRef.get() : null;
        if (child == null) {
            mState = state;
            return;
        }
        animateToState(child, state);
    }

    private void animateToState(V child, int state) {
        mState = state;
        float targetY;
        if (state == STATE_EXPANDED) {
            targetY = mMaxOffset;
        } else if (state == STATE_PEEKED) {
            targetY = mPeekOffset;
        } else {
            targetY = mMinOffset;
        }

        ValueAnimator animator = ValueAnimator.ofFloat(child.getTranslationY(), targetY);
        animator.addUpdateListener(animation -> {
            child.setTranslationY((Float) animation.getAnimatedValue());
            dispatchOnSlide(child);
        });
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (mCallback != null) {
                    mCallback.onStateChanged(child, mState);
                }
            }
        });
        animator.setDuration(300);
        animator.start();
    }

    public int getState() {
        return mState;
    }

    private void dispatchOnSlide(View topSheet) {
        if (mCallback != null && mMaxOffset != mMinOffset) {
            float slideOffset = (float) (topSheet.getTranslationY() - mMinOffset) / (mMaxOffset - mMinOffset);
            mCallback.onSlide(topSheet, slideOffset);
        }
    }

    public void setTopSheetCallback(TopSheetCallback callback) {
        mCallback = callback;
    }

    public static <V extends View> TopSheetBehavior<V> from(V view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (!(params instanceof CoordinatorLayout.LayoutParams)) {
            throw new IllegalArgumentException("The view is not a direct child of CoordinatorLayout");
        }
        CoordinatorLayout.Behavior<?> behavior = ((CoordinatorLayout.LayoutParams) params).getBehavior();
        if (!(behavior instanceof TopSheetBehavior)) {
            throw new IllegalArgumentException("The view's behavior is not an instance of TopSheetBehavior");
        }
        return (TopSheetBehavior<V>) behavior;
    }
}
