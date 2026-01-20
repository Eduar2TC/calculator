package com.eduar2tc.calculator.behavior;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.util.Log;
import android.view.VelocityTracker;
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
    // estado al iniciar el forwarded touch (guardar nivel inicial para posible snap)
    private int mStartState = STATE_COLLAPSED;
    // translation inicial cuando se inicia un forwarded drag (declarada más abajo para forwarding)
    private int mTouchSlop;
    private boolean mIsDragging;
    // tracker para velocidad (usar en decisión por velocidad+distancia)
    private VelocityTracker mVelocityTracker;
    private static final float FLING_VELOCITY = 1000f; // px/s

    private int mMinOffset;
    private int mMaxOffset;
    private int mPeekOffset;
    private int mPeekHeight;

    private WeakReference<V> mViewRef;
    private View mHandle;
    private TopSheetCallback mCallback;

    // Referencia al child scrolable (RecyclerView)
    private WeakReference<View> mScrollingChildRef;
    private boolean mInitialTouchInScrollingChild = false;

    // --- Forwarding control: cuando true, eventos se están reenviando desde otra vista (EditText)
    // y debemos evitar nested-scrolls del child para que el TopSheet siga el dedo sin rebotes.
    private boolean mForwardingActive = false;
    // último Y recibido por los eventos reenviados (usar delta incremental)
    private float mLastForwardedY = Float.NaN;
    // coordenada Y del primer ACTION_DOWN reenviado (en coordenadas del TopSheet)
    private float mForwardStartY = Float.NaN;
    // timestamp del ACTION_DOWN de forwarding
    private long mForwardStartTime = 0L;
    // translation del sheet al iniciar el forwarding
    private float mStartTranslationY = Float.NaN;
    // animator actual (si hay animación en curso)
    private ValueAnimator mAnimator = null;
    // última translation aplicada (para depuración/consistencia)
    private float mLastAppliedTranslation = Float.NaN;
    // timestamp (ms) de la última actualización mapeada (para calcular dt y aplicar suavizado condicional)
    private long mLastMappedTime = 0L;

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
            View scrollingChild = child.findViewById(R.id.recyclerViewHistory);
            if (scrollingChild != null) mScrollingChildRef = new WeakReference<>(scrollingChild);
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
        // Si hay forwarding activo, NO interceptar: dejamos que solo los eventos reenviados controlen la hoja
        if (mForwardingActive) return false;

        int action = ev.getActionMasked();
        View scrollingChild = mScrollingChildRef != null ? mScrollingChildRef.get() : null;
        if (scrollingChild == null) scrollingChild = child.findViewById(R.id.recyclerViewHistory);

        if (action == MotionEvent.ACTION_DOWN) {
            mInitialY = ev.getY();
            mIsDragging = false;
            mInitialTouchInScrollingChild = scrollingChild != null && isPointInChildBounds(scrollingChild, ev);
            Log.d("TopSheetBehavior", "ACTION_DOWN inScrollingChild=" + mInitialTouchInScrollingChild);
            return false;
        } else if (action == MotionEvent.ACTION_MOVE) {
            float dy = ev.getY() - mInitialY;
            if (Math.abs(dy) <= mTouchSlop) return false;

            if (mInitialTouchInScrollingChild && scrollingChild != null) {
                Log.d("TopSheetBehavior", "MOVE started in scrolling child - not intercepting");
                return false;
            }

            boolean isTouchOnHandle = isPointInChildBounds(mHandle, ev);
            boolean isTouchOnSheet = !mInitialTouchInScrollingChild && isPointInChildBounds(child, ev);
            if (isTouchOnHandle || isTouchOnSheet) {
                Log.d("TopSheetBehavior", "Intercepting move on sheet or handle");
                mInitialY = ev.getY();
                mIsDragging = true;
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull MotionEvent ev) {
        // si estamos en forwarding, evitamos procesar los eventos nativos del sheet (evita doble-control)
        if (mForwardingActive) return false;
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mInitialY = ev.getY();
                mIsDragging = false;
                mStartTranslationY = child.getTranslationY();
                if (mVelocityTracker == null) mVelocityTracker = VelocityTracker.obtain();
                mVelocityTracker.clear();
                mVelocityTracker.addMovement(ev);
                return true;

            case MotionEvent.ACTION_MOVE:
                float dy = ev.getY() - mInitialY;
                if (!mIsDragging && Math.abs(dy) > mTouchSlop) {
                    mIsDragging = true;
                }
                if (mVelocityTracker == null) mVelocityTracker = VelocityTracker.obtain();
                mVelocityTracker.addMovement(ev);
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
                    float yvel = 0f;
                    if (mVelocityTracker != null) {
                        mVelocityTracker.computeCurrentVelocity(1000);
                        yvel = mVelocityTracker.getYVelocity();
                    }
                    if (Math.abs(yvel) > FLING_VELOCITY) {
                        if (yvel > 0) animateToState(child, STATE_EXPANDED);
                        else animateToState(child, STATE_COLLAPSED);
                    } else {
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
                }
                mIsDragging = false;
                if (mVelocityTracker != null) {
                    mVelocityTracker.clear();
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
        }
        return true;
    }

    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V child,
                                  @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        // Si hay forwarding activo, ignorar nested pre-scroll para evitar competencia/rebote
        if (mForwardingActive) return;
        if (dy == 0) return;
        if (target != null) {
            boolean canUp = target.canScrollVertically(1);
            boolean canDown = target.canScrollVertically(-1);
            Log.d("TopSheetBehavior", "onNestedPreScroll dy=" + dy + " canUp=" + canUp + " canDown=" + canDown);
            if ((dy > 0 && canUp) || (dy < 0 && canDown)) {
                return;
            }
        }

        float oldY = child.getTranslationY();
        float newY = oldY - dy;
        float clampedY = Math.max(mMinOffset, Math.min(newY, mMaxOffset));
        float delta = oldY - clampedY; // cuánto del dy fue consumido por mover el sheet
        if (Math.abs(delta) > 0.0f) {
            child.setTranslationY(clampedY);
            dispatchOnSlide(child);
            consumed[1] = (int) delta;
            Log.d("TopSheetBehavior", "Consumed dy=" + consumed[1] + " newTranslation=" + clampedY);
        }
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V child,
                               @NonNull View target, int dxConsumed, int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed, int type) {
        // Ignorar nested scrolls mientras se esté reenviando el gesto
        if (mForwardingActive) return;
        Log.d("TopSheetBehavior", "onNestedScroll dyUnconsumed=" + dyUnconsumed);
        if (dyUnconsumed == 0) return;
        float oldY = child.getTranslationY();
        float newY = oldY - dyUnconsumed;
        float clampedY = Math.max(mMinOffset, Math.min(newY, mMaxOffset));
        if (clampedY != oldY) {
            child.setTranslationY(clampedY);
            dispatchOnSlide(child);
        }
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

        // Cancelar animador previo si existe para evitar que compita con forwarding
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
            mAnimator = null;
        }
        mAnimator = ValueAnimator.ofFloat(child.getTranslationY(), targetY);
        mAnimator.addUpdateListener(animation -> {
            child.setTranslationY((Float) animation.getAnimatedValue());
            dispatchOnSlide(child);
        });
        mAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (mCallback != null) mCallback.onStateChanged(child, mState);
                mAnimator = null;
            }
            @Override
            public void onAnimationCancel(android.animation.Animator animation) {
                mAnimator = null;
            }
        });
        mAnimator.setDuration(300);
        mAnimator.start();
    }

    public int getState() {
        return mState;
    }

    /**
     * Procesa MotionEvents reenviados desde vistas externas (por ejemplo EditText) mapeadas
     * en coordenadas del TopSheet. Permite que el TopSheet siga el dedo y decida el estado
     * al soltar.
     */
    public boolean onForwardedTouchEvent(@NonNull MotionEvent ev) {
        V child = mViewRef != null ? mViewRef.get() : null;
        if (child == null) return false;

        int action = ev.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                // iniciar forwarding (usar coordenadas mapeadas del sender)
                mForwardingActive = true;
                mForwardStartY = ev.getY();
                mForwardStartTime = System.currentTimeMillis();
                mLastForwardedY = mForwardStartY;
                mStartTranslationY = child.getTranslationY();
                mStartState = mState;
                mIsDragging = true;
                // cancelar animador en curso para que no compita con el dragging
                if (mAnimator != null && mAnimator.isRunning()) {
                    mAnimator.cancel();
                    mAnimator = null;
                }
                if (child.getParent() != null) child.getParent().requestDisallowInterceptTouchEvent(true);
                if (mVelocityTracker == null) mVelocityTracker = VelocityTracker.obtain();
                mVelocityTracker.clear();
                mVelocityTracker.addMovement(ev);
                mLastAppliedTranslation = mStartTranslationY;
                // inicializar timestamp para cálculos de dt y suavizado
                mLastMappedTime = System.currentTimeMillis();
                Log.d("TopSheetBehavior", "FDOWN mappedY=" + mForwardStartY + " startTrans=" + mStartTranslationY + " state=" + mState);
                return true;
            }

            case MotionEvent.ACTION_MOVE: {
                if (mVelocityTracker == null) mVelocityTracker = VelocityTracker.obtain();
                mVelocityTracker.addMovement(ev);
                if (!mIsDragging) {
                    mIsDragging = true;
                    mStartTranslationY = child.getTranslationY();
                }
                // usar desplazamiento absoluto respecto al primer DOWN reenviado (evita acumulación/jitter)
                float rawY = ev.getY();
                float totalDy = rawY - mForwardStartY; // positivo = hacia abajo
                float targetTranslation = mStartTranslationY + totalDy;
                float clamped = Math.max(mMinOffset, Math.min(targetTranslation, mMaxOffset));
                float prev = child.getTranslationY();
                long now = System.currentTimeMillis();
                long dt = now - mLastMappedTime;
                // aplicar suavizado condicional sólo para actualizaciones con dt muy pequeño (alta frecuencia)
                if (!Float.isNaN(mLastAppliedTranslation) && dt > 0 && dt < 16) {
                    final float alpha = 0.85f; // suave pero rápido
                    float applied = mLastAppliedTranslation + (clamped - mLastAppliedTranslation) * alpha;
                    child.setTranslationY(applied);
                    mLastAppliedTranslation = applied;
                } else {
                    child.setTranslationY(clamped);
                    mLastAppliedTranslation = clamped;
                }
                mLastMappedTime = now;
                dispatchOnSlide(child);
                mLastForwardedY = rawY;
                Log.d("TopSheetBehavior", "FMOVE rawY=" + rawY + " totalDy=" + totalDy + " prev=" + prev + " applied=" + child.getTranslationY() + " dt=" + dt);
                 return true;
             }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                float loggedTotalDy = Float.NaN;
                if (mIsDragging) {
                    float currentY = child.getTranslationY();
                    float totalDy = currentY - mStartTranslationY; // positivo = hacia abajo
                    loggedTotalDy = totalDy;
                    float yvel = 0f;
                    if (mVelocityTracker != null) {
                        mVelocityTracker.computeCurrentVelocity(1000);
                        yvel = mVelocityTracker.getYVelocity();
                    }

                    // fallback: si no hay VelocityTracker o velocidad es 0, estimar por mapped del forwarding
                    if (Math.abs(yvel) < 1e-3f) {
                        long dtTotal = mLastMappedTime - mForwardStartTime;
                        if (dtTotal > 0 && !Float.isNaN(mLastForwardedY) && !Float.isNaN(mForwardStartY)) {
                            float dy = mLastForwardedY - mForwardStartY; // px
                            yvel = dy / (dtTotal / 1000f); // px/s
                        }
                    }

                    // Si la velocidad es alta, snap por dirección
                    if (Math.abs(yvel) > FLING_VELOCITY) {
                        if (yvel > 0) animateToState(child, STATE_EXPANDED);
                        else animateToState(child, STATE_COLLAPSED);
                    } else {
                        float smallThreshold = Math.max(mTouchSlop * 6.0f, Math.abs(mMaxOffset - mMinOffset) * 0.08f);
                        if (Math.abs(totalDy) <= smallThreshold) {
                            animateToState(child, mStartState);
                        } else {
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
                    }
                }

                // limpiar estado
                mIsDragging = false;
                mForwardingActive = false;
                mLastForwardedY = Float.NaN;
                mForwardStartY = Float.NaN;
                mForwardStartTime = 0L;
                mStartTranslationY = Float.NaN;
                mStartState = STATE_COLLAPSED;
                if (child.getParent() != null) child.getParent().requestDisallowInterceptTouchEvent(false);
                if (mVelocityTracker != null) {
                    mVelocityTracker.clear();
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                Log.d("TopSheetBehavior", "FEND currentY=" + child.getTranslationY() + " totalDy=" + loggedTotalDy + " stateAfter=" + mState);
                 return true;
             }
        }
        return false;
    }

    // Exponer estado de forwarding para que otras clases (p. ej. MainActivity) puedan evitar feedback
    public boolean isForwardingActive() {
        return mForwardingActive;
    }

    // Public API para controlar forwarding mediante coordenadas mapeadas (mappedY = rawY - topLoc[1])
    public void beginForwardDrag(float mappedY) {
        V child = mViewRef != null ? mViewRef.get() : null;
        if (child == null) return;
        mForwardingActive = true;
        mForwardStartY = mappedY;
        mForwardStartTime = System.currentTimeMillis();
        mLastForwardedY = mappedY;
        mStartTranslationY = child.getTranslationY();
        mStartState = mState;
        mIsDragging = true;
        // cancelar animador
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
            mAnimator = null;
        }
        if (child.getParent() != null) child.getParent().requestDisallowInterceptTouchEvent(true);
        // Inicializar VelocityTracker para esta ruta de forwarding también (evita recursos sin inicializar y permite detectar flings si se añade movimiento)
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
            mVelocityTracker.clear();
        }
        mLastAppliedTranslation = mStartTranslationY;
        // inicializar timestamp para cálculos de dt y suavizado
        mLastMappedTime = System.currentTimeMillis();
        Log.d("TopSheetBehavior", "beginForwardDrag mappedY=" + mappedY + " startTrans=" + mStartTranslationY);
    }

    public void updateForwardDrag(float mappedY) {
        V child = mViewRef != null ? mViewRef.get() : null;
        if (child == null || !mForwardingActive) return;
        float totalDy = mappedY - mForwardStartY;
        float targetTranslation = mStartTranslationY + totalDy;
        float clamped = Math.max(mMinOffset, Math.min(targetTranslation, mMaxOffset));
        float prev = child.getTranslationY();

        // Aplicar suavizado condicional similar a onForwardedTouchEvent para evitar oscilaciones
        long now = System.currentTimeMillis();
        long dt = now - mLastMappedTime;
        if (!Float.isNaN(mLastAppliedTranslation) && dt > 0 && dt < 32) {
            final float alpha = 0.9f; // respuesta rápida pero evita jitter
            float applied = mLastAppliedTranslation + (clamped - mLastAppliedTranslation) * alpha;
            child.setTranslationY(applied);
            mLastAppliedTranslation = applied;
        } else {
            child.setTranslationY(clamped);
            mLastAppliedTranslation = clamped;
        }
        mLastMappedTime = now;

        dispatchOnSlide(child);
        mLastForwardedY = mappedY;
        Log.d("TopSheetBehavior", "updateForwardDrag mappedY=" + mappedY + " totalDy=" + totalDy + " prev=" + prev + " applied=" + child.getTranslationY());
    }

    public void endForwardDrag() {
        V child = mViewRef != null ? mViewRef.get() : null;
        if (child == null || !mForwardingActive) return;
        float currentY = child.getTranslationY();
        float totalDy = currentY - mStartTranslationY;
        float yvel = 0f;
        if (mVelocityTracker != null) {
            mVelocityTracker.computeCurrentVelocity(1000);
            yvel = mVelocityTracker.getYVelocity();
        }
        // fallback: estimar velocidad si VelocityTracker no tiene datos
        if (Math.abs(yvel) < 1e-3f) {
            long dtTotal = mLastMappedTime - mForwardStartTime;
            if (dtTotal > 0 && !Float.isNaN(mLastForwardedY) && !Float.isNaN(mForwardStartY)) {
                float dy = mLastForwardedY - mForwardStartY; // px
                yvel = dy / (dtTotal / 1000f); // px/s
            }
        }
        if (Math.abs(yvel) > FLING_VELOCITY) {
            if (yvel > 0) animateToState(child, STATE_EXPANDED);
            else animateToState(child, STATE_COLLAPSED);
        } else {
            float smallThreshold = Math.max(mTouchSlop * 10.0f, Math.abs(mMaxOffset - mMinOffset) * 0.14f);
            if (Math.abs(totalDy) <= smallThreshold) {
                animateToState(child, mStartState);
            } else {
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
        }
        // limpiar
        mIsDragging = false;
        mForwardingActive = false;
        mLastForwardedY = Float.NaN;
        mForwardStartY = Float.NaN;
        mForwardStartTime = 0L;
        mStartTranslationY = Float.NaN;
        mStartState = STATE_COLLAPSED;
        if (child.getParent() != null) child.getParent().requestDisallowInterceptTouchEvent(false);
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
        Log.d("TopSheetBehavior", "endForwardDrag currentY=" + currentY + " totalDy=" + totalDy + " stateAfter=" + mState);
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
