package android.support.v4.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.v4.view.AccessibilityDelegateCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.widget.ViewDragHelper.Callback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class SlidingPaneLayout extends ViewGroup {
    private static final int DEFAULT_FADE_COLOR = -858993460;
    private static final int DEFAULT_OVERHANG_SIZE = 32;
    static final SlidingPanelLayoutImpl IMPL;
    private static final int MIN_FLING_VELOCITY = 400;
    private static final String TAG = "SlidingPaneLayout";
    private boolean mCanSlide;
    private int mCoveredFadeColor;
    private final ViewDragHelper mDragHelper;
    private boolean mFirstLayout;
    private float mInitialMotionX;
    private float mInitialMotionY;
    private boolean mIsUnableToDrag;
    private final int mOverhangSize;
    private PanelSlideListener mPanelSlideListener;
    private int mParallaxBy;
    private float mParallaxOffset;
    private final ArrayList<DisableLayerRunnable> mPostedRunnables;
    private boolean mPreservedOpenState;
    private Drawable mShadowDrawable;
    private float mSlideOffset;
    private int mSlideRange;
    private View mSlideableView;
    private int mSliderFadeColor;
    private final Rect mTmpRect;

    class AccessibilityDelegate extends AccessibilityDelegateCompat {
        private final Rect mTmpRect = new Rect();

        AccessibilityDelegate() {
        }

        private void copyNodeInfoNoChildren(AccessibilityNodeInfoCompat accessibilityNodeInfoCompat, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat2) {
            Rect rect = this.mTmpRect;
            accessibilityNodeInfoCompat2.getBoundsInParent(rect);
            accessibilityNodeInfoCompat.setBoundsInParent(rect);
            accessibilityNodeInfoCompat2.getBoundsInScreen(rect);
            accessibilityNodeInfoCompat.setBoundsInScreen(rect);
            accessibilityNodeInfoCompat.setVisibleToUser(accessibilityNodeInfoCompat2.isVisibleToUser());
            accessibilityNodeInfoCompat.setPackageName(accessibilityNodeInfoCompat2.getPackageName());
            accessibilityNodeInfoCompat.setClassName(accessibilityNodeInfoCompat2.getClassName());
            accessibilityNodeInfoCompat.setContentDescription(accessibilityNodeInfoCompat2.getContentDescription());
            accessibilityNodeInfoCompat.setEnabled(accessibilityNodeInfoCompat2.isEnabled());
            accessibilityNodeInfoCompat.setClickable(accessibilityNodeInfoCompat2.isClickable());
            accessibilityNodeInfoCompat.setFocusable(accessibilityNodeInfoCompat2.isFocusable());
            accessibilityNodeInfoCompat.setFocused(accessibilityNodeInfoCompat2.isFocused());
            accessibilityNodeInfoCompat.setAccessibilityFocused(accessibilityNodeInfoCompat2.isAccessibilityFocused());
            accessibilityNodeInfoCompat.setSelected(accessibilityNodeInfoCompat2.isSelected());
            accessibilityNodeInfoCompat.setLongClickable(accessibilityNodeInfoCompat2.isLongClickable());
            accessibilityNodeInfoCompat.addAction(accessibilityNodeInfoCompat2.getActions());
            accessibilityNodeInfoCompat.setMovementGranularities(accessibilityNodeInfoCompat2.getMovementGranularities());
        }

        public boolean filter(View view) {
            return SlidingPaneLayout.this.isDimmed(view);
        }

        public void onInitializeAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent) {
            super.onInitializeAccessibilityEvent(view, accessibilityEvent);
            accessibilityEvent.setClassName(SlidingPaneLayout.class.getName());
        }

        public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            AccessibilityNodeInfoCompat obtain = AccessibilityNodeInfoCompat.obtain(accessibilityNodeInfoCompat);
            super.onInitializeAccessibilityNodeInfo(view, obtain);
            copyNodeInfoNoChildren(accessibilityNodeInfoCompat, obtain);
            obtain.recycle();
            accessibilityNodeInfoCompat.setClassName(SlidingPaneLayout.class.getName());
            accessibilityNodeInfoCompat.setSource(view);
            ViewParent parentForAccessibility = ViewCompat.getParentForAccessibility(view);
            if (parentForAccessibility instanceof View) {
                accessibilityNodeInfoCompat.setParent((View) parentForAccessibility);
            }
            int childCount = SlidingPaneLayout.this.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = SlidingPaneLayout.this.getChildAt(i);
                if (!filter(childAt) && childAt.getVisibility() == 0) {
                    ViewCompat.setImportantForAccessibility(childAt, 1);
                    accessibilityNodeInfoCompat.addChild(childAt);
                }
            }
        }

        public boolean onRequestSendAccessibilityEvent(ViewGroup viewGroup, View view, AccessibilityEvent accessibilityEvent) {
            return !filter(view) ? super.onRequestSendAccessibilityEvent(viewGroup, view, accessibilityEvent) : false;
        }
    }

    private class DisableLayerRunnable implements Runnable {
        final View mChildView;

        DisableLayerRunnable(View view) {
            this.mChildView = view;
        }

        public void run() {
            if (this.mChildView.getParent() == SlidingPaneLayout.this) {
                ViewCompat.setLayerType(this.mChildView, 0, null);
                SlidingPaneLayout.this.invalidateChildRegion(this.mChildView);
            }
            SlidingPaneLayout.this.mPostedRunnables.remove(this);
        }
    }

    private class DragHelperCallback extends Callback {
        private DragHelperCallback() {
        }

        public int clampViewPositionHorizontal(View view, int i, int i2) {
            LayoutParams layoutParams = (LayoutParams) SlidingPaneLayout.this.mSlideableView.getLayoutParams();
            int paddingLeft = layoutParams.leftMargin + SlidingPaneLayout.this.getPaddingLeft();
            return Math.min(Math.max(i, paddingLeft), SlidingPaneLayout.this.mSlideRange + paddingLeft);
        }

        public int getViewHorizontalDragRange(View view) {
            return SlidingPaneLayout.this.mSlideRange;
        }

        public void onEdgeDragStarted(int i, int i2) {
            SlidingPaneLayout.this.mDragHelper.captureChildView(SlidingPaneLayout.this.mSlideableView, i2);
        }

        public void onViewCaptured(View view, int i) {
            SlidingPaneLayout.this.setAllChildrenVisible();
        }

        public void onViewDragStateChanged(int i) {
            if (SlidingPaneLayout.this.mDragHelper.getViewDragState() != 0) {
                return;
            }
            if (SlidingPaneLayout.this.mSlideOffset == 0.0f) {
                SlidingPaneLayout.this.updateObscuredViewsVisibility(SlidingPaneLayout.this.mSlideableView);
                SlidingPaneLayout.this.dispatchOnPanelClosed(SlidingPaneLayout.this.mSlideableView);
                SlidingPaneLayout.this.mPreservedOpenState = false;
                return;
            }
            SlidingPaneLayout.this.dispatchOnPanelOpened(SlidingPaneLayout.this.mSlideableView);
            SlidingPaneLayout.this.mPreservedOpenState = true;
        }

        public void onViewPositionChanged(View view, int i, int i2, int i3, int i4) {
            SlidingPaneLayout.this.onPanelDragged(i);
            SlidingPaneLayout.this.invalidate();
        }

        public void onViewReleased(View view, float f, float f2) {
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            int paddingLeft = layoutParams.leftMargin + SlidingPaneLayout.this.getPaddingLeft();
            if (f > 0.0f || (f == 0.0f && SlidingPaneLayout.this.mSlideOffset > 0.5f)) {
                paddingLeft += SlidingPaneLayout.this.mSlideRange;
            }
            SlidingPaneLayout.this.mDragHelper.settleCapturedViewAt(paddingLeft, view.getTop());
            SlidingPaneLayout.this.invalidate();
        }

        public boolean tryCaptureView(View view, int i) {
            return SlidingPaneLayout.this.mIsUnableToDrag ? false : ((LayoutParams) view.getLayoutParams()).slideable;
        }
    }

    public static class LayoutParams extends MarginLayoutParams {
        private static final int[] ATTRS = new int[]{16843137};
        Paint dimPaint;
        boolean dimWhenOffset;
        boolean slideable;
        public float weight = 0.0f;

        public LayoutParams() {
            super(-1, -1);
        }

        public LayoutParams(int i, int i2) {
            super(i, i2);
        }

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, ATTRS);
            this.weight = obtainStyledAttributes.getFloat(0, 0.0f);
            obtainStyledAttributes.recycle();
        }

        public LayoutParams(LayoutParams layoutParams) {
            super(layoutParams);
            this.weight = layoutParams.weight;
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
        }

        public LayoutParams(MarginLayoutParams marginLayoutParams) {
            super(marginLayoutParams);
        }
    }

    public interface PanelSlideListener {
        void onPanelClosed(View view);

        void onPanelOpened(View view);

        void onPanelSlide(View view, float f);
    }

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            public SavedState[] newArray(int i) {
                return new SavedState[i];
            }
        };
        boolean isOpen;

        private SavedState(Parcel parcel) {
            super(parcel);
            this.isOpen = parcel.readInt() != 0;
        }

        SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeInt(this.isOpen ? 1 : 0);
        }
    }

    public static class SimplePanelSlideListener implements PanelSlideListener {
        public void onPanelClosed(View view) {
        }

        public void onPanelOpened(View view) {
        }

        public void onPanelSlide(View view, float f) {
        }
    }

    interface SlidingPanelLayoutImpl {
        void invalidateChildRegion(SlidingPaneLayout slidingPaneLayout, View view);
    }

    static class SlidingPanelLayoutImplBase implements SlidingPanelLayoutImpl {
        SlidingPanelLayoutImplBase() {
        }

        public void invalidateChildRegion(SlidingPaneLayout slidingPaneLayout, View view) {
            ViewCompat.postInvalidateOnAnimation(slidingPaneLayout, view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        }
    }

    static class SlidingPanelLayoutImplJB extends SlidingPanelLayoutImplBase {
        private Method mGetDisplayList;
        private Field mRecreateDisplayList;

        SlidingPanelLayoutImplJB() {
            try {
                this.mGetDisplayList = View.class.getDeclaredMethod("getDisplayList", (Class[]) null);
            } catch (Throwable e) {
                Log.e(SlidingPaneLayout.TAG, "Couldn't fetch getDisplayList method; dimming won't work right.", e);
            }
            try {
                this.mRecreateDisplayList = View.class.getDeclaredField("mRecreateDisplayList");
                this.mRecreateDisplayList.setAccessible(true);
            } catch (Throwable e2) {
                Log.e(SlidingPaneLayout.TAG, "Couldn't fetch mRecreateDisplayList field; dimming will be slow.", e2);
            }
        }

        public void invalidateChildRegion(SlidingPaneLayout slidingPaneLayout, View view) {
            if (this.mGetDisplayList == null || this.mRecreateDisplayList == null) {
                view.invalidate();
                return;
            }
            try {
                this.mRecreateDisplayList.setBoolean(view, true);
                this.mGetDisplayList.invoke(view, (Object[]) null);
            } catch (Throwable e) {
                Log.e(SlidingPaneLayout.TAG, "Error refreshing display list state", e);
            }
            super.invalidateChildRegion(slidingPaneLayout, view);
        }
    }

    static class SlidingPanelLayoutImplJBMR1 extends SlidingPanelLayoutImplBase {
        SlidingPanelLayoutImplJBMR1() {
        }

        public void invalidateChildRegion(SlidingPaneLayout slidingPaneLayout, View view) {
            ViewCompat.setLayerPaint(view, ((LayoutParams) view.getLayoutParams()).dimPaint);
        }
    }

    static {
        int i = VERSION.SDK_INT;
        if (i >= 17) {
            IMPL = new SlidingPanelLayoutImplJBMR1();
        } else if (i >= 16) {
            IMPL = new SlidingPanelLayoutImplJB();
        } else {
            IMPL = new SlidingPanelLayoutImplBase();
        }
    }

    public SlidingPaneLayout(Context context) {
        this(context, null);
    }

    public SlidingPaneLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public SlidingPaneLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mSliderFadeColor = DEFAULT_FADE_COLOR;
        this.mFirstLayout = true;
        this.mTmpRect = new Rect();
        this.mPostedRunnables = new ArrayList();
        float f = context.getResources().getDisplayMetrics().density;
        this.mOverhangSize = (int) ((32.0f * f) + 0.5f);
        ViewConfiguration.get(context);
        setWillNotDraw(false);
        ViewCompat.setAccessibilityDelegate(this, new AccessibilityDelegate());
        ViewCompat.setImportantForAccessibility(this, 1);
        this.mDragHelper = ViewDragHelper.create(this, 0.5f, new DragHelperCallback());
        this.mDragHelper.setEdgeTrackingEnabled(1);
        this.mDragHelper.setMinVelocity(f * 400.0f);
    }

    private boolean closePane(View view, int i) {
        if (!this.mFirstLayout && !smoothSlideTo(0.0f, i)) {
            return false;
        }
        this.mPreservedOpenState = false;
        return true;
    }

    private void dimChildView(View view, float f, int i) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        if (f > 0.0f && i != 0) {
            int i2 = (((int) (((float) ((ViewCompat.MEASURED_STATE_MASK & i) >>> 24)) * f)) << 24) | (ViewCompat.MEASURED_SIZE_MASK & i);
            if (layoutParams.dimPaint == null) {
                layoutParams.dimPaint = new Paint();
            }
            layoutParams.dimPaint.setColorFilter(new PorterDuffColorFilter(i2, Mode.SRC_OVER));
            if (ViewCompat.getLayerType(view) != 2) {
                ViewCompat.setLayerType(view, 2, layoutParams.dimPaint);
            }
            invalidateChildRegion(view);
        } else if (ViewCompat.getLayerType(view) != 0) {
            if (layoutParams.dimPaint != null) {
                layoutParams.dimPaint.setColorFilter(null);
            }
            Runnable disableLayerRunnable = new DisableLayerRunnable(view);
            this.mPostedRunnables.add(disableLayerRunnable);
            ViewCompat.postOnAnimation(this, disableLayerRunnable);
        }
    }

    private void invalidateChildRegion(View view) {
        IMPL.invalidateChildRegion(this, view);
    }

    private void onPanelDragged(int i) {
        if (this.mSlideableView == null) {
            this.mSlideOffset = 0.0f;
            return;
        }
        LayoutParams layoutParams = (LayoutParams) this.mSlideableView.getLayoutParams();
        this.mSlideOffset = ((float) (i - (getPaddingLeft() + layoutParams.leftMargin))) / ((float) this.mSlideRange);
        if (this.mParallaxBy != 0) {
            parallaxOtherViews(this.mSlideOffset);
        }
        if (layoutParams.dimWhenOffset) {
            dimChildView(this.mSlideableView, this.mSlideOffset, this.mSliderFadeColor);
        }
        dispatchOnPanelSlide(this.mSlideableView);
    }

    private boolean openPane(View view, int i) {
        if (!this.mFirstLayout && !smoothSlideTo(1.0f, i)) {
            return false;
        }
        this.mPreservedOpenState = true;
        return true;
    }

    private void parallaxOtherViews(float f) {
        int i = 0;
        LayoutParams layoutParams = (LayoutParams) this.mSlideableView.getLayoutParams();
        int i2 = (!layoutParams.dimWhenOffset || layoutParams.leftMargin > 0) ? 0 : 1;
        int childCount = getChildCount();
        while (i < childCount) {
            View childAt = getChildAt(i);
            if (childAt != this.mSlideableView) {
                int i3 = (int) ((1.0f - this.mParallaxOffset) * ((float) this.mParallaxBy));
                this.mParallaxOffset = f;
                childAt.offsetLeftAndRight(i3 - ((int) ((1.0f - f) * ((float) this.mParallaxBy))));
                if (i2 != 0) {
                    dimChildView(childAt, 1.0f - this.mParallaxOffset, this.mCoveredFadeColor);
                }
            }
            i++;
        }
    }

    private static boolean viewIsOpaque(View view) {
        if (!ViewCompat.isOpaque(view)) {
            if (VERSION.SDK_INT >= 18) {
                return false;
            }
            Drawable background = view.getBackground();
            if (background == null) {
                return false;
            }
            if (background.getOpacity() != -1) {
                return false;
            }
        }
        return true;
    }

    protected boolean canScroll(View view, boolean z, int i, int i2, int i3) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int scrollX = view.getScrollX();
            int scrollY = view.getScrollY();
            for (int childCount = viewGroup.getChildCount() - 1; childCount >= 0; childCount--) {
                View childAt = viewGroup.getChildAt(childCount);
                if (i2 + scrollX >= childAt.getLeft() && i2 + scrollX < childAt.getRight() && i3 + scrollY >= childAt.getTop() && i3 + scrollY < childAt.getBottom()) {
                    if (canScroll(childAt, true, i, (i2 + scrollX) - childAt.getLeft(), (i3 + scrollY) - childAt.getTop())) {
                        return true;
                    }
                }
            }
        }
        return z && ViewCompat.canScrollHorizontally(view, -i);
    }

    @Deprecated
    public boolean canSlide() {
        return this.mCanSlide;
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams layoutParams) {
        return (layoutParams instanceof LayoutParams) && super.checkLayoutParams(layoutParams);
    }

    public boolean closePane() {
        return closePane(this.mSlideableView, 0);
    }

    public void computeScroll() {
        if (!this.mDragHelper.continueSettling(true)) {
            return;
        }
        if (this.mCanSlide) {
            ViewCompat.postInvalidateOnAnimation(this);
        } else {
            this.mDragHelper.abort();
        }
    }

    void dispatchOnPanelClosed(View view) {
        if (this.mPanelSlideListener != null) {
            this.mPanelSlideListener.onPanelClosed(view);
        }
        sendAccessibilityEvent(32);
    }

    void dispatchOnPanelOpened(View view) {
        if (this.mPanelSlideListener != null) {
            this.mPanelSlideListener.onPanelOpened(view);
        }
        sendAccessibilityEvent(32);
    }

    void dispatchOnPanelSlide(View view) {
        if (this.mPanelSlideListener != null) {
            this.mPanelSlideListener.onPanelSlide(view, this.mSlideOffset);
        }
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        View childAt = getChildCount() > 1 ? getChildAt(1) : null;
        if (childAt != null && this.mShadowDrawable != null) {
            int intrinsicWidth = this.mShadowDrawable.getIntrinsicWidth();
            int left = childAt.getLeft();
            this.mShadowDrawable.setBounds(left - intrinsicWidth, childAt.getTop(), left, childAt.getBottom());
            this.mShadowDrawable.draw(canvas);
        }
    }

    protected boolean drawChild(Canvas canvas, View view, long j) {
        boolean drawChild;
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        int save = canvas.save(2);
        if (!(!this.mCanSlide || layoutParams.slideable || this.mSlideableView == null)) {
            canvas.getClipBounds(this.mTmpRect);
            this.mTmpRect.right = Math.min(this.mTmpRect.right, this.mSlideableView.getLeft());
            canvas.clipRect(this.mTmpRect);
        }
        if (VERSION.SDK_INT >= 11) {
            drawChild = super.drawChild(canvas, view, j);
        } else if (!layoutParams.dimWhenOffset || this.mSlideOffset <= 0.0f) {
            if (view.isDrawingCacheEnabled()) {
                view.setDrawingCacheEnabled(false);
            }
            drawChild = super.drawChild(canvas, view, j);
        } else {
            if (!view.isDrawingCacheEnabled()) {
                view.setDrawingCacheEnabled(true);
            }
            Bitmap drawingCache = view.getDrawingCache();
            if (drawingCache != null) {
                canvas.drawBitmap(drawingCache, (float) view.getLeft(), (float) view.getTop(), layoutParams.dimPaint);
                drawChild = false;
            } else {
                Log.e(TAG, "drawChild: child view " + view + " returned null drawing cache");
                drawChild = super.drawChild(canvas, view, j);
            }
        }
        canvas.restoreToCount(save);
        return drawChild;
    }

    protected android.view.ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams();
    }

    public android.view.ViewGroup.LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(getContext(), attributeSet);
    }

    protected android.view.ViewGroup.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof MarginLayoutParams ? new LayoutParams((MarginLayoutParams) layoutParams) : new LayoutParams(layoutParams);
    }

    public int getCoveredFadeColor() {
        return this.mCoveredFadeColor;
    }

    public int getParallaxDistance() {
        return this.mParallaxBy;
    }

    public int getSliderFadeColor() {
        return this.mSliderFadeColor;
    }

    boolean isDimmed(View view) {
        if (view != null) {
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            if (this.mCanSlide && layoutParams.dimWhenOffset && this.mSlideOffset > 0.0f) {
                return true;
            }
        }
        return false;
    }

    public boolean isOpen() {
        return !this.mCanSlide || this.mSlideOffset == 1.0f;
    }

    public boolean isSlideable() {
        return this.mCanSlide;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mFirstLayout = true;
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mFirstLayout = true;
        int size = this.mPostedRunnables.size();
        for (int i = 0; i < size; i++) {
            ((DisableLayerRunnable) this.mPostedRunnables.get(i)).run();
        }
        this.mPostedRunnables.clear();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onInterceptTouchEvent(android.view.MotionEvent r8) {
        /*
        r7 = this;
        r1 = 1;
        r2 = 0;
        r3 = android.support.v4.view.MotionEventCompat.getActionMasked(r8);
        r0 = r7.mCanSlide;
        if (r0 != 0) goto L_0x002d;
    L_0x000a:
        if (r3 != 0) goto L_0x002d;
    L_0x000c:
        r0 = r7.getChildCount();
        if (r0 <= r1) goto L_0x002d;
    L_0x0012:
        r0 = r7.getChildAt(r1);
        if (r0 == 0) goto L_0x002d;
    L_0x0018:
        r4 = r7.mDragHelper;
        r5 = r8.getX();
        r5 = (int) r5;
        r6 = r8.getY();
        r6 = (int) r6;
        r0 = r4.isViewUnder(r0, r5, r6);
        if (r0 != 0) goto L_0x0041;
    L_0x002a:
        r0 = r1;
    L_0x002b:
        r7.mPreservedOpenState = r0;
    L_0x002d:
        r0 = r7.mCanSlide;
        if (r0 == 0) goto L_0x0037;
    L_0x0031:
        r0 = r7.mIsUnableToDrag;
        if (r0 == 0) goto L_0x0043;
    L_0x0035:
        if (r3 == 0) goto L_0x0043;
    L_0x0037:
        r0 = r7.mDragHelper;
        r0.cancel();
        r2 = super.onInterceptTouchEvent(r8);
    L_0x0040:
        return r2;
    L_0x0041:
        r0 = r2;
        goto L_0x002b;
    L_0x0043:
        r0 = 3;
        if (r3 == r0) goto L_0x0048;
    L_0x0046:
        if (r3 != r1) goto L_0x004e;
    L_0x0048:
        r0 = r7.mDragHelper;
        r0.cancel();
        goto L_0x0040;
    L_0x004e:
        switch(r3) {
            case 0: goto L_0x005e;
            case 1: goto L_0x0051;
            case 2: goto L_0x0082;
            default: goto L_0x0051;
        };
    L_0x0051:
        r0 = r2;
    L_0x0052:
        r3 = r7.mDragHelper;
        r3 = r3.shouldInterceptTouchEvent(r8);
        if (r3 != 0) goto L_0x005c;
    L_0x005a:
        if (r0 == 0) goto L_0x0040;
    L_0x005c:
        r2 = r1;
        goto L_0x0040;
    L_0x005e:
        r7.mIsUnableToDrag = r2;
        r0 = r8.getX();
        r3 = r8.getY();
        r7.mInitialMotionX = r0;
        r7.mInitialMotionY = r3;
        r4 = r7.mDragHelper;
        r5 = r7.mSlideableView;
        r0 = (int) r0;
        r3 = (int) r3;
        r0 = r4.isViewUnder(r5, r0, r3);
        if (r0 == 0) goto L_0x0051;
    L_0x0078:
        r0 = r7.mSlideableView;
        r0 = r7.isDimmed(r0);
        if (r0 == 0) goto L_0x0051;
    L_0x0080:
        r0 = r1;
        goto L_0x0052;
    L_0x0082:
        r0 = r8.getX();
        r3 = r8.getY();
        r4 = r7.mInitialMotionX;
        r0 = r0 - r4;
        r0 = java.lang.Math.abs(r0);
        r4 = r7.mInitialMotionY;
        r3 = r3 - r4;
        r3 = java.lang.Math.abs(r3);
        r4 = r7.mDragHelper;
        r4 = r4.getTouchSlop();
        r4 = (float) r4;
        r4 = (r0 > r4 ? 1 : (r0 == r4 ? 0 : -1));
        if (r4 <= 0) goto L_0x0051;
    L_0x00a3:
        r0 = (r3 > r0 ? 1 : (r3 == r0 ? 0 : -1));
        if (r0 <= 0) goto L_0x0051;
    L_0x00a7:
        r0 = r7.mDragHelper;
        r0.cancel();
        r7.mIsUnableToDrag = r1;
        goto L_0x0040;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.widget.SlidingPaneLayout.onInterceptTouchEvent(android.view.MotionEvent):boolean");
    }

    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int i5 = i3 - i;
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int childCount = getChildCount();
        if (this.mFirstLayout) {
            float f = (this.mCanSlide && this.mPreservedOpenState) ? 1.0f : 0.0f;
            this.mSlideOffset = f;
        }
        int i6 = 0;
        int i7 = paddingLeft;
        while (i6 < childCount) {
            int i8;
            View childAt = getChildAt(i6);
            if (childAt.getVisibility() == 8) {
                i8 = paddingLeft;
                paddingLeft = i7;
            } else {
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                int measuredWidth = childAt.getMeasuredWidth();
                if (layoutParams.slideable) {
                    int min = (Math.min(paddingLeft, (i5 - paddingRight) - this.mOverhangSize) - i7) - (layoutParams.leftMargin + layoutParams.rightMargin);
                    this.mSlideRange = min;
                    layoutParams.dimWhenOffset = ((layoutParams.leftMargin + i7) + min) + (measuredWidth / 2) > i5 - paddingRight;
                    int i9 = (int) (((float) min) * this.mSlideOffset);
                    i7 += layoutParams.leftMargin + i9;
                    this.mSlideOffset = ((float) i9) / ((float) this.mSlideRange);
                    i8 = 0;
                } else if (!this.mCanSlide || this.mParallaxBy == 0) {
                    i8 = 0;
                    i7 = paddingLeft;
                } else {
                    i8 = (int) ((1.0f - this.mSlideOffset) * ((float) this.mParallaxBy));
                    i7 = paddingLeft;
                }
                i8 = i7 - i8;
                childAt.layout(i8, paddingTop, i8 + measuredWidth, childAt.getMeasuredHeight() + paddingTop);
                i8 = childAt.getWidth() + paddingLeft;
                paddingLeft = i7;
            }
            i6++;
            i7 = paddingLeft;
            paddingLeft = i8;
        }
        if (this.mFirstLayout) {
            if (this.mCanSlide) {
                if (this.mParallaxBy != 0) {
                    parallaxOtherViews(this.mSlideOffset);
                }
                if (((LayoutParams) this.mSlideableView.getLayoutParams()).dimWhenOffset) {
                    dimChildView(this.mSlideableView, this.mSlideOffset, this.mSliderFadeColor);
                }
            } else {
                for (i8 = 0; i8 < childCount; i8++) {
                    dimChildView(getChildAt(i8), 0.0f, this.mSliderFadeColor);
                }
            }
            updateObscuredViewsVisibility(this.mSlideableView);
        }
        this.mFirstLayout = false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void onMeasure(int r15, int r16) {
        /*
        r14 = this;
        r3 = android.view.View.MeasureSpec.getMode(r15);
        r2 = android.view.View.MeasureSpec.getSize(r15);
        r1 = android.view.View.MeasureSpec.getMode(r16);
        r0 = android.view.View.MeasureSpec.getSize(r16);
        r4 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        if (r3 == r4) goto L_0x007e;
    L_0x0014:
        r4 = r14.isInEditMode();
        if (r4 == 0) goto L_0x0076;
    L_0x001a:
        r4 = -2147483648; // 0xffffffff80000000 float:-0.0 double:NaN;
        if (r3 != r4) goto L_0x006e;
    L_0x001e:
        r9 = r1;
        r10 = r2;
        r2 = r0;
    L_0x0021:
        r1 = -1;
        r0 = 0;
        switch(r9) {
            case -2147483648: goto L_0x00a5;
            case 1073741824: goto L_0x0098;
            default: goto L_0x0026;
        };
    L_0x0026:
        r5 = 0;
        r4 = 0;
        r2 = r14.getPaddingLeft();
        r2 = r10 - r2;
        r3 = r14.getPaddingRight();
        r3 = r2 - r3;
        r11 = r14.getChildCount();
        r2 = 2;
        if (r11 <= r2) goto L_0x0042;
    L_0x003b:
        r2 = "SlidingPaneLayout";
        r6 = "onMeasure: More than two child views are not supported.";
        android.util.Log.e(r2, r6);
    L_0x0042:
        r2 = 0;
        r14.mSlideableView = r2;
        r2 = 0;
        r8 = r2;
        r2 = r3;
        r3 = r4;
        r4 = r5;
        r5 = r0;
    L_0x004b:
        if (r8 >= r11) goto L_0x0135;
    L_0x004d:
        r12 = r14.getChildAt(r8);
        r0 = r12.getLayoutParams();
        r0 = (android.support.v4.widget.SlidingPaneLayout.LayoutParams) r0;
        r6 = r12.getVisibility();
        r7 = 8;
        if (r6 != r7) goto L_0x00b4;
    L_0x005f:
        r6 = 0;
        r0.dimWhenOffset = r6;
    L_0x0062:
        r0 = r2;
        r2 = r3;
        r3 = r4;
        r4 = r5;
    L_0x0066:
        r5 = r8 + 1;
        r8 = r5;
        r5 = r4;
        r4 = r3;
        r3 = r2;
        r2 = r0;
        goto L_0x004b;
    L_0x006e:
        if (r3 != 0) goto L_0x001e;
    L_0x0070:
        r2 = 300; // 0x12c float:4.2E-43 double:1.48E-321;
        r9 = r1;
        r10 = r2;
        r2 = r0;
        goto L_0x0021;
    L_0x0076:
        r0 = new java.lang.IllegalStateException;
        r1 = "Width must have an exact value or MATCH_PARENT";
        r0.<init>(r1);
        throw r0;
    L_0x007e:
        if (r1 != 0) goto L_0x001e;
    L_0x0080:
        r3 = r14.isInEditMode();
        if (r3 == 0) goto L_0x0090;
    L_0x0086:
        if (r1 != 0) goto L_0x001e;
    L_0x0088:
        r1 = -2147483648; // 0xffffffff80000000 float:-0.0 double:NaN;
        r0 = 300; // 0x12c float:4.2E-43 double:1.48E-321;
        r9 = r1;
        r10 = r2;
        r2 = r0;
        goto L_0x0021;
    L_0x0090:
        r0 = new java.lang.IllegalStateException;
        r1 = "Height must not be UNSPECIFIED";
        r0.<init>(r1);
        throw r0;
    L_0x0098:
        r0 = r14.getPaddingTop();
        r0 = r2 - r0;
        r1 = r14.getPaddingBottom();
        r0 = r0 - r1;
        r1 = r0;
        goto L_0x0026;
    L_0x00a5:
        r0 = r14.getPaddingTop();
        r0 = r2 - r0;
        r1 = r14.getPaddingBottom();
        r1 = r0 - r1;
        r0 = 0;
        goto L_0x0026;
    L_0x00b4:
        r6 = r0.weight;
        r7 = 0;
        r6 = (r6 > r7 ? 1 : (r6 == r7 ? 0 : -1));
        if (r6 <= 0) goto L_0x00c2;
    L_0x00bb:
        r6 = r0.weight;
        r4 = r4 + r6;
        r6 = r0.width;
        if (r6 == 0) goto L_0x0062;
    L_0x00c2:
        r6 = r0.leftMargin;
        r7 = r0.rightMargin;
        r6 = r6 + r7;
        r7 = r0.width;
        r13 = -2;
        if (r7 != r13) goto L_0x0107;
    L_0x00cc:
        r6 = r10 - r6;
        r7 = -2147483648; // 0xffffffff80000000 float:-0.0 double:NaN;
        r6 = android.view.View.MeasureSpec.makeMeasureSpec(r6, r7);
    L_0x00d4:
        r7 = r0.height;
        r13 = -2;
        if (r7 != r13) goto L_0x011e;
    L_0x00d9:
        r7 = -2147483648; // 0xffffffff80000000 float:-0.0 double:NaN;
        r7 = android.view.View.MeasureSpec.makeMeasureSpec(r1, r7);
    L_0x00df:
        r12.measure(r6, r7);
        r6 = r12.getMeasuredWidth();
        r7 = r12.getMeasuredHeight();
        r13 = -2147483648; // 0xffffffff80000000 float:-0.0 double:NaN;
        if (r9 != r13) goto L_0x00f4;
    L_0x00ee:
        if (r7 <= r5) goto L_0x00f4;
    L_0x00f0:
        r5 = java.lang.Math.min(r7, r1);
    L_0x00f4:
        r2 = r2 - r6;
        if (r2 >= 0) goto L_0x0133;
    L_0x00f7:
        r6 = 1;
    L_0x00f8:
        r0.slideable = r6;
        r3 = r3 | r6;
        r0 = r0.slideable;
        if (r0 == 0) goto L_0x0062;
    L_0x00ff:
        r14.mSlideableView = r12;
        r0 = r2;
        r2 = r3;
        r3 = r4;
        r4 = r5;
        goto L_0x0066;
    L_0x0107:
        r7 = r0.width;
        r13 = -1;
        if (r7 != r13) goto L_0x0115;
    L_0x010c:
        r6 = r10 - r6;
        r7 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r6 = android.view.View.MeasureSpec.makeMeasureSpec(r6, r7);
        goto L_0x00d4;
    L_0x0115:
        r6 = r0.width;
        r7 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r6 = android.view.View.MeasureSpec.makeMeasureSpec(r6, r7);
        goto L_0x00d4;
    L_0x011e:
        r7 = r0.height;
        r13 = -1;
        if (r7 != r13) goto L_0x012a;
    L_0x0123:
        r7 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r7 = android.view.View.MeasureSpec.makeMeasureSpec(r1, r7);
        goto L_0x00df;
    L_0x012a:
        r7 = r0.height;
        r13 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r7 = android.view.View.MeasureSpec.makeMeasureSpec(r7, r13);
        goto L_0x00df;
    L_0x0133:
        r6 = 0;
        goto L_0x00f8;
    L_0x0135:
        if (r3 != 0) goto L_0x013c;
    L_0x0137:
        r0 = 0;
        r0 = (r4 > r0 ? 1 : (r4 == r0 ? 0 : -1));
        if (r0 <= 0) goto L_0x0229;
    L_0x013c:
        r0 = r14.mOverhangSize;
        r12 = r10 - r0;
        r0 = 0;
        r9 = r0;
    L_0x0142:
        if (r9 >= r11) goto L_0x0229;
    L_0x0144:
        r13 = r14.getChildAt(r9);
        r0 = r13.getVisibility();
        r6 = 8;
        if (r0 != r6) goto L_0x0154;
    L_0x0150:
        r0 = r9 + 1;
        r9 = r0;
        goto L_0x0142;
    L_0x0154:
        r0 = r13.getLayoutParams();
        r0 = (android.support.v4.widget.SlidingPaneLayout.LayoutParams) r0;
        r6 = r13.getVisibility();
        r7 = 8;
        if (r6 == r7) goto L_0x0150;
    L_0x0162:
        r6 = r0.width;
        if (r6 != 0) goto L_0x019d;
    L_0x0166:
        r6 = r0.weight;
        r7 = 0;
        r6 = (r6 > r7 ? 1 : (r6 == r7 ? 0 : -1));
        if (r6 <= 0) goto L_0x019d;
    L_0x016d:
        r6 = 1;
        r8 = r6;
    L_0x016f:
        if (r8 == 0) goto L_0x01a0;
    L_0x0171:
        r6 = 0;
        r7 = r6;
    L_0x0173:
        if (r3 == 0) goto L_0x01c6;
    L_0x0175:
        r6 = r14.mSlideableView;
        if (r13 == r6) goto L_0x01c6;
    L_0x0179:
        r6 = r0.width;
        if (r6 >= 0) goto L_0x0150;
    L_0x017d:
        if (r7 > r12) goto L_0x0186;
    L_0x017f:
        r6 = r0.weight;
        r7 = 0;
        r6 = (r6 > r7 ? 1 : (r6 == r7 ? 0 : -1));
        if (r6 <= 0) goto L_0x0150;
    L_0x0186:
        if (r8 == 0) goto L_0x01bb;
    L_0x0188:
        r6 = r0.height;
        r7 = -2;
        if (r6 != r7) goto L_0x01a6;
    L_0x018d:
        r0 = -2147483648; // 0xffffffff80000000 float:-0.0 double:NaN;
        r0 = android.view.View.MeasureSpec.makeMeasureSpec(r1, r0);
    L_0x0193:
        r6 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r6 = android.view.View.MeasureSpec.makeMeasureSpec(r12, r6);
        r13.measure(r6, r0);
        goto L_0x0150;
    L_0x019d:
        r6 = 0;
        r8 = r6;
        goto L_0x016f;
    L_0x01a0:
        r6 = r13.getMeasuredWidth();
        r7 = r6;
        goto L_0x0173;
    L_0x01a6:
        r6 = r0.height;
        r7 = -1;
        if (r6 != r7) goto L_0x01b2;
    L_0x01ab:
        r0 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r0 = android.view.View.MeasureSpec.makeMeasureSpec(r1, r0);
        goto L_0x0193;
    L_0x01b2:
        r0 = r0.height;
        r6 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r0 = android.view.View.MeasureSpec.makeMeasureSpec(r0, r6);
        goto L_0x0193;
    L_0x01bb:
        r0 = r13.getMeasuredHeight();
        r6 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r0 = android.view.View.MeasureSpec.makeMeasureSpec(r0, r6);
        goto L_0x0193;
    L_0x01c6:
        r6 = r0.weight;
        r8 = 0;
        r6 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1));
        if (r6 <= 0) goto L_0x0150;
    L_0x01cd:
        r6 = r0.width;
        if (r6 != 0) goto L_0x0207;
    L_0x01d1:
        r6 = r0.height;
        r8 = -2;
        if (r6 != r8) goto L_0x01f2;
    L_0x01d6:
        r6 = -2147483648; // 0xffffffff80000000 float:-0.0 double:NaN;
        r6 = android.view.View.MeasureSpec.makeMeasureSpec(r1, r6);
    L_0x01dc:
        if (r3 == 0) goto L_0x0212;
    L_0x01de:
        r8 = r0.leftMargin;
        r0 = r0.rightMargin;
        r0 = r0 + r8;
        r0 = r10 - r0;
        r8 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r8 = android.view.View.MeasureSpec.makeMeasureSpec(r0, r8);
        if (r7 == r0) goto L_0x0150;
    L_0x01ed:
        r13.measure(r8, r6);
        goto L_0x0150;
    L_0x01f2:
        r6 = r0.height;
        r8 = -1;
        if (r6 != r8) goto L_0x01fe;
    L_0x01f7:
        r6 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r6 = android.view.View.MeasureSpec.makeMeasureSpec(r1, r6);
        goto L_0x01dc;
    L_0x01fe:
        r6 = r0.height;
        r8 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r6 = android.view.View.MeasureSpec.makeMeasureSpec(r6, r8);
        goto L_0x01dc;
    L_0x0207:
        r6 = r13.getMeasuredHeight();
        r8 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r6 = android.view.View.MeasureSpec.makeMeasureSpec(r6, r8);
        goto L_0x01dc;
    L_0x0212:
        r8 = 0;
        r8 = java.lang.Math.max(r8, r2);
        r0 = r0.weight;
        r8 = (float) r8;
        r0 = r0 * r8;
        r0 = r0 / r4;
        r0 = (int) r0;
        r0 = r0 + r7;
        r7 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r0 = android.view.View.MeasureSpec.makeMeasureSpec(r0, r7);
        r13.measure(r0, r6);
        goto L_0x0150;
    L_0x0229:
        r14.setMeasuredDimension(r10, r5);
        r14.mCanSlide = r3;
        r0 = r14.mDragHelper;
        r0 = r0.getViewDragState();
        if (r0 == 0) goto L_0x023d;
    L_0x0236:
        if (r3 != 0) goto L_0x023d;
    L_0x0238:
        r0 = r14.mDragHelper;
        r0.abort();
    L_0x023d:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.widget.SlidingPaneLayout.onMeasure(int, int):void");
    }

    protected void onRestoreInstanceState(Parcelable parcelable) {
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(savedState.getSuperState());
        if (savedState.isOpen) {
            openPane();
        } else {
            closePane();
        }
        this.mPreservedOpenState = savedState.isOpen;
    }

    protected Parcelable onSaveInstanceState() {
        Parcelable savedState = new SavedState(super.onSaveInstanceState());
        savedState.isOpen = isSlideable() ? isOpen() : this.mPreservedOpenState;
        return savedState;
    }

    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        if (i != i3) {
            this.mFirstLayout = true;
        }
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!this.mCanSlide) {
            return super.onTouchEvent(motionEvent);
        }
        this.mDragHelper.processTouchEvent(motionEvent);
        float x;
        float y;
        switch (motionEvent.getAction() & MotionEventCompat.ACTION_MASK) {
            case 0:
                x = motionEvent.getX();
                y = motionEvent.getY();
                this.mInitialMotionX = x;
                this.mInitialMotionY = y;
                return true;
            case 1:
                if (!isDimmed(this.mSlideableView)) {
                    return true;
                }
                x = motionEvent.getX();
                y = motionEvent.getY();
                float f = x - this.mInitialMotionX;
                float f2 = y - this.mInitialMotionY;
                int touchSlop = this.mDragHelper.getTouchSlop();
                if ((f * f) + (f2 * f2) >= ((float) (touchSlop * touchSlop)) || !this.mDragHelper.isViewUnder(this.mSlideableView, (int) x, (int) y)) {
                    return true;
                }
                closePane(this.mSlideableView, 0);
                return true;
            default:
                return true;
        }
    }

    public boolean openPane() {
        return openPane(this.mSlideableView, 0);
    }

    public void requestChildFocus(View view, View view2) {
        super.requestChildFocus(view, view2);
        if (!isInTouchMode() && !this.mCanSlide) {
            this.mPreservedOpenState = view == this.mSlideableView;
        }
    }

    void setAllChildrenVisible() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            if (childAt.getVisibility() == 4) {
                childAt.setVisibility(0);
            }
        }
    }

    public void setCoveredFadeColor(int i) {
        this.mCoveredFadeColor = i;
    }

    public void setPanelSlideListener(PanelSlideListener panelSlideListener) {
        this.mPanelSlideListener = panelSlideListener;
    }

    public void setParallaxDistance(int i) {
        this.mParallaxBy = i;
        requestLayout();
    }

    public void setShadowDrawable(Drawable drawable) {
        this.mShadowDrawable = drawable;
    }

    public void setShadowResource(int i) {
        setShadowDrawable(getResources().getDrawable(i));
    }

    public void setSliderFadeColor(int i) {
        this.mSliderFadeColor = i;
    }

    @Deprecated
    public void smoothSlideClosed() {
        closePane();
    }

    @Deprecated
    public void smoothSlideOpen() {
        openPane();
    }

    boolean smoothSlideTo(float f, int i) {
        if (this.mCanSlide) {
            LayoutParams layoutParams = (LayoutParams) this.mSlideableView.getLayoutParams();
            if (this.mDragHelper.smoothSlideViewTo(this.mSlideableView, (int) (((float) (layoutParams.leftMargin + getPaddingLeft())) + (((float) this.mSlideRange) * f)), this.mSlideableView.getTop())) {
                setAllChildrenVisible();
                ViewCompat.postInvalidateOnAnimation(this);
                return true;
            }
        }
        return false;
    }

    void updateObscuredViewsVisibility(View view) {
        int i;
        int i2;
        int i3;
        int i4;
        int paddingLeft = getPaddingLeft();
        int width = getWidth() - getPaddingRight();
        int paddingTop = getPaddingTop();
        int height = getHeight() - getPaddingBottom();
        if (view == null || !viewIsOpaque(view)) {
            i = 0;
            i2 = 0;
            i3 = 0;
            i4 = 0;
        } else {
            i2 = view.getLeft();
            i3 = view.getRight();
            i4 = view.getTop();
            i = view.getBottom();
        }
        int childCount = getChildCount();
        int i5 = 0;
        while (i5 < childCount) {
            View childAt = getChildAt(i5);
            if (childAt != view) {
                int i6 = (Math.max(paddingLeft, childAt.getLeft()) < i2 || Math.max(paddingTop, childAt.getTop()) < i4 || Math.min(width, childAt.getRight()) > i3 || Math.min(height, childAt.getBottom()) > i) ? 0 : 4;
                childAt.setVisibility(i6);
                i5++;
            } else {
                return;
            }
        }
    }
}
