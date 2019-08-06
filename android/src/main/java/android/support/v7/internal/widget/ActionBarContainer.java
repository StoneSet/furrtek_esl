package android.support.v7.internal.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.support.v4.widget.ExploreByTouchHelper;
import android.support.v7.appcompat.R;
import android.support.v7.view.ActionMode;
import android.support.v7.view.ActionMode.Callback;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

public class ActionBarContainer extends FrameLayout {
    private ActionBarView mActionBarView;
    private Drawable mBackground;
    private boolean mIsSplit;
    private boolean mIsStacked;
    private boolean mIsTransitioning;
    private Drawable mSplitBackground;
    private Drawable mStackedBackground;
    private View mTabContainer;

    public ActionBarContainer(Context context) {
        this(context, null);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ActionBarContainer(android.content.Context r6, android.util.AttributeSet r7) {
        /*
        r5 = this;
        r0 = 0;
        r1 = 1;
        r5.<init>(r6, r7);
        r2 = 0;
        r5.setBackgroundDrawable(r2);
        r2 = android.support.v7.appcompat.R.styleable.ActionBar;
        r2 = r6.obtainStyledAttributes(r7, r2);
        r3 = 10;
        r3 = r2.getDrawable(r3);
        r5.mBackground = r3;
        r3 = 11;
        r3 = r2.getDrawable(r3);
        r5.mStackedBackground = r3;
        r3 = r5.getId();
        r4 = android.support.v7.appcompat.R.id.split_action_bar;
        if (r3 != r4) goto L_0x0031;
    L_0x0027:
        r5.mIsSplit = r1;
        r3 = 12;
        r3 = r2.getDrawable(r3);
        r5.mSplitBackground = r3;
    L_0x0031:
        r2.recycle();
        r2 = r5.mIsSplit;
        if (r2 == 0) goto L_0x0041;
    L_0x0038:
        r2 = r5.mSplitBackground;
        if (r2 != 0) goto L_0x003d;
    L_0x003c:
        r0 = r1;
    L_0x003d:
        r5.setWillNotDraw(r0);
        return;
    L_0x0041:
        r2 = r5.mBackground;
        if (r2 != 0) goto L_0x003d;
    L_0x0045:
        r2 = r5.mStackedBackground;
        if (r2 == 0) goto L_0x003c;
    L_0x0049:
        goto L_0x003d;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v7.internal.widget.ActionBarContainer.<init>(android.content.Context, android.util.AttributeSet):void");
    }

    private void drawBackgroundDrawable(Drawable drawable, Canvas canvas) {
        Rect bounds = drawable.getBounds();
        if (!(drawable instanceof ColorDrawable) || bounds.isEmpty() || VERSION.SDK_INT >= 11) {
            drawable.draw(canvas);
            return;
        }
        canvas.save();
        canvas.clipRect(bounds);
        drawable.draw(canvas);
        canvas.restore();
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (this.mBackground != null && this.mBackground.isStateful()) {
            this.mBackground.setState(getDrawableState());
        }
        if (this.mStackedBackground != null && this.mStackedBackground.isStateful()) {
            this.mStackedBackground.setState(getDrawableState());
        }
        if (this.mSplitBackground != null && this.mSplitBackground.isStateful()) {
            this.mSplitBackground.setState(getDrawableState());
        }
    }

    public View getTabContainer() {
        return this.mTabContainer;
    }

    public void onDraw(Canvas canvas) {
        if (getWidth() != 0 && getHeight() != 0) {
            if (!this.mIsSplit) {
                if (this.mBackground != null) {
                    drawBackgroundDrawable(this.mBackground, canvas);
                }
                if (this.mStackedBackground != null && this.mIsStacked) {
                    drawBackgroundDrawable(this.mStackedBackground, canvas);
                }
            } else if (this.mSplitBackground != null) {
                drawBackgroundDrawable(this.mSplitBackground, canvas);
            }
        }
    }

    public void onFinishInflate() {
        super.onFinishInflate();
        this.mActionBarView = (ActionBarView) findViewById(R.id.action_bar);
    }

    public boolean onHoverEvent(MotionEvent motionEvent) {
        return true;
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return this.mIsTransitioning || super.onInterceptTouchEvent(motionEvent);
    }

    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        boolean z2 = true;
        boolean z3 = false;
        super.onLayout(z, i, i2, i3, i4);
        boolean z4 = (this.mTabContainer == null || this.mTabContainer.getVisibility() == 8) ? false : true;
        if (!(this.mTabContainer == null || this.mTabContainer.getVisibility() == 8)) {
            int measuredHeight = getMeasuredHeight();
            int measuredHeight2 = this.mTabContainer.getMeasuredHeight();
            if ((this.mActionBarView.getDisplayOptions() & 2) == 0) {
                int childCount = getChildCount();
                for (measuredHeight = 0; measuredHeight < childCount; measuredHeight++) {
                    View childAt = getChildAt(measuredHeight);
                    if (!(childAt == this.mTabContainer || this.mActionBarView.isCollapsed())) {
                        childAt.offsetTopAndBottom(measuredHeight2);
                    }
                }
                this.mTabContainer.layout(i, 0, i3, measuredHeight2);
            } else {
                this.mTabContainer.layout(i, measuredHeight - measuredHeight2, i3, measuredHeight);
            }
        }
        if (!this.mIsSplit) {
            boolean z5;
            if (this.mBackground != null) {
                this.mBackground.setBounds(this.mActionBarView.getLeft(), this.mActionBarView.getTop(), this.mActionBarView.getRight(), this.mActionBarView.getBottom());
                z5 = true;
            } else {
                z5 = false;
            }
            if (z4 && this.mStackedBackground != null) {
                z3 = true;
            }
            this.mIsStacked = z3;
            if (z3) {
                this.mStackedBackground.setBounds(this.mTabContainer.getLeft(), this.mTabContainer.getTop(), this.mTabContainer.getRight(), this.mTabContainer.getBottom());
            } else {
                z2 = z5;
            }
        } else if (this.mSplitBackground != null) {
            this.mSplitBackground.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
        } else {
            z2 = false;
        }
        if (z2) {
            invalidate();
        }
    }

    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        if (this.mActionBarView != null) {
            int i3;
            LayoutParams layoutParams = (LayoutParams) this.mActionBarView.getLayoutParams();
            if (this.mActionBarView.isCollapsed()) {
                i3 = 0;
            } else {
                i3 = layoutParams.bottomMargin + (this.mActionBarView.getMeasuredHeight() + layoutParams.topMargin);
            }
            if (this.mTabContainer != null && this.mTabContainer.getVisibility() != 8 && MeasureSpec.getMode(i2) == ExploreByTouchHelper.INVALID_ID) {
                setMeasuredDimension(getMeasuredWidth(), Math.min(i3 + this.mTabContainer.getMeasuredHeight(), MeasureSpec.getSize(i2)));
            }
        }
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        super.onTouchEvent(motionEvent);
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setPrimaryBackground(android.graphics.drawable.Drawable r8) {
        /*
        r7 = this;
        r0 = 0;
        r1 = 1;
        r2 = r7.mBackground;
        if (r2 == 0) goto L_0x0011;
    L_0x0006:
        r2 = r7.mBackground;
        r3 = 0;
        r2.setCallback(r3);
        r2 = r7.mBackground;
        r7.unscheduleDrawable(r2);
    L_0x0011:
        r7.mBackground = r8;
        if (r8 == 0) goto L_0x0039;
    L_0x0015:
        r8.setCallback(r7);
        r2 = r7.mActionBarView;
        if (r2 == 0) goto L_0x0039;
    L_0x001c:
        r2 = r7.mBackground;
        r3 = r7.mActionBarView;
        r3 = r3.getLeft();
        r4 = r7.mActionBarView;
        r4 = r4.getTop();
        r5 = r7.mActionBarView;
        r5 = r5.getRight();
        r6 = r7.mActionBarView;
        r6 = r6.getBottom();
        r2.setBounds(r3, r4, r5, r6);
    L_0x0039:
        r2 = r7.mIsSplit;
        if (r2 == 0) goto L_0x0049;
    L_0x003d:
        r2 = r7.mSplitBackground;
        if (r2 != 0) goto L_0x0042;
    L_0x0041:
        r0 = r1;
    L_0x0042:
        r7.setWillNotDraw(r0);
        r7.invalidate();
        return;
    L_0x0049:
        r2 = r7.mBackground;
        if (r2 != 0) goto L_0x0042;
    L_0x004d:
        r2 = r7.mStackedBackground;
        if (r2 == 0) goto L_0x0041;
    L_0x0051:
        goto L_0x0042;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v7.internal.widget.ActionBarContainer.setPrimaryBackground(android.graphics.drawable.Drawable):void");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setSplitBackground(android.graphics.drawable.Drawable r6) {
        /*
        r5 = this;
        r0 = 0;
        r1 = 1;
        r2 = r5.mSplitBackground;
        if (r2 == 0) goto L_0x0011;
    L_0x0006:
        r2 = r5.mSplitBackground;
        r3 = 0;
        r2.setCallback(r3);
        r2 = r5.mSplitBackground;
        r5.unscheduleDrawable(r2);
    L_0x0011:
        r5.mSplitBackground = r6;
        if (r6 == 0) goto L_0x002d;
    L_0x0015:
        r6.setCallback(r5);
        r2 = r5.mIsSplit;
        if (r2 == 0) goto L_0x002d;
    L_0x001c:
        r2 = r5.mSplitBackground;
        if (r2 == 0) goto L_0x002d;
    L_0x0020:
        r2 = r5.mSplitBackground;
        r3 = r5.getMeasuredWidth();
        r4 = r5.getMeasuredHeight();
        r2.setBounds(r0, r0, r3, r4);
    L_0x002d:
        r2 = r5.mIsSplit;
        if (r2 == 0) goto L_0x003d;
    L_0x0031:
        r2 = r5.mSplitBackground;
        if (r2 != 0) goto L_0x0036;
    L_0x0035:
        r0 = r1;
    L_0x0036:
        r5.setWillNotDraw(r0);
        r5.invalidate();
        return;
    L_0x003d:
        r2 = r5.mBackground;
        if (r2 != 0) goto L_0x0036;
    L_0x0041:
        r2 = r5.mStackedBackground;
        if (r2 == 0) goto L_0x0035;
    L_0x0045:
        goto L_0x0036;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v7.internal.widget.ActionBarContainer.setSplitBackground(android.graphics.drawable.Drawable):void");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setStackedBackground(android.graphics.drawable.Drawable r8) {
        /*
        r7 = this;
        r0 = 0;
        r1 = 1;
        r2 = r7.mStackedBackground;
        if (r2 == 0) goto L_0x0011;
    L_0x0006:
        r2 = r7.mStackedBackground;
        r3 = 0;
        r2.setCallback(r3);
        r2 = r7.mStackedBackground;
        r7.unscheduleDrawable(r2);
    L_0x0011:
        r7.mStackedBackground = r8;
        if (r8 == 0) goto L_0x003d;
    L_0x0015:
        r8.setCallback(r7);
        r2 = r7.mIsStacked;
        if (r2 == 0) goto L_0x003d;
    L_0x001c:
        r2 = r7.mStackedBackground;
        if (r2 == 0) goto L_0x003d;
    L_0x0020:
        r2 = r7.mStackedBackground;
        r3 = r7.mTabContainer;
        r3 = r3.getLeft();
        r4 = r7.mTabContainer;
        r4 = r4.getTop();
        r5 = r7.mTabContainer;
        r5 = r5.getRight();
        r6 = r7.mTabContainer;
        r6 = r6.getBottom();
        r2.setBounds(r3, r4, r5, r6);
    L_0x003d:
        r2 = r7.mIsSplit;
        if (r2 == 0) goto L_0x004d;
    L_0x0041:
        r2 = r7.mSplitBackground;
        if (r2 != 0) goto L_0x0046;
    L_0x0045:
        r0 = r1;
    L_0x0046:
        r7.setWillNotDraw(r0);
        r7.invalidate();
        return;
    L_0x004d:
        r2 = r7.mBackground;
        if (r2 != 0) goto L_0x0046;
    L_0x0051:
        r2 = r7.mStackedBackground;
        if (r2 == 0) goto L_0x0045;
    L_0x0055:
        goto L_0x0046;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v7.internal.widget.ActionBarContainer.setStackedBackground(android.graphics.drawable.Drawable):void");
    }

    public void setTabContainer(ScrollingTabContainerView scrollingTabContainerView) {
        if (this.mTabContainer != null) {
            removeView(this.mTabContainer);
        }
        this.mTabContainer = scrollingTabContainerView;
        if (scrollingTabContainerView != null) {
            addView(scrollingTabContainerView);
            ViewGroup.LayoutParams layoutParams = scrollingTabContainerView.getLayoutParams();
            layoutParams.width = -1;
            layoutParams.height = -2;
            scrollingTabContainerView.setAllowCollapse(false);
        }
    }

    public void setTransitioning(boolean z) {
        this.mIsTransitioning = z;
        setDescendantFocusability(z ? 393216 : AccessibilityEventCompat.TYPE_GESTURE_DETECTION_START);
    }

    public void setVisibility(int i) {
        super.setVisibility(i);
        boolean z = i == 0;
        if (this.mBackground != null) {
            this.mBackground.setVisible(z, false);
        }
        if (this.mStackedBackground != null) {
            this.mStackedBackground.setVisible(z, false);
        }
        if (this.mSplitBackground != null) {
            this.mSplitBackground.setVisible(z, false);
        }
    }

    public ActionMode startActionModeForChild(View view, Callback callback) {
        return null;
    }

    protected boolean verifyDrawable(Drawable drawable) {
        return (drawable == this.mBackground && !this.mIsSplit) || ((drawable == this.mStackedBackground && this.mIsStacked) || ((drawable == this.mSplitBackground && this.mIsSplit) || super.verifyDrawable(drawable)));
    }
}
